package thaumcraft.api.world;

import java.util.Objects;
import java.util.function.BooleanSupplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/** Protected single-cell transformations with rollback and deferred native item/XP drops. */
public final class BlockReplacementApi {
    @FunctionalInterface public interface Access {
        boolean replace(ServerPlayer player,ItemStack tool,BlockPos pos,Direction face,
                        BlockState expected,BlockState replacement,BooleanSupplier payment);
    }
    private static volatile Access access;
    public static synchronized void install(Access implementation){
        if(access!=null)throw new IllegalStateException("Block replacement API already installed");
        access=Objects.requireNonNull(implementation);
    }
    /**
     * Server-thread only. Prepare the target and its support/notification area
     * with ChunkPreparationApi first; this operation never waits for generation.
     * The current state must still equal expected. Protection/support rejection
     * leaves the old block and its entity data intact and discards provisional
     * native item/experience drops. Payment runs once after validation; it must
     * atomically return false without debit or return true after debit.
     * A successful transformation publishes the deferred drops once.
     */
    public static boolean replace(ServerPlayer player,ItemStack tool,BlockPos pos,Direction face,
                                  BlockState expected,BlockState replacement,BooleanSupplier payment){
        Objects.requireNonNull(player);Objects.requireNonNull(tool);Objects.requireNonNull(pos);Objects.requireNonNull(face);
        Objects.requireNonNull(expected);Objects.requireNonNull(replacement);Objects.requireNonNull(payment);
        if(!player.server.isSameThread())throw new IllegalStateException("Server thread required");
        var backend=access;if(backend==null)throw new IllegalStateException("Block replacement API unavailable");
        return backend.replace(player,tool,pos.immutable(),face,expected,replacement,payment);
    }
    private BlockReplacementApi(){}
}
