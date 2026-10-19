package thaumcraft.mixin;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import thaumcraft.equipment.SpecialArmorProtection;

@Mixin(LivingEntity.class)
public abstract class SpecialArmorProtectionMixin {
    @Inject(method="getDamageAfterArmorAbsorb",at=@At("HEAD"),cancellable=true)
    private void thaumcraft$specialArmor(DamageSource source,float damage,CallbackInfoReturnable<Float> result) {
        if((Object)this instanceof Player player&&SpecialArmorProtection.active(player))
            result.setReturnValue(SpecialArmorProtection.absorb(player,source,damage));
    }
    @Inject(method="getArmorValue",at=@At("HEAD"),cancellable=true)
    private void thaumcraft$specialArmorDisplay(CallbackInfoReturnable<Integer> result) {
        if((Object)this instanceof Player player&&SpecialArmorProtection.active(player))
            result.setReturnValue(SpecialArmorProtection.display(player));
    }
}
