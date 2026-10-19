package thaumcraft.api.crafting;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Only identity and intent cross the wire. Inventory and ingredients are resolved by the server. */
public record ArcaneTransferRequest(int menuId,int stateId,ResourceLocation displayId,boolean maximum) implements CustomPacketPayload {
    public static final Type<ArcaneTransferRequest> TYPE=new Type<>(ResourceLocation.parse("thaumcraft:arcane_transfer"));
    public static final StreamCodec<RegistryFriendlyByteBuf,ArcaneTransferRequest> CODEC=new StreamCodec<>(){
        public ArcaneTransferRequest decode(RegistryFriendlyByteBuf b){return new ArcaneTransferRequest(b.readVarInt(),b.readVarInt(),b.readResourceLocation(),b.readBoolean());}
        public void encode(RegistryFriendlyByteBuf b,ArcaneTransferRequest p){b.writeVarInt(p.menuId);b.writeVarInt(p.stateId);b.writeResourceLocation(p.displayId);b.writeBoolean(p.maximum);}
    };
    @Override public Type<ArcaneTransferRequest> type(){return TYPE;}
}
