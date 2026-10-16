package thaumcraft.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record AdminUiPayload(int kind,BlockPos pos,boolean enabled) implements CustomPacketPayload {
    public static final Type<AdminUiPayload> TYPE=new Type<>(ResourceLocation.parse("thaumcraft:admin_ui"));
    public static final StreamCodec<RegistryFriendlyByteBuf,AdminUiPayload> CODEC=StreamCodec.of((b,p)->{b.writeVarInt(p.kind);b.writeBlockPos(p.pos);b.writeBoolean(p.enabled);},b->new AdminUiPayload(b.readVarInt(),b.readBlockPos(),b.readBoolean()));
    @Override public Type<? extends CustomPacketPayload> type(){return TYPE;}
}
