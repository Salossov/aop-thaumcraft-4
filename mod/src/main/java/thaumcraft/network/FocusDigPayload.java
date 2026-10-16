package thaumcraft.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

public record FocusDigPayload(int player,Vec3 endpoint,boolean active) implements CustomPacketPayload {
    public static final Type<FocusDigPayload> TYPE=new Type<>(ResourceLocation.parse("thaumcraft:focus_dig"));
    public static final StreamCodec<RegistryFriendlyByteBuf,FocusDigPayload> CODEC=StreamCodec.of((b,p)->{b.writeVarInt(p.player);b.writeDouble(p.endpoint.x);b.writeDouble(p.endpoint.y);b.writeDouble(p.endpoint.z);b.writeBoolean(p.active);},b->new FocusDigPayload(b.readVarInt(),new Vec3(b.readDouble(),b.readDouble(),b.readDouble()),b.readBoolean()));
    @Override public Type<? extends CustomPacketPayload> type(){return TYPE;}
}
