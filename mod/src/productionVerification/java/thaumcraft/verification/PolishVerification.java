package thaumcraft.verification;

import com.mojang.authlib.GameProfile;
import java.lang.reflect.*;
import java.nio.file.*;
import java.util.*;
import java.util.function.BiConsumer;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import thaumcraft.api.aspects.*;
import thaumcraft.knowledge.KnowledgeService;
import thaumcraft.production.*;
import thaumcraft.registry.*;
import thaumcraft.world.*;
import thaumcraft.world.dim.EldritchTeleporter;

/** Regressions for the audit findings, using native block capabilities and saved worlds. */
final class PolishVerification {
    static void placement(ServerLevel level,BiConsumer<Boolean,String> check){
        var player=FakePlayerFactory.get(level,new GameProfile(UUID.randomUUID(),"PolishPlacementQA"));player.setGameMode(GameType.CREATIVE);int index=0;
        for(var block:List.of(R3Content.NODE_STABILIZER.get(),R3Content.ADVANCED_NODE_STABILIZER.get()))for(boolean sneak:new boolean[]{false,true}){
            var p=new BlockPos(1400+index++*4,160,1400);level.getChunkAt(p);for(var q:BlockPos.betweenClosed(p.below(),p.above()))level.setBlock(q,Blocks.AIR.defaultBlockState(),2);
            level.setBlock(p,R2Content.AURA_NODE.get().defaultBlockState(),2);player.moveTo(p.getCenter().add(0,0,3));player.setShiftKeyDown(sneak);player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,new ItemStack(block.asItem()));
            var hit=new net.minecraft.world.phys.BlockHitResult(p.getCenter(),Direction.DOWN,p,false);
            var result=player.gameMode.useItemOn(player,level,player.getMainHandItem(),net.minecraft.world.InteractionHand.MAIN_HAND,hit);
            check.accept(result.consumesAction()&&level.getBlockState(p.below()).is(block)&&level.getBlockState(p).is(R2Content.AURA_NODE.get()),"native stabilizer placement below node: "+block+", sneaking="+sneak);
            player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,new ItemStack(R3Content.NODE_CONVERTER.get().asItem()));hit=new net.minecraft.world.phys.BlockHitResult(p.getCenter(),Direction.UP,p,false);
            result=player.gameMode.useItemOn(player,level,player.getMainHandItem(),net.minecraft.world.InteractionHand.MAIN_HAND,hit);
            check.accept(result.consumesAction()&&level.getBlockState(p.above()).is(R3Content.NODE_CONVERTER.get()),"native converter placement above node, sneaking="+sneak);
        }
        player.setShiftKeyDown(false);
    }
    static void run(ServerLevel level,BiConsumer<Boolean,String> check)throws Exception {
        leafException(check);
        staleEssentia(level,check);
        profilePortal(level,check);
        profilePopulation(level,check);
        animation(check);
    }

    private static void leafException(BiConsumer<Boolean,String> check)throws Exception {
        int[] failed={0},writes={0},reads={0};
        var world=(WorldGenLevel)Proxy.newProxyInstance(WorldGenLevel.class.getClassLoader(),new Class<?>[]{WorldGenLevel.class},(proxy,method,args)->switch(method.getName()){
            case "isOutsideBuildHeight" -> false;
            case "getBlockState" -> {
                if(++reads[0]>500)throw new AssertionError("Tree repeated the same failed coordinate instead of making progress");
                var p=(BlockPos)args[0];
                if(p.getX()==0&&p.getZ()==0){failed[0]++;throw new IllegalStateException("Injected persistent leaf read failure");}
                yield Blocks.AIR.defaultBlockState();
            }
            case "setBlock" -> {writes[0]++;yield true;}
            case "toString" -> "PolishFaultWorld";
            default -> throw new UnsupportedOperationException(method.getName());
        });
        var tree=new LegacyBigMagicTree(false);var field=LegacyBigMagicTree.class.getDeclaredField("worldObj");field.setAccessible(true);field.set(tree,world);
        var layer=LegacyBigMagicTree.class.getDeclaredMethod("genTreeLayer",int.class,int.class,int.class,float.class,byte.class,net.minecraft.world.level.block.Block.class);layer.setAccessible(true);
        layer.invoke(tree,0,80,0,2.5f,(byte)1,Blocks.OAK_LEAVES);
        check.accept(failed[0]==1&&writes[0]>0&&reads[0]<100,"persistent tree leaf exception is visited once and subsequent leaves still generate");
    }

    private static TransportBlockEntity jar(ServerLevel level,BlockPos pos,Aspect aspect,int count){
        level.getChunkAt(pos);level.setBlockAndUpdate(pos,R3Content.WARDED_JAR.get().defaultBlockState());
        var jar=(TransportBlockEntity)level.getBlockEntity(pos);jar.store.clear();jar.store.filter(null);if(count>0)jar.addToContainer(aspect,count);jar.sync();return jar;
    }
    private static void animation(BiConsumer<Boolean,String> check){
        for(int fps:new int[]{30,60,144}){
            float scale=0,hover=1;
            for(int i=0;i<fps/2;i++){scale=thaumcraft.client.FrameAnimation.aspect(scale,1d/fps,true);hover=thaumcraft.client.FrameAnimation.hover(hover,1.3f,1d/fps);}
            double expected=.265+(.005-.265)*Math.pow(.9,30);
            check.accept(Math.abs(scale-expected)<.000001&&Math.abs(hover-(1.3-.3*Math.pow(.75,30)))<.000001,"HUD and hover match the 60 FPS reference after 500ms at "+fps+" FPS");
            for(int i=0;i<fps;i++)scale=thaumcraft.client.FrameAnimation.aspect(scale,1d/fps,false);
            check.accept(scale==0,"HUD fade completes after the same elapsed second at "+fps+" FPS");
        }
        float old=0,updated=0;for(int i=0;i<60;i++){old=Math.max(0,old-.005f);if(old<.3f)old+=.031f-old/10;updated=thaumcraft.client.FrameAnimation.aspect(updated,1d/60,true);check.accept(Math.abs(old-updated)<.000001,"60 FPS original recurrence preserved at frame "+i);}
    }
    private static void staleEssentia(ServerLevel level,BiConsumer<Boolean,String> check)throws Exception {
        var origin=new BlockPos(1024,160,1024);var first=origin.west(2);var later=origin.east(2);
        for(var cell:BlockPos.betweenClosed(origin.offset(-3,-3,-3),origin.offset(3,3,3))){level.getChunkAt(cell);level.setBlock(cell,Blocks.AIR.defaultBlockState(),2);}
        jar(level,first,Aspects.FIRE,2);var survivor=jar(level,later,Aspects.FIRE,3);var cache=new RitualEssentia();
        check.accept(first.equals(cache.drain(level,origin,Aspects.FIRE,3)),"infusion cache retains original first-source order");
        level.removeBlock(first,false);var started=System.nanoTime();var result=cache.drain(level,origin,Aspects.FIRE,3);
        check.accept(later.equals(result)&&survivor.store.amount()==2,"removed first cached jar does not hide a valid later jar or impose a five-second wait");
        var replacement=origin.south(2);level.removeBlock(later,false);jar(level,replacement,Aspects.FIRE,4);
        check.accept(replacement.equals(cache.drain(level,origin,Aspects.FIRE,3)),"all stale cached jars trigger one immediate rescan and find the relocated source");
        var rows=new LinkedHashMap<String,Object>();rows.put("cacheInvalidationCallMs",(System.nanoTime()-started)/1e6);rows.put("furnaceDistillationTicks",40);rows.put("alumentumDistillationTicks",20);
        metrics("essentia",rows);
    }

    private static int loaded(ServerLevel level,int cx,int cz,int radius){int n=0;for(int x=-radius;x<=radius;x++)for(int z=-radius;z<=radius;z++)if(level.hasChunk(cx+x,cz+z))n++;return n;}
    private static void profilePortal(ServerLevel level,BiConsumer<Boolean,String> check)throws Exception {
        var p=new BlockPos(24008,120,24008);var replacement=p.east(3);level.getChunkAt(p);level.setBlock(p,R5Content.ELDRITCH_PORTAL.get().defaultBlockState(),2);
        int before=loaded(level,p.getX()>>4,p.getZ()>>4,8);long time=System.nanoTime();var first=EldritchTeleporter.findPortal(level,p.getCenter());double cold=(System.nanoTime()-time)/1e6;
        int after=loaded(level,p.getX()>>4,p.getZ()>>4,8);check.accept(p.equals(first),"cold portal search finds the native portal");
        time=System.nanoTime();var warm=EldritchTeleporter.findPortal(level,p.getCenter());double cached=(System.nanoTime()-time)/1e6;
        check.accept(p.equals(warm)&&loaded(level,p.getX()>>4,p.getZ()>>4,8)==after,"validated warm portal cache adds no chunks");
        level.removeBlock(p,false);level.setBlock(replacement,R5Content.ELDRITCH_PORTAL.get().defaultBlockState(),2);
        check.accept(replacement.equals(EldritchTeleporter.findPortal(level,p.getCenter())),"removed cached portal is rejected and a replacement is selected immediately");
        level.removeBlock(replacement,false);check.accept(EldritchTeleporter.findPortal(level,p.getCenter())==null,"removing every portal cannot return an indefinitely refreshed stale position");
        metrics("portal",Map.of("coldSearchMs",cold,"warmSearchMs",cached,"loadedBefore",before,"loadedAfter",after,"searchChunkColumns",289,"world",level.dimension().location().toString()));
    }
    private static void profilePopulation(ServerLevel level,BiConsumer<Boolean,String> check)throws Exception {
        var p=new ChunkPos(1800,1800);int before=loaded(level,p.x,p.z,2);long time=System.nanoTime();
        var request=new NaturalPopulationData.Request(p.toLong(),PopulationPolicy.populationSeed(level.getSeed(),p.x,p.z),true,"polish-profile-v1");
        NaturalPopulation.apply(level,request);double first=(System.nanoTime()-time)/1e6;int after=loaded(level,p.x,p.z,2);
        time=System.nanoTime();NaturalPopulation.apply(level,request);double repeated=(System.nanoTime()-time)/1e6;
        check.accept(level.getChunk(p.x,p.z).getData(NaturalPopulation.VERSION).equals(request.version()),"profiled population persists its completion marker and a repeat is idempotent");
        metrics("population",Map.of("coldApplyMs",first,"repeatApplyMs",repeated,"loadedBefore",before,"loadedAfter",after,"preloadChunkColumns",25));
    }
    private static void metrics(String name,Object value)throws Exception {
        var path=Path.of(System.getProperty("thaumcraft.r3.root"),"analysis/port-polish-2026-10-15/"+name+"-metrics.json");Files.createDirectories(path.getParent());
        Files.writeString(path,new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(value));
    }

    static void prepareProductionSave(ServerLevel l,BiConsumer<Boolean,String> check){
        var p=new BlockPos(64,100,0);for(var cell:BlockPos.betweenClosed(p.offset(-6,-3,-6),p.offset(6,1,6))){l.getChunkAt(cell);l.setBlock(cell,Blocks.AIR.defaultBlockState(),2);}
        l.setBlockAndUpdate(p,R3Content.INFUSION_MATRIX.get().defaultBlockState());l.setBlockAndUpdate(p.below(2),R3Content.ARCANE_PEDESTAL.get().defaultBlockState());
        ((PedestalBlockEntity)l.getBlockEntity(p.below(2))).setItem(0,new ItemStack(Items.IRON_INGOT));
        for(int x:new int[]{-1,1})for(int z:new int[]{-1,1}){var s=R3Content.INFUSION_PILLAR.get().defaultBlockState();l.setBlock(p.offset(x,-2,z),s,2);l.setBlock(p.offset(x,-1,z),s.setValue(InfusionPillarBlock.HALF,DoubleBlockHalf.UPPER),2);}
        for(var d:Direction.Plane.HORIZONTAL){var q=p.below(2).relative(d,3);l.setBlockAndUpdate(q,R3Content.ARCANE_PEDESTAL.get().defaultBlockState());((PedestalBlockEntity)l.getBlockEntity(q)).setItem(0,new ItemStack(Items.QUARTZ));}
        jar(l,p.offset(5,-2,0),Aspects.WATER,3);jar(l,p.offset(-5,-2,0),Aspects.FIRE,2);
        var player=FakePlayerFactory.get(l,new GameProfile(UUID.fromString("b6f782a1-cd21-4b72-a090-83c541f0ac01"),"IndependentInfusionSave"));KnowledgeService.initialize(player);KnowledgeService.mutate(player,k->k.completeResearch("INFUSION"));
        var matrix=(InfusionMatrixBlockEntity)l.getBlockEntity(p);matrix.active=true;check.accept(matrix.start(player)&&matrix.instability==0,"independent production-save starts a stable authorized ritual");matrix.craftCycle();
        check.accept(matrix.remainingIngredients()==4&&matrix.remainingEssentia().values().stream().mapToInt(Integer::intValue).sum()==4,"independent production-save consumes exactly one actual essentia before disk save");
        var target=jar(l,new BlockPos(3,109,0),Aspects.FIRE,32);target.applyLabel(Aspects.FIRE);
        var tubePos=new BlockPos(1,110,0);l.setBlock(tubePos,Blocks.AIR.defaultBlockState(),2);l.setBlockAndUpdate(tubePos,R3Content.TUBE.get().defaultBlockState());((TransportBlockEntity)l.getBlockEntity(tubePos)).wandClick(Direction.EAST,false);
        var cp=new BlockPos(20,100,0);l.setBlock(cp,Blocks.AIR.defaultBlockState(),2);l.setBlockAndUpdate(cp,R3Content.CRUCIBLE.get().defaultBlockState());var crucible=(CrucibleBlockEntity)l.getBlockEntity(cp);crucible.fluidHandler.fill(new FluidStack(net.minecraft.world.level.material.Fluids.WATER,950),FluidAction.EXECUTE);crucible.heat=200;crucible.setChanged();
    }
    private PolishVerification(){}
}
