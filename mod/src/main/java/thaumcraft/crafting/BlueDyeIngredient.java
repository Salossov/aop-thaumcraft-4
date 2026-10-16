package thaumcraft.crafting;

import com.mojang.serialization.MapCodec;
import java.util.stream.Stream;
import net.minecraft.world.item.*;
import net.neoforged.neoforge.common.crafting.*;
import thaumcraft.config.ThaumcraftConfig;

/** A live server-setting predicate; matching never relies on an ingredient item cache. */
public final class BlueDyeIngredient implements ICustomIngredient {
    public static final MapCodec<BlueDyeIngredient> CODEC = MapCodec.unit(BlueDyeIngredient::new);
    public static Item selectedItem() { return ThaumcraftConfig.MODERN_BLUE_DYE.get() ? Items.BLUE_DYE : Items.LAPIS_LAZULI; }
    @Override public boolean test(ItemStack stack) { return stack.is(selectedItem()); }
    @Override public Stream<ItemStack> getItems() { return Stream.of(new ItemStack(Items.BLUE_DYE), new ItemStack(Items.LAPIS_LAZULI)); }
    @Override public boolean isSimple() { return false; }
    @Override public IngredientType<?> getType() { return R3Recipes.BLUE_DYE.get(); }
}
