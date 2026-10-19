package thaumcraft.api.aspects;

import java.util.Map;
import net.minecraft.world.item.ItemStack;

/** Variant-dependent object/scanning aspects; distinct from contained essentia. */
public interface IObjectAspectItem {
    /** Reviewed intrinsic minima, merged with data-pack object and modifier rules. */
    Map<String, Integer> objectAspects(ItemStack stack);
    /** Stable variant suffix. Empty retains the ordinary item/group scan identity. */
    default String scanVariant(ItemStack stack) { return ""; }
    /** Strip incidental NBT while preserving variant components needed for a legacy slot scan. */
    default ItemStack inventoryScanStack(ItemStack stack) { return new ItemStack(stack.getItem()); }
}
