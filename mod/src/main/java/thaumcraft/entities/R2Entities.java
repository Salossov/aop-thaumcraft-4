package thaumcraft.entities;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.*;

public final class R2Entities {
    private static final DeferredRegister<EntityType<?>> TYPES=DeferredRegister.create(Registries.ENTITY_TYPE,"thaumcraft");
    public static final DeferredHolder<EntityType<?>,EntityType<FallingTaint>> FALLING_TAINT=TYPES.register("falling_taint",()->EntityType.Builder.<FallingTaint>of(FallingTaint::new,MobCategory.MISC).sized(.98f,.98f).clientTrackingRange(4).updateInterval(3).build("thaumcraft:falling_taint"));
    public static final DeferredHolder<EntityType<?>,EntityType<BrainyZombie>> BRAINY=TYPES.register("brainy_zombie",()->EntityType.Builder.of(BrainyZombie::new,MobCategory.MONSTER).sized(0.6f,1.8f).build("thaumcraft:brainy_zombie"));
    public static final DeferredHolder<EntityType<?>,EntityType<BrainyZombie>> GIANT_BRAINY=TYPES.register("giant_brainy_zombie",()->EntityType.Builder.of(BrainyZombie::new,MobCategory.MONSTER).sized(1.32f,3.96f).build("thaumcraft:giant_brainy_zombie"));
    public static final DeferredHolder<EntityType<?>,EntityType<AspectOrb>> ASPECT_ORB=TYPES.register("aspect_orb",()->EntityType.Builder.<AspectOrb>of(AspectOrb::new,MobCategory.MISC).sized(0.5f,0.5f).clientTrackingRange(8).updateInterval(3).build("thaumcraft:aspect_orb"));
    public static final DeferredHolder<EntityType<?>,EntityType<TaintSpore>> TAINT_SPORE=TYPES.register("taint_spore",()->EntityType.Builder.<TaintSpore>of(TaintSpore::new,MobCategory.MONSTER).sized(0.5f,0.5f).build("thaumcraft:taint_spore"));
    public static final DeferredHolder<EntityType<?>,EntityType<TaintSpider>> TAINT_SPIDER=TYPES.register("taint_spider",()->EntityType.Builder.<TaintSpider>of(TaintSpider::new,MobCategory.MONSTER).sized(0.4f,0.3f).build("thaumcraft:taint_spider"));
    public static final DeferredHolder<EntityType<?>,EntityType<TaintSwarmer>> TAINT_SWARMER=TYPES.register("taint_spore_swarmer",()->EntityType.Builder.<TaintSwarmer>of(TaintSwarmer::new,MobCategory.MONSTER).sized(1,1).build("thaumcraft:taint_spore_swarmer"));
    public static final DeferredHolder<EntityType<?>,EntityType<TaintSwarm>> TAINT_SWARM=TYPES.register("taint_swarm",()->EntityType.Builder.<TaintSwarm>of(TaintSwarm::new,MobCategory.MONSTER).sized(2,2).build("thaumcraft:taint_swarm"));
    public static final DeferredHolder<EntityType<?>,EntityType<ThaumicSlime>> THAUMIC_SLIME=TYPES.register("thaumic_slime",()->EntityType.Builder.<ThaumicSlime>of(ThaumicSlime::new,MobCategory.MONSTER).sized(0.5f,0.5f).build("thaumcraft:thaumic_slime"));
    public static final DeferredHolder<EntityType<?>,EntityType<TaintedCreature>> TAINT_PIG=creature("taint_pig",0.9f,0.9f),TAINT_COW=creature("taint_cow",0.9f,1.3f),TAINT_CHICKEN=creature("taint_chicken",0.5f,0.8f),TAINT_SHEEP=creature("taint_sheep",0.9f,1.3f),TAINT_VILLAGER=creature("taint_villager",0.6f,1.8f);
    public static final DeferredHolder<EntityType<?>,EntityType<TaintCreeper>> TAINT_CREEPER=TYPES.register("taint_creeper",()->EntityType.Builder.<TaintCreeper>of(TaintCreeper::new,MobCategory.MONSTER).sized(0.6f,1.7f).build("thaumcraft:taint_creeper"));
    public static final DeferredHolder<EntityType<?>,EntityType<Wisp>> WISP=TYPES.register("wisp",()->EntityType.Builder.<Wisp>of(Wisp::new,MobCategory.MONSTER).sized(0.9f,0.9f).build("thaumcraft:wisp"));
    public static final DeferredHolder<EntityType<?>,EntityType<Taintacle>> TAINTACLE=tentacle("taintacle",0.66f,3),TAINTACLE_SMALL=tentacle("taintacle_small",0.22f,1);
    public static final DeferredHolder<EntityType<?>,EntityType<EldritchGuardian>> ELDRITCH_GUARDIAN=TYPES.register("eldritch_guardian",()->EntityType.Builder.<EldritchGuardian>of(EldritchGuardian::new,MobCategory.MONSTER).sized(0.8f,2.25f).eyeHeight(2.1f).build("thaumcraft:eldritch_guardian"));
    public static final DeferredHolder<EntityType<?>,EntityType<EldritchOrb>> ELDRITCH_ORB=TYPES.register("eldritch_orb",()->EntityType.Builder.<EldritchOrb>of(EldritchOrb::new,MobCategory.MISC).sized(0.25f,0.25f).clientTrackingRange(8).updateInterval(1).build("thaumcraft:eldritch_orb"));
    private static DeferredHolder<EntityType<?>,EntityType<Taintacle>> tentacle(String name,float width,float height) {return TYPES.register(name,()->EntityType.Builder.<Taintacle>of(Taintacle::new,MobCategory.MONSTER).sized(width,height).build("thaumcraft:"+name));}
    private static DeferredHolder<EntityType<?>,EntityType<TaintedCreature>> creature(String name,float width,float height) {return TYPES.register(name,()->EntityType.Builder.<TaintedCreature>of(TaintedCreature::new,MobCategory.MONSTER).sized(width,height).build("thaumcraft:"+name));}
    public static void register(IEventBus bus) { TYPES.register(bus);bus.addListener(R2Entities::attributes); }
    private static void attributes(EntityAttributeCreationEvent event) {
        event.put(BRAINY.get(),BrainyZombie.attributes(false).build());event.put(GIANT_BRAINY.get(),BrainyZombie.attributes(true).build());
        event.put(TAINT_SPORE.get(),TaintSpore.attributes(false).build());event.put(TAINT_SPIDER.get(),TaintSpider.attributes().build());
        event.put(TAINT_SWARMER.get(),TaintSpore.attributes(true).build());event.put(TAINT_SWARM.get(),TaintSwarm.attributes().build());event.put(THAUMIC_SLIME.get(),ThaumicSlime.attributes().build());
        event.put(TAINT_PIG.get(),TaintedCreature.attributes(TaintedCreature.Kind.PIG).build());event.put(TAINT_COW.get(),TaintedCreature.attributes(TaintedCreature.Kind.COW).build());event.put(TAINT_CHICKEN.get(),TaintedCreature.attributes(TaintedCreature.Kind.CHICKEN).build());event.put(TAINT_SHEEP.get(),TaintedCreature.attributes(TaintedCreature.Kind.SHEEP).build());event.put(TAINT_VILLAGER.get(),TaintedCreature.attributes(TaintedCreature.Kind.VILLAGER).build());event.put(TAINT_CREEPER.get(),TaintCreeper.attributes().build());
        event.put(WISP.get(),Wisp.attributes().build());event.put(TAINTACLE.get(),Taintacle.attributes(false).build());event.put(TAINTACLE_SMALL.get(),Taintacle.attributes(true).build());event.put(ELDRITCH_GUARDIAN.get(),EldritchGuardian.attributes().build());
    }
    private R2Entities() {}
}
