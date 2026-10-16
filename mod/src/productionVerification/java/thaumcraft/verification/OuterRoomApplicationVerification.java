package thaumcraft.verification;

import java.util.*;
import java.util.function.BiConsumer;
import java.nio.file.*;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.phys.AABB;
import thaumcraft.blocks.*;
import thaumcraft.entities.*;
import thaumcraft.registry.*;
import thaumcraft.production.EldritchAltarBlockEntity;
import thaumcraft.world.WorldBiomes;
import thaumcraft.world.dim.*;
import thaumcraft.world.dim.rooms.*;

/** Native room placement and restart, independently of the pending chunk decoration RNG policy. */
final class OuterRoomApplicationVerification {
    private static ServerLevel outer(ServerLevel level){return level.getServer().getLevel(EldritchTeleporter.OUTER);}
    private static final int CX=500,CZ=500;
    private static final BlockPos CONTROL=new BlockPos(CX*16+8,90,CZ*16+8);
    private static final Path PROOF=Path.of(System.getProperty("thaumcraft.r3.root"),"analysis/full_port/room-application-reload-control.json");
    private static int loadedLockCount=-2;private static byte loadedLockFacing;private static long loadedLockTime;
    static void prepare(ServerLevel level){var world=outer(level);
        if(System.getProperty("thaumcraft.r3.phase","").equals("room-application-reload")){
            world.getChunkAt(CONTROL);var tile=(EldritchLockBlockEntity)world.getBlockEntity(CONTROL);
            loadedLockCount=tile==null?-2:tile.count;loadedLockFacing=tile==null?-1:tile.facing;loadedLockTime=world.getGameTime();
        }
for(int x=CX;x<=CX+29;x++)for(int z=CZ;z<=CZ+1;z++){world.setChunkForced(x,z,true);world.getChunk(x,z);}}
    static boolean ready(ServerLevel level){var world=outer(level);for(int x=CX;x<=CX+29;x++)for(int z=CZ;z<=CZ+1;z++)if(!world.areEntitiesLoaded(ChunkPos.asLong(x,z)))return false;return true;}
    private static void release(ServerLevel level){var world=outer(level);for(int x=CX;x<=CX+29;x++)for(int z=CZ;z<=CZ+1;z++)world.setChunkForced(x,z,false);}
    static void run(ServerLevel level,BiConsumer<Boolean,String> check)throws Exception{
        var world=outer(level);var difficulty=world.getDifficulty();world.getServer().setDifficulty(Difficulty.NORMAL,true);var mode=thaumcraft.config.ThaumcraftConfig.BIOME_CHANGE_MODE.get();thaumcraft.config.ThaumcraftConfig.BIOME_CHANGE_MODE.set(thaumcraft.config.ThaumcraftConfig.BiomeChangeMode.TC4_1X1);
        try{
            for(int feature=1;feature<=14;feature++){
                int cx=CX+(feature-1)*2;var origin=new BlockPos(cx*16,50,CZ*16);var area=new AABB(net.minecraft.world.phys.Vec3.atLowerCornerOf(origin.offset(0,-3,0)),net.minecraft.world.phys.Vec3.atLowerCornerOf(origin.offset(17,20,17)));world.getEntitiesOfClass(Entity.class,area).forEach(Entity::discard);
                for(int x=0;x<=16;x++)for(int z=0;z<=16;z++)for(int y=48;y<=65;y++)world.setBlock(new BlockPos(cx*16+x,y,CZ*16+z),Blocks.AIR.defaultBlockState(),2);
                var cell=new MazeCell();cell.feature=(byte)feature;cell.north=true;
                var sink=new RoomWorldSink(world,cell,new Random(113+feature));RoomLayouts.generateCell(sink,new Random(42),cx,CZ,cell);sink.finish();
                int solid=0,air=0,stairs=0,locks=0,fibres=0;
                for(int x=0;x<=16;x++)for(int z=0;z<=16;z++)for(int y=49;y<=64;y++){
                    var p=new BlockPos(cx*16+x,y,CZ*16+z);var s=world.getBlockState(p);if(s.isAir())air++;else solid++;
                    if(s.is(R5Content.ELDRITCH_STAIRS.get()))stairs++;
                    if(s.is(R5Content.ELDRITCH_LOCK.get())){locks++;var tile=(EldritchLockBlockEntity)world.getBlockEntity(p);check.accept(tile.facing==Direction.NORTH.ordinal()&&s.getValue(EldritchLockBlock.FACING)==tile.facing&&tile.count==-1,"boss doorway has native persisted inactive north lock, feature"+feature);}
                    if(s.getBlock() instanceof TaintFibresBlock){fibres++;if(s.is(R2Content.TAINT_FIBRES.get())){boolean face=false;for(var d:Direction.values())face|=s.getValue(MultifaceBlock.getFaceProperty(d));check.accept(face,"generated fibres have visible support faces at "+p);}}
                }
                check.accept(solid>100&&air>100,"native feature"+feature+" creates a room with walls and an interior");
                if(feature<=8)check.accept(stairs>0,"native feature"+feature+" retains original stair geometry");
                if(feature>=2&&feature<=5)check.accept(locks==1,"native boss quadrant"+feature+" has one original entrance lock");
                if(feature==1)check.accept(world.getBlockState(origin.offset(8,3,8)).is(R5Content.ELDRITCH_PORTAL.get()),"portal room creates actual travel portal in its obelisk");
                if(feature==6){
                    var items=world.getEntitiesOfClass(PermanentItemEntity.class,area);var guardians=world.getEntitiesOfClass(EldritchGuardian.class,area);
                    check.accept(items.size()==1&&items.getFirst().getItem().is(R5Content.RUNIC_KEY.get()),"key room contains one actual permanent Rune Key entity");
                    var item=items.getFirst();check.accept(item.position().distanceTo(new net.minecraft.world.phys.Vec3(cx*16+8.5,53.5,CZ*16+8.5))<1e-6,"key has original spawn position and zero initial motion");
                    check.accept(guardians.size()==3&&guardians.stream().allMatch(g->g.getRestrictRadius()==16&&g.getRestrictCenter().equals(origin.offset(8,2,8))),"normal key room guards use native guardian entities and original home radius16");guardians.forEach(g->g.setNoAi(true));
                    ((thaumcraft.mixin.ItemAgeAccessor)(Object)item).thaumcraft$setAge(item.lifespan-6);for(int i=0;i<3;i++)item.tick();check.accept(!item.isRemoved()&&item.getAge()<5,"key renews item lifetime before vanilla despawn");
                    check.accept(!item.hurt(world.damageSources().explosion(null,null),100)&&!item.isRemoved(),"permanent key is immune to explosion damage");
                    var nbt=new CompoundTag();item.save(nbt);var copy=net.minecraft.world.entity.EntityType.loadEntityRecursive(nbt,world,e->e);check.accept(copy instanceof PermanentItemEntity&&((PermanentItemEntity)copy).getItem().is(R5Content.RUNIC_KEY.get()),"native key entity type and contents round trip through entity NBT");copy.discard();
                    Files.writeString(PROOF,"{\"key\":\""+item.getUUID()+"\"}\n");
                }
                if(feature==13){check.accept(fibres>0&&WorldBiomes.effective(world,origin.offset(8,4,8)).is(WorldBiomes.TAINT),"tainted passage creates native visible fibres and exact biome overlay");check.accept(!world.getChunk(cx,CZ).getData(RoomBiomeWrites.COLUMNS.get()).isEmpty(),"tainted room keeps durable column attachment on its real chunk");}
                if(feature==14){var tile=world.getBlockEntity(origin.offset(8,4,8));check.accept(tile instanceof SpawnerBlockEntity,"spider passage has a real vanilla spawner block entity");var nbt=((SpawnerBlockEntity)tile).saveWithoutMetadata(world.registryAccess());check.accept(nbt.getCompound("SpawnData").getCompound("entity").getString("id").equals("thaumcraft:mind_spider"),"actual spawner data targets the native Mind Spider entity");}
            }
            var sink=new RoomWorldSink(world,new MazeCell(),new Random(0));world.setBlock(CONTROL,Blocks.STONE.defaultBlockState(),2);sink.place(CONTROL.getX(),CONTROL.getY(),CONTROL.getZ(),1,null);check.accept(world.getBlockState(CONTROL).is(Blocks.STONE),"conditional room bedrock does not overwrite an occupied position");
            sink.place(CONTROL.getX(),CONTROL.getY(),CONTROL.getZ(),99,null);check.accept(world.getBlockState(CONTROL).is(Blocks.BEDROCK),"original replace-bedrock code replaces occupied position");sink.place(CONTROL.getX(),CONTROL.getY(),CONTROL.getZ(),8,null);
            for(int code:new int[]{2,18,19,20,21})sink.place(CONTROL.getX(),CONTROL.getY(),CONTROL.getZ(),code,null);check.accept(world.getBlockState(CONTROL).is(R5Content.ELDRITCH_VOID.get()),"stone, rock, crust and trap placements preserve existing Eldritch Void");
            var door=R5Content.VOID_DOOR.get().defaultBlockState();world.setBlock(CONTROL,door,2);check.accept(door.getShape(world,CONTROL).isEmpty()&&!door.getCollisionShape(world,CONTROL).isEmpty()&&door.getDestroySpeed(world,CONTROL)==-1,"original void door is invisible to selection, solid to movement and unbreakable");
            var body=R5Content.ELDRITCH_BODY.get().defaultBlockState();check.accept(body.getRenderShape()==RenderShape.INVISIBLE&&!body.getCollisionShape(world,CONTROL).isEmpty()&&body.getLightEmission(world,CONTROL)==8,"original obelisk body is invisible, solid and emits light8");
            sink.raw(CONTROL.getX(),CONTROL.getY(),CONTROL.getZ(),1,8,2);sink.lock(CONTROL.getX(),CONTROL.getY(),CONTROL.getZ(),Direction.WEST);var lock=(EldritchLockBlockEntity)world.getBlockEntity(CONTROL);lock.count=17;lock.setChanged();
            net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.event.server.ServerStoppingEvent event)->{
                if(event.getServer()!=world.getServer())return;
                try{
                    var actual=(EldritchLockBlockEntity)world.getBlockEntity(CONTROL);
                    if(actual==null||actual.count<17||actual.count>18)throw new AssertionError("Unexpected lock countdown before final server save");
                    var proof=com.google.gson.JsonParser.parseString(Files.readString(PROOF)).getAsJsonObject();proof.addProperty("lock_count",actual.count);proof.addProperty("lock_facing",actual.facing);Files.writeString(PROOF,proof.toString());
                }catch(Exception e){throw new RuntimeException(e);}
            });
            check.accept(lock.renderBounds().getXsize()==5.5,"lock render bounds retain the original animated arm extent");
            int altarX=12000;while(LabyrinthData.get(level).inRange(altarX>>4,1000,31,31))altarX+=1024;var altarPos=new BlockPos(altarX,140,16000);level.setBlock(altarPos,R3Content.ELDRITCH_ALTAR.get().defaultBlockState(),2);var altar=(EldritchAltarBlockEntity)level.getBlockEntity(altarPos);var maze=LabyrinthData.get(level);int before=maze.snapshot().size();altar.installEye();check.accept(altar.eyes==1&&maze.snapshot().size()>before,"first ordinary installed eye allocates a real SavedData labyrinth before wand activation");level.removeBlock(altarPos,false);
        }finally{world.getServer().setDifficulty(difficulty,true);thaumcraft.config.ThaumcraftConfig.BIOME_CHANGE_MODE.set(mode);release(level);}
    }
    static void reload(ServerLevel level,BiConsumer<Boolean,String> check)throws Exception{
        var mode=thaumcraft.config.ThaumcraftConfig.BIOME_CHANGE_MODE.get();thaumcraft.config.ThaumcraftConfig.BIOME_CHANGE_MODE.set(thaumcraft.config.ThaumcraftConfig.BiomeChangeMode.TC4_1X1);try{var world=outer(level);var proof=com.google.gson.JsonParser.parseString(Files.readString(PROOF)).getAsJsonObject();var key=world.getEntity(UUID.fromString(proof.get("key").getAsString()));check.accept(key instanceof PermanentItemEntity&&!key.isRemoved()&&((PermanentItemEntity)key).getItem().is(R5Content.RUNIC_KEY.get()),"permanent native key survives an actual dedicated-server restart");
            var lock=(EldritchLockBlockEntity)world.getBlockEntity(CONTROL);check.accept(lock!=null&&loadedLockCount==proof.get("lock_count").getAsInt()&&loadedLockFacing==proof.get("lock_facing").getAsByte()&&lock.facing==Direction.WEST.ordinal()&&lock.count>=loadedLockCount&&lock.count<=loadedLockCount+(world.getGameTime()-loadedLockTime)&&lock.getBlockState().getValue(EldritchLockBlock.FACING)==Direction.WEST.ordinal(),"native lock reloads exact final-saved facing/countdown before ticking, then advances only with loaded server ticks");
            var p=new BlockPos((CX+24)*16+8,54,CZ*16+8);check.accept(WorldBiomes.effective(world,p).is(WorldBiomes.TAINT),"generated taint biome survives actual restart");check.accept(!world.getChunk(p).getData(RoomBiomeWrites.COLUMNS.get()).isEmpty(),"serialized chunk retains generated taint columns after actual restart");
        }finally{thaumcraft.config.ThaumcraftConfig.BIOME_CHANGE_MODE.set(mode);release(level);}
    }
    private OuterRoomApplicationVerification(){}
}
