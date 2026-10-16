package thaumcraft.items;

import java.util.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

public final class FocusPouchItem extends Item implements ICurioItem {
    public static final int SIZE=18;
    public FocusPouchItem(){super(new Properties().stacksTo(1).rarity(Rarity.RARE));}
    public static List<ItemStack> contents(ItemStack stack){var contents=stack.getOrDefault(DataComponents.CONTAINER,ItemContainerContents.EMPTY);var items=new ArrayList<ItemStack>();for(int i=0;i<SIZE;i++)items.add(i<contents.getSlots()?contents.getStackInSlot(i):ItemStack.EMPTY);return items;}
    public static void store(ItemStack pouch,List<ItemStack> contents){if(contents.size()!=SIZE||contents.stream().anyMatch(stack->!stack.isEmpty()&&(!(stack.getItem() instanceof thaumcraft.api.wands.ItemFocusBasic)||stack.getCount()!=1)))throw new IllegalArgumentException("Invalid focus pouch contents");pouch.set(DataComponents.CONTAINER,ItemContainerContents.fromItems(contents));}
    @Override public InteractionResultHolder<ItemStack> use(Level level,Player player,InteractionHand hand){var stack=player.getItemInHand(hand);if(player instanceof net.minecraft.server.level.ServerPlayer server){int slot=hand==InteractionHand.MAIN_HAND?player.getInventory().selected:40;server.openMenu(new SimpleMenuProvider((id,inventory,p)->new thaumcraft.menu.FocusPouchMenu(id,inventory,slot),stack.getHoverName()),buffer->buffer.writeVarInt(slot));}return InteractionResultHolder.sidedSuccess(stack,level.isClientSide);}
}
