package thaumcraft.menu;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import thaumcraft.entities.TravelingTrunk;

public final class TrunkMenu extends AbstractContainerMenu {
    public final TravelingTrunk trunk;
    public TrunkMenu(int id,Inventory inventory,RegistryFriendlyByteBuf buffer){this(id,inventory,(TravelingTrunk)inventory.player.level().getEntity(buffer.readVarInt()));}
    public TrunkMenu(int id,Inventory inventory,TravelingTrunk trunk){super(thaumcraft.registry.R3Content.TRUNK_MENU.get(),id);this.trunk=java.util.Objects.requireNonNull(trunk);if(!inventory.player.level().isClientSide)trunk.open();for(int row=0;row<trunk.rows();row++)for(int col=0;col<9;col++)addSlot(new Slot(trunk.inventory,col+row*9,8+col*18,15+row*23));for(int row=0;row<3;row++)for(int col=0;col<9;col++)addSlot(new Slot(inventory,col+row*9+9,8+col*18,118+row*18));for(int col=0;col<9;col++)addSlot(new Slot(inventory,col,8+col*18,176));}
    @Override public boolean stillValid(Player player){return trunk.isAlive()&&trunk.allowed(player)&&player.level()==trunk.level()&&player.distanceToSqr(trunk)<64;}
    @Override public boolean clickMenuButton(Player player,int button){if(button!=1||!stillValid(player))return false;trunk.toggleStay();return true;}
    @Override public ItemStack quickMoveStack(Player player,int index){if(!stillValid(player)||index<0||index>=slots.size())return ItemStack.EMPTY;var slot=slots.get(index);if(!slot.hasItem())return ItemStack.EMPTY;var stack=slot.getItem();var original=stack.copy();int end=trunk.rows()*9;if(!(index<end?moveItemStackTo(stack,end,slots.size(),true):moveItemStackTo(stack,0,end,false)))return ItemStack.EMPTY;if(stack.isEmpty())slot.setByPlayer(ItemStack.EMPTY);else slot.setChanged();return original;}
    @Override public void removed(Player player){super.removed(player);if(!player.level().isClientSide)trunk.close();}
}
