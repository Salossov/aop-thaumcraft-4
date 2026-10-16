package thaumcraft.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record GolemBellResetPayload() implements CustomPacketPayload {
    public static final Type<GolemBellResetPayload> TYPE=new Type<>(ResourceLocation.parse("thaumcraft:golem_bell_reset"));
    public static final StreamCodec<RegistryFriendlyByteBuf,GolemBellResetPayload> CODEC=StreamCodec.unit(new GolemBellResetPayload());
    @Override public Type<? extends CustomPacketPayload> type(){return TYPE;}
}
