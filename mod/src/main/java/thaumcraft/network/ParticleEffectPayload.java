package thaumcraft.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.network.PacketDistributor;
import thaumcraft.api.client.ParticleApi;

/** Explicit addon particle transport; ordinary legacy local proxy calls never use it. */
public record ParticleEffectPayload(ResourceLocation dimension, ParticleApi.Effect effect) implements CustomPacketPayload {
    public ParticleEffectPayload { java.util.Objects.requireNonNull(dimension); java.util.Objects.requireNonNull(effect); }
    public static final Type<ParticleEffectPayload> TYPE = new Type<>(ResourceLocation.parse("thaumcraft:addon_particle"));
    public static final StreamCodec<RegistryFriendlyByteBuf,ParticleEffectPayload> CODEC = new StreamCodec<>() {
        public ParticleEffectPayload decode(RegistryFriendlyByteBuf buffer) {
            var dimension=buffer.readResourceLocation();
            return new ParticleEffectPayload(dimension,new ParticleApi.Effect(buffer.readEnum(ParticleApi.Kind.class),
                    buffer.readDouble(),buffer.readDouble(),buffer.readDouble(),buffer.readFloat(),buffer.readVarInt(),
                    buffer.readFloat(),buffer.readFloat(),buffer.readFloat(),buffer.readBoolean(),buffer.readBoolean(),buffer.readFloat()));
        }
        public void encode(RegistryFriendlyByteBuf buffer,ParticleEffectPayload packet) {
            var effect=packet.effect();buffer.writeResourceLocation(packet.dimension());buffer.writeEnum(effect.kind());
            buffer.writeDouble(effect.x());buffer.writeDouble(effect.y());buffer.writeDouble(effect.z());buffer.writeFloat(effect.size());buffer.writeVarInt(effect.palette());
            buffer.writeFloat(effect.red());buffer.writeFloat(effect.green());buffer.writeFloat(effect.blue());
            buffer.writeBoolean(effect.shrink());buffer.writeBoolean(effect.noClip());buffer.writeFloat(effect.gravity());
        }
    };
    public static void send(ServerLevel level,ParticleApi.Effect effect) {
        PacketDistributor.sendToPlayersNear(level,null,effect.x(),effect.y(),effect.z(),64,
                new ParticleEffectPayload(level.dimension().location(),effect));
    }
    @Override public Type<ParticleEffectPayload> type(){return TYPE;}
}
