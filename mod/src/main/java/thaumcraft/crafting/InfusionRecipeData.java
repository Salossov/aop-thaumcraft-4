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
import thaumcraft.api.crafting.InfusionRecipe;

public final class InfusionRecipeData extends InfusionRecipe {
    public InfusionRecipeData(String research, Ingredient input, java.util.List<Ingredient> components, ItemStack result, Map<String, Integer> aspects, int instability) {
        super(research, input, components, result, aspects, instability);
    }
    @Override public RecipeSerializer<?> getSerializer() { return R3Recipes.INFUSION_SERIALIZER.get(); }
    @Override public RecipeType<?> getType() { return R3Recipes.INFUSION_TYPE.get(); }
    public static final class Serializer implements RecipeSerializer<InfusionRecipeData> {
        private static final MapCodec<InfusionRecipeData> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                Codec.STRING.fieldOf("research").forGetter(InfusionRecipeData::research),
                Ingredient.CODEC_NONEMPTY.fieldOf("input").forGetter(InfusionRecipeData::input),
                Ingredient.CODEC_NONEMPTY.listOf().fieldOf("components").forGetter(InfusionRecipeData::components),
                ItemStack.CODEC.validate(thaumcraft.api.crafting.IInfusionOutput::validateOutput).fieldOf("result").forGetter(InfusionRecipeData::getRecipeOutput),
                Codec.unboundedMap(Codec.STRING, Codec.INT).fieldOf("aspects").forGetter(InfusionRecipeData::aspects),
                Codec.INT.fieldOf("instability").forGetter(InfusionRecipeData::instability)
        ).apply(i, InfusionRecipeData::new));
        private static final StreamCodec<RegistryFriendlyByteBuf, InfusionRecipeData> STREAM = new StreamCodec<>() {
            public InfusionRecipeData decode(RegistryFriendlyByteBuf b) {
                String research = b.readUtf(128); var input = Ingredient.CONTENTS_STREAM_CODEC.decode(b);
                int length=b.readVarInt();if(length<1||length>64)throw new IllegalArgumentException("Invalid infusion component count");
                var components=new java.util.ArrayList<Ingredient>();for(int j=0;j<length;j++)components.add(Ingredient.CONTENTS_STREAM_CODEC.decode(b));
                var result = ItemStack.STREAM_CODEC.decode(b); int size = b.readVarInt();
                if (size < 0 || size > 48) throw new IllegalArgumentException("Invalid aspect count");
                var aspects = new LinkedHashMap<String, Integer>();
                for (int n = 0; n < size; n++) { if (aspects.put(b.readUtf(128), b.readVarInt()) != null) throw new IllegalArgumentException("Duplicate aspect"); }
                return new InfusionRecipeData(research, input, components, result, aspects, b.readVarInt());
            }
            public void encode(RegistryFriendlyByteBuf b, InfusionRecipeData r) {
                b.writeUtf(r.research(), 128); Ingredient.CONTENTS_STREAM_CODEC.encode(b, r.input());b.writeVarInt(r.components().size());r.components().forEach(c->Ingredient.CONTENTS_STREAM_CODEC.encode(b,c));
                ItemStack.STREAM_CODEC.encode(b, r.getRecipeOutput()); b.writeVarInt(r.aspects().size());
                r.aspects().forEach((tag, n) -> { b.writeUtf(tag, 128); b.writeVarInt(n); });b.writeVarInt(r.instability());
            }
        };
        public MapCodec<InfusionRecipeData> codec() { return CODEC; }
        public StreamCodec<RegistryFriendlyByteBuf, InfusionRecipeData> streamCodec() { return STREAM; }
    }
}
