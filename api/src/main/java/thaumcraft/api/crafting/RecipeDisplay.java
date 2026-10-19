package thaumcraft.api.crafting;

import java.util.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

/** Viewer-independent effective recipe. Vis uses centivis; essentia uses indivisible units. */
public record RecipeDisplay(ResourceLocation id,ResourceLocation recipeId,Kind kind,List<String> research,List<Ingredient> inputs,List<ItemStack> outputs,Map<String,Integer> essentia,Map<String,Integer> aspectOutputs,Map<String,Integer> vis,int instability,int experience,List<ItemStack> catalysts,List<Component> notes) {
    public enum Kind {ARCANE,CRUCIBLE,INFUSION,INFERNAL,DISTILLATION,CENTRIFUGE,CRYSTALLIZATION}
    public RecipeDisplay {Objects.requireNonNull(id);Objects.requireNonNull(kind);research=List.copyOf(research);inputs=List.copyOf(inputs);outputs=outputs.stream().map(ItemStack::copy).toList();essentia=Map.copyOf(essentia);aspectOutputs=Map.copyOf(aspectOutputs);vis=Map.copyOf(vis);catalysts=catalysts.stream().map(ItemStack::copy).toList();notes=List.copyOf(notes);}
    @Override public List<ItemStack> outputs(){return outputs.stream().map(ItemStack::copy).toList();}
    @Override public List<ItemStack> catalysts(){return catalysts.stream().map(ItemStack::copy).toList();}
}
