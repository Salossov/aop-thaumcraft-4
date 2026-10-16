package thaumcraft.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

/** Visual endpoints only. Damage and item/flux changes remain server-owned. */
public record InfusionZapPayload(Vec3 from,Vec3 to) implements CustomPacketPayload {
    public static final Type<InfusionZapPayload> TYPE=new Type<>(ResourceLocation.parse("thaumcraft:infusion_zap"));
    public static final StreamCodec<RegistryFriendlyByteBuf,InfusionZapPayload> CODEC=new StreamCodec<>(){
        public InfusionZapPayload decode(RegistryFriendlyByteBuf b){return new InfusionZapPayload(read(b),read(b));}
        public void encode(RegistryFriendlyByteBuf b,InfusionZapPayload p){write(b,p.from);write(b,p.to);}
        private Vec3 read(RegistryFriendlyByteBuf b){return new Vec3(b.readDouble(),b.readDouble(),b.readDouble());}
        private void write(RegistryFriendlyByteBuf b,Vec3 p){b.writeDouble(p.x);b.writeDouble(p.y);b.writeDouble(p.z);}
    };
    @Override public Type<InfusionZapPayload> type(){return TYPE;}
}
