package thaumcraft.mixin;

import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import thaumcraft.api.entities.LivingEquipmentTickEvent;

@Mixin(LivingEntity.class)
public abstract class LivingEquipmentTickMixin {
    @Inject(method="aiStep",at=@At("HEAD"))
    private void thaumcraft$equipmentTick(CallbackInfo callback) {
        LivingEquipmentTickEvent.BUS.post(new LivingEquipmentTickEvent((LivingEntity)(Object)this));
    }
}
