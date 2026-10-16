package thaumcraft.production;

import net.minecraft.core.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.api.aspects.*;
import thaumcraft.registry.R4Content;

/** All four outputs see the controller's tank; a nozzle never owns a duplicate tank. */
public final class AdvancedAlchemyNozzleBlockEntity extends BlockEntity implements IAspectContainer,IEssentiaTransport {
    public AdvancedAlchemyNozzleBlockEntity(BlockPos p,BlockState s){super(R4Content.ADVANCED_NOZZLE_ENTITY.get(),p,s);}
    public AdvancedAlchemyBlockEntity furnace(){if(level==null)return null;var p=AdvancedAlchemyBlock.core(worldPosition,getBlockState());return level.hasChunkAt(p)&&level.getBlockEntity(p) instanceof AdvancedAlchemyBlockEntity f?f:null;}
    public Direction outward(){var o=AdvancedAlchemyBlock.offset(getBlockState().getValue(AdvancedAlchemyBlock.PART));return o.getX()!=0?(o.getX()<0?Direction.WEST:Direction.EAST):(o.getZ()<0?Direction.NORTH:Direction.SOUTH);}
    @Override public AspectList getAspects(){var f=furnace();return f==null?new AspectList():f.store.getAspects();}
    @Override public void setAspects(AspectList a){}
    @Override public boolean doesContainerAccept(Aspect a){return false;}
    @Override public int addToContainer(Aspect a,int n){return n;}
    @Override public boolean takeFromContainer(Aspect a,int n){var f=furnace();if(f==null||!f.store.takeFromContainer(a,n))return false;if(n>0)f.sync();return true;}
    @Override public boolean doesContainerContainAmount(Aspect a,int n){var f=furnace();return f!=null&&f.store.doesContainerContainAmount(a,n);}
    @Override public int containerContains(Aspect a){var f=furnace();return f==null?0:f.store.containerContains(a);}
    @Override public boolean doesContainerContain(AspectList a){return false;}
    @Override public boolean takeFromContainer(AspectList a){return false;}
    @Override public boolean isConnectable(Direction face){return face==outward();}
    @Override public boolean canInputFrom(Direction face){return false;}
    @Override public boolean canOutputTo(Direction face){return isConnectable(face);}
    @Override public void setSuction(Aspect a,int n){}
    @Override public Aspect getSuctionType(Direction face){return null;}
    @Override public int getSuctionAmount(Direction face){return 0;}
    @Override public int getMinimumSuction(){return 0;}
    @Override public boolean renderExtendedTube(){return false;}
    @Override public Aspect getEssentiaType(Direction face){var f=furnace();return f==null?null:f.store.first();}
    @Override public int getEssentiaAmount(Direction face){var a=getEssentiaType(face);return a==null?0:containerContains(a);}
    @Override public int addEssentia(Aspect a,int n,Direction face){return 0;}
    @Override public int takeEssentia(Aspect a,int n,Direction face){return canOutputTo(face)&&takeFromContainer(a,n)?n:0;}
}
