package thaumcraft.verification;

import java.util.*;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.server.level.ServerLevel;
import thaumcraft.config.ThaumcraftConfig;
import thaumcraft.world.*;

final class NaturalPopulationVerification {
    static void run(ServerLevel level,BiConsumer<Boolean,String> check){
        var data=new NaturalPopulationData();long seed=PopulationPolicy.populationSeed(level.getSeed(),430,220);
        var request=new NaturalPopulationData.Request(ChunkPos.asLong(430,220),seed,true,"qa-1");
        check.accept(data.enqueue(request)&&!data.enqueue(request),"duplicate chunk load cannot duplicate pending population");
        var restored=NaturalPopulationData.load(data.save(new net.minecraft.nbt.CompoundTag(),level.registryAccess()),level.registryAccess());
        check.accept(restored.requests().equals(List.of(request)),"queue round trip retains chunk seed, fresh flag, version and order");
        restored.completed(request.chunk());check.accept(restored.requests().isEmpty(),"completed request removed from persistent queue");
        var actual=NaturalPopulationData.get(level);var chunk=level.getChunk(430,220);var old=chunk.getData(NaturalPopulation.VERSION);var version=ThaumcraftConfig.REGENERATION_VERSION.get();
        try{
            chunk.setData(NaturalPopulation.VERSION,"");NaturalPopulation.enqueue(level,chunk,true);
            var queued=actual.requests().stream().filter(r->r.chunk()==request.chunk()).findFirst().orElseThrow();
            check.accept(queued.fresh()&&queued.seed()==seed&&queued.version().equals(version),"fresh FULL native chunk receives source population seed");
            NaturalPopulation.apply(level,queued);
            check.accept(chunk.getData(NaturalPopulation.VERSION).equals(version)&&actual.requests().stream().noneMatch(r->r.chunk()==request.chunk()),"native population finishes with saved attachment and queue acknowledgement");
            var marker=new BlockPos(430*16+8,250,220*16+8);level.setBlock(marker,net.minecraft.world.level.block.Blocks.DIAMOND_BLOCK.defaultBlockState(),2);
            NaturalPopulation.enqueue(level,chunk,true);NaturalPopulation.apply(level,queued);
            check.accept(level.getBlockState(marker).is(net.minecraft.world.level.block.Blocks.DIAMOND_BLOCK)&&actual.requests().stream().noneMatch(r->r.chunk()==request.chunk()),"completed native chunk cannot be populated twice");level.removeBlock(marker,false);
            chunk.setData(NaturalPopulation.VERSION,"");boolean regen=ThaumcraftConfig.REGENERATE_AURA.get();ThaumcraftConfig.REGENERATE_AURA.set(true);
            try{NaturalPopulation.enqueue(level,chunk,false);check.accept(actual.requests().stream().anyMatch(r->r.chunk()==request.chunk()&&!r.fresh()),"approved regeneration schedules existing chunk without fresh-only structures");}finally{ThaumcraftConfig.REGENERATE_AURA.set(regen);actual.completed(request.chunk());}
            var magic=level.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.BIOME).getHolderOrThrow(WorldBiomes.MAGICAL).value();
            check.accept(!magic.getGenerationSettings().features().isEmpty()&&magic.getGenerationSettings().getCarvers(net.minecraft.world.level.levelgen.GenerationStep.Carving.AIR).iterator().hasNext(),"magical forest has approved native vanilla features and carvers");
            var checkpoint=level.getChunk(431,220);checkpoint.setData(NaturalPopulation.VERSION,"qa-native-population");checkpoint.setUnsaved(true);
            actual.completed(ChunkPos.asLong(432,220));
            actual.enqueue(new NaturalPopulationData.Request(ChunkPos.asLong(432,220),9917,false,"qa-reload"));
            check.accept(true,"actual terrain generator "+level.getChunkSource().getGenerator().getClass().getSimpleName());
        }finally{chunk.setData(NaturalPopulation.VERSION,old);}
    }
    static void reload(ServerLevel level,BiConsumer<Boolean,String> check){
        check.accept(level.getChunk(431,220).getData(NaturalPopulation.VERSION).equals("qa-native-population"),"population attachment survived actual server shutdown and restart");
        var data=NaturalPopulationData.get(level);long key=ChunkPos.asLong(432,220);
        var request=data.requests().stream().filter(r->r.chunk()==key).findFirst().orElseThrow();
        check.accept(request.seed()==9917&&!request.fresh()&&request.version().equals("qa-reload"),"pending request survived actual dimension SavedData reload");
        data.completed(key);level.getChunk(431,220).setData(NaturalPopulation.VERSION,ThaumcraftConfig.REGENERATION_VERSION.get());
    }
    private NaturalPopulationVerification(){}
}
