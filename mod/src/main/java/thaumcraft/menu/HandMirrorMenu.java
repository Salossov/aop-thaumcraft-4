package thaumcraft.menu;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import thaumcraft.items.HandMirrorItem;
import thaumcraft.registry.R3Content;

/** Transfers after a completed inventory transaction, never from Slot.setChanged. */
public final class HandMirrorMenu extends AbstractContainerMenu {
    private final Inventory inventory;
    private final int locked;
    private final ItemStack mirror;
    private final SimpleContainer input=new SimpleContainer(1);
    private boolean sending;
    public HandMirrorMenu(int id,Inventory inventory,RegistryFriendlyByteBuf buffer){this(id,inventory,buffer.readVarInt());}
    public HandMirrorMenu(int id,Inventory inventory,int slot){
        super(R3Content.HAND_MIRROR_MENU.get(),id);
        if(slot<0||slot>=inventory.getContainerSize())throw new IllegalArgumentException("Invalid mirror slot");
        this.inventory=inventory;locked=slot;mirror=inventory.getItem(slot);
        addSlot(new Slot(input,0,80,24){@Override public boolean mayPlace(ItemStack stack){return !(stack.getItem() instanceof HandMirrorItem);}});
        for(int row=0;row<3;row++)for(int col=0;col<9;col++)playerSlot(col+row*9+9,8+col*18,84+row*18);
        for(int col=0;col<9;col++)playerSlot(col,8+col*18,142);
    }
    private void playerSlot(int slot,int x,int y){addSlot(new Slot(inventory,slot,x,y){@Override public boolean mayPickup(Player player){return slot!=locked;}@Override public boolean mayPlace(ItemStack stack){return slot!=locked;}});}
    @Override public boolean stillValid(Player player){return player==inventory.player&&inventory.getItem(locked).getItem() instanceof HandMirrorItem&&(player.level().isClientSide||inventory.getItem(locked)==mirror);}
    private void send(){
        if(sending||!stillValid(inventory.player)||!(inventory.player instanceof net.minecraft.server.level.ServerPlayer player)||input.isEmpty())return;
        sending=true;
        try{var target=HandMirrorItem.destination(player,mirror);var stack=input.getItem(0);if(target!=null&&!(stack.getItem() instanceof HandMirrorItem)&&target.eject(stack))input.setItem(0,ItemStack.EMPTY);}finally{sending=false;}
    }
    @Override public void broadcastChanges(){send();super.broadcastChanges();}
    @Override public void clicked(int index,int button,ClickType type,Player player){if(!stillValid(player)||index>0&&index<slots.size()&&slots.get(index).getContainerSlot()==locked||type==ClickType.SWAP&&(button==locked||button==40&&locked==40))return;super.clicked(index,button,type,player);send();}
    @Override public ItemStack quickMoveStack(Player player,int index){
        if(index<0||index>=slots.size()||!stillValid(player))return ItemStack.EMPTY;
        var slot=slots.get(index);if(!slot.hasItem()||!slot.mayPickup(player))return ItemStack.EMPTY;
        var stack=slot.getItem();var original=stack.copy();
        if(!(index==0?moveItemStackTo(stack,1,slots.size(),true):moveItemStackTo(stack,0,1,false)))return ItemStack.EMPTY;
        if(stack.isEmpty())slot.setByPlayer(ItemStack.EMPTY);else slot.setChanged();slot.onTake(player,stack);return original;
    }
    @Override public void removed(Player player){if(!player.level().isClientSide)clearContainer(player,input);super.removed(player);}
}
