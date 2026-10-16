package thaumcraft.verification;

import java.util.*;
import java.util.function.BiConsumer;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.*;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.chunk.*;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.blending.Blender;
import thaumcraft.world.dim.OuterChunkGenerator;

final class OuterGeneratorVerification {
    static void run(ServerLevel level,BiConsumer<Boolean,String> check){
        var biome=level.registryAccess().registryOrThrow(Registries.BIOME).getHolderOrThrow(Biomes.THE_VOID);var generator=new OuterChunkGenerator(biome);var random=level.getChunkSource().randomState();var ops=RegistryOps.create(JsonOps.INSTANCE,level.registryAccess());
        var encoded=ChunkGenerator.CODEC.encodeStart(ops,generator).getOrThrow();var decoded=ChunkGenerator.CODEC.parse(ops,encoded).getOrThrow();check.accept(decoded instanceof OuterChunkGenerator&&encoded.getAsJsonObject().get("type").getAsString().equals("thaumcraft:outer_lands"),"own Outer Lands generator survives native registry-dispatched level codec");
        check.accept(decoded.getBiomeSource().possibleBiomes().equals(Set.of(biome)),"original fixed biome survives native generator serialization");check.accept(generator.getMinY()==0&&generator.getGenDepth()==128&&generator.getSpawnHeight(LevelHeightAccessor.create(0,128))==50,"own generator retains original chunk range 0..127 and average ground level 50");
        var height=LevelHeightAccessor.create(0,128);
        for(var pos:List.of(new ChunkPos(0,0),new ChunkPos(-2,-2),new ChunkPos(22,-44))){
            var chunk=new ProtoChunk(pos,UpgradeData.EMPTY,height,level.registryAccess().registryOrThrow(Registries.BIOME),null);
            check.accept(generator.fillFromNoise(Blender.empty(),random,level.structureManager(),chunk).join()==chunk,"original empty base generation uses native chunk instance at "+pos);
            check.accept(generator.createBiomes(random,Blender.empty(),level.structureManager(),chunk).join()==chunk,"own generator fills native quart biomes at "+pos);
            chunk.setPersistedStatus(net.minecraft.world.level.chunk.status.ChunkStatus.BIOMES);
            boolean empty=true,uniform=true;for(int x=0;x<16;x++)for(int z=0;z<16;z++)for(int y=0;y<128;y++)empty&=chunk.getBlockState(new BlockPos(pos.getMinBlockX()+x,y,pos.getMinBlockZ()+z)).isAir();for(int x=0;x<4;x++)for(int z=0;z<4;z++)for(int y=0;y<32;y++)uniform&=chunk.getNoiseBiome(pos.x*4+x,y,pos.z*4+z).is(Biomes.THE_VOID);
            check.accept(empty&&uniform,"all 32768 original base cells are air and every native biome quart is fixed at "+pos);
            var column=generator.getBaseColumn(pos.getMinBlockX(),pos.getMinBlockZ(),height,random);boolean air=true;for(int y=0;y<128;y++)air&=column.getBlock(y).isAir();check.accept(air&&generator.getBaseHeight(pos.getMinBlockX(),pos.getMinBlockZ(),Heightmap.Types.WORLD_SURFACE_WG,height,random)==0,"native height queries expose original void without a hidden ground layer at "+pos);
        }
    }
}
