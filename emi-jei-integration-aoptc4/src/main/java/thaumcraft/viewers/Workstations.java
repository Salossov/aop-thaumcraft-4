package thaumcraft.viewers;
import java.util.*;
import net.minecraft.world.item.ItemStack;
import thaumcraft.api.crafting.RecipeDisplay;
/** Never register a workstation once per recipe: JEI does not deduplicate catalysts. */
public final class Workstations {
 public static Map<RecipeDisplay.Kind,List<ItemStack>> collect(List<RecipeDisplay> recipes){var result=new EnumMap<RecipeDisplay.Kind,List<ItemStack>>(RecipeDisplay.Kind.class);for(var r:recipes)for(var stack:r.catalysts()){var bucket=result.computeIfAbsent(r.kind(),k->new ArrayList<>());if(!stack.isEmpty()&&bucket.stream().noneMatch(s->ItemStack.isSameItemSameComponents(s,stack)))bucket.add(stack.copyWithCount(1));}result.replaceAll((kind,values)->List.copyOf(values));return Collections.unmodifiableMap(result);}
 private Workstations(){}
}
