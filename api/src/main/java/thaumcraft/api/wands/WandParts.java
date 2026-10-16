package thaumcraft.api.wands;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

/** Saved, synchronized identity of a composite wand. Vis is stored separately. */
public record WandParts(String rod, String cap) {
    public static final WandParts DEFAULT = new WandParts("wood", "iron");
    public WandParts {
        if (rod == null || cap == null || !rod.matches("[a-z0-9_.:-]{1,128}") || !cap.matches("[a-z0-9_.:-]{1,128}"))
            throw new IllegalArgumentException("Invalid wand parts");
    }
    public static final Codec<WandParts> CODEC = RecordCodecBuilder.create(i -> i.group(
        Codec.STRING.fieldOf("rod").forGetter(WandParts::rod), Codec.STRING.fieldOf("cap").forGetter(WandParts::cap)
    ).apply(i, WandParts::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, WandParts> STREAM_CODEC = new StreamCodec<>() {
        public WandParts decode(RegistryFriendlyByteBuf b) { return new WandParts(b.readUtf(128), b.readUtf(128)); }
        public void encode(RegistryFriendlyByteBuf b, WandParts p) { b.writeUtf(p.rod, 128); b.writeUtf(p.cap, 128); }
    };
}
