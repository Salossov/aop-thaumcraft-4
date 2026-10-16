package thaumcraft.api;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;

/** Original equipment warp contract expressed with native game types. */
public interface IWarpingGear {
    int getWarp(ItemStack stack,Player player);
}
