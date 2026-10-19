package thaumcraft.verification;

import java.nio.file.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.*;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import thaumcraft.entities.*;
import thaumcraft.registry.R2Content;
import thaumcraft.world.WorldBiomes;

/** Ordinary dedicated-server spawning over five days: never calls NaturalSpawner itself. */
final class TaintNaturalReview {
 static final Map<String,Integer> natural=new TreeMap<>();static final List<Object> samples=new ArrayList<>(),tentacles=new ArrayList<>();static boolean registered,observing;static long start;
 static void prepare(int scene){var p=PhotoLiveScene.player;var l=p.serverLevel();
  if(scene==129){observing=true;natural.clear();samples.clear();tentacles.clear();if(!registered){registered=true;NeoForge.EVENT_BUS.addListener((EntityJoinLevelEvent event)->{if(!observing||event.getLevel().isClientSide||!(event.getEntity() instanceof Mob mob)||mob.getSpawnType()!=MobSpawnType.NATURAL)return;String id=BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType()).toString();natural.merge(id,1,Integer::sum);if(mob instanceof Taintacle)tentacles.add(Map.of("position",List.of(mob.getX(),mob.getY(),mob.getZ()),"gameTime",mob.level().getGameTime(),"dayTime",mob.level().getDayTime()));});}p.setGameMode(GameType.CREATIVE);l.setDayTime(18000);p.teleportTo(22923.5,67,17529.5);}
  if(scene>=130&&scene<=133){for(int i=0;i<4;i++){var at=new BlockPos(i*2-3,65,0);l.setBlockAndUpdate(at,(i<2?i==0?R2Content.TAINT_GROWTH:R2Content.TAINT_TENDRILS:R2Content.TAINT_SPORE_STALK).get().defaultBlockState().setValue(thaumcraft.blocks.TaintFibresBlock.OCCUPIED,i==3));}p.teleportTo(scene==131?6.5:scene==133?-6.5:.5,66,scene==132?-6.5:scene==130?6.5:.5);}
 }
 static void tick(int scene,int age)throws Exception{var p=PhotoLiveScene.player;var l=p.serverLevel();
  if(scene>=130&&scene<=133){for(int i=0;i<4;i++){var at=new BlockPos(i*2-3,65,0);if(!l.getBlockState(at).is(i<2?i==0?R2Content.TAINT_GROWTH:R2Content.TAINT_TENDRILS:R2Content.TAINT_SPORE_STALK))l.setBlockAndUpdate(at,(i<2?i==0?R2Content.TAINT_GROWTH:R2Content.TAINT_TENDRILS:R2Content.TAINT_SPORE_STALK).get().defaultBlockState());if(i>=2)l.setBlock(at,l.getBlockState(at).setValue(thaumcraft.blocks.TaintFibresBlock.OCCUPIED,i==3),2);}}
  if(scene!=129)return;
  if(age==100){PhotoLiveScene.check(l.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING),"ordinary natural mob spawning gamerule enabled");PhotoLiveScene.check(l.getBiome(p.blockPosition()).is(WorldBiomes.TAINT),"AFK player is in actual generated tainted biome");start=l.getGameTime();p.server.getCommands().performPrefixedCommand(p.createCommandSourceStack().withPermission(4),"tick sprint 120000t");}
  if(age>=100&&(age-100)%200==0){var state=l.getChunkSource().getLastSpawnState();if(state!=null)samples.add(Map.of("elapsed",l.getGameTime()-start,"monsterCapCount",state.getMobCategoryCounts().getInt(MobCategory.MONSTER),"spawnableChunks",state.getSpawnableChunkCount(),"naturalTaintacles",natural.getOrDefault("thaumcraft:taintacle",0)));}
  if(age==120100){observing=false;var proof=new LinkedHashMap<String,Object>();proof.put("finished",true);proof.put("ordinaryTicks",l.getGameTime()-start);proof.put("naturalJoinedEntities",natural);proof.put("taintacleSpawns",tentacles);proof.put("samples",samples);proof.put("passed",!tentacles.isEmpty());Files.writeString(PhotoLiveScene.root().resolve("natural-spawn-result.json"),new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(proof));PhotoLiveScene.check(!tentacles.isEmpty(),"five days of ordinary server ticking produce natural taintacles without direct spawner calls");}
 }
 private TaintNaturalReview(){}
}
