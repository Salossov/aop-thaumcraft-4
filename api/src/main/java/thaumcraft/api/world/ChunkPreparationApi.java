package thaumcraft.api.world;

import java.util.Collection;
import java.util.Objects;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;

/** Temporary, owner-bound FULL chunk preparation without waiting on generation. */
public final class ChunkPreparationApi {
    public interface Request extends AutoCloseable {
        /** Submits bounded work and polls availability. Never joins a chunk future. */
        boolean advance(int coldBudget);
        boolean closed();
        int submitted();
        int total();
        /** Idempotently releases every ticket belonging to this request. */
        @Override void close();
    }
    public interface Access {
        Request prepare(BlockEntity owner,Collection<ChunkPos> positions);
        default Request prepare(ServerPlayer owner,Collection<ChunkPos> positions){throw new UnsupportedOperationException("Player chunk preparation unavailable");}
        default Request prepare(BlockEntity owner,ServerLevel destination,Collection<ChunkPos> positions){
            if(owner.getLevel()==destination)return prepare(owner,positions);
            throw new UnsupportedOperationException("Cross-level chunk preparation unavailable");
        }
        default Request prepare(ServerPlayer owner,ServerLevel destination,Collection<ChunkPos> positions){
            if(owner.level()==destination)return prepare(owner,positions);
            throw new UnsupportedOperationException("Cross-level player chunk preparation unavailable");
        }
    }
    private static volatile Access access;
    public static synchronized void install(Access backend){if(access!=null)throw new IllegalStateException("Chunk preparation API already installed");access=Objects.requireNonNull(backend);}
    /**
     * Requires a registered server-side owner on the server thread. The request
     * expires when its owner is removed, moved, unloaded, or stops block ticking,
     * and when the level/server closes. It does not force the owner to tick.
     * The caller must also close on completion or cancellation. Holds FULL chunks
     * only; does not force entity or block ticking in the requested area.
     */
    public static Request prepare(BlockEntity owner,Collection<ChunkPos> positions){
        var backend=access;if(backend==null)throw new IllegalStateException("Chunk preparation API unavailable");
        return backend.prepare(Objects.requireNonNull(owner),Objects.requireNonNull(positions));
    }
    /** Server-thread request owned by this connected, living player in the current
     * level. Expires on logout, death, replacement, dimension change or level close.
     * Callers must additionally cancel when their bound item/action becomes stale.
     * Walking does not pin the player's former location or force it to tick.
     */
    public static Request prepare(ServerPlayer owner,Collection<ChunkPos> positions){
        var backend=access;if(backend==null)throw new IllegalStateException("Chunk preparation API unavailable");
        return backend.prepare(Objects.requireNonNull(owner),Objects.requireNonNull(positions));
    }
    /** Prepare FULL columns in another level of the same server while keeping
     * ownership in the original level. Either level closing expires the request;
     * owner removal/movement/unload or stopping block ticks also expires it.
     */
    public static Request prepare(BlockEntity owner,ServerLevel destination,Collection<ChunkPos> positions){
        var backend=access;if(backend==null)throw new IllegalStateException("Chunk preparation API unavailable");
        return backend.prepare(Objects.requireNonNull(owner),Objects.requireNonNull(destination),Objects.requireNonNull(positions));
    }
    /** Cross-level preparation bound to this connected player in their original
     * level. Leaving that level cancels preparation, so callers close after use.
     */
    public static Request prepare(ServerPlayer owner,ServerLevel destination,Collection<ChunkPos> positions){
        var backend=access;if(backend==null)throw new IllegalStateException("Chunk preparation API unavailable");
        return backend.prepare(Objects.requireNonNull(owner),Objects.requireNonNull(destination),Objects.requireNonNull(positions));
    }
    private ChunkPreparationApi(){}
}
