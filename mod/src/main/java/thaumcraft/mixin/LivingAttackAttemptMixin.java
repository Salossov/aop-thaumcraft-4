package thaumcraft.mixin;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import thaumcraft.api.entities.LivingAttackAttemptEvent;

@Mixin(LivingEntity.class)
public abstract class LivingAttackAttemptMixin {
    @Inject(method="hurt",at=@At("HEAD"),cancellable=true)
    private void thaumcraft$attackAttempt(DamageSource source,float amount,CallbackInfoReturnable<Boolean> result){
        var event=new LivingAttackAttemptEvent((LivingEntity)(Object)this,source,amount);
        LivingAttackAttemptEvent.BUS.post(event);
        if(event.isCanceled())result.setReturnValue(false);
    }
}
