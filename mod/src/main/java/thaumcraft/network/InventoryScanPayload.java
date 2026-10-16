package thaumcraft.network;

import java.util.UUID;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Client identifies a live slot only; aspect values and rewards never cross this request. */
public record InventoryScanPayload(UUID session,int containerId,int slot,long sequence,net.minecraft.world.item.ItemStack catalog,boolean creativeCursor) implements CustomPacketPayload {
    public InventoryScanPayload(UUID session,int containerId,int slot,long sequence){this(session,containerId,slot,sequence,net.minecraft.world.item.ItemStack.EMPTY,false);}
    public static final Type<InventoryScanPayload> TYPE=new Type<>(ResourceLocation.parse("thaumcraft:inventory_scan"));
    public static final StreamCodec<RegistryFriendlyByteBuf,InventoryScanPayload> CODEC=StreamCodec.of((buffer,value)->{buffer.writeUUID(value.session);buffer.writeVarInt(value.containerId);buffer.writeVarInt(value.slot);buffer.writeVarLong(value.sequence);net.minecraft.world.item.ItemStack.OPTIONAL_STREAM_CODEC.encode(buffer,value.catalog);buffer.writeBoolean(value.creativeCursor);},buffer->new InventoryScanPayload(buffer.readUUID(),buffer.readVarInt(),buffer.readVarInt(),buffer.readVarLong(),net.minecraft.world.item.ItemStack.OPTIONAL_STREAM_CODEC.decode(buffer),buffer.readBoolean()));
    @Override public Type<InventoryScanPayload> type(){return TYPE;}
}
