package thaumcraft.effects;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.*;
import net.neoforged.neoforge.registries.*;

public final class WarpEffects {
    private static final DeferredRegister<MobEffect> EFFECTS=DeferredRegister.create(Registries.MOB_EFFECT,"thaumcraft");
    public static final DeferredHolder<MobEffect,MobEffect> THAUMARHIA=effect("thaumarhia",Kind.FLUX),UNNATURAL_HUNGER=effect("unnatural_hunger",Kind.HUNGER),SUN_SCORNED=effect("sun_scorned",Kind.SUN),INFECTIOUS_EXHAUST=effect("infectious_vis_exhaust",Kind.INFECTION),DEATH_GAZE=effect("death_gaze",Kind.GAZE),BLURRED_VISION=effect("blurred_vision",Kind.BLUR),WARP_WARD=effect("warp_ward",Kind.WARD);
    private enum Kind{FLUX,HUNGER,SUN,INFECTION,GAZE,BLUR,WARD}
    private static DeferredHolder<MobEffect,MobEffect> effect(String id,Kind kind){return EFFECTS.register(id,()->new Effect(kind));}
    private static final class Effect extends MobEffect {
        private final Kind kind;
        private Effect(Kind kind){super(kind==Kind.WARD?MobEffectCategory.BENEFICIAL:MobEffectCategory.HARMFUL,switch(kind){case FLUX->0x664477;case HUNGER->0x446633;case SUN->16308330;case INFECTION->0x665577;case GAZE->0x664433;case BLUR->0x808080;case WARD->14742263;});this.kind=kind;}
        @Override public void fillEffectCures(java.util.Set<EffectCure> cures,MobEffectInstance instance){super.fillEffectCures(cures,instance);}
        @Override public boolean shouldApplyEffectTickThisTick(int remaining,int amplifier){return switch(kind){case HUNGER->true;case FLUX->remaining%20==0;case SUN,INFECTION->remaining%40==0;default->false;};}
        @Override public boolean applyEffectTick(LivingEntity target,int amplifier){
            if(target.level().isClientSide)return true;
            var level=target.level();
            switch(kind){
                case HUNGER->{if(target instanceof Player player)player.causeFoodExhaustion(.025f*(amplifier+1));}
                case FLUX->{var pos=target.blockPosition();if(level.random.nextInt(15)==0&&level.isEmptyBlock(pos))thaumcraft.fluids.FluxBlock.setUnits(level,pos,thaumcraft.registry.R2Content.FLUX_GOO.get(),8);}
                case SUN->{float brightness=target.getLightLevelDependentMagicValue();if(brightness>.5&&level.random.nextFloat()*30<(brightness-.4)*2&&level.canSeeSky(target.blockPosition()))target.igniteForSeconds(4);else if(brightness<.25&&level.random.nextFloat()>brightness*2)target.heal(1);}
                case INFECTION->{for(var entity:level.getEntitiesOfClass(LivingEntity.class,target.getBoundingBox().inflate(4)))if(entity!=target&&!entity.hasEffect(INFECTIOUS_EXHAUST)){var spread=new MobEffectInstance(amplifier>0?INFECTIOUS_EXHAUST:TaintEffects.VIS_EXHAUST,6000,Math.max(0,amplifier-1),false,true);spread.getCures().add(EffectCures.MILK);entity.addEffect(spread);}}
                default->{}
            }
            return true;
        }
    }
    public static void register(IEventBus bus){EFFECTS.register(bus);}
    private WarpEffects(){}
}
