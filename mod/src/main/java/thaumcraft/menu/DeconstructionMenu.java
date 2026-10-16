package thaumcraft.menu;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import thaumcraft.api.aspects.*;
import thaumcraft.production.DeconstructionTableBlockEntity;
import thaumcraft.registry.R4Content;

public final class DeconstructionMenu extends AbstractContainerMenu {
    private final Container table;
    private final ContainerData data;
    public DeconstructionMenu(int id,Inventory inventory,RegistryFriendlyByteBuf ignored){this(id,inventory,new SimpleContainer(1),new SimpleContainerData(2));}
    public DeconstructionMenu(int id,Inventory inventory,Container table,ContainerData data){super(R4Content.DECONSTRUCTOR_MENU.get(),id);checkContainerSize(table,1);checkContainerDataCount(data,2);this.table=table;this.data=data;
        addSlot(new Slot(table,0,64,16){@Override public boolean mayPlace(ItemStack stack){return table.canPlaceItem(0,stack);}});
        for(int row=0;row<3;row++)for(int col=0;col<9;col++)addSlot(new Slot(inventory,col+row*9+9,8+col*18,84+row*18));
        for(int col=0;col<9;col++)addSlot(new Slot(inventory,col,8+col*18,142));addDataSlots(data);
    }
    public int progressScaled(int size){return data.get(0)*size/40;}
    public Aspect pendingAspect(){int n=data.get(1);return n>0&&n<=Aspects.PRIMALS.size()?Aspects.PRIMALS.get(n-1):null;}
    @Override public boolean stillValid(Player player){return table.stillValid(player);}
    @Override public boolean clickMenuButton(Player player,int button){return button==1&&player instanceof net.minecraft.server.level.ServerPlayer server&&table instanceof DeconstructionTableBlockEntity tile&&tile.claim(server);}
    @Override public ItemStack quickMoveStack(Player player,int index){if(index<0||index>=slots.size())return ItemStack.EMPTY;var slot=slots.get(index);if(!slot.hasItem())return ItemStack.EMPTY;var stack=slot.getItem();var copy=stack.copy();if(index==0?!moveItemStackTo(stack,1,slots.size(),true):!table.canPlaceItem(0,stack)||!moveItemStackTo(stack,0,1,false))return ItemStack.EMPTY;if(stack.isEmpty())slot.setByPlayer(ItemStack.EMPTY);else slot.setChanged();slot.onTake(player,stack);return copy;}
}
