package thaumcraft.entities;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.*;
public final class R3Entities {
 private static final DeferredRegister<EntityType<?>> TYPES=DeferredRegister.create(Registries.ENTITY_TYPE,"thaumcraft");
 public static final DeferredHolder<EntityType<?>,EntityType<Dart>> DART=TYPES.register("dart",()->EntityType.Builder.<Dart>of(Dart::new,MobCategory.MISC).sized(.5f,.5f).setShouldReceiveVelocityUpdates(false).clientTrackingRange(4).updateInterval(20).build("thaumcraft:dart"));
 public static final DeferredHolder<EntityType<?>,EntityType<GolemBobber>> GOLEM_BOBBER=TYPES.register("golem_bobber",()->EntityType.Builder.<GolemBobber>of(GolemBobber::new,MobCategory.MISC).sized(.25f,.25f).noSave().setShouldReceiveVelocityUpdates(false).clientTrackingRange(4).updateInterval(64).build("thaumcraft:golem_bobber"));
 public static final DeferredHolder<EntityType<?>,EntityType<BottleTaintProjectile>> BOTTLE_TAINT=TYPES.register("bottle_taint",()->EntityType.Builder.<BottleTaintProjectile>of(BottleTaintProjectile::new,MobCategory.MISC).sized(.25f,.25f).clientTrackingRange(8).updateInterval(1).build("thaumcraft:bottle_taint"));
 public static final DeferredHolder<EntityType<?>,EntityType<PrimalArrow>> PRIMAL_ARROW=TYPES.register("primal_arrow",()->EntityType.Builder.<PrimalArrow>of(PrimalArrow::new,MobCategory.MISC).sized(.5f,.5f).setShouldReceiveVelocityUpdates(false).clientTrackingRange(4).updateInterval(20).build("thaumcraft:primal_arrow"));
 public static final DeferredHolder<EntityType<?>,EntityType<MindSpider>> MIND_SPIDER=TYPES.register("mind_spider",()->EntityType.Builder.<MindSpider>of(MindSpider::new,MobCategory.MONSTER).sized(.3f,.3f).clientTrackingRange(8).build("thaumcraft:mind_spider"));
 public static final DeferredHolder<EntityType<?>,EntityType<TravelingTrunk>> TRUNK=TYPES.register("traveling_trunk",()->EntityType.Builder.<TravelingTrunk>of(TravelingTrunk::new,MobCategory.MISC).sized(.8f,.8f).fireImmune().clientTrackingRange(8).build("thaumcraft:traveling_trunk"));
 public static final DeferredHolder<EntityType<?>,EntityType<ThaumicGolem>> GOLEM=TYPES.register("golem",()->EntityType.Builder.<ThaumicGolem>of(ThaumicGolem::new,MobCategory.MISC).sized(.4f,.95f).clientTrackingRange(8).build("thaumcraft:golem"));
 public static final DeferredHolder<EntityType<?>,EntityType<AlumentumProjectile>> ALUMENTUM=TYPES.register("alumentum",()->EntityType.Builder.<AlumentumProjectile>of(AlumentumProjectile::new,MobCategory.MISC).sized(.25f,.25f).clientTrackingRange(8).updateInterval(1).build("thaumcraft:alumentum"));
 public static final DeferredHolder<EntityType<?>,EntityType<FocusProjectile>> PRIMAL_ORB=TYPES.register("primal_orb",()->EntityType.Builder.<FocusProjectile>of(FocusProjectile::new,MobCategory.MISC).sized(.25f,.25f).clientTrackingRange(8).updateInterval(1).build("thaumcraft:primal_orb"));
 public static final DeferredHolder<EntityType<?>,EntityType<FocusProjectile>> FOCUS_PROJECTILE=TYPES.register("focus_projectile",()->EntityType.Builder.<FocusProjectile>of(FocusProjectile::new,MobCategory.MISC).sized(.25f,.25f).clientTrackingRange(8).updateInterval(1).build("thaumcraft:focus_projectile"));
 public static final DeferredHolder<EntityType<?>,EntityType<FocusFireBat>> FIREBAT=TYPES.register("firebat",()->EntityType.Builder.<FocusFireBat>of(FocusFireBat::new,MobCategory.MONSTER).sized(.5f,.9f).fireImmune().clientTrackingRange(8).build("thaumcraft:firebat"));
 public static void register(IEventBus bus){TYPES.register(bus);bus.addListener(R3Entities::attributes);}
 private static void attributes(net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent event){event.put(FIREBAT.get(),FocusFireBat.attributes().build());event.put(GOLEM.get(),ThaumicGolem.attributes().build());event.put(TRUNK.get(),TravelingTrunk.attributes().build());event.put(MIND_SPIDER.get(),MindSpider.attributes().build());}
 private R3Entities(){}
}
