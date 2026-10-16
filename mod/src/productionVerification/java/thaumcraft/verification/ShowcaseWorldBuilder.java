package thaumcraft.verification;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.function.BiConsumer;
import com.google.gson.*;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import thaumcraft.api.aspects.*;
import thaumcraft.api.nodes.NodeState;
import thaumcraft.blocks.AuraNodeBlockEntity;
import thaumcraft.data.ThaumcraftData;
import thaumcraft.entities.R5Entities;
import thaumcraft.items.WandVis;
import thaumcraft.knowledge.KnowledgeService;
import thaumcraft.registry.*;
import thaumcraft.world.*;

/**
 * Builds a playable QA showcase world: natural TC4 biomes at spawn, one of each
 * structure, trees, cultists, aura nodes, plus a starter kit and a signposted
 * coordinate index. The world stays on disk for manual visual acceptance.
 */
final class ShowcaseWorldBuilder {
    private static final int Y=150;
    private final ServerLevel level;
    private final BiConsumer<Boolean,String> check;
    private final JsonObject index=new JsonObject();
    private ShowcaseWorldBuilder(ServerLevel level,BiConsumer<Boolean,String> check){this.level=level;this.check=check;}

    static void run(ServerLevel level,BiConsumer<Boolean,String> check) throws IOException {
        var builder=new ShowcaseWorldBuilder(level,check);
        builder.main();
        builder.biomes();
        builder.structures();
        builder.trees();
        builder.cultists();
        builder.nodes();
        builder.knowledge();
        level.getServer().saveEverything(false,true,true);
        var root=Path.of(System.getProperty("thaumcraft.r3.root"));
        var world=level.getServer().getWorldPath(LevelResource.ROOT).toAbsolutePath().normalize().toString();
        builder.index.addProperty("world",world);
        Files.writeString(root.resolve("analysis/comparison/showcase-world.json"),
            new GsonBuilder().setPrettyPrinting().create().toJson(builder.index)+"\n");
    }

    private void mark(String name,int x,int y,int z){var point=new JsonObject();point.addProperty("x",x);point.addProperty("y",y);point.addProperty("z",z);index.add(name,point);}

    private void floor(int x1,int z1,int x2,int z2){
        for(int z=z1;z<=z2;z++)for(int x=x1;x<=x2;x++)level.setBlock(new BlockPos(x,Y-1,z),Blocks.STONE.defaultBlockState(),2);
        for(int z=z1;z<=z2;z++)for(int x=x1;x<=x2;x++)for(int y=Y;y<Y+6;y++)level.setBlock(new BlockPos(x,y,z),Blocks.AIR.defaultBlockState(),2);
    }

    private void sign(int x,int z,String... lines){
        var pos=new BlockPos(x,Y,z);level.setBlock(pos,Blocks.OAK_SIGN.defaultBlockState(),2);
        var sign=(SignBlockEntity)level.getBlockEntity(pos);var text=sign.getFrontText();
        for(int i=0;i<Math.min(4,lines.length);i++)text=text.setMessage(i,Component.literal(lines[i]));sign.setText(text,true);sign.setChanged();
    }

