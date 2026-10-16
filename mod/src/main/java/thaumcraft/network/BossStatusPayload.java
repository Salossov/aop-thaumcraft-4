package thaumcraft.network;

import java.util.UUID;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Authoritative status for the existing vanilla boss-bar identity. */
public record BossStatusPayload(UUID bar,float health,float maxHealth,float shield,float maxShield,int rage,boolean removed) implements CustomPacketPayload {
    public static final Type<BossStatusPayload> TYPE=new Type<>(ResourceLocation.parse("thaumcraft:boss_status"));
    public static final StreamCodec<RegistryFriendlyByteBuf,BossStatusPayload> CODEC=StreamCodec.of((buffer,p)->{buffer.writeUUID(p.bar);buffer.writeFloat(p.health);buffer.writeFloat(p.maxHealth);buffer.writeFloat(p.shield);buffer.writeFloat(p.maxShield);buffer.writeVarInt(p.rage);buffer.writeBoolean(p.removed);},buffer->new BossStatusPayload(buffer.readUUID(),buffer.readFloat(),buffer.readFloat(),buffer.readFloat(),buffer.readFloat(),buffer.readVarInt(),buffer.readBoolean()));
    @Override public Type<? extends CustomPacketPayload> type(){return TYPE;}
}
