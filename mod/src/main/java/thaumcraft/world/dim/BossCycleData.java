package thaumcraft.world.dim;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.saveddata.SavedData;

/** Original BossMapData counter belongs to the same world storage as its maze. */
public final class BossCycleData extends SavedData {
    private static final Factory<BossCycleData> FACTORY=new Factory<>(BossCycleData::new,BossCycleData::load);
    private int count;
    public static BossCycleData get(ServerLevel level){return level.getServer().overworld().getDataStorage().computeIfAbsent(FACTORY,"thaumcraft_boss_cycle");}
    public int count(){return count;}
    /** Preserve original int overflow, quarter-chance extra increment and modulo order. */
    public int next(RandomSource random){++count;if(random.nextFloat()<.25f)++count;setDirty();return count%4;}
    public static BossCycleData load(CompoundTag tag,HolderLookup.Provider registries){var data=new BossCycleData();data.count=tag.getInt("bossCount");return data;}
    @Override public CompoundTag save(CompoundTag tag,HolderLookup.Provider registries){tag.putInt("bossCount",count);return tag;}
}
