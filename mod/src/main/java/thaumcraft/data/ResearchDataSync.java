package thaumcraft.data;

import com.google.gson.Gson;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import thaumcraft.api.research.ResearchDefinition;

public final class ResearchDataSync {
    private static final Gson GSON=new Gson();
    public static void encode(CompoundTag target,Map<String,ResearchDefinition> definitions) {
        var list=new ListTag();definitions.values().stream().sorted(java.util.Comparator.comparing(ResearchDefinition::key)).forEach(d->list.add(StringTag.valueOf(GSON.toJson(d))));target.put("researchDefinitions",list);
    }
    public static Map<String,ResearchDefinition> decode(CompoundTag data) {
        var result=new LinkedHashMap<String,ResearchDefinition>();for(var tag:data.getList("researchDefinitions",Tag.TAG_STRING)) { var d=GSON.fromJson(tag.getAsString(),ResearchDefinition.class);result.put(d.key(),d); }return Map.copyOf(result);
    }
    private ResearchDataSync() {}
}
