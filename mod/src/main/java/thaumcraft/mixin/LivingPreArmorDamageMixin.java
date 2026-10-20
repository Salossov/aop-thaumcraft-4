package thaumcraft.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import thaumcraft.api.entities.LivingPreArmorDamageEvent;

/** Update the active NeoForge container before armor reductions are calculated.
 * Both overrides are covered; neither calls the other on this engine version.
 */
@Mixin({LivingEntity.class,Player.class})
public abstract class LivingPreArmorDamageMixin {
    @WrapMethod(method="actuallyHurt")
    private void thaumcraft$beforeArmor(DamageSource source,float amount,Operation<Void> original) {
        var entity=(LivingEntity)(Object)this;
        if(!entity.isInvulnerableTo(source)) {
            var container=((LivingDamageContainersAccessor)entity).thaumcraft$damageContainers().peek();
            var event=new LivingPreArmorDamageEvent(entity,source,container.getNewDamage());
            LivingPreArmorDamageEvent.BUS.post(event);
            container.setNewDamage(event.damage());
            amount=event.damage();
            if(amount<=0)return;
        }
        original.call(source,amount);
    }
}
