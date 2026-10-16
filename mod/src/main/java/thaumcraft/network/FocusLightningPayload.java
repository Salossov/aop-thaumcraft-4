package thaumcraft.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

/** Source entity IDs for chain zaps; main bolts additionally carry their world hit endpoint. */
public record FocusLightningPayload(int source,int target,Vec3 endpoint,boolean fromWand,boolean blockSparkles) implements CustomPacketPayload {
    public static final Type<FocusLightningPayload> TYPE=new Type<>(ResourceLocation.parse("thaumcraft:focus_lightning"));
    public static final StreamCodec<RegistryFriendlyByteBuf,FocusLightningPayload> CODEC=new StreamCodec<>(){
        public FocusLightningPayload decode(RegistryFriendlyByteBuf b){return new FocusLightningPayload(b.readVarInt(),b.readVarInt(),new Vec3(b.readDouble(),b.readDouble(),b.readDouble()),b.readBoolean(),b.readBoolean());}
        public void encode(RegistryFriendlyByteBuf b,FocusLightningPayload p){b.writeVarInt(p.source);b.writeVarInt(p.target);b.writeDouble(p.endpoint.x);b.writeDouble(p.endpoint.y);b.writeDouble(p.endpoint.z);b.writeBoolean(p.fromWand);b.writeBoolean(p.blockSparkles);}
    };
    @Override public Type<FocusLightningPayload> type(){return TYPE;}
}
