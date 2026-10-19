package thaumcraft.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** 1.7.10's selector rejected non-living candidates before casting them. Keep
 * that rejection when an addon redirects a formerly player-targeting goal.
 */
@Mixin(NearestAttackableTargetGoal.class)
public abstract class NearestTargetNonLivingMixin {
    @Shadow @Final protected Class<? extends LivingEntity> targetType;
    @Shadow protected LivingEntity target;
    @Inject(method="findTarget",at=@At("HEAD"),cancellable=true)
    private void thaumcraft$rejectNonLivingTarget(CallbackInfo callback) {
        if(!LivingEntity.class.isAssignableFrom(targetType)) {target=null;callback.cancel();}
    }
}
