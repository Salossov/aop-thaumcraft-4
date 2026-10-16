package thaumcraft.verification;

import java.util.*;
import java.util.function.BiConsumer;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.level.LevelEvent;
import thaumcraft.config.ThaumcraftConfig;
import thaumcraft.entities.*;
import thaumcraft.items.WispEssenceItem;
import thaumcraft.knowledge.KnowledgeService;
import thaumcraft.network.ClientGuardianMist;
import thaumcraft.registry.R2Content;
import thaumcraft.world.WorldBiomes;

final class AmbientVerification {
    static void run(ServerLevel level,BiConsumer<Boolean,String> check) {
        var registry=level.registryAccess().registryOrThrow(Registries.BIOME);
        var eerie=registry.getHolderOrThrow(WorldBiomes.EERIE);var taint=registry.getHolderOrThrow(WorldBiomes.TAINT);var magical=registry.getHolderOrThrow(WorldBiomes.MAGICAL);
        check.accept(eerie.value().getSkyColor()==0x222299&&eerie.value().getWaterColor()==3035999,"Eerie biome has original TC4 colours");
        check.accept(taint.value().getSkyColor()==8144127&&taint.value().getWaterColor()==0xCC1188,"Tainted Land has original TC4 colours");
        check.accept(magical.value().getWaterColor()==30702,"PURE node cleanup biome exists");
        var entries=eerie.value().getMobSettings().getMobs(MobCategory.MONSTER).unwrap();
        check.accept(eerie.value().getMobSettings().getMobs(MobCategory.CREATURE).unwrap().isEmpty()&&eerie.value().getMobSettings().getMobs(MobCategory.AMBIENT).unwrap().stream().anyMatch(e->e.type==EntityType.BAT&&e.getWeight().asInt()==3),"extra Eerie bat uses AMBIENT by default");
        check.accept(entries.stream().anyMatch(e->e.type==R2Entities.WISP.get()&&e.getWeight().asInt()==3)&&entries.stream().anyMatch(e->e.type==R2Entities.ELDRITCH_GUARDIAN.get()&&e.getWeight().asInt()==1),"Eerie Wisp and guardian weights match TC4 3:1");
        check.accept(taint.value().getMobSettings().getMobs(MobCategory.MONSTER).unwrap().size()==1&&taint.value().getMobSettings().getMobs(MobCategory.MONSTER).unwrap().getFirst().type==R2Entities.TAINTACLE.get(),"Tainted Land monster list contains only Taintacle");
        var pos=new BlockPos(80,90,80);level.getChunkAt(pos);for(var p:BlockPos.betweenClosed(pos.offset(-4,-1,-4),pos.offset(4,4,4)))level.setBlockAndUpdate(p,p.getY()==89||p.getY()==94?Blocks.STONE.defaultBlockState():Blocks.AIR.defaultBlockState());
        for(var old:level.getEntitiesOfClass(LivingEntity.class,new AABB(pos).inflate(40)))if(!(old instanceof net.minecraft.world.entity.player.Player))old.discard();
        level.setDayTime(18000);level.updateSkyBrightness();
        ThaumcraftConfig.BIOME_CHANGE_MODE.set(ThaumcraftConfig.BiomeChangeMode.TC4_1X1);ThaumcraftConfig.BIOME_OVERLAY_GLOBAL.set(false);ThaumcraftConfig.NODE_BIOME_CHANGES.set(true);ThaumcraftConfig.BIOME_CREATURE_SPAWNS.set(true);
        WorldBiomes.change(level,pos.getX(),pos.getZ(),WorldBiomes.EERIE);
        var event=new LevelEvent.PotentialSpawns(level,MobCategory.MONSTER,pos,net.minecraft.util.random.WeightedRandomList.create());BiomeSpawns.potential(event);
        check.accept(event.getSpawnerDataList().stream().anyMatch(e->e.type==R2Entities.WISP.get())&&!level.getBiome(pos).is(WorldBiomes.EERIE),"TC4-only 1x1 overlay supplies original spawns while general biome queries stay native");
        ThaumcraftConfig.BIOME_CREATURE_SPAWNS.set(false);BiomeSpawns.potential(event);
        check.accept(event.getSpawnerDataList().stream().noneMatch(e->e.type==R2Entities.WISP.get()||e.type==R2Entities.ELDRITCH_GUARDIAN.get()),"natural-creature switch removes TC4 candidates");
        ThaumcraftConfig.BIOME_CREATURE_SPAWNS.set(true);
        var wisp=R2Entities.WISP.get().create(level);wisp.moveTo(pos.getX()+0.5,pos.getY(),pos.getZ()+0.5,0,0);level.addFreshEntity(wisp);
        check.accept(wisp.getMaxHealth()==22&&wisp.getAttributeValue(Attributes.ATTACK_DAMAGE)==3,"Wisp original health 22, damage 3");
        var eerieAspects=Set.of("tenebrae","exanimis","perditio","alienis","venenum","mortuus");
        for(int i=0;i<48;i++) {wisp.setAspect(null);wisp.chooseAspect();if(!eerieAspects.contains(wisp.aspect().getTag()))throw new AssertionError("Eerie Wisp aspect");}
        check.accept(true,"Eerie Wisp chooses only the original six aspects");
        var saved=new CompoundTag();wisp.saveWithoutId(saved);var restored=R2Entities.WISP.get().create(level);restored.load(saved);
        check.accept(restored.aspect()==wisp.aspect(),"Wisp aspect survives NBT round trip");
        var chosen=wisp.aspect();wisp.hurt(level.damageSources().generic(),1000);
        check.accept(level.getEntitiesOfClass(ItemEntity.class,wisp.getBoundingBox().inflate(3),e->e.getItem().is(R2Content.WISP_ESSENCE.get())&&WispEssenceItem.aspects(e.getItem()).getAmount(chosen)==2).size()==1,"Wisp drops exactly two units of its own aspect");wisp.discard();
        WorldBiomes.change(level,pos.getX(),pos.getZ(),WorldBiomes.TAINT);level.setBlockAndUpdate(pos,R2Content.TAINT_FIBRES.get().defaultBlockState());
        var random=new LegacyRandomSource(1){@Override public int nextInt(int bound){return bound-1;}};
        check.accept(level.getBlockState(pos).is(R2Content.TAINT_FIBRES.get()),"spawn fixture retains taint fibres");
        check.accept(WorldBiomes.is(level,pos,WorldBiomes.TAINT),"spawn fixture sees Tainted Land through TC4-only overlay");
        check.accept(BiomeSpawns.dark(level,pos,random),"spawn fixture is dark: sky="+level.getBrightness(net.minecraft.world.level.LightLayer.SKY,pos)+", local="+level.getMaxLocalRawBrightness(pos)+", darken="+level.getSkyDarken());
        check.accept(level.getEntitiesOfClass(Taintacle.class,new AABB(pos).inflate(24,8,24)).isEmpty(),"spawn fixture has no existing tentacles");
        check.accept(BiomeSpawns.tentacle(R2Entities.TAINTACLE.get(),level,MobSpawnType.NATURAL,pos,random),"natural Taintacle accepts taint fibres in Tainted Land");
        var tentacle=R2Entities.TAINTACLE.get().create(level);tentacle.moveTo(pos.getX()+0.5,pos.getY(),pos.getZ()+0.5,0,0);level.addFreshEntity(tentacle);
        check.accept(tentacle.getMaxHealth()==50&&tentacle.getAttributeValue(Attributes.ATTACK_DAMAGE)==7,"Taintacle original health 50, damage 7");
        check.accept(!BiomeSpawns.tentacle(R2Entities.TAINTACLE.get(),level,MobSpawnType.NATURAL,pos,random),"Taintacle population exclusion within 24x8x24");
        var before=tentacle.position();tentacle.move(MoverType.SELF,new Vec3(1,1,1));check.accept(tentacle.getX()==before.x&&tentacle.getY()==before.y&&tentacle.getZ()==before.z,"Taintacle cannot walk or rise");
        var victim=EntityType.COW.create(level);victim.moveTo(tentacle.position());level.addFreshEntity(victim);float health=victim.getHealth();tentacle.doHurtTarget(victim);
        check.accept(Math.abs(victim.getHealth()-(health-7))<0.001,"tentacle damage uses its original damage source and strength");
        victim.moveTo(pos.getX()+0.5,pos.getY(),pos.getZ()+2.5,0,0);WorldBiomes.change(level,pos.getX(),pos.getZ()+2,WorldBiomes.TAINT);level.setBlockAndUpdate(victim.blockPosition(),R2Content.TAINT_FIBRES.get().defaultBlockState());
        check.accept(tentacle.spawnTentacle(victim),"adult Taintacle produces a temporary tiny tentacle on tainted ground");
        var tiny=R2Entities.TAINTACLE_SMALL.get().create(level);tiny.moveTo(tentacle.position());tiny.setNoAi(true);level.addFreshEntity(tiny);
        check.accept(tiny.getMaxHealth()==8&&tiny.getAttributeValue(Attributes.ATTACK_DAMAGE)==2,"tiny tentacle health 8 and damage 2");
        for(int tick=0;tick<201&&tiny.isAlive();tick++)tiny.tick();check.accept(!tiny.isAlive(),"tiny tentacle expires after its original 200-tick lifetime");
        var guardian=R2Entities.ELDRITCH_GUARDIAN.get().create(level);guardian.moveTo(pos.getX()+3.5,pos.getY(),pos.getZ()+0.5,0,0);level.addFreshEntity(guardian);
        check.accept(guardian.getMaxHealth()==50&&guardian.getAttributeValue(Attributes.ATTACK_DAMAGE)==7&&guardian.getArmorValue()==4,"guardian original health 50, damage 7, armour 4");
        guardian.getAttribute(Attributes.ARMOR).setBaseValue(0);guardian.hurt(level.damageSources().magic(),8);
        check.accept(guardian.getHealth()==46,"guardian halves magic damage");
        guardian.shootOrb(victim);check.accept(level.getEntitiesOfClass(EldritchOrb.class,guardian.getBoundingBox().inflate(3)).stream().anyMatch(e->e.getOwner()==guardian),"guardian creates its attributed ranged orb");
        var player=FakePlayerFactory.get(level,new GameProfile(UUID.randomUUID(),"TC4_Ambient_Test"));KnowledgeService.initialize(player);int oldWarp=KnowledgeService.get(player).temporaryWarp();guardian.sonicAttack(player);int gained=KnowledgeService.get(player).temporaryWarp()-oldWarp;
        check.accept(player.hasEffect(MobEffects.WITHER)&&player.getEffect(MobEffects.WITHER).getDuration()==400&&gained>=1&&gained<=3,"guardian sonic attack applies 400 ticks of wither and 1..3 temporary warp");
        var orb=new EldritchOrb(level,guardian);orb.moveTo(victim.position());victim.setHealth(victim.getMaxHealth());victim.invulnerableTime=0;health=victim.getHealth();orb.impact(guardian);
        check.accept(Math.abs(victim.getHealth()-(health-7*0.666f))<0.001&&victim.hasEffect(MobEffects.WEAKNESS)&&victim.getEffect(MobEffects.WEAKNESS).getDuration()==160,"eldritch orb damages nearby targets at 0.666 attack strength and weakens for 160 ticks: health="+health+" -> "+victim.getHealth()+", weakness="+victim.getEffect(MobEffects.WEAKNESS));
        check.accept(guardian.getHealth()==46,"orb excludes its owner from area damage");orb.tickCount++;orb.tick();check.accept(orb.isRemoved(),"impacted orb is removed after the final entity effect");
        check.accept(!BiomeSpawns.guardian(R2Entities.ELDRITCH_GUARDIAN.get(),level,MobSpawnType.NATURAL,pos,random),"guardian population exclusion within 32x16x32");
        ClientGuardianMist.clear();ClientGuardianMist.accept();for(int i=0;i<100;i++)ClientGuardianMist.tick();check.accept(Math.abs(ClientGuardianMist.density()-0.1f)<0.00001,"guardian mist increases smoothly over 100 ticks");
        ClientGuardianMist.accept();check.accept(ClientGuardianMist.remaining()==200,"nearby guardian refreshes mist to 200 ticks without stacking");for(int i=0;i<200;i++)ClientGuardianMist.tick();ClientGuardianMist.tick();check.accept(ClientGuardianMist.density()==0,"guardian mist expires without permanent client state");
        for(var entity:level.getEntitiesOfClass(LivingEntity.class,new AABB(pos).inflate(12)))if(entity instanceof Taintacle||entity instanceof EldritchGuardian||entity==victim)entity.discard();
        var bad=new ArrayList<String>();for(var key:thaumcraft.data.ThaumcraftData.get(level.getServer()).objects().keySet()) {
            var id=net.minecraft.resources.ResourceLocation.parse(key);if(!net.minecraft.core.registries.BuiltInRegistries.ITEM.containsKey(id)&&!net.minecraft.core.registries.BuiltInRegistries.BLOCK.containsKey(id)&&!key.equals("minecraft:lit_redstone_ore"))bad.add(key);
        }
        check.accept(bad.isEmpty(),"default object registry mappings have valid IDs: "+bad);
    }
    private AmbientVerification() {}
}
