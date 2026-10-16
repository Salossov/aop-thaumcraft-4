package thaumcraft.world.dim.rooms;

import java.lang.reflect.Proxy;
import java.util.*;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import net.minecraft.core.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.material.FluidState;
import thaumcraft.blocks.*;
import thaumcraft.registry.*;
import thaumcraft.entities.R3Entities;

/** Virtual original room world: no chunk reads, entity creation or server writes while planning. */
public final class RoomPlan implements BlockGetter {
    private record Placement(BlockState state,int flags){}
    private record Lock(BlockPos pos,int facing){}
    private record Key(BlockPos pos,List<BlockPos> guards){}
    private final Long2ObjectLinkedOpenHashMap<Placement> blocks=new Long2ObjectLinkedOpenHashMap<>();
    private final List<Lock> locks=new ArrayList<>();
    private final List<Key> keys=new ArrayList<>();
    private final Set<BlockPos> spawners=new LinkedHashSet<>(),taint=new LinkedHashSet<>();
    private Map<ChunkPos,long[]> chunks;
    private List<Placement> palette;
    // Native shape updates only need terrain reads here. Unsupported engine operations fail explicitly.
    private final LevelAccessor shapes=(LevelAccessor)Proxy.newProxyInstance(RoomPlan.class.getClassLoader(),new Class<?>[]{LevelAccessor.class},(proxy,method,args)->switch(method.getName()){
        case "getBlockState"->getBlockState((BlockPos)args[0]);
        case "getFluidState"->getFluidState((BlockPos)args[0]);
        case "getBlockEntity"->null;
        case "getHeight"->128;
        case "getMinBuildHeight"->0;
        case "getMaxBuildHeight"->128;
        case "isClientSide"->false;
        case "toString"->"TC4 virtual room shape access";
        default->throw new UnsupportedOperationException("Room shape requested engine operation: "+method);
    });
    @Override public BlockState getBlockState(BlockPos pos){var value=blocks.get(pos.asLong());return value==null?Blocks.AIR.defaultBlockState():value.state;}
    @Override public FluidState getFluidState(BlockPos pos){return getBlockState(pos).getFluidState();}
    @Override public BlockEntity getBlockEntity(BlockPos pos){return null;}
    @Override public int getHeight(){return 128;}
    @Override public int getMinBuildHeight(){return 0;}
    LevelAccessor shapeAccess(){return shapes;}
    void put(BlockPos pos,BlockState state,int flags){if(pos.getY()<0||pos.getY()>=128)throw new IllegalArgumentException("Room outside original height: "+pos);blocks.put(pos.asLong(),new Placement(state,flags));}
    void lock(BlockPos pos,int facing){locks.add(new Lock(pos.immutable(),facing));}
    void key(BlockPos pos,List<BlockPos> guards){keys.add(new Key(pos.immutable(),List.copyOf(guards)));}
    void spawner(BlockPos pos){spawners.add(pos.immutable());}
    void taint(BlockPos pos){taint.add(pos.immutable());}
    /** Release the virtual world after compilation; retained chunks contain only palette-coded runs. */
    void freeze(){
        var ids=new HashMap<Placement,Integer>();var values=new ArrayList<Placement>();values.add(null);
        var dense=new HashMap<ChunkPos,int[]>();
        for(var entry:blocks.long2ObjectEntrySet()){
            var pos=BlockPos.of(entry.getLongKey());var value=entry.getValue();int id=ids.computeIfAbsent(value,unused->{values.add(value);return values.size()-1;});
            var buffer=dense.computeIfAbsent(new ChunkPos(pos),unused->new int[32768]);buffer[(pos.getY()<<8)|((pos.getZ()&15)<<4)|(pos.getX()&15)]=id;
        }
        var packed=new HashMap<ChunkPos,long[]>();
        for(var entry:dense.entrySet()){
            var runs=new it.unimi.dsi.fastutil.longs.LongArrayList();var buffer=entry.getValue();
            for(int start=0;start<buffer.length;){int id=buffer[start],end=start+1;while(end<buffer.length&&buffer[end]==id)end++;if(id!=0)runs.add(((long)start<<32)|((long)(end-start)<<16)|id);start=end;}
            packed.put(entry.getKey(),runs.toLongArray());
        }
        chunks=Map.copyOf(packed);palette=Collections.unmodifiableList(values);blocks.clear();blocks.trim();
    }
    private static boolean inside(BlockPos p,ChunkPos chunk){return p.getX()>>4==chunk.x&&p.getZ()>>4==chunk.z;}
    public void apply(WorldGenLevel world,ChunkAccess chunk){
        var area=chunk.getPos();
        if(chunks==null)throw new IllegalStateException("Room plan was not frozen");
        var runs=chunks.get(area);if(runs==null)return;
        for(long run:runs){int start=(int)(run>>>32),length=(int)((run>>>16)&65535);var placement=palette.get((int)(run&65535));for(int index=start;index<start+length;index++)world.setBlock(new BlockPos(area.getMinBlockX()+(index&15),index>>>8,area.getMinBlockZ()+((index>>>4)&15)),placement.state,placement.flags);}
        for(var lock:locks)if(inside(lock.pos,area)&&world.getBlockEntity(lock.pos) instanceof EldritchLockBlockEntity tile){tile.facing=(byte)lock.facing;chunk.setUnsaved(true);}
        for(var pos:spawners)if(inside(pos,area)&&world.getBlockEntity(pos) instanceof SpawnerBlockEntity tile){tile.getSpawner().setEntityId(R3Entities.MIND_SPIDER.get(),world instanceof net.minecraft.server.level.ServerLevel?world.getLevel():null,world.getRandom(),pos);chunk.setUnsaved(true);}
        for(var key:keys)if(inside(key.pos,area))RoomWorldSink.spawnKeyRoom(world,key.pos,key.guards);
        for(var pos:taint)if(inside(pos,area))RoomBiomeWrites.put(world,pos.getX(),pos.getZ());
    }
}
