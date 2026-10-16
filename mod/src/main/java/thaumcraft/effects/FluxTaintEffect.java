package thaumcraft.effects;

import net.minecraft.world.effect.*;
import net.minecraft.world.entity.LivingEntity;
import thaumcraft.entities.TaintedMob;

public final class FluxTaintEffect extends MobEffect {
    public FluxTaintEffect() { super(MobEffectCategory.HARMFUL,0x663377); }
    @Override public boolean shouldApplyEffectTickThisTick(int duration,int amplifier) { int interval=40>>amplifier;return interval<=0||duration%interval==0; }
    @Override public boolean applyEffectTick(LivingEntity entity,int amplifier) {
        if(entity instanceof TaintedMob) entity.heal(1);
        else if(!entity.isInvertedHealAndHarm()) entity.hurt(TaintEffects.damage(entity),1);
        return true;
    }
}
