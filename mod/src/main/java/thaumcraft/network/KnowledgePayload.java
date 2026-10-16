package thaumcraft.network;

import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record KnowledgePayload(UUID player,UUID session,CompoundTag data) implements CustomPacketPayload {
    public static final Type<KnowledgePayload> TYPE=new Type<>(ResourceLocation.parse("thaumcraft:knowledge_snapshot"));
    public static final StreamCodec<RegistryFriendlyByteBuf,KnowledgePayload> CODEC=new StreamCodec<>() {
        @Override public KnowledgePayload decode(RegistryFriendlyByteBuf buf) { return new KnowledgePayload(buf.readUUID(),buf.readUUID(),ByteBufCodecs.COMPOUND_TAG.decode(buf)); }
        @Override public void encode(RegistryFriendlyByteBuf buf,KnowledgePayload packet) { buf.writeUUID(packet.player);buf.writeUUID(packet.session);ByteBufCodecs.COMPOUND_TAG.encode(buf,packet.data); }
    };
    @Override public Type<KnowledgePayload> type() { return TYPE; }
}
