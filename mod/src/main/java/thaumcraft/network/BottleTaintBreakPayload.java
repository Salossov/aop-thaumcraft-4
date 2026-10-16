package thaumcraft.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

/** The impact is server-owned, including when the client misses its collision tick. */
public record BottleTaintBreakPayload(Vec3 position,float height) implements CustomPacketPayload {
    public static final Type<BottleTaintBreakPayload> TYPE=new Type<>(ResourceLocation.parse("thaumcraft:bottle_taint_break"));
    public static final StreamCodec<RegistryFriendlyByteBuf,BottleTaintBreakPayload> CODEC=new StreamCodec<>(){
        public BottleTaintBreakPayload decode(RegistryFriendlyByteBuf b){return new BottleTaintBreakPayload(new Vec3(b.readDouble(),b.readDouble(),b.readDouble()),b.readFloat());}
        public void encode(RegistryFriendlyByteBuf b,BottleTaintBreakPayload p){b.writeDouble(p.position.x);b.writeDouble(p.position.y);b.writeDouble(p.position.z);b.writeFloat(p.height);}
    };
    @Override public Type<BottleTaintBreakPayload> type(){return TYPE;}
}
