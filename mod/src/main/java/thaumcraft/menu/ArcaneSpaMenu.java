package thaumcraft.menu;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import thaumcraft.production.ArcaneSpaBlockEntity;
import thaumcraft.registry.R4Content;

public final class ArcaneSpaMenu extends AbstractContainerMenu {
    private final Container tile;
    private final ContainerData data;
    public ArcaneSpaMenu(int id,Inventory inventory,RegistryFriendlyByteBuf ignored){this(id,inventory,new SimpleContainer(1),new SimpleContainerData(3));}
    public ArcaneSpaMenu(int id,Inventory inventory,Container tile,ContainerData data){super(R4Content.SPA_MENU.get(),id);checkContainerSize(tile,1);checkContainerDataCount(data,3);this.tile=tile;this.data=data;addSlot(new Slot(tile,0,65,31){@Override public boolean mayPlace(ItemStack s){return s.is(R4Content.BATH_SALTS.get());}});for(int row=0;row<3;row++)for(int col=0;col<9;col++)addSlot(new Slot(inventory,col+row*9+9,8+col*18,84+row*18));for(int col=0;col<9;col++)addSlot(new Slot(inventory,col,8+col*18,142));addDataSlots(data);}
    public boolean mixing(){return data.get(0)!=0;}
    public FluidStack fluid(){return data.get(2)>0?new FluidStack(BuiltInRegistries.FLUID.byId(data.get(1)),data.get(2)):FluidStack.EMPTY;}
    @Override public boolean stillValid(Player player){return tile.stillValid(player);}
    @Override public boolean clickMenuButton(Player player,int button){if(button!=1||!stillValid(player)||!(tile instanceof ArcaneSpaBlockEntity spa))return false;spa.toggleMix();return true;}
    @Override public ItemStack quickMoveStack(Player player,int index){if(index<0||index>=slots.size())return ItemStack.EMPTY;var slot=slots.get(index);if(!slot.hasItem())return ItemStack.EMPTY;var stack=slot.getItem();var copy=stack.copy();if(index==0?!moveItemStackTo(stack,1,slots.size(),true):!stack.is(R4Content.BATH_SALTS.get())||!moveItemStackTo(stack,0,1,false))return ItemStack.EMPTY;if(stack.isEmpty())slot.setByPlayer(ItemStack.EMPTY);else slot.setChanged();slot.onTake(player,stack);return copy;}
}
