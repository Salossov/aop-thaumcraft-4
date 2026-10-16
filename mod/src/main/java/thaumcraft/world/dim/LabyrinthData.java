package thaumcraft.world.dim;

import java.util.*;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

/** Shared by dimensions of one server, owned and saved by that server's overworld. */
public final class LabyrinthData extends SavedData {
    public static final String ID="thaumcraft_labyrinth";
    private static final Factory<LabyrinthData> FACTORY=new Factory<>(LabyrinthData::new,LabyrinthData::load);
    public record Position(int x,int z){}
    public record Generated(long requestedSeed,long acceptedSeed,int attempts,int[][] grid){}
    public record PlanSnapshot(long revision,Map<Position,Short> cells){}
    private final Map<Position,Short> cells=new HashMap<>();
    private long revision;
    private static final Map<net.minecraft.server.MinecraftServer,LabyrinthData> SERVERS=Collections.synchronizedMap(new WeakHashMap<>());
    public static LabyrinthData get(ServerLevel level){
        var server=level.getServer();var cached=SERVERS.get(server);if(cached!=null)return cached;
        if(!server.isSameThread())throw new IllegalStateException("Labyrinth SavedData must be loaded before chunk generation");
        var data=server.overworld().getDataStorage().computeIfAbsent(FACTORY,ID);SERVERS.put(server,data);return data;
    }
    /** Called while the overworld loads, before asynchronous Outer Lands decoration can start. */
    public static void register(){
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.event.level.LevelEvent.Load event)->{if(event.getLevel() instanceof ServerLevel level&&level.dimension().equals(net.minecraft.world.level.Level.OVERWORLD))SERVERS.put(level.getServer(),level.getDataStorage().computeIfAbsent(FACTORY,ID));});
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.event.server.ServerStoppedEvent event)->SERVERS.remove(event.getServer()));
    }
    public synchronized short packed(int x,int z){return cells.getOrDefault(new Position(x,z),(short)0);}
    public synchronized MazeCell cell(int x,int z){var raw=cells.get(new Position(x,z));return raw==null?null:new MazeCell(raw);}
    public synchronized Map<Position,Short> snapshot(){return Map.copyOf(cells);}
    public synchronized long revision(){return revision;}
    public synchronized PlanSnapshot planSnapshot(){return new PlanSnapshot(revision,Map.copyOf(cells));}
    public synchronized void put(int x,int z,short value){var prior=cells.put(new Position(x,z),value);if(prior==null||prior!=value){revision++;setDirty();}}
    public synchronized void remove(int x,int z){if(cells.remove(new Position(x,z))!=null){revision++;setDirty();}}
    public synchronized boolean inRange(int chunkX,int chunkZ,int width,int height){
        if(width<0||height<0)throw new IllegalArgumentException("Negative maze range");
        int minX=Math.subtractExact(chunkX,width),maxX=Math.addExact(chunkX,width),minZ=Math.subtractExact(chunkZ,height),maxZ=Math.addExact(chunkZ,height);
        for(var p:cells.keySet())if(p.x>=minX&&p.x<=maxX&&p.z>=minZ&&p.z<=maxZ)return true;
        return false;
    }
    /** Returns null for an overlap, without writing a partial maze or touching existing cells. */
    public synchronized Generated generate(int chunkX,int chunkZ,int width,int height,long seed){
        if(width<11||width>29||height<11||height>29||(width&1)==0||(height&1)==0)throw new IllegalArgumentException("TC4 maze sizes must be odd, 11..29");
        if(inRange(chunkX,chunkZ,width,height))return null;
        int col=Math.subtractExact(chunkX,1+width/2),row=Math.subtractExact(chunkZ,1+height/2);
        long current=seed;int attempts=0;LegacyMazeGenerator maze;
        do{if(++attempts>1000)throw new IllegalStateException("Could not generate original TC4 maze");maze=new LegacyMazeGenerator(width,height,current++);}while(!maze.generate());
        for(int x=0;x<width;x++)for(int z=0;z<height;z++)if(maze.grid[z][x]>0)cells.put(new Position(Math.addExact(col,x),Math.addExact(row,z)),(short)maze.grid[z][x]);
        revision++;setDirty();return new Generated(seed,current-1,attempts,maze.grid);
    }
    public static LabyrinthData load(CompoundTag root,HolderLookup.Provider registries){
        if(root.getInt("schema")!=1)throw new IllegalArgumentException("Unsupported TC4 labyrinth schema");var result=new LabyrinthData();
        for(var value:root.getList("cells",Tag.TAG_COMPOUND)){var cell=(CompoundTag)value;var position=new Position(cell.getInt("x"),cell.getInt("z"));if(result.cells.put(position,cell.getShort("cell"))!=null)throw new IllegalArgumentException("Duplicate labyrinth cell "+position);}
        return result;
    }
    @Override public synchronized CompoundTag save(CompoundTag root,HolderLookup.Provider registries){
        root.putInt("schema",1);var list=new ListTag();
        cells.entrySet().stream().sorted(Comparator.<Map.Entry<Position,Short>>comparingInt(e->e.getKey().x).thenComparingInt(e->e.getKey().z)).forEach(entry->{if(entry.getValue()<=0)return;var tag=new CompoundTag();tag.putInt("x",entry.getKey().x);tag.putInt("z",entry.getKey().z);tag.putShort("cell",entry.getValue());list.add(tag);});root.put("cells",list);return root;
    }
}
