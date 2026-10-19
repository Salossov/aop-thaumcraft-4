package thaumcraft.api.research;

import java.util.*;
import net.minecraft.server.level.ServerPlayer;
import thaumcraft.api.book.BookCatalog;
import thaumcraft.api.knowledge.PlayerKnowledge;

/** The core installs the server adapter; client snapshots are replaced atomically on synchronization. */
public final class ResearchApi {
    public interface Access {PlayerKnowledge knowledge(ServerPlayer player);void sync(ServerPlayer player);boolean exists(ServerPlayer player,String research);boolean grant(ServerPlayer player,String research);
        default boolean completeAspect(ServerPlayer player,thaumcraft.api.aspects.Aspect aspect,short amount){throw new UnsupportedOperationException("Aspect completion is unavailable");}
        default Map<String,ResearchDefinition> definitions(ServerPlayer player){throw new UnsupportedOperationException("Server research catalog is unavailable");}
    }
    private static Access access;
    private record Snapshot(Map<String,ResearchDefinition> definitions,BookCatalog books,PlayerKnowledge knowledge){}
    private static volatile Snapshot snapshot=new Snapshot(Map.of(),BookCatalog.EMPTY,new PlayerKnowledge());
    public static void install(Access value){if(access!=null)throw new IllegalStateException("Research API already installed");access=Objects.requireNonNull(value);}
    public static PlayerKnowledge knowledge(ServerPlayer player){return access.knowledge(player).copy();}
    public static boolean knows(ServerPlayer player,String research){return research.isEmpty()||access.knowledge(player).completedResearch().contains(research);}
    public static boolean exists(ServerPlayer player,String research){return access.exists(player,research);}
    public static boolean grant(ServerPlayer player,String research){if(!player.server.isSameThread())throw new IllegalStateException("Server thread required");if(!access.exists(player,research))throw new IllegalArgumentException("Unknown research "+research);return access.grant(player,research);}
    /** TC4 completeAspect: discover directly and SET the pool, without prerequisite checks. */
    public static boolean completeAspect(ServerPlayer player,thaumcraft.api.aspects.Aspect aspect,short amount){
        if(!player.server.isSameThread())throw new IllegalStateException("Server thread required");
        return aspect!=null&&access.completeAspect(player,aspect,amount);
    }
    public static Map<String,ResearchDefinition> definitions(){return snapshot.definitions;}
    /** Effective server catalog, including dynamic requirements and config/tag filtering. */
    public static Map<String,ResearchDefinition> definitions(ServerPlayer player){
        if(!player.server.isSameThread())throw new IllegalStateException("Server thread required");
        return Collections.unmodifiableMap(new LinkedHashMap<>(access.definitions(player)));
    }
    public static BookCatalog books(){return snapshot.books;}
    public static PlayerKnowledge clientKnowledge(){return snapshot.knowledge.copy();}
    public static void accept(Map<String,ResearchDefinition> research,BookCatalog catalog,PlayerKnowledge state){snapshot=new Snapshot(Map.copyOf(research),Objects.requireNonNull(catalog),state.copy());}
    private ResearchApi(){}
}
