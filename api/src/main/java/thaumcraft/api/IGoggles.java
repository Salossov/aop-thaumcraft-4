package thaumcraft.api;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/** Original TC4 extension for the head-slot aspect HUD. */
public interface IGoggles {
    boolean showIngamePopups(ItemStack stack, LivingEntity wearer);
}
