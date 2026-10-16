package thaumcraft.crafting;

import java.util.Map;
import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.*;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import thaumcraft.api.aspects.Aspect;

/** A separate recipe type: vanilla crafting tables cannot bypass research or vis. */
public record ArcaneRecipe(String research,Map<String,Integer> vis,ShapedRecipePattern pattern,ItemStack result) implements thaumcraft.api.crafting.IArcaneRecipe {
    public ArcaneRecipe {
        vis=Map.copyOf(vis);
        if(vis.isEmpty()||vis.size()>6) throw new IllegalArgumentException("Arcane cost needs primal vis");
        vis.forEach((tag,amount)->{var a=Aspect.getAspect(tag);if(a==null||!a.isPrimal()||amount<=0||amount>32767) throw new IllegalArgumentException("Invalid arcane cost "+tag);});
    }
    @Override public boolean matches(CraftingInput input,Level level) { return pattern.matches(input); }
    @Override public ItemStack assemble(CraftingInput input,HolderLookup.Provider registries) { return result.copy(); }
    @Override public boolean canCraftInDimensions(int width,int height) { return width>=pattern.width()&&height>=pattern.height(); }
    @Override public ItemStack getResultItem(HolderLookup.Provider registries) { return result; }
    @Override public NonNullList<Ingredient> getIngredients() { return pattern.ingredients(); }
    @Override public RecipeSerializer<?> getSerializer() { return R2Recipes.ARCANE_SERIALIZER.get(); }
    @Override public RecipeType<?> getType() { return R2Recipes.ARCANE_TYPE.get(); }
    public static final class Serializer implements RecipeSerializer<ArcaneRecipe> {
        private static final MapCodec<ArcaneRecipe> CODEC=RecordCodecBuilder.mapCodec(instance->instance.group(
            Codec.STRING.fieldOf("research").forGetter(ArcaneRecipe::research),
            Codec.unboundedMap(Codec.STRING,Codec.INT).fieldOf("vis").forGetter(ArcaneRecipe::vis),
            ShapedRecipePattern.MAP_CODEC.forGetter(ArcaneRecipe::pattern),ItemStack.STRICT_CODEC.fieldOf("result").forGetter(ArcaneRecipe::result)
        ).apply(instance,ArcaneRecipe::new));
        private static final StreamCodec<RegistryFriendlyByteBuf,ArcaneRecipe> STREAM=new StreamCodec<>() {
            public ArcaneRecipe decode(RegistryFriendlyByteBuf b) {
                String research=b.readUtf(128);int count=b.readVarInt();if(count<1||count>6) throw new IllegalArgumentException("Invalid vis count");
                var costs=new java.util.LinkedHashMap<String,Integer>();for(int i=0;i<count;i++) costs.put(b.readUtf(32),b.readVarInt());
                return new ArcaneRecipe(research,costs,ShapedRecipePattern.STREAM_CODEC.decode(b),ItemStack.STREAM_CODEC.decode(b));
            }
            public void encode(RegistryFriendlyByteBuf b,ArcaneRecipe recipe) {
                b.writeUtf(recipe.research,128);b.writeVarInt(recipe.vis.size());recipe.vis.forEach((tag,amount)->{b.writeUtf(tag,32);b.writeVarInt(amount);});
                ShapedRecipePattern.STREAM_CODEC.encode(b,recipe.pattern);ItemStack.STREAM_CODEC.encode(b,recipe.result);
            }
        };
        public MapCodec<ArcaneRecipe> codec() { return CODEC; }
        public StreamCodec<RegistryFriendlyByteBuf,ArcaneRecipe> streamCodec() { return STREAM; }
    }
}
