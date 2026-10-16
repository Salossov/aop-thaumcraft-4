package thaumcraft.network;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
public record ChampionShieldPayload(int source,int target) implements CustomPacketPayload {
    public static final Type<ChampionShieldPayload> TYPE=new Type<>(ResourceLocation.parse("thaumcraft:champion_shield"));
    public static final StreamCodec<RegistryFriendlyByteBuf,ChampionShieldPayload> CODEC=new StreamCodec<>(){public ChampionShieldPayload decode(RegistryFriendlyByteBuf b){return new ChampionShieldPayload(b.readInt(),b.readInt());}public void encode(RegistryFriendlyByteBuf b,ChampionShieldPayload p){b.writeInt(p.source);b.writeInt(p.target);}};
    @Override public Type<ChampionShieldPayload> type(){return TYPE;}
}
