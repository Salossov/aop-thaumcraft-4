package thaumcraft.api.crafting;

import java.util.Map;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Recipe;

public interface IArcaneRecipe extends Recipe<CraftingInput> {
    String research();
    Map<String, Integer> vis();
    default Map<String,Integer> vis(CraftingInput input){return vis();}
    default boolean mayCraft(net.minecraft.world.entity.player.Player player,CraftingInput input){return true;}
}
