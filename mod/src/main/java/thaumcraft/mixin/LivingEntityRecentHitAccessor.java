package thaumcraft.mixin;

import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LivingEntity.class)
public interface LivingEntityRecentHitAccessor {
    @Accessor("lastHurtByPlayerTime") void thaumcraft$setRecentlyHit(int ticks);
    @Accessor("lastHurtByPlayerTime") int thaumcraft$getRecentlyHit();
}
