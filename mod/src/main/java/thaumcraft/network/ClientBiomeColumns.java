package thaumcraft.network;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.resources.ResourceLocation;
import thaumcraft.api.world.BiomeColumns;

/** Client mirror has no filesystem access and receives only server snapshots. */
public final class ClientBiomeColumns {
    private static UUID session;
    private static boolean enabled,global;
    private static final Map<ResourceLocation,BiomeColumns> DIMENSIONS=new HashMap<>();
    private static java.util.function.Consumer<BiomeColumnsPayload> listener=packet->{};
    public static void listener(java.util.function.Consumer<BiomeColumnsPayload> value) { listener=value; }
    public static boolean accept(BiomeColumnsPayload packet) {
        if(packet.reset()) { clear();session=packet.session();enabled=packet.enabled();global=packet.global();listener.accept(packet);return true; }
        if(!packet.session().equals(session)) return false;
        enabled=packet.enabled();global=packet.global();var columns=DIMENSIONS.computeIfAbsent(packet.dimension(),id->new BiomeColumns());
        if(packet.forget()) { columns.forget(packet.x(),packet.z());listener.accept(packet);return true; }
        boolean changed=columns.accept(packet.x(),packet.z(),packet.chunk());if(changed) listener.accept(packet);return changed;
    }
    public static String get(ResourceLocation dimension,int x,int z) { var columns=DIMENSIONS.get(dimension);return !enabled||columns==null?null:columns.get(x,z); }
    public static boolean global() { return enabled&&global; }
    public static void clear() { session=null;enabled=false;global=false;DIMENSIONS.clear(); }
    private ClientBiomeColumns() {}
}
