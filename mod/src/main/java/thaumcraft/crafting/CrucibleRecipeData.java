package thaumcraft.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import thaumcraft.api.crafting.CrucibleRecipe;

public final class CrucibleRecipeData extends CrucibleRecipe {
    public CrucibleRecipeData(String research, Ingredient catalyst, ItemStack result, Map<String, Integer> aspects) {
        super(research, catalyst, result, aspects);
    }
    @Override public RecipeSerializer<?> getSerializer() { return R3Recipes.CRUCIBLE_SERIALIZER.get(); }
    @Override public RecipeType<?> getType() { return R3Recipes.CRUCIBLE_TYPE.get(); }
    public static final class Serializer implements RecipeSerializer<CrucibleRecipeData> {
        private static final MapCodec<CrucibleRecipeData> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                Codec.STRING.fieldOf("research").forGetter(CrucibleRecipeData::research),
                Ingredient.CODEC_NONEMPTY.fieldOf("catalyst").forGetter(CrucibleRecipeData::catalyst),
                ItemStack.STRICT_CODEC.fieldOf("result").forGetter(CrucibleRecipeData::getRecipeOutput),
                Codec.unboundedMap(Codec.STRING, Codec.INT).fieldOf("aspects").forGetter(CrucibleRecipeData::aspects)
        ).apply(i, CrucibleRecipeData::new));
        private static final StreamCodec<RegistryFriendlyByteBuf, CrucibleRecipeData> STREAM = new StreamCodec<>() {
            public CrucibleRecipeData decode(RegistryFriendlyByteBuf b) {
                String research = b.readUtf(128); var catalyst = Ingredient.CONTENTS_STREAM_CODEC.decode(b);
                var result = ItemStack.STREAM_CODEC.decode(b); int size = b.readVarInt();
                if (size < 1 || size > 48) throw new IllegalArgumentException("Invalid aspect count");
                var aspects = new LinkedHashMap<String, Integer>();
                for (int n = 0; n < size; n++) { if (aspects.put(b.readUtf(32), b.readVarInt()) != null) throw new IllegalArgumentException("Duplicate aspect"); }
                return new CrucibleRecipeData(research, catalyst, result, aspects);
            }
            public void encode(RegistryFriendlyByteBuf b, CrucibleRecipeData r) {
                b.writeUtf(r.research(), 128); Ingredient.CONTENTS_STREAM_CODEC.encode(b, r.catalyst());
                ItemStack.STREAM_CODEC.encode(b, r.getRecipeOutput()); b.writeVarInt(r.aspects().size());
                r.aspects().forEach((tag, n) -> { b.writeUtf(tag, 32); b.writeVarInt(n); });
            }
        };
        public MapCodec<CrucibleRecipeData> codec() { return CODEC; }
        public StreamCodec<RegistryFriendlyByteBuf, CrucibleRecipeData> streamCodec() { return STREAM; }
    }
}
