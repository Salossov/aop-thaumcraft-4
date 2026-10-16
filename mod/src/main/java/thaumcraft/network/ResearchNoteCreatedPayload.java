package thaumcraft.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Acknowledges a note actually delivered by the server, after its inventory transaction. */
public record ResearchNoteCreatedPayload(int containerId,String key) implements CustomPacketPayload {
    public static final Type<ResearchNoteCreatedPayload> TYPE=new Type<>(ResourceLocation.parse("thaumcraft:research_note_created"));
    public static final StreamCodec<RegistryFriendlyByteBuf,ResearchNoteCreatedPayload> CODEC=StreamCodec.composite(ByteBufCodecs.VAR_INT,ResearchNoteCreatedPayload::containerId,ByteBufCodecs.STRING_UTF8,ResearchNoteCreatedPayload::key,ResearchNoteCreatedPayload::new);
    @Override public Type<ResearchNoteCreatedPayload> type(){return TYPE;}
}
