package thaumcraft.menu;

import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import thaumcraft.items.ThaumicArmorItem;
import thaumcraft.registry.R3Content;

public final class HarnessMenu extends AbstractContainerMenu {
    private final Inventory inventory;
    private final int harnessSlot;
    private final ItemStack harness;
    private final SimpleContainer jar;
    public HarnessMenu(int id,Inventory inventory,RegistryFriendlyByteBuf data){this(id,inventory,data.readVarInt());}
    public HarnessMenu(int id,Inventory inventory,int slot){
        super(R3Content.HARNESS_MENU.get(),id);
        if(slot<0||slot>=inventory.getContainerSize())throw new IllegalArgumentException("Invalid harness slot");
        this.inventory=inventory;harnessSlot=slot;harness=inventory.getItem(slot);
        jar=new SimpleContainer(1){@Override public void setChanged(){super.setChanged();store();}};
        if(!inventory.player.level().isClientSide)jar.setItem(0,harness.getOrDefault(DataComponents.CONTAINER,ItemContainerContents.EMPTY).copyOne());
        addSlot(new Slot(jar,0,80,32){
            @Override public int getMaxStackSize(){return 1;}
            @Override public boolean mayPlace(ItemStack stack){return stack.is(R3Content.WARDED_JAR.get().asItem());}
        });
        for(int row=0;row<3;row++)for(int col=0;col<9;col++)addPlayerSlot(col+row*9+9,8+col*18,84+row*18);
        for(int col=0;col<9;col++)addPlayerSlot(col,8+col*18,142);
    }
    private void addPlayerSlot(int index,int x,int y){addSlot(new Slot(inventory,index,x,y){
        @Override public boolean mayPickup(Player player){return index!=harnessSlot;}
        @Override public boolean mayPlace(ItemStack stack){return index!=harnessSlot;}
    });}
    private void store(){if(!inventory.player.level().isClientSide){harness.set(DataComponents.CONTAINER,ItemContainerContents.fromItems(List.of(jar.getItem(0))));inventory.setChanged();}}
    public int lockedHotbarSlot(){return harnessSlot<9?harnessSlot:-1;}
    @Override public boolean stillValid(Player player){
        var current=inventory.getItem(harnessSlot);
        return player==inventory.player&&current.getItem() instanceof ThaumicArmorItem item&&item.kind()==ThaumicArmorItem.Kind.HARNESS&&(player.level().isClientSide||current==harness);
    }
    @Override public void clicked(int index,int button,ClickType type,Player player){
        if(!stillValid(player)||index>=1&&index<slots.size()&&slots.get(index).getContainerSlot()==harnessSlot)return;
        if(type==ClickType.SWAP&&button==harnessSlot)return;
        super.clicked(index,button,type,player);store();
    }
    @Override public ItemStack quickMoveStack(Player player,int index){
        if(index<0||index>=slots.size()||!stillValid(player))return ItemStack.EMPTY;
        var slot=slots.get(index);if(!slot.mayPickup(player)||!slot.hasItem())return ItemStack.EMPTY;
        var stack=slot.getItem();var copy=stack.copy();
        if(!(index==0?moveItemStackTo(stack,1,slots.size(),true):moveItemStackTo(stack,0,1,false)))return ItemStack.EMPTY;
        if(stack.isEmpty())slot.setByPlayer(ItemStack.EMPTY);else slot.setChanged();slot.onTake(player,stack);store();return copy;
    }
    @Override public void removed(Player player){store();super.removed(player);}
}
