package thaumcraft.api.world;

import java.util.Collection;
import java.util.Objects;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;

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
    public interface Access { Request prepare(BlockEntity owner,Collection<ChunkPos> positions); }
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
    private ChunkPreparationApi(){}
}
