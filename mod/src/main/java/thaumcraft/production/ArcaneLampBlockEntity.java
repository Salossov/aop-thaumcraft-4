package thaumcraft.production;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import thaumcraft.api.aspects.*;
import thaumcraft.registry.R3Content;

public final class ArcaneLampBlockEntity extends BlockEntity implements IEssentiaTransport {
    public static final TagKey<Block> BLACKLIST=TagKey.create(Registries.BLOCK,ResourceLocation.fromNamespaceAndPath("thaumcraft","growth_lamp_blacklist"));
    private int charges=-1,count,drawDelay;
    private boolean reserve;
    private final ArrayList<BlockPos> columns=new ArrayList<>();
    public ArcaneLampBlockEntity(BlockPos pos,BlockState state){super(R3Content.LAMP_ENTITY.get(),pos,state);}
    public ArcaneLampBlock.Kind kind(){return ((ArcaneLampBlock)getBlockState().getBlock()).kind;}
    public int charges(){return charges;}
    private Direction facing(){return getBlockState().getValue(ArcaneLampBlock.FACING);}
    private boolean draw(){
        if(++drawDelay%5!=0)return false;var face=facing();var pos=worldPosition.relative(face);if(!level.hasChunkAt(pos))return false;
        var transport=level.getCapability(EssentiaCapabilities.TRANSPORT,pos,face.getOpposite());return transport!=null&&transport.isConnectable(face.getOpposite())&&transport.canOutputTo(face.getOpposite())&&transport.getSuctionAmount(face.getOpposite())<getSuctionAmount(face)&&transport.takeEssentia(getSuctionType(face),1,face.getOpposite())==1;
    }
    public void serverTick(){
        if(!(level instanceof ServerLevel server))return;int previous=charges;
        switch(kind()){
            case LIGHT->light(server);
            case GROWTH->{if(charges<=0){if(reserve){charges=100;reserve=false;}else if(draw())charges=100;}if(!reserve&&draw()){reserve=true;setChanged();}if(charges==0)charges=-1;if(charges>0)grow(server);}
            case FERTILITY->{if(charges<4&&draw())charges++;if(charges>1&&count++%300==0)breed(server);}
        }
        if(charges!=previous){setChanged();var state=getBlockState();if(state.getValue(ArcaneLampBlock.LIT)!=(charges>0||kind()==ArcaneLampBlock.Kind.LIGHT))level.setBlock(worldPosition,state.setValue(ArcaneLampBlock.LIT,charges>0||kind()==ArcaneLampBlock.Kind.LIGHT),3);level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),3);}
    }
    private void grow(ServerLevel server){
        if(columns.isEmpty()){for(int x=-6;x<=6;x++)for(int z=-6;z<=6;z++)columns.add(worldPosition.offset(x,6,z));Collections.shuffle(columns,new Random(server.random.nextLong()));}
        var top=columns.remove(columns.size()-1);if(!server.hasChunkAt(top))return;
        for(int y=Math.min(top.getY(),server.getMaxBuildHeight()-1);y>=Math.max(worldPosition.getY()-6,server.getMinBuildHeight());y--){var pos=new BlockPos(top.getX(),y,top.getZ());if(worldPosition.distSqr(pos)>=36)continue;var state=server.getBlockState(pos);var block=state.getBlock();
            if(state.is(BLACKLIST)||state.is(BlockTags.LEAVES)||block instanceof GrassBlock||block instanceof CropBlock crop&&crop.isMaxAge(state)||block instanceof CocoaBlock&&state.getValue(CocoaBlock.AGE)>=2||block instanceof NetherWartBlock&&state.getValue(NetherWartBlock.AGE)>=3)continue;
            if(!(block instanceof BonemealableBlock)&&!(block instanceof BushBlock)&&!(block instanceof SugarCaneBlock))continue;
            if(!state.isRandomlyTicking())continue;charges--;state.randomTick(server,pos,server.random);
            if(!state.equals(server.getBlockState(pos)))server.sendParticles(new net.minecraft.core.particles.DustParticleOptions(new org.joml.Vector3f(64/255f,1,64/255f),1),pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5,6,.3,.3,.3,.02);return;
        }
    }
    private void breed(ServerLevel server){var animals=server.getEntitiesOfClass(Animal.class,new AABB(worldPosition).inflate(7));for(var animal:animals){if(animal.getAge()!=0||animal.isInLove())continue;var same=animals.stream().filter(other->other.getClass()==animal.getClass()).toList();if(same.size()>7)continue;Animal partner=null;for(var other:same){if(other.getAge()!=0||other.isInLove())continue;if(partner==null)partner=other;else{charges-=2;partner.setInLove(null);other.setInLove(null);return;}}}}
    private void light(ServerLevel server){var random=server.random;int x=worldPosition.getX()+random.nextInt(16)-random.nextInt(16),z=worldPosition.getZ()+random.nextInt(16)-random.nextInt(16);var test=new BlockPos(x,worldPosition.getY(),z);if(!server.hasChunkAt(test))return;int y=Math.max(server.getMinBuildHeight()+5,Math.min(worldPosition.getY()+random.nextInt(16)-random.nextInt(16),server.getHeight(Heightmap.Types.MOTION_BLOCKING,x,z)+4));var pos=new BlockPos(x,y,z);if(server.isOutsideBuildHeight(pos)||!server.isEmptyBlock(pos)||server.getMaxLocalRawBrightness(pos)>=9)return;server.setBlock(pos,R3Content.LAMP_LIGHT.get().defaultBlockState(),3);if(server.getBlockEntity(pos) instanceof LampLightBlockEntity tile)tile.source(worldPosition);}
    @Override public boolean isConnectable(Direction face){return kind()!=ArcaneLampBlock.Kind.LIGHT&&face==facing();}
    @Override public boolean canInputFrom(Direction face){return isConnectable(face);}
    @Override public boolean canOutputTo(Direction face){return false;}
    @Override public void setSuction(Aspect aspect,int amount){}
    @Override public Aspect getSuctionType(Direction face){return kind()==ArcaneLampBlock.Kind.GROWTH?Aspects.PLANT:Aspects.LIFE;}
    @Override public int getSuctionAmount(Direction face){return !isConnectable(face)?0:kind()==ArcaneLampBlock.Kind.GROWTH?(!reserve||charges<=0?128:0):128-Math.max(0,charges)*10;}
    @Override public int takeEssentia(Aspect aspect,int amount,Direction face){return 0;}
    @Override public int addEssentia(Aspect aspect,int amount,Direction face){return 0;}
    @Override public Aspect getEssentiaType(Direction face){return null;}
    @Override public int getEssentiaAmount(Direction face){return 0;}
    @Override public int getMinimumSuction(){return 0;}
    @Override public boolean renderExtendedTube(){return false;}
    @Override protected void saveAdditional(CompoundTag tag,HolderLookup.Provider registries){super.saveAdditional(tag,registries);tag.putInt("Charges",charges);tag.putBoolean("Reserve",reserve);}
    @Override protected void loadAdditional(CompoundTag tag,HolderLookup.Provider registries){super.loadAdditional(tag,registries);charges=Math.max(-1,Math.min(kind()==ArcaneLampBlock.Kind.FERTILITY?4:100,tag.getInt("Charges")));reserve=tag.getBoolean("Reserve");}
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider registries){var tag=new CompoundTag();saveAdditional(tag,registries);return tag;}
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket(){return ClientboundBlockEntityDataPacket.create(this);}
}
