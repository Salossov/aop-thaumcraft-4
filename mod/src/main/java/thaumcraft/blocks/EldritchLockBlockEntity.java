package thaumcraft.blocks;

import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import thaumcraft.registry.R5Content;

/** Original 101-tick keyed lock and persisted boss encounter cycle. */
public final class EldritchLockBlockEntity extends BlockEntity {
    public static java.util.function.Consumer<EldritchLockBlockEntity> clientEffects=tile->{};
    public int count=-1;
    public byte facing;
    public EldritchLockBlockEntity(BlockPos pos,BlockState state){super(R5Content.LOCK_ENTITY.get(),pos,state);}
    public void setFacing(Direction direction){facing=(byte)(direction==null?6:direction.ordinal());if(level!=null)level.setBlock(worldPosition,getBlockState().setValue(EldritchLockBlock.FACING,(int)facing),2);setChanged();}
    public AABB renderBounds(){return new AABB(worldPosition).inflate(2.25);}
    public void activate(){if(count!=-1||level==null||level.isClientSide)return;count=0;setChanged();level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),3);level.playSound(null,worldPosition,thaumcraft.registry.R2Sounds.RUNIC_SHIELD_CHARGE.get(),net.minecraft.sounds.SoundSource.BLOCKS,1,1);}
    public static void tick(net.minecraft.world.level.Level level,BlockPos pos,BlockState state,EldritchLockBlockEntity tile){
        if(tile.count==-1)return;++tile.count;
        if(level.isClientSide){clientEffects.accept(tile);return;}
        tile.setChanged();if(tile.count%5==0)level.playSound(null,pos,thaumcraft.registry.R2Sounds.PUMP.get(),net.minecraft.sounds.SoundSource.BLOCKS,1,1);
        if(tile.count>100)tile.spawnEncounter();
    }
    public void spawnEncounter(){
        if(!(level instanceof net.minecraft.server.level.ServerLevel server))return;
        server.playSound(null,worldPosition,thaumcraft.registry.R2Sounds.ICE.get(),net.minecraft.sounds.SoundSource.BLOCKS,1,1);
        int cx=worldPosition.getX()>>4,cz=worldPosition.getZ()>>4,centerX=cx,centerZ=cz,exit=0;
        var maze=thaumcraft.world.dim.LabyrinthData.get(server);
        for(int a=-2;a<=2;a++)for(int b=-2;b<=2;b++){var cell=maze.cell(cx+a,cz+b);if(cell!=null&&cell.feature==2){centerX=cx+a;centerZ=cz+b;}if(cell!=null&&cell.feature>=2&&cell.feature<=5&&(cell.north||cell.south||cell.east||cell.west))exit=cell.feature;}
        int kind=thaumcraft.world.dim.BossCycleData.get(server).next(server.random);
        for(var player:server.players())if(player.distanceToSqr(worldPosition.getX(),worldPosition.getY(),worldPosition.getZ())<300)player.sendSystemMessage(net.minecraft.network.chat.Component.translatable("tc.boss."+new String[]{"golem","warden","crimson","taint"}[kind]));
        var sink=new thaumcraft.world.dim.rooms.NativeArenaSink(server,worldPosition,centerX,centerZ);
        thaumcraft.world.dim.rooms.ArenaRoomLayout.generate(sink,server.random,centerX,centerZ,exit,kind);sink.finish();
        for(var p:BlockPos.betweenClosed(worldPosition.offset(-2,-2,-2),worldPosition.offset(2,2,2))){var s=server.getBlockState(p);if(s.is(R5Content.VOID_DOOR.get())||s.is(R5Content.ELDRITCH_PORTAL.get())||s.is(R5Content.ELDRITCH_FIELD.get())||s.is(thaumcraft.registry.R2Content.AURA_NODE.get())){net.neoforged.neoforge.network.PacketDistributor.sendToPlayersNear(server,null,p.getX(),p.getY(),p.getZ(),32,new thaumcraft.network.BlockSparklePayload(p.immutable(),0x400040));server.removeBlock(p,false);}}
        server.removeBlock(worldPosition,false);
    }
    @Override protected void saveAdditional(CompoundTag tag,HolderLookup.Provider registries){super.saveAdditional(tag,registries);tag.putByte("facing",facing);tag.putShort("count",(short)count);}
    @Override protected void loadAdditional(CompoundTag tag,HolderLookup.Provider registries){super.loadAdditional(tag,registries);facing=tag.getByte("facing");count=tag.contains("count")?tag.getShort("count"):-1;}
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider registries){return saveWithoutMetadata(registries);}
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket(){return ClientboundBlockEntityDataPacket.create(this);}
}
