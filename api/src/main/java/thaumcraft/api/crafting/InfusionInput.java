package thaumcraft.api.crafting;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;
/** Central stack plus one stack from every occupied pedestal. */
public record InfusionInput(ItemStack central,List<ItemStack> components) implements RecipeInput {
    public InfusionInput { central=central.copy();components=components.stream().map(ItemStack::copy).toList(); }
    @Override public ItemStack getItem(int slot){return slot==0?central:components.get(slot-1);}
    @Override public int size(){return 1+components.size();}
}
