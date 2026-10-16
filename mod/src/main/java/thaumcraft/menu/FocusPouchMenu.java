package thaumcraft.menu;

import java.util.*;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import thaumcraft.api.wands.ItemFocusBasic;
import thaumcraft.items.FocusPouchItem;
import thaumcraft.registry.R3Content;

public final class FocusPouchMenu extends AbstractContainerMenu {
    private final Inventory inventory;private final int locked;private final ItemStack pouch;private final SimpleContainer contents;
    public FocusPouchMenu(int id,Inventory inventory,RegistryFriendlyByteBuf buffer){this(id,inventory,buffer.readVarInt());}
    public FocusPouchMenu(int id,Inventory inventory,int slot){super(R3Content.POUCH_MENU.get(),id);if(slot<0||slot>=inventory.getContainerSize())throw new IllegalArgumentException("Invalid pouch slot");this.inventory=inventory;locked=slot;pouch=inventory.getItem(slot);contents=new SimpleContainer(18){@Override public void setChanged(){super.setChanged();save();}};
        if(!inventory.player.level().isClientSide){var items=FocusPouchItem.contents(pouch);for(int i=0;i<18;i++)contents.setItem(i,items.get(i).copy());}
        for(int i=0;i<18;i++)addSlot(new Slot(contents,i,37+i%6*18,51+i/6*18){@Override public boolean mayPlace(ItemStack stack){return stack.getItem() instanceof ItemFocusBasic;}@Override public int getMaxStackSize(){return 1;}});
        for(int row=0;row<3;row++)for(int col=0;col<9;col++)playerSlot(col+row*9+9,8+col*18,151+row*18);for(int col=0;col<9;col++)playerSlot(col,8+col*18,209);
    }
    private void playerSlot(int index,int x,int y){addSlot(new Slot(inventory,index,x,y){@Override public boolean mayPickup(Player player){return index!=locked;}@Override public boolean mayPlace(ItemStack stack){return index!=locked;}});}
    private void save(){if(inventory.player.level().isClientSide)return;var items=new ArrayList<ItemStack>();for(int i=0;i<18;i++)items.add(contents.getItem(i));FocusPouchItem.store(pouch,items);inventory.setChanged();}
    public int lockedHotbar(){return locked<9?locked:-1;}
    @Override public boolean stillValid(Player player){return player==inventory.player&&inventory.getItem(locked).getItem() instanceof FocusPouchItem&&(player.level().isClientSide||inventory.getItem(locked)==pouch);}
    @Override public void clicked(int index,int button,ClickType type,Player player){if(!stillValid(player)||index>=18&&index<slots.size()&&slots.get(index).getContainerSlot()==locked||type==ClickType.SWAP&&(button==locked||button==40&&locked==40))return;super.clicked(index,button,type,player);save();}
    @Override public ItemStack quickMoveStack(Player player,int index){if(index<0||index>=slots.size()||!stillValid(player))return ItemStack.EMPTY;var slot=slots.get(index);if(!slot.hasItem()||!slot.mayPickup(player))return ItemStack.EMPTY;var stack=slot.getItem();var copy=stack.copy();if(!(index<18?moveItemStackTo(stack,18,slots.size(),true):moveItemStackTo(stack,0,18,false)))return ItemStack.EMPTY;if(stack.isEmpty())slot.setByPlayer(ItemStack.EMPTY);else slot.setChanged();slot.onTake(player,stack);save();return copy;}
    @Override public void removed(Player player){save();super.removed(player);}
}
