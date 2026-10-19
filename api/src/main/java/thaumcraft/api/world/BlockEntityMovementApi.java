package thaumcraft.api.world;

import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;

/** Identity-preserving block-entity relocation for devices that move existing blocks. */
public final class BlockEntityMovementApi {
    public interface Access {
        boolean loaded(ServerLevel level,BlockPos pos);
        boolean detach(ServerLevel level,BlockEntity entity);
        boolean relocate(ServerLevel level,BlockEntity entity,BlockPos destination);
    }
    private static volatile Access access;
    public static synchronized void install(Access backend){if(access!=null)throw new IllegalStateException("Block entity movement API already installed");access=Objects.requireNonNull(backend);}
    /** Checks an already available full chunk; never loads or generates one. */
    public static boolean loaded(ServerLevel level,BlockPos pos){var backend=access;return backend!=null&&level!=null&&pos!=null&&backend.loaded(level,pos);}
    /**
     * Detaches the exact registered object before its source block is removed.
     * This prevents inventory-dropping onRemove callbacks from finding a second
     * copy of its contents. The caller retains the object for relocate.
     */
    public static boolean detach(ServerLevel level,BlockEntity entity){var backend=access;return backend!=null&&level!=null&&entity!=null&&backend.detach(level,entity);}
    /**
     * Reattaches that same object after the caller places a compatible destination
     * block. Replaces the fresh placeholder created by native block placement.
     * Also detaches a still-registered source object when necessary. Does not move
     * block states, copy NBT, decide whether a passenger may move, or invoke its
     * optional integration callbacks. Both positions must remain loaded.
     * All mutations require the server thread.
     */
    public static boolean relocate(ServerLevel level,BlockEntity entity,BlockPos destination){var backend=access;return backend!=null&&level!=null&&entity!=null&&destination!=null&&backend.relocate(level,entity,destination.immutable());}
    private BlockEntityMovementApi(){}
}
