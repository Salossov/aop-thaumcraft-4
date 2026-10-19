package thaumcraft.api.aspects;

import net.minecraft.world.item.ItemStack;

/** Read the real contained essentia, rather than the item's object/scanning aspects. */
public interface IAspectItem {
    /** Independent snapshot; null means empty, as in the original item-container API. */
    AspectList getAspects(ItemStack stack);
}
