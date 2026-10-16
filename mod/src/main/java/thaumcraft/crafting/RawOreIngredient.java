package thaumcraft.crafting;

import com.mojang.serialization.MapCodec;
import java.util.Arrays;
import java.util.stream.Stream;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.*;
import thaumcraft.config.ThaumcraftConfig;

public record RawOreIngredient(Ingredient ingredient) implements ICustomIngredient {
    public static final MapCodec<RawOreIngredient> CODEC=Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").xmap(RawOreIngredient::new,RawOreIngredient::ingredient);
    @Override public boolean test(ItemStack stack){return ThaumcraftConfig.RAW_ORE_PURIFICATION.get()&&ingredient.test(stack);}
    @Override public Stream<ItemStack> getItems(){return Arrays.stream(ingredient.getItems());}
    @Override public boolean isSimple(){return false;}
    @Override public IngredientType<?> getType(){return R3Recipes.RAW_ORE_INGREDIENT.get();}
}
