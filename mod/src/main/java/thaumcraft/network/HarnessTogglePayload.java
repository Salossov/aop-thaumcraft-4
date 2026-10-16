package thaumcraft.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record HarnessTogglePayload() implements CustomPacketPayload {
    public static final Type<HarnessTogglePayload> TYPE=new Type<>(ResourceLocation.parse("thaumcraft:harness_toggle"));
    public static final StreamCodec<RegistryFriendlyByteBuf,HarnessTogglePayload> CODEC=StreamCodec.unit(new HarnessTogglePayload());
    public Type<HarnessTogglePayload> type(){return TYPE;}
}
