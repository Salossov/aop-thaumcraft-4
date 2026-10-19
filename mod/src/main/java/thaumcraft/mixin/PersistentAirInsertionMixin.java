package thaumcraft.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import thaumcraft.api.world.IPersistentAir;

/** NeoForge already counts custom air states; bypass only vanilla's earlier insertion shortcut. */
@Mixin(LevelChunk.class)
public abstract class PersistentAirInsertionMixin {
    @WrapOperation(method = "setBlockState", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/state/BlockState;isAir()Z"), require = 1)
    private boolean thaumcraft$keepPersistentAir(BlockState state, Operation<Boolean> original) {
        return original.call(state) && !(state.getBlock() instanceof IPersistentAir);
    }
}
