package thaumcraft.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record InfusionDebugRequestPayload(BlockPos pos) implements CustomPacketPayload {
    public static final Type<InfusionDebugRequestPayload> TYPE=new Type<>(ResourceLocation.parse("thaumcraft:infusion_debug_request"));
    public static final StreamCodec<RegistryFriendlyByteBuf,InfusionDebugRequestPayload> CODEC=StreamCodec.of((buffer,value)->buffer.writeBlockPos(value.pos),buffer->new InfusionDebugRequestPayload(buffer.readBlockPos()));
    @Override public Type<? extends CustomPacketPayload> type(){return TYPE;}
}
