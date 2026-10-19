package example.thaumcraft;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.api.aspects.*;
import thaumcraft.api.visnet.TileVisNode;
/** A relay/consumer, never an infinite vis source. Its inherited lifecycle handles unloaded chunks. */
public final class DeviceEntity extends TileVisNode implements IEssentiaTransport,IAspectContainer {
 public final EssentiaStore store=new EssentiaStore(64,false,false);
 public int charge;
 public DeviceEntity(BlockPos pos,BlockState state){super(ExampleAddon.DEVICE_TYPE.get(),pos,state);}
 public int getRange(){return 8;}
 public boolean isSource(){return false;}
 public void tick(){if(charge<1000){int n=consumeVis(Aspects.AIR,Math.min(4,1000-charge));if(n>0){charge+=n;setChanged();}}if(level.getGameTime()%20==0)changedAndSync();}
 protected void saveAdditional(CompoundTag tag,HolderLookup.Provider lookup){super.saveAdditional(tag,lookup);tag.putInt("charge",charge);var content=new CompoundTag();store.getAspects().asMap().forEach((a,n)->content.putInt(a.getTag(),n));tag.put("essentia",content);}
 protected void loadAdditional(CompoundTag tag,HolderLookup.Provider lookup){super.loadAdditional(tag,lookup);charge=Math.clamp(tag.getInt("charge"),0,1000);var list=new AspectList();var content=tag.getCompound("essentia");int left=64;for(String key:content.getAllKeys()){var a=Aspect.getAspect(key);int n=Math.clamp(content.getInt(key),0,left);if(a!=null&&n>0){list.add(a,n);left-=n;break;}}store.setAspects(list);}
 public AspectList getAspects(){return store.getAspects();}
 public void setAspects(AspectList list){store.setAspects(list);setChanged();}
 public boolean doesContainerAccept(Aspect a){return store.doesContainerAccept(a);}
 public int addToContainer(Aspect a,int n){int remainder=store.addToContainer(a,n);if(remainder!=n)setChanged();return remainder;}
 public boolean takeFromContainer(Aspect a,int n){boolean result=store.takeFromContainer(a,n);if(result)setChanged();return result;}
 public boolean doesContainerContainAmount(Aspect a,int n){return store.doesContainerContainAmount(a,n);}
 public int containerContains(Aspect a){return store.containerContains(a);}
 public boolean isConnectable(Direction face){return true;}
 public boolean canInputFrom(Direction face){return true;}
 public boolean canOutputTo(Direction face){return true;}
 public void setSuction(Aspect a,int amount){}
 public Aspect getSuctionType(Direction face){return store.first();}
 public int getSuctionAmount(Direction face){return store.amount()<64?32:0;}
 public int takeEssentia(Aspect a,int amount,Direction face){int n=Math.min(Math.max(0,amount),store.containerContains(a));if(store.takeFromContainer(a,n)){setChanged();return n;}return 0;}
 public int addEssentia(Aspect a,int amount,Direction face){int n=amount-store.addToContainer(a,amount);if(n>0)setChanged();return n;}
 public Aspect getEssentiaType(Direction face){return store.first();}
 public int getEssentiaAmount(Direction face){return store.amount();}
 public int getMinimumSuction(){return 0;}
 public boolean renderExtendedTube(){return false;}
}
