package thaumcraft.api.aspects;

import net.minecraft.world.item.ItemStack;

/** Original ItemJarFilled identity and contents access, without a core implementation import. */
public interface IEssentiaJarItem extends IAspectItem {
    void setAspects(ItemStack stack,AspectList aspects);
}
