package thaumcraft.api.research;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

/** Server-side research sharing. UUID lookup uses the same world store as online players. */
public final class ResearchSharingApi {
    public interface Access {
        List<String> completed(MinecraftServer server, UUID author);
        boolean complete(ServerPlayer recipient, String key);
    }
    private static Access access;
    public static void install(Access value) {
        if(access!=null)throw new IllegalStateException("Research sharing API already installed");
        access=Objects.requireNonNull(value);
    }
    /** Immutable current snapshot, including unknown/in-progress keys retained by the original. */
    public static List<String> completed(MinecraftServer server,UUID author) {
        if(!server.isSameThread())throw new IllegalStateException("Server thread required");
        return List.copyOf(access.completed(server,Objects.requireNonNull(author)));
    }
    /** Original completeResearch semantics: no prerequisite/catalog check or sibling expansion. */
    public static boolean complete(ServerPlayer recipient,String key) {
        if(!recipient.server.isSameThread())throw new IllegalStateException("Server thread required");
        return access.complete(recipient,Objects.requireNonNull(key));
    }
    private ResearchSharingApi(){}
}
