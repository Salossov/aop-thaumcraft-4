package thaumcraft.api.crafting;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
/** Original spelling retained for addon compatibility. */
public interface IInfusionStabiliser {
    boolean canStabaliseInfusion(Level level,BlockPos pos);
}
