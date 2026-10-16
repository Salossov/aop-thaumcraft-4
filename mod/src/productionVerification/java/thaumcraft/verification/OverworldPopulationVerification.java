package thaumcraft.verification;

import java.util.*;
import java.util.function.BiConsumer;
import java.lang.reflect.Proxy;
import java.nio.file.*;
import com.google.gson.*;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import thaumcraft.blocks.*;
import thaumcraft.registry.*;
import thaumcraft.world.*;

/** Real blocks/tiles and full ordered unchanged-TC4 ring placement controls. */
final class OverworldPopulationVerification {
    static void run(ServerLevel level,BiConsumer<Boolean,String> check)throws Exception{
        var switches=new PopulationPolicy.Switches(true,true,true,false,false,false);
        for(int dim=-1;dim<5;dim++)for(int biome=-1;biome<4;biome++)for(boolean flat:new boolean[]{false,true})for(boolean fresh:new boolean[]{false,true}){
            var gates=PopulationPolicy.gates(dim,biome,true,false,false,flat,fresh,switches);
            check.accept(gates.trees()==(dim==-1&&biome==-1&&!flat&&fresh)&&gates.ores()==(dim!=0&&dim!=2&&biome!=0&&biome!=2)&&gates.aura()==(dim!=0&&dim!=2&&fresh)&&gates.structures()==(dim==-1&&!flat&&fresh)&&gates.totems()==gates.structures(),"original blacklist/flat/newGen population matrix dim"+dim+" biome"+biome+" flat"+flat+" fresh"+fresh);
        }
        for(int dim=-1;dim<4;dim++){
            var regen=new PopulationPolicy.Switches(true,true,true,true,true,true);
            check.accept(!PopulationPolicy.gates(dim,-1,true,false,false,false,false,regen).totems(),"regeneration does not create original new-generation-only totems "+dim);
            check.accept(!PopulationPolicy.gates(dim,-1,false,false,true,false,true,regen).aura(),"original End dispatch skips surface aura "+dim);
        }
        long[] counts=new long[4];var random=RandomSource.create(7721);
        for(int i=0;i<1000000;i++)counts[PopulationPolicy.structure(random).ordinal()]++;
        double[] rates={1d/150,149d/150/66,149d/150*65/66/40,149d/150*65/66*39/40};
        for(int i=0;i<4;i++)check.accept(Math.abs(counts[i]-1000000*rates[i])<6*Math.sqrt(1000000*rates[i]*(1-rates[i])),"ordered conditional structure frequency within six sigma "+PopulationPolicy.Structure.values()[i]);
        for(int x=-12;x<13;x++)for(int y=-12;y<13;y++)for(int z=-12;z<13;z++){var p=new BlockPos(x,y,z);for(var side:Direction.Plane.HORIZONTAL)check.accept(ObsidianTotemBlock.faceTexture(p,side)==Math.abs((side.get3DDataValue()+x%4+y%4+z%4)%4),"signed original totem face index "+p+" "+side);}
        var root=JsonParser.parseString(Files.readString(Path.of(System.getProperty("thaumcraft.r3.root"),"analysis/full_port/original-ring-layouts.json"))).getAsJsonObject();var origin=new BlockPos(5632,130,3072);
        var rows=new ArrayList<List<Integer>>();
        WorldGenLevel capture=(WorldGenLevel)Proxy.newProxyInstance(WorldGenLevel.class.getClassLoader(),new Class[]{WorldGenLevel.class},(proxy,method,args)->{
            if(method.getName().equals("setBlock")&&args.length==3){var p=(BlockPos)args[0];var state=(net.minecraft.world.level.block.state.BlockState)args[1];int code=state.isAir()?0:state.is(Blocks.MOSSY_COBBLESTONE)?9:state.is(R4Content.OBSIDIAN_TILE.get())?10:state.is(R4Content.BANNER.get())?12:11;int meta=code==10?1:code==12?8:state.is(R3Content.ELDRITCH_ALTAR.get())?0:state.is(R5Content.OBELISK.get())?1:state.is(R5Content.ELDRITCH_BODY.get())?2:state.is(R5Content.ELDRITCH_CAP.get())?3:0;rows.add(List.of(p.getX()-origin.getX(),p.getY()-origin.getY()+64,p.getZ()-origin.getZ(),code,meta,(Integer)args[2]));}
            try{return method.invoke(level,args);}catch(java.lang.reflect.InvocationTargetException e){throw e.getCause();}
        });
        try{
            for(var value:root.getAsJsonArray("cases")){
                var test=value.getAsJsonObject();int seed=test.get("seed").getAsInt();
                for(var p:BlockPos.betweenClosed(origin.offset(-4,-4,-4),origin.offset(4,8,4)))level.setBlock(p,p.getY()<=origin.getY()?Blocks.STONE.defaultBlockState():Blocks.AIR.defaultBlockState(),2);
                rows.clear();var ring=new LegacyEldritchRing();ring.chunkX=origin.getX()>>4;ring.chunkZ=origin.getZ()>>4;ring.width=11;ring.height=11;random=RandomSource.create(seed);
                check.accept(ring.generate(capture,random,origin)==test.get("accepted").getAsBoolean(),"unchanged original ring support acceptance seed"+seed);
                var expected=new ArrayList<List<Integer>>();for(var row:test.getAsJsonArray("writes")){var a=new ArrayList<Integer>();for(var entry:row.getAsJsonArray())a.add(entry.getAsInt());expected.add(a);}
                int mismatch=0;while(mismatch<rows.size()&&mismatch<expected.size()&&rows.get(mismatch).equals(expected.get(mismatch)))mismatch++;
                check.accept(rows.equals(expected),"every ordered original ring write/metadata/flag matches native placement seed"+seed+" index"+mismatch+" native="+(mismatch<rows.size()?rows.get(mismatch):"end")+" original="+(mismatch<expected.size()?expected.get(mismatch):"end"));
                check.accept(random.nextLong()==test.get("nextLong").getAsLong(),"unchanged original ring RNG continuation including cleared upper stone draws seed"+seed);
                var altar=(thaumcraft.production.EldritchAltarBlockEntity)level.getBlockEntity(origin.above());
                check.accept(altar.guardianSpawner==test.get("spawner").getAsBoolean()&&altar.spawnType==test.get("spawnType").getAsByte(),"original ring altar variation persisted native tile seed"+seed);
                check.accept(level.isEmptyBlock(origin.above(2))&&level.getBlockState(origin.above(3)).is(R5Content.OBELISK.get())&&level.getBlockState(origin.above(7)).is(R5Content.ELDRITCH_BODY.get()),"original air gap and full obelisk column seed"+seed);
            }
            level.setBlock(origin,R4Content.OBSIDIAN_TOTEM.get().at(level,origin),3);
            level.setBlock(origin.above(),R4Content.OBSIDIAN_TOTEM_NODE.get().at(level,origin.above()),3);
            check.accept(level.getBlockState(origin).getValue(ObsidianTotemBlock.STYLE)==0&&level.getBlockState(origin.above()).getValue(ObsidianTotemBlock.STYLE)==2,"neighbor updates assign original shaded lower/top rune totem faces");
            check.accept(NodeFactory.createTotem(level,origin.above(),RandomSource.create(17))&&level.getBlockState(origin.above()).is(R4Content.OBSIDIAN_TOTEM_NODE.get())&&((AuraNodeBlockEntity)level.getBlockEntity(origin.above())).state.type==thaumcraft.api.nodes.NodeState.Type.DARK,"native embedded dark totem node retains its textured solid body");
        }finally{for(var p:BlockPos.betweenClosed(origin.offset(-4,-4,-4),origin.offset(4,8,4)))level.setBlock(p,Blocks.AIR.defaultBlockState(),2);}
    }
}
