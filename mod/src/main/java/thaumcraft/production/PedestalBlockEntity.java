package thaumcraft.production;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.registry.R3Content;

public class PedestalBlockEntity extends BlockEntity implements WorldlyContainer {
    private NonNullList<ItemStack> items=NonNullList.withSize(1,ItemStack.EMPTY);
    public PedestalBlockEntity(BlockPos p,BlockState s){this(R3Content.PEDESTAL_ENTITY.get(),p,s);}
    protected PedestalBlockEntity(net.minecraft.world.level.block.entity.BlockEntityType<?> type,BlockPos p,BlockState s){super(type,p,s);}
    public void changed(){setChanged();if(level!=null)level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),3);}
    public int getContainerSize(){return 1;}
    public boolean isEmpty(){return items.getFirst().isEmpty();}
    public ItemStack getItem(int slot){return items.get(slot);}
    public ItemStack removeItem(int slot,int n){var result=ContainerHelper.removeItem(items,slot,n);if(!result.isEmpty())changed();return result;}
    public ItemStack removeItemNoUpdate(int slot){return ContainerHelper.takeItem(items,slot);}
    public void setItem(int slot,ItemStack stack){items.set(slot,stack);stack.limitSize(1);changed();}
    public void setInfusionResult(ItemStack stack){items.set(0,stack);changed();}
    public int getMaxStackSize(){return 1;}
    public boolean stillValid(Player p){return level!=null&&level.getBlockEntity(worldPosition)==this&&p.distanceToSqr(worldPosition.getCenter())<=64;}
    public void clearContent(){items.clear();changed();}
    public int[] getSlotsForFace(Direction face){return new int[]{0};}
    public boolean canPlaceItemThroughFace(int slot,ItemStack s,Direction face){return isEmpty();}
    public boolean canTakeItemThroughFace(int slot,ItemStack s,Direction face){return true;}
    @Override protected void saveAdditional(CompoundTag tag,HolderLookup.Provider r){super.saveAdditional(tag,r);ContainerHelper.saveAllItems(tag,items,r);}
    @Override protected void loadAdditional(CompoundTag tag,HolderLookup.Provider r){super.loadAdditional(tag,r);items=NonNullList.withSize(1,ItemStack.EMPTY);ContainerHelper.loadAllItems(tag,items,r);}
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider r){return saveWithoutMetadata(r);}
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket(){return ClientboundBlockEntityDataPacket.create(this);}
}
