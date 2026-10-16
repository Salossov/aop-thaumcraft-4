package thaumcraft.mixin;

import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ServerGamePacketListenerImpl.class)
public interface FloatingCounterAccessor {
    @Accessor("aboveGroundTickCount") void thaumcraft$resetFloatingTicks(int value);
}
