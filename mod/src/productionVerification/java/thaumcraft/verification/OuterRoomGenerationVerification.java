package thaumcraft.verification;

import java.util.*;
import java.util.function.BiConsumer;
import java.nio.file.*;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.phys.AABB;
import thaumcraft.blocks.*;
import thaumcraft.entities.PermanentItemEntity;
import thaumcraft.registry.*;
import thaumcraft.world.WorldBiomes;
import thaumcraft.world.dim.*;
import thaumcraft.world.dim.rooms.RoomBiomeWrites;

/** Actual fresh chunk decoration, compared between two independently created server worlds. */
final class OuterRoomGenerationVerification {
    private static final Path CONTROL=Path.of(System.getProperty("thaumcraft.r3.root"),"analysis/full_port/room-generation-control.json");
    static void prepare(ServerLevel level){var world=level.getServer().getLevel(EldritchTeleporter.OUTER);for(int feature=1;feature<=14;feature++){int cx=700+feature*3;world.setChunkForced(cx,800,true);world.getChunk(cx,800);}}
    static boolean ready(ServerLevel level){var world=level.getServer().getLevel(EldritchTeleporter.OUTER);for(int feature=1;feature<=14;feature++)if(!world.areEntitiesLoaded(net.minecraft.world.level.ChunkPos.asLong(700+feature*3,800)))return false;return true;}
    static void reload(ServerLevel level,BiConsumer<Boolean,String> check)throws Exception{
        var world=level.getServer().getLevel(EldritchTeleporter.OUTER);var mode=thaumcraft.config.ThaumcraftConfig.BIOME_CHANGE_MODE.get();thaumcraft.config.ThaumcraftConfig.BIOME_CHANGE_MODE.set(thaumcraft.config.ThaumcraftConfig.BiomeChangeMode.TC4_1X1);
        try{var proof=com.google.gson.JsonParser.parseString(Files.readString(CONTROL)).getAsJsonObject();for(int feature=1;feature<=14;feature++)check.accept(proof.getAsJsonObject("rooms").get(Integer.toString(feature)).getAsString().equals(digest(world.getChunk(700+feature*3,800))),"generated native feature"+feature+" survives actual server restart without regeneration");
            var keyPos=new BlockPos((700+6*3)*16+8,53,12808);var keys=world.getEntitiesOfClass(PermanentItemEntity.class,new AABB(keyPos).inflate(8));check.accept(keys.size()==1&&keys.getFirst().getItem().is(R5Content.RUNIC_KEY.get()),"key generated inside ProtoChunk persists as one real permanent entity after server restart");
            var taintPos=new BlockPos((700+13*3)*16+8,54,12808);check.accept(WorldBiomes.effective(world,taintPos).is(WorldBiomes.TAINT)&&!world.getChunk(taintPos).getData(RoomBiomeWrites.COLUMNS.get()).isEmpty(),"worldgen biome overlay and serialized protochunk attachment both survive real restart");
        }finally{thaumcraft.config.ThaumcraftConfig.BIOME_CHANGE_MODE.set(mode);for(int feature=1;feature<=14;feature++)world.setChunkForced(700+feature*3,800,false);}
    }
    static void run(ServerLevel level,boolean reverse,BiConsumer<Boolean,String> check)throws Exception{
        var world=level.getServer().getLevel(EldritchTeleporter.OUTER);var data=LabyrinthData.get(world);var mode=thaumcraft.config.ThaumcraftConfig.BIOME_CHANGE_MODE.get();thaumcraft.config.ThaumcraftConfig.BIOME_CHANGE_MODE.set(thaumcraft.config.ThaumcraftConfig.BiomeChangeMode.TC4_1X1);
        try{
            check.accept(world.getChunkSource().getGenerator() instanceof OuterChunkGenerator,"fresh Outer Lands uses actual custom ChunkGenerator");
            for(int feature=1;feature<=14;feature++){var cell=new MazeCell();cell.feature=(byte)feature;cell.north=true;check.accept(data.cell(700+feature*3,800)==null,"test world has no preexisting assigned room feature"+feature);data.put(700+feature*3,800,cell.pack());}
            for(int feature=2;feature<=5;feature++){var cell=new MazeCell();cell.feature=(byte)feature;data.put(-700-feature*3,-800,cell.pack());}
            for(int part=0;part<4;part++){var cell=new MazeCell();cell.feature=(byte)(2+part);cell.north=part==0;data.put(-900+(part&1),-900+(part>>1),cell.pack());}
            var order=new ArrayList<Integer>();for(int feature=1;feature<=14;feature++)order.add(feature);if(reverse)Collections.reverse(order);
            var digests=new TreeMap<Integer,String>();
            for(int feature:order){int cx=700+feature*3;var chunk=world.getChunk(cx,800);int solid=0;for(int x=0;x<16;x++)for(int y=0;y<128;y++)for(int z=0;z<16;z++)if(!chunk.getBlockState(new BlockPos(cx*16+x,y,12800+z)).isAir())solid++;
                check.accept(solid>100,"normal generation creates assigned room feature"+feature+" without explicit placement helper");digests.put(feature,digest(chunk));
                if(feature==1)check.accept(world.getBlockState(new BlockPos(cx*16+8,53,12808)).is(R5Content.ELDRITCH_PORTAL.get()),"fresh portal room creates real travel portal during decoration");
                if(feature>=2&&feature<=5){var p=new BlockPos(cx*16+8,55,12803);check.accept(world.getBlockEntity(p) instanceof EldritchLockBlockEntity&&world.getBlockState(p).getValue(EldritchLockBlock.FACING)==Direction.NORTH.ordinal(),"actual protochunk boss lock retains tile and facing after conversion, feature"+feature);}
                if(feature==13)check.accept(!chunk.getData(RoomBiomeWrites.COLUMNS.get()).isEmpty(),"taint columns survive actual ProtoChunk to LevelChunk conversion");
                if(feature==14){var tile=world.getBlockEntity(new BlockPos(cx*16+8,54,12808));check.accept(tile instanceof SpawnerBlockEntity&&((SpawnerBlockEntity)tile).saveWithoutMetadata(world.registryAccess()).getCompound("SpawnData").getCompound("entity").getString("id").equals("thaumcraft:mind_spider"),"worldgen spawner survives protochunk conversion with real Mind Spider data");}
            }
            for(int feature=2;feature<=5;feature++){var chunk=world.getChunk(-700-feature*3,-800);check.accept(!chunk.getBlockState(new BlockPos(chunk.getPos().getMinBlockX()+8,50,chunk.getPos().getMinBlockZ()+8)).isAir(),"boss quadrant without external exit generates safely at negative chunks, feature"+feature);}
            check.accept(world.getBlockState(new BlockPos(0,55,0)).isAir(),"approved correction prevents doorway artifacts at world0,0");
            var arenaOrder=new ArrayList<>(List.of(0,1,2,3));if(reverse)Collections.reverse(arenaOrder);var arenaDigests=new TreeMap<Integer,String>();for(int part:arenaOrder){var chunk=world.getChunk(-900+(part&1),-900+(part>>1));arenaDigests.put(part,digest(chunk));}
            for(var p:List.of(new BlockPos(-900*16+15,54,-900*16+8),new BlockPos(-900*16+16,54,-900*16+8),new BlockPos(-900*16+8,54,-900*16+15),new BlockPos(-900*16+8,54,-900*16+16)))check.accept(world.getBlockState(p).isAir(),"four native contiguous boss quadrants have an open interior across chunk seam "+p);
            var result=new LinkedHashMap<String,Object>();result.put("seed",world.getSeed());result.put("rooms",digests);
            result.put("arena",arenaDigests);
            var generated=data.generate(3000,-3000,29,29,123456789L);check.accept(generated!=null,"largest original29x29 maze is allocated in a fresh test world");
            var mazeCells=data.snapshot().keySet().stream().filter(p->p.x()>=2984&&p.x()<=3014&&p.z()>=-3016&&p.z()<=-2986).sorted(Comparator.comparingInt(LabyrinthData.Position::x).thenComparingInt(LabyrinthData.Position::z)).toList();
            check.accept(mazeCells.size()>200,"whole connected maximum-size maze is planned with hundreds of original rooms");
            var sample=new ArrayList<LabyrinthData.Position>();for(int i=0;i<mazeCells.size();i+=Math.max(1,mazeCells.size()/12))sample.add(mazeCells.get(i));if(reverse)Collections.reverse(sample);
            var mazeDigests=new TreeMap<String,String>();for(var p:sample){var chunk=world.getChunk(p.x(),p.z());check.accept(!chunk.getBlockState(new BlockPos(p.x()*16+8,50,p.z()*16+8)).isAir(),"compiled full maze applies a real room in chunk "+p);mazeDigests.put(p.x()+","+p.z(),digest(chunk));}
            result.put("maximum_maze",mazeDigests);
            if(!reverse)Files.writeString(CONTROL,new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(result)+"\n");else{var prior=com.google.gson.JsonParser.parseString(Files.readString(CONTROL)).getAsJsonObject();check.accept(prior.get("seed").getAsLong()==world.getSeed(),"independent fresh test worlds use the same seed");for(var entry:digests.entrySet())check.accept(prior.getAsJsonObject("rooms").get(entry.getKey().toString()).getAsString().equals(entry.getValue()),"feature"+entry.getKey()+" has identical native block geometry and decoration when loading order is reversed");for(var entry:arenaDigests.entrySet())check.accept(prior.getAsJsonObject("arena").get(entry.getKey().toString()).getAsString().equals(entry.getValue()),"contiguous boss quadrant"+entry.getKey()+" retains native geometry and decoration with reversed load order across independent worlds");for(var entry:mazeDigests.entrySet())check.accept(prior.getAsJsonObject("maximum_maze").get(entry.getKey()).getAsString().equals(entry.getValue()),"maximum29x29 maze room is identical with reversed loading: "+entry.getKey());}
        }finally{thaumcraft.config.ThaumcraftConfig.BIOME_CHANGE_MODE.set(mode);}
    }
    private static String digest(ChunkAccess chunk)throws Exception{
        var hash=java.security.MessageDigest.getInstance("SHA-256");for(int x=0;x<16;x++)for(int y=0;y<128;y++)for(int z=0;z<16;z++){var p=new BlockPos(chunk.getPos().getMinBlockX()+x,y,chunk.getPos().getMinBlockZ()+z);hash.update(chunk.getBlockState(p).toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));hash.update((byte)0);}return HexFormat.of().formatHex(hash.digest());
    }
    private OuterRoomGenerationVerification(){}
}
