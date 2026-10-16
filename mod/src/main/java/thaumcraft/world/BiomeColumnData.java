package thaumcraft.world;

import java.util.HashMap;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import thaumcraft.api.world.BiomeColumns;

/** One compressed NBT file per dimension, managed by the world's save lifecycle. */
public final class BiomeColumnData extends SavedData {
    public static final int SCHEMA=1;
    private static final Factory<BiomeColumnData> FACTORY=new Factory<>(BiomeColumnData::new,BiomeColumnData::load);
    public final BiomeColumns columns=new BiomeColumns();
    public static BiomeColumnData get(ServerLevel level) { return level.getDataStorage().computeIfAbsent(FACTORY,"thaumcraft_biome_columns"); }
    public static BiomeColumnData load(CompoundTag root,HolderLookup.Provider registries) {
        if(root.getInt("schema")!=SCHEMA) throw new IllegalArgumentException("Unsupported Thaumcraft biome columns schema: "+root.getInt("schema"));
        var data=new BiomeColumnData();
        for(var tag:root.getList("chunks",Tag.TAG_COMPOUND)) {
            var chunk=(CompoundTag)tag;var cells=new HashMap<Integer,String>();
            for(var entry:chunk.getList("cells",Tag.TAG_COMPOUND)) {
                var cell=(CompoundTag)entry;int index=cell.getInt("index");
                var biome=net.minecraft.resources.ResourceLocation.parse(cell.getString("biome"));
                if(cells.put(index,biome.toString())!=null) throw new IllegalArgumentException("Duplicate biome column");
            }
            if(!data.columns.accept(chunk.getInt("x"),chunk.getInt("z"),new BiomeColumns.Chunk(chunk.getLong("revision"),cells))) throw new IllegalArgumentException("Duplicate biome chunk");
        }
        return data;
    }
    @Override public CompoundTag save(CompoundTag root,HolderLookup.Provider registries) {
        root.putInt("schema",SCHEMA);var chunks=new ListTag();
        columns.chunks().forEach((key,value)->{
            var chunk=new CompoundTag();chunk.putInt("x",(int)(long)key);chunk.putInt("z",(int)(key>>32));chunk.putLong("revision",value.revision());
            var cells=new ListTag();value.cells().forEach((index,biome)->{var cell=new CompoundTag();cell.putInt("index",index);cell.putString("biome",biome);cells.add(cell);});
            chunk.put("cells",cells);chunks.add(chunk);
        });root.put("chunks",chunks);return root;
    }
}
