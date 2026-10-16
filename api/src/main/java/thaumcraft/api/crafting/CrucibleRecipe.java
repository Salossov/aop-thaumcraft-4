package thaumcraft.api.crafting;

import java.util.Map;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.IAspectContainer;

/** Public TC4 recipe contract, backed by Minecraft ingredients and reloadable recipes. */
public abstract class CrucibleRecipe implements Recipe<SingleRecipeInput> {
    private final String research;
    private final Ingredient catalyst;
    private final ItemStack result;
    private final Map<String, Integer> aspects;

    protected CrucibleRecipe(String research, Ingredient catalyst, ItemStack result, Map<String, Integer> aspects) {
        this.research = research;
        this.catalyst = catalyst;
        this.result = result.copy();
        this.aspects = Map.copyOf(aspects);
        if (research.isBlank() || catalyst.isEmpty() || result.isEmpty() || aspects.isEmpty()) throw new IllegalArgumentException("Incomplete crucible recipe");
        aspects.forEach((tag, n) -> { if (Aspect.getAspect(tag) == null || n <= 0 || n > 32767) throw new IllegalArgumentException("Invalid essentia cost " + tag); });
    }
    public String research() { return research; }
    public Ingredient catalyst() { return catalyst; }
    public Map<String, Integer> aspects() { return aspects; }
    public ItemStack getRecipeOutput() { return result.copy(); }
    public boolean matches(IAspectContainer container, ItemStack input) {
        return catalyst.test(input) && aspects.entrySet().stream().allMatch(e -> container.doesContainerContainAmount(Aspect.getAspect(e.getKey()), e.getValue()));
    }
    public boolean consume(IAspectContainer container) {
        if (!aspects.entrySet().stream().allMatch(e -> container.doesContainerContainAmount(Aspect.getAspect(e.getKey()), e.getValue()))) return false;
        aspects.forEach((tag, n) -> container.takeFromContainer(Aspect.getAspect(tag), n)); return true;
    }
    @Override public boolean matches(SingleRecipeInput input, Level level) { return catalyst.test(input.item()); }
    @Override public ItemStack assemble(SingleRecipeInput input, HolderLookup.Provider registries) { return getRecipeOutput(); }
    @Override public ItemStack getResultItem(HolderLookup.Provider registries) { return getRecipeOutput(); }
    @Override public net.minecraft.core.NonNullList<Ingredient> getIngredients() { return net.minecraft.core.NonNullList.of(Ingredient.EMPTY,catalyst); }
    @Override public boolean canCraftInDimensions(int width, int height) { return width * height >= 1; }
    @Override public boolean isSpecial() { return true; }
}
