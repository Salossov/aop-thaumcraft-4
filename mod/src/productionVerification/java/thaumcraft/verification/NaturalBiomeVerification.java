package thaumcraft.verification;

import java.util.*;
import java.util.function.BiConsumer;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.biome.*;
import net.minecraft.server.level.ServerLevel;
import thaumcraft.world.*;

final class NaturalBiomeVerification {
    static void run(ServerLevel level,BiConsumer<Boolean,String> check){
        boolean tb=NaturalBiomeIntegration.terraBlenderPresent();
        check.accept(tb==Boolean.getBoolean("thaumcraft.qa.terrablender"),"actual optional library matches requested test mode");
        var generator=level.getChunkSource().getGenerator();var source=generator.getBiomeSource();
        check.accept(source instanceof MultiNoiseBiomeSource,"native overworld uses multi-noise climate selection");
        var possible=source.possibleBiomes();
        check.accept(possible.stream().anyMatch(b->b.is(WorldBiomes.MAGICAL)),"native biome source advertises magical forest");
        check.accept(possible.stream().anyMatch(b->b.is(WorldBiomes.TAINT)),"native biome source advertises tainted land");
        check.accept(possible.stream().noneMatch(b->b.is(WorldBiomes.EERIE)),"Eerie remains a node/obelisk-created biome");
        var preset=level.registryAccess().registryOrThrow(Registries.MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST).getOrThrow(net.minecraft.resources.ResourceKey.create(Registries.MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST,net.minecraft.resources.ResourceLocation.withDefaultNamespace("overworld")));
        long forest=preset.parameters().values().stream().filter(p->p.getSecond().is(WorldBiomes.MAGICAL)).count();
        check.accept(tb?forest==0:forest>0,"fallback preset is disabled exactly when TerraBlender is present");
        var mapped=new ArrayList<com.mojang.datafixers.util.Pair<Climate.ParameterPoint,net.minecraft.resources.ResourceKey<Biome>>>();
        for(var entry:NaturalBiomeClimate.vanilla())NaturalBiomeClimate.map(entry,70,entry.getSecond(),mapped::add);
        mapped.addAll(NaturalBiomeClimate.dedicated());
        check.accept(mapped.stream().filter(p->p.getSecond().equals(WorldBiomes.MAGICAL)||p.getSecond().equals(WorldBiomes.TAINT)).allMatch(p->p.getFirst().temperature().min()>=Climate.quantizeCoord(-.45f)&&p.getFirst().temperature().max()<=Climate.quantizeCoord(.55f)),"TC4 additions stay within cool/temperate/warm surface climates");
        check.accept(NaturalBiomeClimate.dedicated().size()==2&&NaturalBiomeClimate.dedicated().stream().map(com.mojang.datafixers.util.Pair::getSecond).distinct().count()==2,"exactly one dedicated point per TC4 biome (no per-cell duplicates)");
        var sampler=level.getChunkSource().randomState().sampler();var found=new HashMap<String,net.minecraft.core.BlockPos>();
        for(int x=-64;x<64&&found.size()<2;x++)for(int z=-64;z<64&&found.size()<2;z++){
            int qx=x*128,qz=z*128;var biome=source.getNoiseBiome(qx,16,qz,sampler);
            if(biome.is(WorldBiomes.MAGICAL)||biome.is(WorldBiomes.TAINT))found.putIfAbsent(biome.unwrapKey().orElseThrow().location().getPath(),new net.minecraft.core.BlockPos(qx*4,64,qz*4));
        }
        check.accept(found.containsKey("magical_forest"),"world-seeded native climate sampling reaches magical forest: "+found.get("magical_forest"));
        check.accept(found.containsKey("tainted_land"),"world-seeded native climate sampling reaches tainted land: "+found.get("tainted_land"));
        for(var entry:found.entrySet()){
            var pos=entry.getValue();var chunk=level.getChunk(pos.getX()>>4,pos.getZ()>>4);
            check.accept(chunk.getPersistedStatus()==net.minecraft.world.level.chunk.status.ChunkStatus.FULL,"native terrain/features finish a FULL "+entry.getKey()+" chunk");
            check.accept(level.getNoiseBiome(pos.getX()>>2,16,pos.getZ()>>2).unwrapKey().orElseThrow().location().getPath().equals(entry.getKey()),"generated chunk stores the sampled "+entry.getKey()+" biome");
            var queue=NaturalPopulationData.get(level);var request=queue.requests().stream().filter(r->r.chunk()==chunk.getPos().toLong()).findFirst();
            if(request.isPresent())NaturalPopulation.apply(level,request.get());
            check.accept(chunk.getData(NaturalPopulation.VERSION).equals(thaumcraft.config.ThaumcraftConfig.REGENERATION_VERSION.get()),"ordered TC4 decorators complete the natural "+entry.getKey()+" chunk");
        }
        var nether=level.getServer().getLevel(net.minecraft.world.level.Level.NETHER);
        check.accept(nether.getChunkSource().getGenerator().getBiomeSource().possibleBiomes().stream().noneMatch(b->b.is(WorldBiomes.MAGICAL)||b.is(WorldBiomes.TAINT)||b.is(WorldBiomes.EERIE)),"Nether climate map untouched");
        if(tb){
            var regions=terrablender.api.Regions.get(terrablender.api.RegionType.OVERWORLD);
            check.accept(regions.stream().filter(r->r.getName().equals(TerraBlenderBiomes.ID)).count()==1,"exactly one registered TC4 TerraBlender region");
        }
    }
}
