package thaumcraft.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record RunicShieldPayload(int entityId,int charge,int maximum,boolean struck) implements CustomPacketPayload {
    public RunicShieldPayload{if(charge<0||maximum<0||charge>maximum||maximum>32767)throw new IllegalArgumentException("Invalid runic shield");}
    public static final Type<RunicShieldPayload> TYPE=new Type<>(ResourceLocation.parse("thaumcraft:runic_shield"));
    public static final StreamCodec<RegistryFriendlyByteBuf,RunicShieldPayload> CODEC=new StreamCodec<>(){
        public RunicShieldPayload decode(RegistryFriendlyByteBuf b){return new RunicShieldPayload(b.readVarInt(),b.readVarInt(),b.readVarInt(),b.readBoolean());}
        public void encode(RegistryFriendlyByteBuf b,RunicShieldPayload p){b.writeVarInt(p.entityId);b.writeVarInt(p.charge);b.writeVarInt(p.maximum);b.writeBoolean(p.struck);}
    };
    public Type<RunicShieldPayload> type(){return TYPE;}
}
