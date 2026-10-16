package thaumcraft.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record GuardianMistPayload() implements CustomPacketPayload {
    public static final Type<GuardianMistPayload> TYPE=new Type<>(ResourceLocation.parse("thaumcraft:guardian_mist"));
    public static final StreamCodec<RegistryFriendlyByteBuf,GuardianMistPayload> CODEC=StreamCodec.unit(new GuardianMistPayload());
    @Override public Type<GuardianMistPayload> type() {return TYPE;}
}
