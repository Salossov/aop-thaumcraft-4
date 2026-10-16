package thaumcraft.knowledge;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import thaumcraft.api.knowledge.PlayerKnowledge;

public final class KnowledgeNbt {
    public static CompoundTag encode(PlayerKnowledge state) {
        var out=new CompoundTag();out.putLong("revision",state.revision());
        out.put("research",strings(state.completedResearch()));
        var pools=new CompoundTag();state.aspectPools().forEach(pools::putInt);out.put("aspectPools",pools);
        for(var kind:PlayerKnowledge.ScanKind.values()) out.put("scanned"+kind.name(),strings(state.scanned(kind)));
        var warp=new CompoundTag();warp.putInt("permanent",state.permanentWarp());warp.putInt("sticky",state.stickyWarp());
        warp.putInt("temporary",state.temporaryWarp());warp.putInt("counter",state.warpCounter());out.put("warp",warp);return out;
    }
    public static PlayerKnowledge decode(CompoundTag tag) {
        var state=new PlayerKnowledge();state.revision(tag.getLong("revision"));
        tag.getList("research",Tag.TAG_STRING).forEach(x->state.completeResearch(x.getAsString()));
        var pools=tag.getCompound("aspectPools");for(var key:pools.getAllKeys()) state.setPool(key,pools.getInt(key));
        for(var kind:PlayerKnowledge.ScanKind.values()) tag.getList("scanned"+kind.name(),Tag.TAG_STRING).forEach(x->state.recordScan(kind,x.getAsString()));
        var warp=tag.getCompound("warp");state.setWarp(warp.getInt("permanent"),warp.getInt("sticky"),warp.getInt("temporary"),warp.getInt("counter"));return state;
    }
    private static ListTag strings(Iterable<String> values) { var tag=new ListTag();values.forEach(x->tag.add(StringTag.valueOf(x)));return tag; }
    private KnowledgeNbt() {}
}
