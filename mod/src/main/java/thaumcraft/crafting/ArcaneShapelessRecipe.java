package thaumcraft.crafting;

import java.util.*;
import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.*;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import thaumcraft.api.crafting.IArcaneRecipe;

public record ArcaneShapelessRecipe(String research, Map<String, Integer> vis, NonNullList<Ingredient> ingredients, ItemStack result) implements IArcaneRecipe {
    public ArcaneShapelessRecipe {
        vis = Map.copyOf(vis);
        if (research.isBlank() || ingredients.isEmpty() || ingredients.size() > 9 || result.isEmpty() || vis.isEmpty() || vis.size() > 6) throw new IllegalArgumentException("Invalid shapeless arcane recipe");
        vis.forEach((tag, n) -> { var a = thaumcraft.api.aspects.Aspect.getAspect(tag); if (a == null || !a.isPrimal() || n <= 0 || n > 32767) throw new IllegalArgumentException("Invalid vis " + tag); });
        var copy = NonNullList.<Ingredient>create(); copy.addAll(ingredients); ingredients = copy; result = result.copy();
    }
    @Override public boolean matches(CraftingInput input, Level level) {
        var stacks = new ArrayList<ItemStack>(); for (int i = 0; i < input.size(); i++) if (!input.getItem(i).isEmpty()) stacks.add(input.getItem(i));
        return stacks.size() == ingredients.size() && assign(0, stacks, new boolean[ingredients.size()]);
    }
    private boolean assign(int n, List<ItemStack> stacks, boolean[] used) {
        if (n == stacks.size()) return true;
        for (int i = 0; i < used.length; i++) if (!used[i] && ingredients.get(i).test(stacks.get(n))) { used[i] = true; if (assign(n + 1, stacks, used)) return true; used[i] = false; }
        return false;
    }
    @Override public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) { return result.copy(); }
    @Override public ItemStack getResultItem(HolderLookup.Provider registries) { return result.copy(); }
    @Override public NonNullList<Ingredient> getIngredients() { return ingredients; }
    @Override public boolean canCraftInDimensions(int width, int height) { return width * height >= ingredients.size(); }
    @Override public RecipeType<?> getType() { return R2Recipes.ARCANE_TYPE.get(); }
    @Override public RecipeSerializer<?> getSerializer() { return R3Recipes.ARCANE_SHAPELESS.get(); }
    public static final class Serializer implements RecipeSerializer<ArcaneShapelessRecipe> {
        private static final MapCodec<ArcaneShapelessRecipe> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                Codec.STRING.fieldOf("research").forGetter(ArcaneShapelessRecipe::research), Codec.unboundedMap(Codec.STRING, Codec.INT).fieldOf("vis").forGetter(ArcaneShapelessRecipe::vis),
                Ingredient.CODEC_NONEMPTY.listOf().xmap(list -> { var values = NonNullList.<Ingredient>create(); values.addAll(list); return values; }, List::copyOf).fieldOf("ingredients").forGetter(ArcaneShapelessRecipe::ingredients),
                ItemStack.STRICT_CODEC.fieldOf("result").forGetter(ArcaneShapelessRecipe::result)
        ).apply(i, ArcaneShapelessRecipe::new));
        private static final StreamCodec<RegistryFriendlyByteBuf, ArcaneShapelessRecipe> STREAM = new StreamCodec<>() {
            public ArcaneShapelessRecipe decode(RegistryFriendlyByteBuf b) {
                String key = b.readUtf(128); int size = b.readVarInt(); if (size < 1 || size > 6) throw new IllegalArgumentException("Vis count");
                var vis = new LinkedHashMap<String, Integer>(); for (int n = 0; n < size; n++) if (vis.put(b.readUtf(128), b.readVarInt()) != null) throw new IllegalArgumentException("Duplicate vis");
                size = b.readVarInt(); if (size < 1 || size > 9) throw new IllegalArgumentException("Ingredient count");
                var ingredients = NonNullList.<Ingredient>create(); for (int n = 0; n < size; n++) ingredients.add(Ingredient.CONTENTS_STREAM_CODEC.decode(b));
                return new ArcaneShapelessRecipe(key, vis, ingredients, ItemStack.STREAM_CODEC.decode(b));
            }
            public void encode(RegistryFriendlyByteBuf b, ArcaneShapelessRecipe r) {
                b.writeUtf(r.research, 128); b.writeVarInt(r.vis.size()); r.vis.forEach((tag, n) -> { b.writeUtf(tag, 128); b.writeVarInt(n); });
                b.writeVarInt(r.ingredients.size()); r.ingredients.forEach(ingredient -> Ingredient.CONTENTS_STREAM_CODEC.encode(b, ingredient)); ItemStack.STREAM_CODEC.encode(b, r.result);
            }
        };
        public MapCodec<ArcaneShapelessRecipe> codec() { return CODEC; }
        public StreamCodec<RegistryFriendlyByteBuf, ArcaneShapelessRecipe> streamCodec() { return STREAM; }
    }
}