    private void main(){
        floor(-32,-32,320,320);
        level.setDefaultSpawnPos(new BlockPos(0,Y,12),180);
        level.setDayTime(6000);level.setWeatherParameters(0,6000,false,false);
        var rules=level.getGameRules();
        for(var key:List.of(GameRules.RULE_DAYLIGHT,GameRules.RULE_WEATHER_CYCLE,GameRules.RULE_DOMOBSPAWNING,GameRules.RULE_MOBGRIEFING))rules.getRule(key).set(false,level.getServer());
        rules.getRule(GameRules.RULE_KEEPINVENTORY).set(true,level.getServer());
        sign(0,10,"AOP Thaumcraft 4","QA showcase","Cheats enabled","See index chest");
        mark("spawn",0,Y,12);
        var wand=new ItemStack(R2Content.WAND.get());for(var a:Aspects.PRIMALS)WandVis.set(wand,a,2500);
        var starter=new BlockPos(-4,Y,8);
        level.setBlock(starter,Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING,Direction.SOUTH),2);
        var chest=(ChestBlockEntity)level.getBlockEntity(starter);
        chest.setItem(0,new ItemStack(R2Content.BOOK.get()));chest.setItem(1,wand);
        chest.setItem(2,new ItemStack(R2Content.THAUMOMETER.get()));chest.setItem(3,new ItemStack(R2Content.GOGGLES.get()));
        chest.setItem(4,new ItemStack(R2Content.SCRIBING_TOOLS.get()));chest.setItem(5,new ItemStack(R3Content.RESONATOR.get()));
        sign(-4,11,"Starter kit","Book + wand","Meter + tools","");
        mark("starter_chest",-4,Y,8);
    }

    private void chunkAt(int x,int z){level.getChunk(x>>4,z>>4);}
    private int ground(int x,int z){return level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING,x,z);}

    private void biomes(){
        // 96x96 native biome patches drawn through the same code path as node conversion.
        int mX=48,mZ=64,tX=176,tZ=64;
        for(int dx=0;dx<96;dx++)for(int dz=0;dz<96;dz++){
            chunkAt(mX+dx,mZ+dz);chunkAt(tX+dx,tZ+dz);
        }
        for(int dx=0;dx<96;dx+=4)for(int dz=0;dz<96;dz+=4){
            for(int ox=0;ox<4;ox++)for(int oz=0;oz<4;oz++){
                WorldBiomes.change(level,mX+dx+ox,mZ+dz+oz,WorldBiomes.MAGICAL);
                WorldBiomes.change(level,tX+dx+ox,tZ+dz+oz,WorldBiomes.TAINT);
            }
        }
        sign(mX-4,mZ-4,"Magical forest","Native biome","96 x 96","");
        sign(tX-4,tZ-4,"Tainted land","Native biome","96 x 96","");
        mark("magical_forest",mX+48,ground(mX+48,mZ+48),mZ+48);
        mark("tainted_land",tX+48,ground(tX+48,tZ+48),tZ+48);
        check.accept(WorldBiomes.is(level,new BlockPos(mX+48,ground(mX+48,mZ+48),mZ+48),WorldBiomes.MAGICAL),"showcase magical forest reads back");
        check.accept(WorldBiomes.is(level,new BlockPos(tX+48,ground(tX+48,tZ+48),tZ+48),WorldBiomes.TAINT),"showcase tainted land reads back");
    }

    private boolean ring(int x,int z){
        chunkAt(x,z);
        var ring=new LegacyEldritchRing();
        ring.chunkX=x>>4;ring.chunkZ=z>>4;
        ring.width=15;ring.height=15;
        var data=thaumcraft.world.dim.LabyrinthData.get(level);
        var origin=new BlockPos(x,Math.max(Y,ground(x,z)+1),z);
        // Force placement: clear the 7x7 pad so validSurface passes deterministically.
        for(int dx=-3;dx<=3;dx++)for(int dz=-3;dz<=3;dz++){
            var p=origin.offset(dx,0,dz);
            level.setBlock(p,Blocks.GRASS_BLOCK.defaultBlockState(),2);
            for(int q=1;q<8;q++)level.setBlock(p.above(q),Blocks.AIR.defaultBlockState(),2);
            for(int q=1;q<5;q++)level.setBlock(p.below(q),Blocks.DIRT.defaultBlockState(),2);
        }
        boolean placed=ring.generate(level,level.random,origin);
        if(placed)data.generate(x>>4,z>>4,ring.width,ring.height,level.random.nextLong());
        return placed;
    }

    private void structures(){
        int baseX=48,baseZ=192,dx=0;
        // Mound (19 wide, 17 tall): flat pad, generous headroom.
        {
            int x=baseX+dx,z=baseZ;dx+=48;chunkAt(x,z);
            int y=ground(x,z)+9;
            var origin=new BlockPos(x,y-9,z);
            for(int ox=-2;ox<=20;ox++)for(int oz=-2;oz<=20;oz++){
                var p=origin.offset(ox,8,oz);
                level.setBlock(p,Blocks.GRASS_BLOCK.defaultBlockState(),2);
                for(int q=1;q<20;q++)level.setBlock(p.above(q),Blocks.AIR.defaultBlockState(),2);
            }
            boolean placed=new LegacyMound().generate(level,level.random,origin);
            sign(x-4,z-4,"Stone mound","generate=" + placed,"","");
            if(placed)StructureIndexData.get(level).record(level,StructureIndexData.Kind.MOUND,x+9,y,z+9);
            mark("mound",x+9,y,z+9);
            check.accept(placed,"showcase mound placed");
        }
        // Hilltop stones (needs Y>=85, grass/dirt/stone pad with air above).
        {
            int x=baseX+dx,z=baseZ;dx+=48;chunkAt(x,z);
            int y=Math.max(Y,86);
            var origin=new BlockPos(x,y,z);
            for(int ox=-4;ox<=4;ox++)for(int oz=-4;oz<=4;oz++){
                var p=origin.offset(ox,0,oz);
                level.setBlock(p,Blocks.GRASS_BLOCK.defaultBlockState(),2);
                for(int q=1;q<8;q++)level.setBlock(p.above(q),Blocks.AIR.defaultBlockState(),2);
                for(int q=1;q<5;q++)level.setBlock(p.below(q),Blocks.DIRT.defaultBlockState(),2);
            }
            boolean placed=new LegacyHilltopStones().generate(level,level.random,origin);
            sign(x-6,z-6,"Hilltop stones","generate=" + placed,"","");
            if(placed)StructureIndexData.get(level).record(level,StructureIndexData.Kind.HILLTOP,x,y,z);
            mark("hilltop",x,y,z);
            check.accept(placed,"showcase hilltop placed");
        }
        // Eldritch ring altar (cultist home).
        {
            int x=baseX+dx,z=baseZ;dx+=48;
            boolean placed=ring(x,z);
            sign(x-6,z-6,"Eldritch ring","Cultist altar","generate=" + placed,"");
            if(placed)StructureIndexData.get(level).record(level,StructureIndexData.Kind.RING_ALTAR,x,ground(x,z)+1,z);
            mark("ring_altar",x,ground(x,z)+1,z);
            check.accept(placed,"showcase ring altar placed");
        }
        // Obsidian totem row (force auraGen=false path via direct call below is random; place pad only).
        {
            int x=baseX+dx,z=baseZ;dx+=48;chunkAt(x,z);
            sign(x-4,z-4,"Totem ground","Obsidian totem","Pads cleared","");
            mark("totem_pad",x,ground(x,z),z);
        }
    }

    private void flatPad(int x,int y,int z,int radius){
        for(int ox=-radius;ox<=radius;ox++)for(int oz=-radius;oz<=radius;oz++){
            var p=new BlockPos(x+ox,y,z+oz);
            level.setBlock(p,Blocks.GRASS_BLOCK.defaultBlockState(),2);
            for(int q=1;q<40;q++)level.setBlock(p.above(q),Blocks.AIR.defaultBlockState(),2);
            for(int q=1;q<5;q++)level.setBlock(p.below(q),Blocks.DIRT.defaultBlockState(),2);
        }
    }

    private void chunkArea(int x,int z,int radiusChunks){
        for(int dx=-radiusChunks;dx<=radiusChunks;dx++)for(int dz=-radiusChunks;dz<=radiusChunks;dz++)
            level.getChunk((x>>4)+dx,(z>>4)+dz);
    }

    private BlockPos tryGreatwood(int x,int y,int z,boolean spiders){
        // Same retry-with-offset pattern as natural worldgen: geometry is RNG-seeded
        // and a single fixed origin can fail its space check.
        // Origin is the air ABOVE the grass pad: TC4 checks the trunk line from the
        // origin upward, so starting inside the grass block itself always fails.
        for(int attempt=0;attempt<8;attempt++){
            int ox=x+attempt*4,oz=z+attempt*2;
            chunkArea(ox,oz,1);flatPad(ox,y,oz,14);
            if(new LegacyGreatwoodTree().generate(level,new java.util.Random(level.random.nextLong()),ox,y+1,oz,spiders))
                return new BlockPos(ox,y+1,oz);
        }
        return null;
    }

    private void trees(){
        // Silverwood grove inside the magical forest patch (pedestal at floor level).
        int sX=64,sZ=80;chunkAt(sX,sZ);
        int sY=Y;flatPad(sX,sY,sZ,12);
        boolean silver=new LegacySilverwoodTree(false,7,4).func_76484_a(level,level.random,sX,sY,sZ);
        sign(sX-8,sZ-8,"Silverwood","generate=" + silver,"","");
        mark("silverwood",sX,sY,sZ);
        check.accept(silver,"showcase silverwood placed");
        // Greatwood + spider variant in the tainted patch.
        var greatPos=tryGreatwood(192,Y,80,false);
        boolean great=greatPos!=null;
        int gX=greatPos==null?192:greatPos.getX(),gY=Y,gZ=greatPos==null?80:greatPos.getZ();
        sign(gX-10,gZ-10,"Greatwood","generate=" + great,"","");
        mark("greatwood",gX,gY,gZ);
        check.accept(great,"showcase greatwood placed");
        var spiderPos=tryGreatwood(216,Y,104,true);
        boolean spider=spiderPos!=null;
        int wX=spiderPos==null?216:spiderPos.getX(),wY=Y,wZ=spiderPos==null?104:spiderPos.getZ();
        sign(wX-10,wZ-10,"Spider tree","Chest+spawner","generate=" + spider);
        if(spider)StructureIndexData.get(level).record(level,StructureIndexData.Kind.SPIDER_TREE,wX,wY,wZ);
        mark("spider_tree",wX,wY,wZ);
        check.accept(spider,"showcase spider tree placed");
    }

    private void cultists(){
        int x=baseRingX(),z=baseRingZ();
        int placed=0;
        for(int[] offset:new int[][]{{-2,-2},{-2,2},{2,-2},{2,2}}){
            var pos=new BlockPos(x+offset[0],ground(x+offset[0],z+offset[1])+1,z+offset[1]);
            var mob=R5Entities.CULTIST_CLERIC.get().create(level);
            if(mob==null)continue;
            mob.moveTo(pos.getX()+.5,pos.getY(),pos.getZ()+.5,0,0);
            mob.finalizeSpawn(level,level.getCurrentDifficultyAt(pos),MobSpawnType.COMMAND,null);
            if(level.addFreshEntity(mob)){placed++;mob.setRitualist(true);}
        }
        for(int i=0;i<2;i++){
            var pos=new BlockPos(x+(i==0?-5:5),ground(x+(i==0?-5:5),z)+1,z);
            var mob=R5Entities.CULTIST_KNIGHT.get().create(level);
            if(mob==null)continue;
            mob.moveTo(pos.getX()+.5,pos.getY(),pos.getZ()+.5,0,0);
            mob.finalizeSpawn(level,level.getCurrentDifficultyAt(pos),MobSpawnType.COMMAND,null);
            if(level.addFreshEntity(mob))placed++;
        }
        sign(x-8,z+8,"Cultists","4 clerics + 2","knights","");
        mark("cultists",x,ground(x,z)+1,z);
        check.accept(placed>=6,"showcase cultists summoned: "+placed);
    }

    private int baseRingX(){return 48+48*2;}
    private int baseRingZ(){return 192;}

    private void nodes(){
        var primals=new LinkedHashMap<String,Integer>();
        for(var a:Aspects.PRIMALS)primals.put(a.getTag(),50);
        int i=0;
        for(var type:NodeState.Type.values()){
            int x=24+i*24,z=280;chunkAt(x,z);
            var p=new BlockPos(x,Y,z);
            level.setBlock(p,Blocks.GRASS_BLOCK.defaultBlockState(),2);
            level.setBlock(p.above(),R2Content.AURA_NODE.get().defaultBlockState(),2);
            ((AuraNodeBlockEntity)level.getBlockEntity(p.above())).initialize(primals,type,NodeState.Modifier.NONE);
            sign(x,z+4,"Aura node",type.name(),"","");
            mark("node_"+type.name().toLowerCase(Locale.ROOT),x,Y+1,z);
            i++;
        }
        check.accept(i==NodeState.Type.values().length,"showcase covers every node type");
    }

    private void knowledge(){
        for(var name:List.of("QA_Showcase","Dev","R3Dev")){
            var uuid=UUID.nameUUIDFromBytes(("OfflinePlayer:"+name).getBytes(StandardCharsets.UTF_8));
            var player=FakePlayerFactory.get(level,new GameProfile(uuid,name));
            KnowledgeService.initialize(player);
            KnowledgeService.mutate(player,k->{
                for(var key:ThaumcraftData.get(level.getServer()).research().keySet())k.completeResearch(key);
                for(var aspect:Aspect.all().values())k.setPool(aspect.getTag(),500);
                return true;
            });
            KnowledgeService.mutate(player,k->{k.setWarp(0,0,0,0);return true;});
            check.accept(KnowledgeService.get(player).totalWarp()==0,"showcase profile starts without warp: "+name);
        }
        // Coordinate index chest at spawn: paper map of every POI.
        var indexPos=new BlockPos(4,Y,12);
        level.setBlock(indexPos,Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING,Direction.SOUTH),2);
        var chest=(ChestBlockEntity)level.getBlockEntity(indexPos);
        int slot=0;
        for(var key:List.of("spawn","starter_chest","magical_forest","tainted_land","mound","hilltop","ring_altar","cultists","silverwood","greatwood","spider_tree")){
            if(!index.has(key)||slot>=27)continue;
            var point=index.getAsJsonObject(key);
            var page=new ItemStack(net.minecraft.world.item.Items.PAPER);
            page.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME,Component.literal(key+": "+point.get("x").getAsInt()+" "+point.get("y").getAsInt()+" "+point.get("z").getAsInt()));
            chest.setItem(slot++,page);
        }
        for(var type:NodeState.Type.values()){
            var key="node_"+type.name().toLowerCase(Locale.ROOT);
            if(!index.has(key)||slot>=27)continue;
            var point=index.getAsJsonObject(key);
            var page=new ItemStack(net.minecraft.world.item.Items.PAPER);
            page.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME,Component.literal(key+": "+point.get("x").getAsInt()+" "+point.get("y").getAsInt()+" "+point.get("z").getAsInt()));
            chest.setItem(slot++,page);
        }
        sign(4,14,"POI index","Paper coords","in chest","");
    }
}
