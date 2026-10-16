package thaumcraft.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record SonicPayload(int source) implements CustomPacketPayload {
    public static final Type<SonicPayload> TYPE=new Type<>(ResourceLocation.parse("thaumcraft:sonic"));
    public static final StreamCodec<RegistryFriendlyByteBuf,SonicPayload> CODEC=new StreamCodec<>(){
        public SonicPayload decode(RegistryFriendlyByteBuf b){return new SonicPayload(b.readInt());}
        public void encode(RegistryFriendlyByteBuf b,SonicPayload p){b.writeInt(p.source);}
    };
    @Override public Type<SonicPayload> type(){return TYPE;}
}
