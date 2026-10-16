package thaumcraft.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record WandMiscPayload() implements CustomPacketPayload {
    public static final Type<WandMiscPayload> TYPE=new Type<>(ResourceLocation.parse("thaumcraft:wand_misc"));
    public static final StreamCodec<RegistryFriendlyByteBuf,WandMiscPayload> CODEC=StreamCodec.unit(new WandMiscPayload());
    @Override public Type<? extends CustomPacketPayload> type(){return TYPE;}
}
