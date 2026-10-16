package thaumcraft.crafting;

import com.mojang.serialization.MapCodec;
import java.util.Arrays;
import java.util.stream.Stream;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.*;
import thaumcraft.config.ThaumcraftConfig;

/** Gates crafting dynamically without unregistering copper objects from existing worlds. */
public record CopperIngredient(Ingredient ingredient) implements ICustomIngredient {
    public static final MapCodec<CopperIngredient> CODEC=Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").xmap(CopperIngredient::new,CopperIngredient::ingredient);
    @Override public boolean test(ItemStack stack){return ThaumcraftConfig.ENABLE_COPPER_CONTENT.get()&&ingredient.test(stack);}
    @Override public Stream<ItemStack> getItems(){return Arrays.stream(ingredient.getItems());}
    @Override public boolean isSimple(){return false;}
    @Override public IngredientType<?> getType(){return R3Recipes.COPPER_INGREDIENT.get();}
}
