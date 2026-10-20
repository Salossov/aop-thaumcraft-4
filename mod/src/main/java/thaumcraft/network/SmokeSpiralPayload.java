package thaumcraft.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.network.PacketDistributor;
import thaumcraft.api.client.ParticleApi;

/** Explicit level-scoped transport for one original smoke spiral. */
public record SmokeSpiralPayload(ResourceLocation dimension,ParticleApi.SmokeSpiral effect) implements CustomPacketPayload {
    public SmokeSpiralPayload{java.util.Objects.requireNonNull(dimension);java.util.Objects.requireNonNull(effect);}
    public static final Type<SmokeSpiralPayload> TYPE=new Type<>(ResourceLocation.parse("thaumcraft:smoke_spiral"));
    public static final StreamCodec<RegistryFriendlyByteBuf,SmokeSpiralPayload> CODEC=new StreamCodec<>(){
        public SmokeSpiralPayload decode(RegistryFriendlyByteBuf buffer){
            return new SmokeSpiralPayload(buffer.readResourceLocation(),new ParticleApi.SmokeSpiral(buffer.readDouble(),buffer.readDouble(),buffer.readDouble(),buffer.readFloat(),buffer.readVarInt(),buffer.readInt(),buffer.readInt()));
        }
        public void encode(RegistryFriendlyByteBuf buffer,SmokeSpiralPayload packet){
            var effect=packet.effect();buffer.writeResourceLocation(packet.dimension());buffer.writeDouble(effect.x());buffer.writeDouble(effect.y());buffer.writeDouble(effect.z());
            buffer.writeFloat(effect.radius());buffer.writeVarInt(effect.start());buffer.writeInt(effect.minY());buffer.writeInt(effect.color());
        }
    };
    public static void send(ServerLevel level,ParticleApi.SmokeSpiral effect){
        PacketDistributor.sendToPlayersNear(level,null,effect.x(),effect.y(),effect.z(),64,new SmokeSpiralPayload(level.dimension().location(),effect));
    }
    @Override public Type<SmokeSpiralPayload> type(){return TYPE;}
}
