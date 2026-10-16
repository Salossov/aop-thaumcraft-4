package thaumcraft.verification;

import java.nio.file.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import thaumcraft.world.*;
import thaumcraft.world.dim.EldritchTeleporter;
import thaumcraft.registry.R5Content;

/** Cold requests advance through ordinary dedicated-server ticks, never a simulated chunk scheduler. */
final class PolishAsyncVerification {
    private final MinecraftServer server;
    private final ServerLevel level;
    private final List<String> checks=new ArrayList<>();
    private final Map<String,Object> measurements=new LinkedHashMap<>();
    private final int run=nextRun(),origin=6000+run*256;
    private final BlockPos portal=new BlockPos(origin*16+8,120,origin*16+8);
    private final ChunkPos population=new ChunkPos(origin+64,origin+64);
    private ChunkPreparation chunks;
    private int ticks,stage,startedTick;
    private long started=System.nanoTime(),previous=started;
    private double maxStep,maxGap;
    private boolean finished;
    private net.neoforged.neoforge.common.util.FakePlayer player;
    private long pipelineTick;
    private double maxPipeline;
    private ScatteredStructureSearch search;
    private boolean locating;
    private PolishAsyncVerification(MinecraftServer server){this.server=server;level=server.overworld();}
    private static int nextRun(){try{var file=Path.of(System.getProperty("thaumcraft.r3.root"),"analysis/port-polish-2026-10-15/async-run-counter.txt");int next=Files.exists(file)?Integer.parseInt(Files.readString(file).trim())+1:1;Files.writeString(file,Integer.toString(next));return next;}catch(Exception error){throw new RuntimeException(error);}}
    static void register(MinecraftServer server){var test=new PolishAsyncVerification(server);NeoForge.EVENT_BUS.addListener(test::tick);
        NeoForge.EVENT_BUS.addListener(net.neoforged.bus.api.EventPriority.HIGHEST,(net.neoforged.neoforge.event.tick.LevelTickEvent.Post event)->{if(event.getLevel()==test.level&&test.stage==4)test.pipelineTick=System.nanoTime();});
        NeoForge.EVENT_BUS.addListener(net.neoforged.bus.api.EventPriority.LOWEST,(net.neoforged.neoforge.event.tick.LevelTickEvent.Post event)->{if(event.getLevel()==test.level&&test.stage==4)test.maxPipeline=Math.max(test.maxPipeline,(System.nanoTime()-test.pipelineTick)/1e6);});
    }
    private void check(boolean ok,String text){if(!ok)throw new AssertionError(text);checks.add(text);}
    private int loaded(ChunkPos pos,int radius){int result=0;for(int x=-radius;x<=radius;x++)for(int z=-radius;z<=radius;z++)if(level.getChunkSource().getChunkNow(pos.x+x,pos.z+z)!=null)result++;return result;}
    private void tick(ServerTickEvent.Post event){
        if(finished||event.getServer()!=server)return;
        long now=System.nanoTime();maxGap=Math.max(maxGap,(now-previous)/1e6);previous=now;ticks++;
        try{
            if(ticks>2400)throw new AssertionError("Chunk preparation did not complete within 2400 ordinary ticks");
            if(stage==0){
                measurements.put("run",run);measurements.put("portalPosition",portal.toString());measurements.put("populationChunk",population.toString());measurements.put("portalLoadedBefore",loaded(new ChunkPos(portal),8));
                check(!EldritchTeleporter.prepare(level,portal.getCenter()),"cold portal returns pending without loading its 289 columns synchronously");
                started=System.nanoTime();startedTick=ticks;stage=1;return;
            }
            if(stage==1){
                long time=System.nanoTime();boolean ready=EldritchTeleporter.prepare(level,portal.getCenter());maxStep=Math.max(maxStep,(System.nanoTime()-time)/1e6);
                if(!ready)return;
                check(ticks-startedTick>=2,"cold portal preparation yields to ordinary server ticks instead of blocking inside the portal call");
                check(loaded(new ChunkPos(portal),8)==289,"all original 17 by 17 portal search columns are FULL before search");
                level.setBlock(portal,R5Content.ELDRITCH_PORTAL.get().defaultBlockState(),2);
                time=System.nanoTime();check(portal.equals(EldritchTeleporter.findPortal(level,portal.getCenter())),"prepared portal search selects the actual portal");measurements.put("preparedSearchMs",(System.nanoTime()-time)/1e6);
                EldritchTeleporter.releasePreparation(level,portal.getCenter());
                measurements.put("portalElapsedMs",(System.nanoTime()-started)/1e6);measurements.put("portalServerTicks",ticks-startedTick);measurements.put("portalMaxPollMs",maxStep);
                measurements.put("populationLoadedBefore",loaded(population,2));chunks=new ChunkPreparation(level,population.x-2,population.x+2,population.z-2,population.z+2);
                started=System.nanoTime();startedTick=ticks;maxStep=0;stage=2;return;
            }
            if(stage==3){
                if(!level.getChunkSource().isPositionTicking(population.toLong()))return;
                player=net.neoforged.neoforge.common.util.FakePlayerFactory.get(level,new com.mojang.authlib.GameProfile(UUID.randomUUID(),"PolishPopulationQA"));player.moveTo(level.getSharedSpawnPos().getCenter());level.addNewPlayer(player);
                check(level.players().contains(player),"production population runs with a native player in its ordinary scheduler");
                NaturalPopulationData.get(level).enqueue(request());
                check(!NaturalPopulation.advance(level,request(),2),"production entry defers a cold population instead of blocking for its locate search");stage=4;return;
            }
            if(stage==4){
                if(!level.getChunk(population.x,population.z).getData(NaturalPopulation.VERSION).equals(request().version()))return;
                measurements.put("productionPipelineMaxTickMs",maxPipeline);measurements.put("populationElapsedMs",(System.nanoTime()-started)/1e6);measurements.put("populationServerTicks",ticks-startedTick);measurements.put("populationMaxPollMs",maxStep);
                check(NaturalPopulation.advance(level,request(),2),"completed population is idempotent through the production entry point");
                player.discard();player=null;level.setChunkForced(population.x,population.z,false);chunks.close();
                search=new ScatteredStructureSearch(level,new BlockPos(population.getMiddleBlockX(),level.getSeaLevel(),population.getMiddleBlockZ()));stage=5;return;
            }
            if(stage==5){
                if(!search.advance(2))return;
                var expected=level.findNearestMapStructure(net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.STRUCTURE,net.minecraft.resources.ResourceLocation.parse("thaumcraft:scattered_temples")),new BlockPos(population.getMiddleBlockX(),level.getSeaLevel(),population.getMiddleBlockZ()),100,false);
                check(Objects.equals(expected,search.result()),"incremental locate returns the same structure as vanilla radius100 with identical tie order and offsets");finish(null);return;
            }
            long time=System.nanoTime();int before=chunks.submitted();boolean ready=chunks.advance(2);maxStep=Math.max(maxStep,(System.nanoTime()-time)/1e6);
            check(chunks.submitted()-before<=2,"population ticket budget is respected at tick "+ticks);
            if(!ready)return;
            check(loaded(population,2)==25&&ticks-startedTick>=13,"population prepares all original 25 columns over multiple server ticks");
            level.setChunkForced(population.x,population.z,true);stage=3;
        }catch(Throwable error){error.printStackTrace();finish(error);}
    }
    private NaturalPopulationData.Request request(){return new NaturalPopulationData.Request(population.toLong(),PopulationPolicy.populationSeed(level.getSeed(),population.x,population.z),true,"polish-async-v2");}
    private void finish(Throwable failure){
        finished=true;if(player!=null)player.discard();level.setChunkForced(population.x,population.z,false);if(search!=null)search.close();if(chunks!=null)chunks.close();EldritchTeleporter.releasePreparation(level,portal.getCenter());
        var result=new LinkedHashMap<String,Object>();result.put("passed",failure==null);result.put("time",java.time.Instant.now().toString());result.put("checks",checks);result.put("count",checks.size());result.put("measurements",measurements);result.put("maxTickIntervalMs",maxGap);if(failure!=null)result.put("error",failure.toString());
        try{var file=Path.of(System.getProperty("thaumcraft.r3.root"),"analysis/r3/evidence/production-server-polish-async.json");Files.createDirectories(file.getParent());Files.writeString(file,new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(result));server.saveEverything(false,true,true);}catch(Exception error){throw new RuntimeException(error);}finally{server.halt(false);}
    }
}
