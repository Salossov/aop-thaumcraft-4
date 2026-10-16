package thaumcraft.api.world;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/** Sparse, exact block columns. Native quart biomes are deliberately separate. */
public final class BiomeColumns {
    public record Chunk(long revision, Map<Integer,String> cells) {
        public Chunk {
            if(revision<0||cells.size()>256) throw new IllegalArgumentException("Invalid biome chunk");
            cells=Map.copyOf(cells);
            for(var cell:cells.entrySet()) {
                if(cell.getKey()<0||cell.getKey()>255||cell.getValue().isBlank()) throw new IllegalArgumentException("Invalid biome column");
            }
        }
    }
    private final Map<Long,Chunk> chunks=new java.util.concurrent.ConcurrentHashMap<>();
    public static long chunkKey(int x,int z) { return (x&0xffffffffL)|((z&0xffffffffL)<<32); }
    public static int cell(int x,int z) { return (x&15)|((z&15)<<4); }
    public String get(int x,int z) { var chunk=chunks.get(chunkKey(x>>4,z>>4));return chunk==null?null:chunk.cells.get(cell(x,z)); }
    public Chunk chunk(int x,int z) { return chunks.getOrDefault(chunkKey(x,z),new Chunk(0,Map.of())); }
    public Map<Long,Chunk> chunks() { return Map.copyOf(chunks); }
    public boolean set(int x,int z,String biome) {
        Objects.requireNonNull(biome);var old=chunk(x>>4,z>>4);int cell=cell(x,z);
        if(biome.equals(old.cells.get(cell))) return false;
        var cells=new HashMap<>(old.cells);cells.put(cell,biome);
        chunks.put(chunkKey(x>>4,z>>4),new Chunk(Math.incrementExact(old.revision),cells));return true;
    }
    /** Full per-chunk replacement; older network snapshots cannot undo new changes. */
    public boolean accept(int x,int z,Chunk incoming) {
        long key=chunkKey(x,z);var old=chunks.get(key);
        if(old!=null&&incoming.revision<=old.revision) return false;
        chunks.put(key,incoming);return true;
    }
    public void forget(int x,int z) { chunks.remove(chunkKey(x,z)); }
    public void clear() { chunks.clear(); }
}
