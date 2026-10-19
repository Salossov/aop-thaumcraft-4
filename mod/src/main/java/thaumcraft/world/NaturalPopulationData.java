package thaumcraft.world;

import java.util.*;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

/** Pending population belongs to the dimension and survives an ordinary save/restart. */
public final class NaturalPopulationData extends SavedData {
    private static final Factory<NaturalPopulationData> FACTORY=new Factory<>(NaturalPopulationData::new,NaturalPopulationData::load);
    public record Request(long chunk,long seed,boolean fresh,String version){}
    private final LinkedHashMap<Long,Request> pending=new LinkedHashMap<>();
    private final Set<net.minecraft.core.BlockPos> temples=new LinkedHashSet<>();
    public void rememberTemple(net.minecraft.core.BlockPos pos){if(temples.add(pos.immutable()))setDirty();}
    public net.minecraft.core.BlockPos nearestTemple(net.minecraft.core.BlockPos origin){return temples.stream().min(Comparator.comparingDouble(pos->pos.distSqr(origin))).orElse(null);}
    public static NaturalPopulationData get(ServerLevel level){return level.getDataStorage().computeIfAbsent(FACTORY,"thaumcraft_population");}
    public List<Request> requests(){return List.copyOf(pending.values());}
    public boolean enqueue(Request request){if(pending.containsKey(request.chunk()))return false;pending.put(request.chunk(),request);setDirty();return true;}
    public void completed(long chunk){if(pending.remove(chunk)!=null)setDirty();}
    public static NaturalPopulationData load(CompoundTag root,HolderLookup.Provider registries){
        if(root.getInt("schema")!=1)throw new IllegalArgumentException("Unknown population queue schema");
        var data=new NaturalPopulationData();
        for(long pos:root.getLongArray("temples"))data.temples.add(net.minecraft.core.BlockPos.of(pos));
        for(var value:root.getList("pending",Tag.TAG_COMPOUND)){
            var row=(CompoundTag)value;var request=new Request(row.getLong("chunk"),row.getLong("seed"),row.getBoolean("fresh"),row.getString("version"));
            if(data.pending.putIfAbsent(request.chunk(),request)!=null)throw new IllegalArgumentException("Duplicate population request");
        }
        return data;
    }
    @Override public CompoundTag save(CompoundTag root,HolderLookup.Provider registries){
        root.putInt("schema",1);root.putLongArray("temples",temples.stream().mapToLong(net.minecraft.core.BlockPos::asLong).toArray());var rows=new ListTag();
        for(var request:pending.values()){var row=new CompoundTag();row.putLong("chunk",request.chunk());row.putLong("seed",request.seed());row.putBoolean("fresh",request.fresh());row.putString("version",request.version());rows.add(row);}
        root.put("pending",rows);return root;
    }
}
