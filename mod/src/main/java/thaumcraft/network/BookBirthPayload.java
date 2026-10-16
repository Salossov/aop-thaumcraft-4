package thaumcraft.network;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
/** Visual cue only; never grants an item or changes player knowledge. */
public record BookBirthPayload(int entityId,Vec3 position) implements CustomPacketPayload {
    public static final Type<BookBirthPayload> TYPE=new Type<>(ResourceLocation.parse("thaumcraft:book_birth"));
    public static final StreamCodec<RegistryFriendlyByteBuf,BookBirthPayload> CODEC=new StreamCodec<>(){
        public BookBirthPayload decode(RegistryFriendlyByteBuf b){return new BookBirthPayload(b.readVarInt(),new Vec3(b.readDouble(),b.readDouble(),b.readDouble()));}
        public void encode(RegistryFriendlyByteBuf b,BookBirthPayload p){b.writeVarInt(p.entityId);b.writeDouble(p.position.x);b.writeDouble(p.position.y);b.writeDouble(p.position.z);}
    };
    @Override public Type<BookBirthPayload> type(){return TYPE;}
}
