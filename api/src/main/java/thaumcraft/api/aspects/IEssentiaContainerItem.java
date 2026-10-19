package thaumcraft.api.aspects;

import net.minecraft.world.item.ItemStack;

/** Original contained-essentia bonus marker. Positive contained units are added to
 * ordinary object aspects for scanning and processing. Merely implementing the
 * read-only IAspectItem inspection contract does not opt a jar into this bonus.
 */
public interface IEssentiaContainerItem extends IAspectItem {
    void setAspects(ItemStack stack,AspectList aspects);
}
