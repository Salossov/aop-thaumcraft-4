package thaumcraft.menu;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import thaumcraft.data.ThaumcraftData;
import thaumcraft.registry.R3Content;

public final class AlchemyFurnaceMenu extends AbstractContainerMenu {
    private final Container furnace;
    private final ContainerData data;
    public AlchemyFurnaceMenu(int id, Inventory inventory, RegistryFriendlyByteBuf ignored) { this(id, inventory, new SimpleContainer(2), new SimpleContainerData(5)); }
    public AlchemyFurnaceMenu(int id, Inventory inventory, Container furnace, ContainerData data) {
        super(R3Content.FURNACE_MENU.get(), id); checkContainerSize(furnace, 2); checkContainerDataCount(data, 5); this.furnace = furnace; this.data = data;
        addSlot(new Slot(furnace, 0, 80, 8) { @Override public boolean mayPlace(ItemStack stack) { return inventory.player.level().isClientSide || !ThaumcraftData.aspects(inventory.player.getServer(), stack).isEmpty(); } });
        addSlot(new Slot(furnace, 1, 80, 48) { @Override public boolean mayPlace(ItemStack stack) { return stack.getBurnTime(RecipeType.SMELTING) > 0; } });
        for (int row = 0; row < 3; row++) for (int col = 0; col < 9; col++) addSlot(new Slot(inventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
        for (int col = 0; col < 9; col++) addSlot(new Slot(inventory, col, 8 + col * 18, 142)); addDataSlots(data);
    }
    public int burnScaled(int height) { return data.get(0) * height / Math.max(1, data.get(1)); }
    public int cookScaled(int height) { return data.get(2) * height / Math.max(1, data.get(3)); }
    public int contentsScaled(int height) { return data.get(4) * height / 50; }
    @Override public boolean stillValid(Player player) { return furnace.stillValid(player); }
    @Override public ItemStack quickMoveStack(Player player, int index) {
        var slot = slots.get(index); if (!slot.hasItem()) return ItemStack.EMPTY;
        var stack = slot.getItem(); var copy = stack.copy();
        if (index < 2) { if (!moveItemStackTo(stack, 2, slots.size(), true)) return ItemStack.EMPTY; }
        else if (stack.getBurnTime(RecipeType.SMELTING) > 0) { if (!moveItemStackTo(stack, 1, 2, false)) return ItemStack.EMPTY; }
        else if (!moveItemStackTo(stack, 0, 1, false)) return ItemStack.EMPTY;
        if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY); else slot.setChanged(); slot.onTake(player, stack); return copy;
    }
}
