package thaumcraft.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * TC4 right-edge notification (original PlayerNotifications): server-owned events
 * surface in the client's right-side HUD stack instead of the action bar or chat.
 */
public record NotifyPayload(String key,String params,int color,boolean italic,boolean whisper,boolean fly) implements CustomPacketPayload {
    public static final Type<NotifyPayload> TYPE=new Type<>(ResourceLocation.parse("thaumcraft:notify"));
    public NotifyPayload(String key,String params,int color,boolean italic,boolean whisper){this(key,params,color,italic,whisper,false);}
    public static final StreamCodec<RegistryFriendlyByteBuf,NotifyPayload> CODEC=StreamCodec.composite(
        ByteBufCodecs.STRING_UTF8,NotifyPayload::key,
        ByteBufCodecs.STRING_UTF8,NotifyPayload::params,
        ByteBufCodecs.INT,NotifyPayload::color,
        ByteBufCodecs.BOOL,NotifyPayload::italic,
        ByteBufCodecs.BOOL,NotifyPayload::whisper,
        ByteBufCodecs.BOOL,NotifyPayload::fly,
        NotifyPayload::new);
    @Override public Type<NotifyPayload> type() {return TYPE;}
}
