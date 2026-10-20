package thaumcraft.api.crafting;

import com.mojang.serialization.DataResult;
import net.minecraft.world.item.ItemStack;

/** Opt-in recipe output counts above an item's ordinary inventory stack limit. */
public interface IInfusionOutput {
    /** Maximum result count, capped at the item codec's limit of 99.
     * This does not change stacking in inventories or ordinary crafting.
     */
    int getInfusionOutputLimit(ItemStack result);

    static DataResult<ItemStack> validateOutput(ItemStack result) {
        return ItemStack.validateComponents(result.getComponents()).flatMap(ignored -> {
            int limit = result.getMaxStackSize();
            if (result.getItem() instanceof IInfusionOutput item)
                limit = Math.max(limit, Math.min(99, item.getInfusionOutputLimit(result)));
            int maximum = limit;
            return result.getCount() <= maximum ? DataResult.success(result)
                    : DataResult.error(() -> "Infusion result count " + result.getCount() + " exceeds maximum " + maximum);
        });
    }
}
