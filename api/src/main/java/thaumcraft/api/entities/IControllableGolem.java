package thaumcraft.api.entities;

import java.util.List;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

/**
 * Implemented by the live Thaumcraft golem entity. Addons may inspect the entity's
 * ordinary LivingEntity properties and use this contract for its work configuration.
 * Mutations require the owning server thread; they affect AI and entity persistence.
 */
public interface IControllableGolem {
    /** Canonical lower-case material name, for example {@code thaumium}. */
    String golemMaterial();
    /** Legacy core index, or -1 for a golem without a core. */
    int core();
    /** Original decoration characters, in their installed order. */
    String decoration();
    BlockPos home();
    Direction homeFace();
    /** An immutable snapshot, in the golem's marker order. */
    List<Marker> golemMarkers();
    /** Changes the actual home and the legacy 35-block home restriction. */
    void setGolemHome(BlockPos position, Direction face);
    /** Replaces the actual AI markers atomically, preserving order and duplicates. */
    void replaceGolemMarkers(List<Marker> markers);

    record Marker(ResourceKey<Level> dimension, BlockPos position, Direction face, int color) {
        public Marker {
            Objects.requireNonNull(dimension, "dimension");
            position = Objects.requireNonNull(position, "position").immutable();
            Objects.requireNonNull(face, "face");
            if (color < -1 || color > 15) throw new IllegalArgumentException("Marker color must be -1..15");
        }
    }
}
