package thaumcraft.api.nodes;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/** Original TC4 extension for equipment revealing aura nodes. */
public interface IRevealer {
    boolean showNodes(ItemStack stack, LivingEntity wearer);
}
