package thaumcraft.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Sent only when a completed research scroll is successfully consumed. */
public record ResearchCompletePayload(String key) implements CustomPacketPayload {
    public static final Type<ResearchCompletePayload> TYPE=new Type<>(ResourceLocation.parse("thaumcraft:research_complete"));
    public static final StreamCodec<RegistryFriendlyByteBuf,ResearchCompletePayload> CODEC=StreamCodec.composite(ByteBufCodecs.STRING_UTF8,ResearchCompletePayload::key,ResearchCompletePayload::new);
    @Override public Type<ResearchCompletePayload> type(){return TYPE;}
}
