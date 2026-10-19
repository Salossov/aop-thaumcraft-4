package thaumcraft.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

/** Original PacketFXBlockZap endpoints; transfer and node growth stay server-owned. */
public record NodeZapPayload(Vec3 from,Vec3 to) implements CustomPacketPayload {
    public static final Type<NodeZapPayload> TYPE=new Type<>(ResourceLocation.parse("thaumcraft:node_zap"));
    public static final StreamCodec<RegistryFriendlyByteBuf,NodeZapPayload> CODEC=new StreamCodec<>(){
        public NodeZapPayload decode(RegistryFriendlyByteBuf b){return new NodeZapPayload(new Vec3(b.readFloat(),b.readFloat(),b.readFloat()),new Vec3(b.readFloat(),b.readFloat(),b.readFloat()));}
        public void encode(RegistryFriendlyByteBuf b,NodeZapPayload p){b.writeFloat((float)p.from.x);b.writeFloat((float)p.from.y);b.writeFloat((float)p.from.z);b.writeFloat((float)p.to.x);b.writeFloat((float)p.to.y);b.writeFloat((float)p.to.z);}
    };
    @Override public Type<NodeZapPayload> type(){return TYPE;}
}
