package thaumcraft.world.dim;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.*;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.*;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.*;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.structure.StructureSet;

/** Original Outer Lands base chunks contain only air; labyrinth features build the rooms. */
public final class OuterChunkGenerator extends ChunkGenerator {
    public static final MapCodec<OuterChunkGenerator> CODEC=RecordCodecBuilder.mapCodec(instance->instance.group(Biome.CODEC.fieldOf("biome").forGetter(generator->generator.biome)).apply(instance,OuterChunkGenerator::new));
    private final Holder<Biome> biome;
    public OuterChunkGenerator(Holder<Biome> biome){super(new FixedBiomeSource(biome));this.biome=biome;}
    @Override protected MapCodec<? extends ChunkGenerator> codec(){return CODEC;}
    @Override public ChunkGeneratorStructureState createState(HolderLookup<StructureSet> structures,RandomState random,long seed){return ChunkGeneratorStructureState.createForFlat(random,seed,biomeSource,Stream.empty());}
    @Override public CompletableFuture<ChunkAccess> fillFromNoise(Blender blender,RandomState random,StructureManager structures,ChunkAccess chunk){return CompletableFuture.completedFuture(chunk);}
    @Override public void buildSurface(WorldGenRegion region,StructureManager structures,RandomState random,ChunkAccess chunk){}
    @Override public void applyCarvers(WorldGenRegion region,long seed,RandomState random,BiomeManager biomes,StructureManager structures,ChunkAccess chunk,GenerationStep.Carving step){}
    @Override public void spawnOriginalMobs(WorldGenRegion region){}
    /** Original Forge1.7.10 GameRegistry chunk seed, including signed long overflow. */
    public static long roomSeed(long worldSeed,int chunkX,int chunkZ){var random=new Random(worldSeed);long x=random.nextLong()>>3,z=random.nextLong()>>3;return (x*chunkX+z*chunkZ)^worldSeed;}
    @Override public void applyBiomeDecoration(WorldGenLevel world,ChunkAccess chunk,StructureManager structures){
        // User-selected canonical plan preserves original neighbor checks and per-chunk seed streams.
        thaumcraft.world.dim.rooms.RoomPlans.apply(world,chunk);
    }
    @Override public int getMinY(){return 0;}
    @Override public int getGenDepth(){return 128;}
    @Override public int getSeaLevel(){return 50;}
    @Override public int getSpawnHeight(LevelHeightAccessor height){return 50;}
    @Override public int getBaseHeight(int x,int z,Heightmap.Types map,LevelHeightAccessor height,RandomState random){return 0;}
    @Override public NoiseColumn getBaseColumn(int x,int z,LevelHeightAccessor height,RandomState random){var states=new BlockState[128];Arrays.fill(states,Blocks.AIR.defaultBlockState());return new NoiseColumn(0,states);}
    @Override public void addDebugScreenInfo(List<String> lines,RandomState random,BlockPos pos){}
}
