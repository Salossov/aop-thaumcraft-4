package thaumcraft.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Original block-sparkle cue. Client-only effect, with no authority over world state. */
public record BlockSparklePayload(BlockPos position,int color) implements CustomPacketPayload {
    public static final Type<BlockSparklePayload> TYPE=new Type<>(ResourceLocation.parse("thaumcraft:block_sparkle"));
    public static final StreamCodec<RegistryFriendlyByteBuf,BlockSparklePayload> CODEC=new StreamCodec<>(){
        public BlockSparklePayload decode(RegistryFriendlyByteBuf b){return new BlockSparklePayload(b.readBlockPos(),b.readInt());}
        public void encode(RegistryFriendlyByteBuf b,BlockSparklePayload p){b.writeBlockPos(p.position);b.writeInt(p.color);}
    };
    @Override public Type<BlockSparklePayload> type(){return TYPE;}
}
