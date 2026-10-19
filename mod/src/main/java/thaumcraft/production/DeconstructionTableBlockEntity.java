package thaumcraft.production;

import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.api.aspects.*;
import thaumcraft.data.ThaumcraftData;
import thaumcraft.knowledge.KnowledgeService;
import thaumcraft.registry.R4Content;

public final class DeconstructionTableBlockEntity extends BlockEntity implements WorldlyContainer,MenuProvider,thaumcraft.api.world.IDeconstructionTable {
    private NonNullList<ItemStack> items=NonNullList.withSize(1,ItemStack.EMPTY);
    private Aspect aspect;
    private int breakTime;
    public final ContainerData data=new ContainerData(){public int getCount(){return 2;}public int get(int i){return i==0?breakTime:aspect==null?0:Aspects.PRIMALS.indexOf(aspect)+1;}public void set(int i,int n){if(i==0)breakTime=n;else aspect=n>0&&n<=Aspects.PRIMALS.size()?Aspects.PRIMALS.get(n-1):null;}};
    public DeconstructionTableBlockEntity(BlockPos p,BlockState s){super(R4Content.DECONSTRUCTOR_ENTITY.get(),p,s);}
    public Aspect pendingAspect(){return aspect;}
    public int breakTime(){return breakTime;}
    private boolean canBreak(){return aspect==null&&!items.getFirst().isEmpty()&&level!=null&&!ThaumcraftData.aspects(level.getServer(),items.getFirst()).isEmpty();}
    public void serverTick(){
        if(!canBreak()){if(breakTime!=0){breakTime=0;setChanged();}return;}
        if(breakTime==0)breakTime=40;
        if(--breakTime==0){var primals=new AspectList();ThaumcraftData.aspects(level.getServer(),items.getFirst()).forEach((tag,n)->reduce(Aspect.getAspect(tag),n,primals));
            if(primals.size()>0&&level.random.nextInt(80)<primals.visSize()){var choices=primals.getAspects();aspect=choices[level.random.nextInt(choices.length)];}
            items.getFirst().shrink(1);if(items.getFirst().isEmpty())items.set(0,ItemStack.EMPTY);sync();
        }else setChanged();
    }
    private static void reduce(Aspect a,int n,AspectList out){if(a==null||n<=0)return;if(a.isPrimal())out.add(a,n);else for(var part:a.getComponents())reduce(part,n,out);}
    public boolean claim(ServerPlayer player){if(aspect==null||!stillValid(player))return false;var result=aspect;KnowledgeService.mutate(player,k->{k.discover(result);k.addPool(result,1);return true;});aspect=null;sync();return true;}
    public void sync(){setChanged();if(level!=null&&!level.isClientSide)level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),3);}
    @Override public Component getDisplayName(){return Component.translatable("block.thaumcraft.deconstruction_table");}
    @Override public AbstractContainerMenu createMenu(int id,Inventory inventory,Player player){return new thaumcraft.menu.DeconstructionMenu(id,inventory,this,data);}
    @Override public int getContainerSize(){return 1;}
    @Override public boolean isEmpty(){return items.getFirst().isEmpty();}
    @Override public ItemStack getItem(int slot){return items.get(slot);}
    @Override public ItemStack removeItem(int slot,int count){var removed=ContainerHelper.removeItem(items,slot,count);if(!removed.isEmpty())sync();return removed;}
    @Override public ItemStack removeItemNoUpdate(int slot){return ContainerHelper.takeItem(items,slot);}
    @Override public void setItem(int slot,ItemStack item){items.set(slot,item);if(item.getCount()>getMaxStackSize())item.setCount(getMaxStackSize());sync();}
    @Override public void clearContent(){items.clear();sync();}
    @Override public boolean stillValid(Player player){return level!=null&&level.getBlockEntity(worldPosition)==this&&player.distanceToSqr(worldPosition.getCenter())<=64;}
    @Override public boolean canPlaceItem(int slot,ItemStack item){return level!=null&&(level.isClientSide||!ThaumcraftData.aspects(level.getServer(),item).isEmpty());}
    @Override public int[] getSlotsForFace(Direction side){return side==Direction.UP?new int[0]:new int[]{0};}
    @Override public boolean canPlaceItemThroughFace(int slot,ItemStack stack,Direction side){return side!=Direction.UP&&canPlaceItem(slot,stack);}
    @Override public boolean canTakeItemThroughFace(int slot,ItemStack stack,Direction side){return true;}
    @Override protected void saveAdditional(CompoundTag tag,HolderLookup.Provider r){super.saveAdditional(tag,r);ContainerHelper.saveAllItems(tag,items,r);if(aspect!=null)tag.putString("Aspect",aspect.getTag());tag.putInt("BreakTime",breakTime);}
    @Override protected void loadAdditional(CompoundTag tag,HolderLookup.Provider r){super.loadAdditional(tag,r);items=NonNullList.withSize(1,ItemStack.EMPTY);ContainerHelper.loadAllItems(tag,items,r);aspect=Aspect.getAspect(tag.getString("Aspect"));if(aspect!=null&&!aspect.isPrimal())aspect=null;breakTime=Math.clamp(tag.getInt("BreakTime"),0,40);}
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider r){return saveWithoutMetadata(r);}
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket(){return ClientboundBlockEntityDataPacket.create(this);}
}
