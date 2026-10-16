package thaumcraft.menu;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import thaumcraft.api.wands.ItemFocusBasic;
import thaumcraft.production.FocalManipulatorBlockEntity;
import thaumcraft.registry.R3Content;

public final class FocalManipulatorMenu extends AbstractContainerMenu {
    public final FocalManipulatorBlockEntity table;
    public FocalManipulatorMenu(int id,Inventory inventory,RegistryFriendlyByteBuf buffer){this(id,inventory,(FocalManipulatorBlockEntity)inventory.player.level().getBlockEntity(buffer.readBlockPos()));}
    public FocalManipulatorMenu(int id,Inventory inventory,FocalManipulatorBlockEntity table){super(R3Content.FOCAL_MENU.get(),id);this.table=table;
        addSlot(new Slot(table,0,88,60){@Override public boolean mayPlace(ItemStack stack){return table.canPlaceItem(0,stack);}@Override public boolean mayPickup(Player player){return !table.active();}@Override public int getMaxStackSize(){return 1;}});
        for(int row=0;row<3;row++)for(int col=0;col<9;col++)addSlot(new Slot(inventory,col+row*9+9,16+col*18,151+row*18));for(int col=0;col<9;col++)addSlot(new Slot(inventory,col,16+col*18,209));
    }
    @Override public boolean stillValid(Player player){return table!=null&&table.stillValid(player);}
    @Override public boolean clickMenuButton(Player player,int button){return player instanceof net.minecraft.server.level.ServerPlayer server&&stillValid(player)&&table.start(server,button);}
    @Override public ItemStack quickMoveStack(Player player,int index){if(index<0||index>=slots.size()||!stillValid(player))return ItemStack.EMPTY;var slot=slots.get(index);if(!slot.hasItem()||!slot.mayPickup(player))return ItemStack.EMPTY;var stack=slot.getItem();var copy=stack.copy();boolean moved=index==0?moveItemStackTo(stack,1,37,true):stack.getItem() instanceof ItemFocusBasic?moveItemStackTo(stack,0,1,false):index<28?moveItemStackTo(stack,28,37,false):moveItemStackTo(stack,1,28,false);if(!moved)return ItemStack.EMPTY;if(stack.isEmpty())slot.setByPlayer(ItemStack.EMPTY);else slot.setChanged();slot.onTake(player,stack);return copy;}
}
