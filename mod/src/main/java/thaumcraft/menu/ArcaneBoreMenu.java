package thaumcraft.menu;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import thaumcraft.production.ArcaneBoreBlockEntity;

public final class ArcaneBoreMenu extends AbstractContainerMenu {
    public final ArcaneBoreBlockEntity bore;
    public ArcaneBoreMenu(int id,Inventory inventory,RegistryFriendlyByteBuf buffer){this(id,inventory,(ArcaneBoreBlockEntity)inventory.player.level().getBlockEntity(buffer.readBlockPos()));}
    public ArcaneBoreMenu(int id,Inventory inventory,ArcaneBoreBlockEntity bore){super(thaumcraft.registry.R3Content.BORE_MENU.get(),id);this.bore=java.util.Objects.requireNonNull(bore);for(int i=0;i<2;i++){final int slot=i;addSlot(new Slot(bore.items,i,i==0?26:74,18){@Override public boolean mayPlace(ItemStack stack){return bore.items.canPlaceItem(slot,stack);}@Override public int getMaxStackSize(){return 1;}});}for(int row=0;row<3;row++)for(int col=0;col<9;col++)addSlot(new Slot(inventory,col+row*9+9,8+col*18,59+row*18));for(int col=0;col<9;col++)addSlot(new Slot(inventory,col,8+col*18,117));}
    @Override public boolean stillValid(Player player){return player.level().getBlockEntity(bore.getBlockPos())==bore&&player.distanceToSqr(bore.getBlockPos().getCenter())<64;}
    @Override public ItemStack quickMoveStack(Player player,int index){if(!stillValid(player)||index<0||index>=slots.size())return ItemStack.EMPTY;var slot=slots.get(index);if(!slot.hasItem())return ItemStack.EMPTY;ItemStack stack=slot.getItem(),original=stack.copy();boolean moved=index<2?moveItemStackTo(stack,2,38,true):bore.items.canPlaceItem(0,stack)?moveItemStackTo(stack,0,1,false):bore.items.canPlaceItem(1,stack)&&moveItemStackTo(stack,1,2,false);if(!moved)return ItemStack.EMPTY;if(stack.isEmpty())slot.setByPlayer(ItemStack.EMPTY);else slot.setChanged();return original;}
}
