package thaumcraft.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Original field-frenzy arc cue; clients only resolve an already existing source entity. */
public record BlockArcPayload(BlockPos position,int source) implements CustomPacketPayload {
    public static final Type<BlockArcPayload> TYPE=new Type<>(ResourceLocation.parse("thaumcraft:block_arc"));
    public static final StreamCodec<RegistryFriendlyByteBuf,BlockArcPayload> CODEC=new StreamCodec<>(){
        public BlockArcPayload decode(RegistryFriendlyByteBuf b){return new BlockArcPayload(b.readBlockPos(),b.readInt());}
        public void encode(RegistryFriendlyByteBuf b,BlockArcPayload p){b.writeBlockPos(p.position);b.writeInt(p.source);}
    };
    @Override public Type<BlockArcPayload> type(){return TYPE;}
}
