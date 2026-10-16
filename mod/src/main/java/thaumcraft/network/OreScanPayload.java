package thaumcraft.network;

import java.util.List;
import java.util.ArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record OreScanPayload(BlockPos origin,List<Marker> markers) implements CustomPacketPayload {
    public record Marker(BlockPos position,int value){public Marker{position=position.immutable();if(value<0&&value!=-5&&value!=-10)throw new IllegalArgumentException("Invalid scan marker");}}
    public OreScanPayload { origin=origin.immutable();markers=List.copyOf(markers);if(markers.size()>4913)throw new IllegalArgumentException("Invalid ore scan size"); }
    public static final Type<OreScanPayload> TYPE=new Type<>(ResourceLocation.parse("thaumcraft:ore_scan"));
    public static final StreamCodec<RegistryFriendlyByteBuf,OreScanPayload> CODEC=new StreamCodec<>(){
        public OreScanPayload decode(RegistryFriendlyByteBuf b){var origin=b.readBlockPos();int n=b.readVarInt();if(n<0||n>4913)throw new IllegalArgumentException("Invalid ore scan");var markers=new ArrayList<Marker>();for(int i=0;i<n;i++)markers.add(new Marker(b.readBlockPos(),b.readVarInt()));return new OreScanPayload(origin,markers);}
        public void encode(RegistryFriendlyByteBuf b,OreScanPayload p){b.writeBlockPos(p.origin);b.writeVarInt(p.markers.size());for(var marker:p.markers){b.writeBlockPos(marker.position);b.writeVarInt(marker.value);}}
    };
    public Type<OreScanPayload> type(){return TYPE;}
}
