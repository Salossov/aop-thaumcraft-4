package thaumcraft.entities;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import thaumcraft.config.ThaumcraftConfig;
import thaumcraft.registry.R2Content;
import thaumcraft.world.WorldBiomes;

public final class BiomeSpawns {
    public static void register(IEventBus bus) {bus.addListener(BiomeSpawns::placements);NeoForge.EVENT_BUS.addListener(BiomeSpawns::potential);}
    private static void placements(RegisterSpawnPlacementsEvent event) {
        var operation=RegisterSpawnPlacementsEvent.Operation.REPLACE;var height=Heightmap.Types.MOTION_BLOCKING_NO_LEAVES;
        event.register(R2Entities.WISP.get(),SpawnPlacementTypes.NO_RESTRICTIONS,height,BiomeSpawns::wisp,operation);
        event.register(R2Entities.TAINTACLE.get(),SpawnPlacementTypes.NO_RESTRICTIONS,height,BiomeSpawns::tentacle,operation);
        event.register(R2Entities.TAINTACLE_SMALL.get(),SpawnPlacementTypes.NO_RESTRICTIONS,height,(type,level,reason,pos,random)->false,operation);
        event.register(R2Entities.ELDRITCH_GUARDIAN.get(),SpawnPlacementTypes.ON_GROUND,height,BiomeSpawns::guardian,operation);
        event.register(R2Entities.BRAINY.get(),SpawnPlacementTypes.ON_GROUND,height,(type,level,reason,pos,random)->dark(level,pos,random),operation);
        event.register(R2Entities.GIANT_BRAINY.get(),SpawnPlacementTypes.ON_GROUND,height,(type,level,reason,pos,random)->dark(level,pos,random),operation);
        event.register(R5Entities.INHABITED_ZOMBIE.get(),SpawnPlacementTypes.ON_GROUND,height,(type,level,reason,pos,random)->ThaumcraftConfig.BIOME_CREATURE_SPAWNS.get()&&dark(level,pos,random),operation);
        event.register(R5Entities.PECH.get(),SpawnPlacementTypes.ON_GROUND,height,BiomeSpawns::pech,operation);
    }
    /** TC4 used a randomized block-light threshold 0..7, rather than modern light=0. */
    public static boolean dark(ServerLevelAccessor level,BlockPos pos,RandomSource random) {
        return level.getDifficulty()!=Difficulty.PEACEFUL&&level.getBrightness(LightLayer.SKY,pos)<=random.nextInt(32)
            &&level.getMaxLocalRawBrightness(pos,level.getLevel().isThundering()?10:level.getLevel().getSkyDarken())<=random.nextInt(8);
    }
    public static boolean wisp(EntityType<Wisp> type,ServerLevelAccessor level,MobSpawnType reason,BlockPos pos,RandomSource random) {
        if(!ThaumcraftConfig.BIOME_CREATURE_SPAWNS.get()||!dark(level,pos,random))return false;
        var box=new net.minecraft.world.phys.AABB(pos).inflate(16);return level.getLevel().getEntitiesOfClass(Wisp.class,box).size()<8;
    }
    public static boolean tentacle(EntityType<Taintacle> type,ServerLevelAccessor level,MobSpawnType reason,BlockPos pos,RandomSource random) {
        if(!ThaumcraftConfig.BIOME_CREATURE_SPAWNS.get()||!dark(level,pos,random)||!WorldBiomes.is(level.getLevel(),pos,WorldBiomes.TAINT))return false;
        var state=level.getBlockState(pos);boolean taint=state.is(R2Content.TAINT_FIBRES.get())||state.is(R2Content.TAINTED_SOIL.get());
        return taint&&level.getLevel().getEntitiesOfClass(Taintacle.class,new net.minecraft.world.phys.AABB(pos).inflate(24,8,24)).isEmpty();
    }
    public static boolean guardian(EntityType<EldritchGuardian> type,ServerLevelAccessor level,MobSpawnType reason,BlockPos pos,RandomSource random) {
        return ThaumcraftConfig.BIOME_CREATURE_SPAWNS.get()&&dark(level,pos,random)&&level.getLevel().getEntitiesOfClass(EldritchGuardian.class,new net.minecraft.world.phys.AABB(pos).inflate(32,16,32)).isEmpty();
    }
    private static final net.minecraft.tags.TagKey<net.minecraft.world.level.biome.Biome> MAGICAL_BIOMES=net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.BIOME,net.minecraft.resources.ResourceLocation.parse("thaumcraft:magical_biomes"));
    /** Source Pech excludes taint and admits non-overworld spawns only in forest/eerie biomes. */
    public static boolean pechBiome(net.minecraft.world.level.Level level,BlockPos pos) {
        var biome=WorldBiomes.effective(level,pos);
        return biome.is(MAGICAL_BIOMES)&&!biome.is(WorldBiomes.TAINT)
            &&(level.dimension().equals(net.minecraft.world.level.Level.OVERWORLD)||biome.is(WorldBiomes.MAGICAL)||biome.is(WorldBiomes.EERIE));
    }
    public static boolean pech(EntityType<Pech> type,ServerLevelAccessor level,MobSpawnType reason,BlockPos pos,RandomSource random) {
        if(!ThaumcraftConfig.BIOME_CREATURE_SPAWNS.get()||!pechBiome(level.getLevel(),pos))return false;
        var box=new net.minecraft.world.phys.AABB(pos.getX()+.2,pos.getY(),pos.getZ()+.2,pos.getX()+.8,pos.getY()+1.8,pos.getZ()+.8).inflate(16);
        return level.getLevel().getEntitiesOfClass(Pech.class,box).size()<4&&dark(level,pos,random)
            &&Mob.checkMobSpawnRules(type,level,reason,pos,random);
    }
    /** Native spawner must also see TC4-only exact columns when general integration is off. */
    public static void potential(LevelEvent.PotentialSpawns event) {
        if(!(event.getLevel() instanceof net.minecraft.server.level.ServerLevel level))return;
        var biome=WorldBiomes.effective(level,event.getPos());boolean tc4=biome.is(WorldBiomes.EERIE)||biome.is(WorldBiomes.TAINT)||biome.is(WorldBiomes.MAGICAL);
        if(tc4) {
            for(var entry:List.copyOf(event.getSpawnerDataList()))event.removeSpawnerData(entry);
            for(var entry:biome.value().getMobSettings().getMobs(event.getMobCategory()).unwrap())event.addSpawnerData(entry);
            if(biome.is(WorldBiomes.EERIE)&&ThaumcraftConfig.LEGACY_EERIE_BAT_CATEGORY.get()) {
                if(event.getMobCategory()==MobCategory.AMBIENT) {for(var entry:List.copyOf(event.getSpawnerDataList()))if(entry.type==EntityType.BAT&&entry.getWeight().asInt()==3)event.removeSpawnerData(entry);}
                else if(event.getMobCategory()==MobCategory.CREATURE)event.addSpawnerData(new net.minecraft.world.level.biome.MobSpawnSettings.SpawnerData(EntityType.BAT,3,1,1));
            }
        }
        if(event.getMobCategory()==MobCategory.MONSTER&&biome.is(MAGICAL_BIOMES)&&!event.getSpawnerDataList().isEmpty()
            &&event.getSpawnerDataList().stream().noneMatch(entry->entry.type==R5Entities.PECH.get()))
            event.addSpawnerData(new net.minecraft.world.level.biome.MobSpawnSettings.SpawnerData(R5Entities.PECH.get(),10,1,1));
        if(!ThaumcraftConfig.BIOME_CREATURE_SPAWNS.get())for(var entry:List.copyOf(event.getSpawnerDataList()))if(entry.type==R2Entities.WISP.get()||entry.type==R2Entities.TAINTACLE.get()||entry.type==R2Entities.ELDRITCH_GUARDIAN.get()||entry.type==R2Entities.BRAINY.get()||entry.type==R2Entities.GIANT_BRAINY.get()||entry.type==R5Entities.INHABITED_ZOMBIE.get()||entry.type==R5Entities.PECH.get())event.removeSpawnerData(entry);
    }
    private BiomeSpawns() {}
}
