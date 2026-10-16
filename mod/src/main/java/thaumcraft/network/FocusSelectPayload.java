package thaumcraft.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record FocusSelectPayload(boolean offHand,int slot) implements CustomPacketPayload {
    public static final Type<FocusSelectPayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath("thaumcraft","focus_select"));
    public static final StreamCodec<RegistryFriendlyByteBuf,FocusSelectPayload> CODEC=StreamCodec.of((buffer,value)->{buffer.writeBoolean(value.offHand);buffer.writeVarInt(value.slot);},buffer->new FocusSelectPayload(buffer.readBoolean(),buffer.readVarInt()));
    @Override public Type<? extends CustomPacketPayload> type(){return TYPE;}
}
