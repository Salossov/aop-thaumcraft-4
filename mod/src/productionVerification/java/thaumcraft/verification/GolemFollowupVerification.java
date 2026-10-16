package thaumcraft.verification;

import java.nio.file.*;
import java.util.*;
import com.google.gson.GsonBuilder;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.*;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import thaumcraft.entities.*;
import thaumcraft.registry.*;

/** Ordinary scheduler regression for the user's hydrated hoe row and mature field. */
final class GolemFollowupVerification {
    static void register(MinecraftServer server){
        var level=server.overworld();var root=Path.of(System.getProperty("thaumcraft.r3.root"),"analysis/golems-aura-tools-audit/round2");
        var farm=new BlockPos(0,81,0);var useHome=new BlockPos(37,81,0);
        for(int x=-1;x<=3;x++)for(int z=-1;z<=1;z++)level.setChunkForced(x,z,true);
        for(var p:BlockPos.betweenClosed(new BlockPos(-7,79,-7),new BlockPos(46,85,7)))level.setBlockAndUpdate(p,p.getY()==79?Blocks.STONE.defaultBlockState():Blocks.AIR.defaultBlockState());
        var crops=new ArrayList<BlockPos>();
        for(int x=-4;x<=4;x++)for(int z=-4;z<=4;z++){var p=farm.offset(x,0,z);level.setBlockAndUpdate(p.below(),Blocks.FARMLAND.defaultBlockState().setValue(FarmBlock.MOISTURE,7));level.setBlockAndUpdate(p,Blocks.WHEAT.defaultBlockState().setValue(CropBlock.AGE,7));crops.add(p);}
        level.setBlockAndUpdate(farm.below(),Blocks.WATER.defaultBlockState());level.setBlockAndUpdate(farm,Blocks.AIR.defaultBlockState());crops.remove(farm);
        var cropGolem=spawn(level,3,farm,ItemStack.EMPTY);cropGolem.moveTo(-.5,81,.5);
        for(int x=35;x<=45;x++)for(int z=-2;z<=2;z++)level.setBlockAndUpdate(new BlockPos(x,80,z),Blocks.STONE.defaultBlockState());
        level.setBlockAndUpdate(useHome.below(),Blocks.CHEST.defaultBlockState());
        var hoeGolem=spawn(level,8,useHome,new ItemStack(Items.DIAMOND_HOE));hoeGolem.moveTo(37.5,81,.5);
        var hoeTargets=new ArrayList<BlockPos>();for(int x=40;x<=42;x++){var p=new BlockPos(x,80,0);hoeTargets.add(p);level.setBlockAndUpdate(p,Blocks.DIRT.defaultBlockState());level.setBlockAndUpdate(p.south(),Blocks.WATER.defaultBlockState());hoeGolem.mark(new GolemMarker(level.dimension(),p,Direction.UP,-1),false);}
        int[] ticks={0},idleSwings={0},mask={0};boolean[] done={false};
        NeoForge.EVENT_BUS.addListener((ServerTickEvent.Post event)->{
            if(done[0]||event.getServer()!=server)return;int tick=++ticks[0];
            for(int i=0;i<hoeTargets.size();i++)if(level.getBlockState(hoeTargets.get(i)).is(Blocks.FARMLAND))mask[0]|=1<<i;
            if(tick>600&&mask[0]==7&&hoeGolem.action()>0)idleSwings[0]++;
            if(tick%100==0)try{Files.createDirectories(root);Files.writeString(root.resolve("farm-progress.json"),new GsonBuilder().setPrettyPrinting().create().toJson(Map.of("tick",tick,"remaining",crops.stream().filter(p->level.getBlockState(p).is(Blocks.WHEAT)).count(),"cropGolem",cropGolem.position().toString(),"navigationDone",cropGolem.getNavigation().isDone(),"idleSwings",idleSwings[0])));}catch(Exception e){throw new RuntimeException(e);}
            if(tick<2400||tick<6000&&crops.stream().anyMatch(p->level.getBlockState(p).is(Blocks.WHEAT)))return;
            done[0]=true;var remaining=crops.stream().filter(p->level.getBlockState(p).is(Blocks.WHEAT)).toList();var result=new LinkedHashMap<String,Object>();
            result.put("passed",remaining.isEmpty()&&mask[0]==7&&idleSwings[0]==0);result.put("serverTicks",tick);result.put("initialMatureCrops",crops.size());result.put("remainingCrops",remaining.stream().map(Object::toString).toList());result.put("tilledMask",mask[0]);result.put("idleHoeSwingTicks",idleSwings[0]);result.put("harvestDropsRemainForGather",level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,new net.minecraft.world.phys.AABB(farm).inflate(10)).size()>0);
            try{Files.createDirectories(root);Files.writeString(root.resolve("ordinary-farm-runtime.json"),new GsonBuilder().setPrettyPrinting().create().toJson(result));}catch(Exception e){throw new RuntimeException(e);}server.halt(false);
        });
    }
    private static ThaumicGolem spawn(net.minecraft.server.level.ServerLevel level,int core,BlockPos home,ItemStack held){
        var body=new ItemStack(R3Equipment.GOLEM_BODIES.get(GolemMaterial.CLAY).get());CustomData.update(DataComponents.CUSTOM_DATA,body,t->t.putInt("core",core));var g=R3Entities.GOLEM.get().create(level);g.initialize(GolemMaterial.CLAY,UUID.randomUUID(),home,Direction.UP,body);g.carried(held);level.addFreshEntity(g);return g;
    }
}
