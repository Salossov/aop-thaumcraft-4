package thaumcraft.api.world;

import java.util.List;
import java.util.Objects;
import java.util.function.IntPredicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

/** Native block-item placement for tools that select a block state and pay from another inventory. */
public final class BlockPlacementApi {
    public record Part(BlockPos pos,BlockState state) {
        public Part { pos=pos.immutable();Objects.requireNonNull(state); }
    }
    public record Plan(BlockState state,int cost,List<Part> parts) {
        public Plan {
            Objects.requireNonNull(state);
            if(cost<1)throw new IllegalArgumentException("Positive item cost required");
            parts=List.copyOf(parts);
        }
    }
    public interface Access {
        @Nullable Plan plan(Player player,InteractionHand hand,BlockPos pos,Direction face,BlockState selected);
        boolean place(Player player,InteractionHand hand,BlockPos pos,Direction face,BlockState selected,IntPredicate debit);
    }
    private static volatile Access access;
    public static synchronized void install(Access backend){if(access!=null)throw new IllegalStateException("Block placement API already installed");access=Objects.requireNonNull(backend);}
    /**
     * Read-only preview in already loaded chunks. Placement direction and variant
     * are retained; connections, fluids, assembly halves and live state come from
     * native placement. Stacked blocks include their complete item cost. No block
     * entity data is copied. Returns null for unavailable or invalid placement.
     */
    @Nullable public static Plan plan(Player player,InteractionHand hand,BlockPos pos,Direction face,BlockState selected){
        var backend=access;return backend==null?null:backend.plan(player,hand,pos,face,selected);
    }
    /**
     * Replans on the server thread, invokes the registered BlockItem's place path,
     * then posts native placement protection events for all captured blocks.
     * Debit must atomically pay the supplied complete cost, or return false
     * without modifying stock. Rejection restores all block snapshots. The
     * caller owns chunk preparation and must not debit in advance.
     */
    public static boolean place(Player player,InteractionHand hand,BlockPos pos,Direction face,BlockState selected,IntPredicate debit){
        var backend=access;return backend!=null&&backend.place(player,hand,pos,face,selected,Objects.requireNonNull(debit));
    }
    private BlockPlacementApi(){}
}
