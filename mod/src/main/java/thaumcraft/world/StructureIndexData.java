package thaumcraft.world;

import java.util.*;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

/** Generated TC4 structure positions per dimension, for QA locate. Survives save/restart. */
public final class StructureIndexData extends SavedData {
    private static final Factory<StructureIndexData> FACTORY=new Factory<>(StructureIndexData::new,StructureIndexData::load);
    public enum Kind { MOUND, HILLTOP, RING_ALTAR, SPIDER_TREE, CULTIST_RING, TOTEM }
    public record Entry(Kind kind,int x,int y,int z){}
    private final Map<ResourceKey<Level>,List<Entry>> entries=new HashMap<>();
    public static StructureIndexData get(ServerLevel level){return level.getDataStorage().computeIfAbsent(FACTORY,"thaumcraft_structures");}
    public void record(ServerLevel level,Kind kind,int x,int y,int z){
        var rows=entries.computeIfAbsent(level.dimension(),d->new ArrayList<>());var entry=new Entry(kind,x,y,z);if(!rows.contains(entry)){rows.add(entry);setDirty();}
    }
    public Entry nearest(ServerLevel level,Kind kind,int x,int z){
        Entry best=null;double distance=Double.MAX_VALUE;
        for(var entry:entries.getOrDefault(level.dimension(),List.of())){
            if(entry.kind()!=kind)continue;
            double dx=entry.x()-x,dz=entry.z()-z,d=dx*dx+dz*dz;
            if(d<distance){distance=d;best=entry;}
        }
        return best;
    }
    public static StructureIndexData load(CompoundTag root,HolderLookup.Provider registries){
        if(root.getInt("schema")!=1)throw new IllegalArgumentException("Unknown structure index schema");
        var data=new StructureIndexData();
        for(var key:root.getAllKeys()){
            if(key.equals("schema"))continue;
            var rows=new ArrayList<Entry>();
            for(var value:root.getList(key,Tag.TAG_COMPOUND)){
                var row=(CompoundTag)value;
                rows.add(new Entry(Kind.valueOf(row.getString("kind")),row.getInt("x"),row.getInt("y"),row.getInt("z")));
            }
            data.entries.put(ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION,ResourceLocation.parse(key)),rows);
        }
        return data;
    }
    @Override public CompoundTag save(CompoundTag root,HolderLookup.Provider registries){
        root.putInt("schema",1);
        for(var dimension:entries.entrySet()){
            var rows=new ListTag();
            for(var entry:dimension.getValue()){var row=new CompoundTag();row.putString("kind",entry.kind().name());row.putInt("x",entry.x());row.putInt("y",entry.y());row.putInt("z",entry.z());rows.add(row);}
            root.put(dimension.getKey().location().toString(),rows);
        }
        return root;
    }
}
