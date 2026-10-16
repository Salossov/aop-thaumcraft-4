package thaumcraft.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import thaumcraft.world.WorldBiomes;

@Mixin(BiomeManager.class)
public abstract class BiomeManagerMixin {
    @Inject(method="getBiome",at=@At("HEAD"),cancellable=true)
    private void thaumcraft$exactColumn(BlockPos pos,CallbackInfoReturnable<Holder<Biome>> callback) {
        var biome=WorldBiomes.globalOverride((BiomeManager)(Object)this,pos);if(biome!=null) callback.setReturnValue(biome);
    }
}
