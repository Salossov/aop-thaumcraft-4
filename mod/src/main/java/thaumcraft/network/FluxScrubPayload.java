package thaumcraft.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** A server-owned successful scrub cues the original purple sparkle at the removed quantum. */
public record FluxScrubPayload(BlockPos position) implements CustomPacketPayload {
    public static final Type<FluxScrubPayload> TYPE=new Type<>(ResourceLocation.parse("thaumcraft:flux_scrub"));
    public static final StreamCodec<RegistryFriendlyByteBuf,FluxScrubPayload> CODEC=StreamCodec.composite(BlockPos.STREAM_CODEC,FluxScrubPayload::position,FluxScrubPayload::new);
    @Override public Type<FluxScrubPayload> type(){return TYPE;}
}
