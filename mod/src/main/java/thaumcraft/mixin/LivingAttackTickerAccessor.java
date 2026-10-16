package thaumcraft.mixin;

import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LivingEntity.class)
public interface LivingAttackTickerAccessor {
    @Accessor("attackStrengthTicker") int thaumcraft$getAttackTicker();
    @Accessor("attackStrengthTicker") void thaumcraft$setAttackTicker(int value);
}
