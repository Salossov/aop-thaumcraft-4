package thaumcraft.effects;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.*;
import net.minecraft.world.damagesource.*;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.*;

public final class TaintEffects {
    private static final DeferredRegister<MobEffect> EFFECTS=DeferredRegister.create(Registries.MOB_EFFECT,"thaumcraft");
    public static final DeferredHolder<MobEffect,MobEffect> FLUX_TAINT=EFFECTS.register("flux_taint",FluxTaintEffect::new);
    public static final DeferredHolder<MobEffect,MobEffect> VIS_EXHAUST=EFFECTS.register("vis_exhaust",VisExhaustEffect::new);
    public static final ResourceKey<DamageType> TAINT=ResourceKey.create(Registries.DAMAGE_TYPE,ResourceLocation.fromNamespaceAndPath("thaumcraft","taint"));
    public static DamageSource damage(LivingEntity entity) { return new DamageSource(entity.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(TAINT)); }
    public static void register(IEventBus bus) { EFFECTS.register(bus);net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(TaintConversions::death); }
    private TaintEffects() {}
}
