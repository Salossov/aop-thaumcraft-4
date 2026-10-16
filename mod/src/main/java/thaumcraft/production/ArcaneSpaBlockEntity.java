package thaumcraft.production;

import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.*;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import thaumcraft.registry.R4Content;

/** Original 5-bucket tank, 40-tick placement and adjacent 5x5 source expansion. */
public final class ArcaneSpaBlockEntity extends BlockEntity implements WorldlyContainer,MenuProvider {
    private NonNullList<ItemStack> items=NonNullList.withSize(1,ItemStack.EMPTY);
    private boolean mix=true;
    private int counter;
    public final FluidTank tank=new FluidTank(5000){@Override protected void onContentsChanged(){sync();}};
    public final ContainerData data=new ContainerData(){public int getCount(){return 3;}public int get(int i){return switch(i){case 0->mix?1:0;case 1->BuiltInRegistries.FLUID.getId(tank.getFluid().getFluid());default->tank.getFluidAmount();};}public void set(int i,int n){if(i==0)mix=n!=0;}};
    public ArcaneSpaBlockEntity(BlockPos p,BlockState s){super(R4Content.SPA_ENTITY.get(),p,s);}
    public boolean mixing(){return mix;}
    public void toggleMix(){mix=!mix;sync();}
    public void sync(){setChanged();if(level!=null&&!level.isClientSide)level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),3);}
    public void serverTick(){
        if(level==null||level.isClientSide||counter++%40!=0||level.hasNeighborSignal(worldPosition)||tank.getFluidAmount()<1000)return;
        if(mix&&(!tank.getFluid().is(Fluids.WATER)||!items.getFirst().is(R4Content.BATH_SALTS.get())))return;
        var fluid=mix?thaumcraft.fluids.R4Fluids.PURE.get():tank.getFluid().getFluid();var target=fluid.defaultFluidState().createLegacyBlock();if(target.isAir())return;
        if(fluid==Fluids.WATER&&level.dimensionType().ultraWarm())return;
        var above=worldPosition.above();
        if(source(above,target)){
            for(int x=-2;x<=2;x++)for(int z=-2;z<=2;z++){var p=above.offset(x,0,z);if(valid(p,target,true)){place(p,target);return;}}
        }else if(valid(above,target,false))place(above,target);
    }
    private boolean source(BlockPos p,BlockState target){var s=level.getBlockState(p);return s.is(target.getBlock())&&s.getFluidState().isSource();}
    private boolean valid(BlockPos p,BlockState target,boolean adjacent){if(!level.hasChunkAt(p)||!level.getBlockState(p.below()).isFaceSturdy(level,p.below(),Direction.UP)||!level.getBlockState(p).canBeReplaced()||source(p,target))return false;if(!adjacent)return true;for(var d:Direction.values())if(source(p.relative(d),target))return true;return false;}
    private void place(BlockPos p,BlockState target){if(!level.setBlock(p,target,3))return;if(mix)removeItem(0,1);tank.drain(1000,FluidAction.EXECUTE);sync();}
    @Override public Component getDisplayName(){return Component.translatable("thaumcraft.spa");}
    @Override public AbstractContainerMenu createMenu(int id,Inventory inventory,Player player){return new thaumcraft.menu.ArcaneSpaMenu(id,inventory,this,data);}
    @Override public int getContainerSize(){return 1;}
    @Override public boolean isEmpty(){return items.getFirst().isEmpty();}
    @Override public ItemStack getItem(int slot){return items.get(slot);}
    @Override public ItemStack removeItem(int slot,int amount){var result=ContainerHelper.removeItem(items,slot,amount);if(!result.isEmpty())setChanged();return result;}
    @Override public ItemStack removeItemNoUpdate(int slot){return ContainerHelper.takeItem(items,slot);}
    @Override public void setItem(int slot,ItemStack stack){items.set(slot,stack.copyWithCount(Math.min(64,stack.getCount())));sync();}
    @Override public void clearContent(){items.clear();sync();}
    @Override public boolean stillValid(Player player){return level!=null&&level.getBlockEntity(worldPosition)==this&&player.distanceToSqr(Vec3.atCenterOf(worldPosition))<=64;}
    @Override public boolean canPlaceItem(int slot,ItemStack stack){return slot==0&&stack.is(R4Content.BATH_SALTS.get());}
    @Override public int[] getSlotsForFace(Direction side){return side==Direction.UP?new int[0]:new int[]{0};}
    @Override public boolean canPlaceItemThroughFace(int slot,ItemStack stack,Direction side){return side!=Direction.UP&&canPlaceItem(slot,stack);}
    @Override public boolean canTakeItemThroughFace(int slot,ItemStack stack,Direction side){return side!=Direction.UP;}
    @Override protected void saveAdditional(CompoundTag tag,HolderLookup.Provider r){super.saveAdditional(tag,r);tag.putBoolean("Mix",mix);tank.writeToNBT(r,tag);ContainerHelper.saveAllItems(tag,items,r);}
    @Override protected void loadAdditional(CompoundTag tag,HolderLookup.Provider r){super.loadAdditional(tag,r);mix=!tag.contains("Mix")||tag.getBoolean("Mix");tank.readFromNBT(r,tag);if(tank.getFluidAmount()>5000)tank.setFluid(tank.getFluid().copyWithAmount(5000));items=NonNullList.withSize(1,ItemStack.EMPTY);ContainerHelper.loadAllItems(tag,items,r);}
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider r){return saveWithoutMetadata(r);}
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket(){return ClientboundBlockEntityDataPacket.create(this);}
}
