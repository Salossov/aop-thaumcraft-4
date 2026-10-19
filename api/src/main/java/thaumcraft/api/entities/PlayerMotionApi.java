package thaumcraft.api.entities;

import java.util.Objects;
import java.util.function.BiConsumer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

/** Authoritative magical launch, including client velocity, fall and floating-counter synchronization. */
public final class PlayerMotionApi {
    private static BiConsumer<ServerPlayer, Vec3> access;
    public static void install(BiConsumer<ServerPlayer, Vec3> implementation) {
        if (access != null) throw new IllegalStateException("Player motion API already installed");
        access = Objects.requireNonNull(implementation);
    }
    public static void launch(ServerPlayer player, Vec3 velocity) {
        if (!player.server.isSameThread()) throw new IllegalStateException("Server thread required");
        if (!Double.isFinite(velocity.x) || !Double.isFinite(velocity.y) || !Double.isFinite(velocity.z)) throw new IllegalArgumentException("Invalid velocity");
        access.accept(player, velocity);
    }
    private PlayerMotionApi() { }
}
