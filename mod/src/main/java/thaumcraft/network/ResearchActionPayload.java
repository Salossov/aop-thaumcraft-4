package thaumcraft.network;

import java.util.UUID;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** The server derives identity, dimension, table, inventory and costs from the open menu. */
public record ResearchActionPayload(int containerId,UUID token,long sequence,int action,String first,String second,int q,int r) implements CustomPacketPayload {
    public static final Type<ResearchActionPayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath("thaumcraft","research_action"));
    public static final StreamCodec<RegistryFriendlyByteBuf,ResearchActionPayload> CODEC=new StreamCodec<>() {
        public ResearchActionPayload decode(RegistryFriendlyByteBuf b) { return new ResearchActionPayload(b.readVarInt(),b.readUUID(),b.readVarLong(),b.readVarInt(),b.readUtf(128),b.readUtf(32),b.readInt(),b.readInt()); }
        public void encode(RegistryFriendlyByteBuf b,ResearchActionPayload p) { b.writeVarInt(p.containerId);b.writeUUID(p.token);b.writeVarLong(p.sequence);b.writeVarInt(p.action);b.writeUtf(p.first,128);b.writeUtf(p.second,32);b.writeInt(p.q);b.writeInt(p.r); }
    };
    @Override public Type<ResearchActionPayload> type() { return TYPE; }
}
