package thaumcraft.blocks;

import java.util.UUID;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.*;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.registry.R3Content;

/** Full BlockState preservation, including properties introduced after 1.7.10. */
public final class FocusMemoryBlockEntity extends BlockEntity {
    private BlockState original=Blocks.AIR.defaultBlockState();
    private UUID owner;
    private int elapsed,duration=120,remaining=1;
    private Direction direction;
    public FocusMemoryBlockEntity(BlockPos pos,BlockState state){super(R3Content.FOCUS_MEMORY_ENTITY.get(),pos,state);}
    public BlockState original(){return original;}
    public boolean ownedBy(UUID player){return player.equals(owner);}
    public void remember(BlockState original,UUID owner,int duration,int remaining,Direction direction){this.original=original;this.owner=owner;this.duration=Math.max(1,duration);this.remaining=Math.max(1,remaining);this.direction=direction;this.elapsed=0;setChanged();if(level!=null)level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),3);}
    public void restore(){if(level!=null&&level.getBlockEntity(worldPosition)==this){level.setBlockAndUpdate(worldPosition,original);level.scheduleTick(worldPosition,original.getBlock(),2);}}
    public void tick(){
        if(level==null||level.isClientSide||!getBlockState().is(R3Content.PORTABLE_HOLE.get()))return;
        if(elapsed==0&&remaining>1&&direction!=null){
            for(int a=-1;a<=1;a++)for(int b=-1;b<=1;b++)if(a!=0||b!=0){var pos=switch(direction.getAxis()){case X->worldPosition.offset(0,a,b);case Y->worldPosition.offset(a,0,b);case Z->worldPosition.offset(a,b,0);};createHole((ServerLevel)level,pos,owner,duration,1,null);}
            createHole((ServerLevel)level,worldPosition.relative(direction),owner,duration,remaining-1,direction);
        }
        if(++elapsed>=duration)restore();else setChanged();
    }
    public static boolean suitable(ServerLevel level,BlockPos pos){if(!level.hasChunkAt(pos)||level.isOutsideBuildHeight(pos)||level.getBlockEntity(pos)!=null)return false;var state=level.getBlockState(pos);return !state.isAir()&&!state.is(Blocks.BEDROCK)&&!state.is(R3Content.PORTABLE_HOLE.get())&&!state.is(R3Content.WARDED_BLOCK.get())&&state.getDestroySpeed(level,pos)>=0&&!state.is(net.minecraft.tags.BlockTags.FEATURES_CANNOT_REPLACE);}
    public static boolean createHole(ServerLevel level,BlockPos pos,UUID owner,int duration,int remaining,Direction direction){
        if(!suitable(level,pos))return false;var player=owner==null?null:level.getServer().getPlayerList().getPlayer(owner);if(player==null||player.level()!=level||!FocusActions.canChange(player,pos))return false;
        var previous=level.getBlockState(pos);if(!level.setBlockAndUpdate(pos,R3Content.PORTABLE_HOLE.get().defaultBlockState()))return false;if(level.getBlockEntity(pos) instanceof FocusMemoryBlockEntity tile){tile.remember(previous,owner,duration,remaining,direction);return true;}level.setBlockAndUpdate(pos,previous);return false;
    }
    @Override protected void saveAdditional(CompoundTag tag,HolderLookup.Provider registries){super.saveAdditional(tag,registries);tag.put("Original",NbtUtils.writeBlockState(original));if(owner!=null)tag.putUUID("Owner",owner);tag.putInt("Elapsed",elapsed);tag.putInt("Duration",duration);tag.putInt("Remaining",remaining);tag.putInt("Direction",direction==null?-1:direction.get3DDataValue());}
    @Override protected void loadAdditional(CompoundTag tag,HolderLookup.Provider registries){super.loadAdditional(tag,registries);original=NbtUtils.readBlockState(registries.lookupOrThrow(Registries.BLOCK),tag.getCompound("Original"));if(original.is(R3Content.PORTABLE_HOLE.get())||original.is(R3Content.WARDED_BLOCK.get()))original=Blocks.AIR.defaultBlockState();owner=tag.hasUUID("Owner")?tag.getUUID("Owner"):null;elapsed=Math.max(0,tag.getInt("Elapsed"));duration=Math.max(1,Math.min(420,tag.getInt("Duration")));remaining=Math.max(1,Math.min(74,tag.getInt("Remaining")));int face=tag.getInt("Direction");direction=face<0?null:Direction.from3DDataValue(face);}
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider registries){return saveWithoutMetadata(registries);}
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket(){return ClientboundBlockEntityDataPacket.create(this);}
}
