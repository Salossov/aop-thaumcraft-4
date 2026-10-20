package thaumcraft.api.world;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/** Server-side item hook after the completed NeoForge break-permission event,
 * before the original block is removed. Returning true defers/cancels this
 * removal. Returning false continues normal harvesting, drops and experience.
 * Called from ServerPlayerGameMode.destroyBlock, including programmatic breaks;
 * implementations that initiate additional breaks must guard reentrancy.
 * The current block state is read again after the callback before harvesting.
 */
public interface IBlockStartBreakTool {
    boolean onBlockStartBreak(ItemStack stack,BlockPos position,ServerPlayer player);
}
