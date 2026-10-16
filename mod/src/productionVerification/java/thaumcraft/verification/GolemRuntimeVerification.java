package thaumcraft.verification;

import java.nio.file.*;
import java.util.*;
import com.google.gson.GsonBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import thaumcraft.entities.ThaumicGolem;

/** Run the exported manual scene through the real server entity/GoalSelector scheduler. */
final class GolemRuntimeVerification {
    private final MinecraftServer server;
    private final ServerLevel level;
    private final Set<ChunkPos> held=new LinkedHashSet<>();
    private final List<String> checks=new ArrayList<>();
    private final Map<Integer,ThaumicGolem> golems=new TreeMap<>();
    private final Map<Integer,Integer> initialTicks=new HashMap<>();
    private int wait,ticks;private boolean started,finished;
    private int wetBobberTicks;private final List<Map<String,Object>> workTrace=new ArrayList<>();
    private int tilledMask;
    private GolemRuntimeVerification(MinecraftServer server){this.server=server;level=server.overworld();}
    static void register(MinecraftServer server){var test=new GolemRuntimeVerification(server);test.holdChunks();NeoForge.EVENT_BUS.addListener(test::tick);}
    private static BlockPos source(int core){return new BlockPos(core%4*28,4,8+core/4*24);}
    private void holdChunks(){for(int core=0;core<12;core++){var c=new ChunkPos(source(core));for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++){var p=new ChunkPos(c.x+x,c.z+z);if(held.add(p)){level.setChunkForced(p.x,p.z,true);level.getChunk(p.x,p.z);}}}}
    private void check(boolean ok,String description){if(!ok)throw new AssertionError(description);checks.add(description);}
    private int emeralds(BlockPos pos){var chest=(ChestBlockEntity)level.getBlockEntity(pos);int n=0;for(int i=0;i<chest.getContainerSize();i++)if(chest.getItem(i).is(Items.EMERALD))n+=chest.getItem(i).getCount();return n;}
    private void tick(ServerTickEvent.Post event){
        if(finished||event.getServer()!=server)return;
        try{
            if(!started){
                for(var entity:level.getAllEntities())if(entity instanceof ThaumicGolem g&&g.core()>=0)golems.put(g.core(),g);
                if(golems.size()!=12){if(++wait>200)throw new AssertionError("all twelve saved golems must load");return;}
                for(var entry:golems.entrySet()){
                    int core=entry.getKey();var g=entry.getValue();check(g.inactive(),"core "+core+" reloads on its powered fetter");
                    var lever=source(core).east().south();level.setBlockAndUpdate(lever,level.getBlockState(lever).setValue(LeverBlock.POWERED,false));
                    check(!g.inactive(),"core "+core+" starts when its real lever releases the fetter");initialTicks.put(core,g.tickCount);
                }
                started=true;return;
            }
            ticks++;
            for(int i=0;i<3;i++)if(level.getBlockState(source(8).east(3+i)).is(net.minecraft.world.level.block.Blocks.FARMLAND))tilledMask|=1<<i;
            for(var entity:level.getAllEntities())if(entity instanceof thaumcraft.entities.GolemBobber bobber&&bobber.isInWater())wetBobberTicks++;
            if(ticks%100==0){var trace=new LinkedHashMap<String,Object>();trace.put("tick",ticks);trace.put("logs",logs());trace.put("water",((thaumcraft.production.CrucibleBlockEntity)level.getBlockEntity(source(5))).water());var field=net.minecraft.world.entity.Mob.class.getDeclaredField("goalSelector");field.setAccessible(true);for(int core:new int[]{5,7,8,11}){var g=golems.get(core);var selector=(net.minecraft.world.entity.ai.goal.GoalSelector)field.get(g);for(var wrapped:selector.getAvailableGoals())if(wrapped.getGoal() instanceof thaumcraft.entities.GolemWorkGoal job){var fields=new LinkedHashMap<String,Object>();fields.put("running",wrapped.isRunning());for(String name:List.of("timeout","delay","target","job","fishing")){var f=job.getClass().getDeclaredField(name);f.setAccessible(true);var value=f.get(job);fields.put(name,value==null?"null":value.toString());}trace.put("core"+core,fields);}}workTrace.add(trace);}
            if(ticks<1600)return;
            // A chunk boundary crossing may defer entity ticks while its sections finish loading.
            for(var entry:golems.entrySet())check(entry.getValue().isAlive()&&entry.getValue().tickCount-initialTicks.get(entry.getKey())>=1590,"core "+entry.getKey()+" survives the 1600-tick server run with at least 1590 entity ticks");
            var fill=golems.get(0);int home=emeralds(source(0)),supplier=emeralds(source(0).east(4));
            check(home>0&&home<=32,"fill executes repeated pickup/deposit actions with the real scheduler");
            check(home+supplier+fill.carried().getCount()==64,"fill conserves all 64 emeralds without repeating consumed actions");
            var empty=golems.get(1);int received=emeralds(source(1).east(4));
            check(received>0,"empty repeats its real scheduled actions");
            check(emeralds(source(1))+received+empty.carried().getCount()==64,"empty preserves inventory counts across repeated scheduling");
            var gather=golems.get(2);int loose=0;for(var entity:level.getAllEntities())if(entity instanceof ItemEntity item&&item.getItem().is(Items.EMERALD)&&item.distanceToSqr(source(2).getCenter())<100)loose+=item.getItem().getCount();
            check(emeralds(source(2))>0,"gather deposits items through real navigation and scheduled ticks");
            check(emeralds(source(2))+gather.carried().getCount()+loose==48,"gather conserves loose, carried and deposited emeralds");
            check(emeralds(source(10).east(4))==0&&emeralds(source(10).west(4))>1,"sorting uses the matching chest and leaves the empty receiver alone");
            check(logs()==0,"lumber completes the two-column tree through normal navigation");
            check(((thaumcraft.production.CrucibleBlockEntity)level.getBlockEntity(source(5))).water()==1000,"liquid golem actually fills the crucible during the normal run");
            check(wetBobberTicks>0,"fishing bobber reaches water instead of hitting the golem pedestal");
            check(tilledMask==7,"use core reaches and tills all three marked dirt blocks");
            finish(null);
        }catch(Throwable failure){failure.printStackTrace();finish(failure);}
    }
    private int logs(){int n=0;for(var pos:BlockPos.betweenClosed(source(7).offset(2,0,-1),source(7).offset(5,8,1)))if(level.getBlockState(pos).is(net.minecraft.tags.BlockTags.LOGS))n++;return n;}
    private void finish(Throwable failure){
        finished=true;var result=new LinkedHashMap<String,Object>();result.put("passed",failure==null);result.put("ordinaryServerTicks",ticks);result.put("cores",golems.size());result.put("checks",checks);result.put("count",checks.size());result.put("workTrace",workTrace);result.put("wetBobberTicks",wetBobberTicks);
        var observations=new ArrayList<Map<String,Object>>();for(var entry:golems.entrySet()){var g=entry.getValue();observations.add(Map.of("core",entry.getKey(),"entityTicks",g.tickCount-initialTicks.getOrDefault(entry.getKey(),g.tickCount),"position",g.position().toString(),"carried",g.carried().toString()));}result.put("observations",observations);if(failure!=null)result.put("error",failure.toString());
        try{var file=Path.of(System.getProperty("thaumcraft.r3.root"),"analysis/r3/evidence/production-server-golem-runtime.json");Files.createDirectories(file.getParent());Files.writeString(file,new GsonBuilder().setPrettyPrinting().create().toJson(result)+"\n");}catch(java.io.IOException e){throw new java.io.UncheckedIOException(e);}finally{for(var p:held)level.setChunkForced(p.x,p.z,false);server.halt(false);}
    }
}
