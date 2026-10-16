package thaumcraft.network;

import java.util.HashMap;
import java.util.UUID;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import thaumcraft.api.world.BiomeColumns;

/** Bounded full snapshot of one watched chunk, never accepted from clients. */
public record BiomeColumnsPayload(UUID session,ResourceLocation dimension,boolean enabled,boolean global,boolean reset,boolean forget,int x,int z,BiomeColumns.Chunk chunk) implements CustomPacketPayload {
    public static final Type<BiomeColumnsPayload> TYPE=new Type<>(ResourceLocation.parse("thaumcraft:biome_columns"));
    public static final StreamCodec<RegistryFriendlyByteBuf,BiomeColumnsPayload> CODEC=new StreamCodec<>() {
        @Override public BiomeColumnsPayload decode(RegistryFriendlyByteBuf buf) {
            var session=buf.readUUID();var dimension=buf.readResourceLocation();boolean enabled=buf.readBoolean(),global=buf.readBoolean(),reset=buf.readBoolean(),forget=buf.readBoolean();
            int x=buf.readInt(),z=buf.readInt();long revision=buf.readLong();int count=buf.readVarInt();
            if(count<0||count>256) throw new IllegalArgumentException("Invalid biome column count");
            var cells=new HashMap<Integer,String>();for(int i=0;i<count;i++) {
                int cell=buf.readUnsignedByte();var biome=buf.readResourceLocation().toString();
                if(cells.put(cell,biome)!=null) throw new IllegalArgumentException("Duplicate biome column");
            }
            return new BiomeColumnsPayload(session,dimension,enabled,global,reset,forget,x,z,new BiomeColumns.Chunk(revision,cells));
        }
        @Override public void encode(RegistryFriendlyByteBuf buf,BiomeColumnsPayload value) {
            buf.writeUUID(value.session);buf.writeResourceLocation(value.dimension);buf.writeBoolean(value.enabled);buf.writeBoolean(value.global);buf.writeBoolean(value.reset);buf.writeBoolean(value.forget);
            buf.writeInt(value.x);buf.writeInt(value.z);buf.writeLong(value.chunk.revision());buf.writeVarInt(value.chunk.cells().size());
            value.chunk.cells().forEach((cell,biome)->{buf.writeByte(cell);buf.writeResourceLocation(ResourceLocation.parse(biome));});
        }
    };
    @Override public Type<BiomeColumnsPayload> type() { return TYPE; }
}
