package thaumcraft.verification;

import java.util.*;
import java.util.function.BiConsumer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import thaumcraft.crafting.ArcaneRecipe;

/** Real native ingredient matching, independently of a recipe merely loading or appearing in a book. */
final class ArcaneIngredientVerification {
    static void run(ServerLevel level,BiConsumer<Boolean,String> check){
        int checked=0;
        for(var id:List.of("wandcapthaumiuminert","wandcapvoidinert","wandcapgold","thaumiumsword","thaumiumpick","thaumiumaxe","thaumiumshovel","thaumiumhoe","voidsword","voidpick","fluxscrubber","deconstructor")){
            var holder=level.getRecipeManager().byKey(ResourceLocation.fromNamespaceAndPath("thaumcraft",id)).orElseThrow();
            Recipe<CraftingInput> recipe;int width,height;
            if(holder.value() instanceof ArcaneRecipe arcane){recipe=arcane;width=arcane.pattern().width();height=arcane.pattern().height();}else if(holder.value() instanceof ShapedRecipe shaped){recipe=shaped;width=shaped.getWidth();height=shaped.getHeight();}else throw new AssertionError("Expected shaped recipe "+id);
            var input=new ArrayList<ItemStack>();
            for(var ingredient:recipe.getIngredients()){
                if(ingredient==Ingredient.EMPTY)input.add(ItemStack.EMPTY);
                else{check.accept(ingredient.getItems().length>0&&Arrays.stream(ingredient.getItems()).noneMatch(s->s.is(Items.BARRIER)),"crafting ingredient resolves to real items without NeoForge's empty-tag barrier: "+id);input.add(ingredient.getItems()[0].copy());}
            }
            var grid=CraftingInput.of(width,height,input);
            check.accept(recipe.matches(grid,level)&&!recipe.assemble(grid,level.registryAccess()).isEmpty(),"original arcane pattern matches actual installed item stacks: "+id);checked++;
        }
        check.accept(checked==12,"all selected original arcane recipes exercise real native tag matching");
        int resolved=0,patterns=0;
        var problems=new ArrayList<String>();
        for(var holder:level.getRecipeManager().getRecipes()){
            if(!holder.id().getNamespace().equals("thaumcraft"))continue;
            var original=holder.value();var input=new ArrayList<ItemStack>();boolean valid=true;
            for(var ingredient:original.getIngredients()){
                if(ingredient==Ingredient.EMPTY){input.add(ItemStack.EMPTY);continue;}
                var choices=ingredient.getItems();
                if(choices.length==0||Arrays.stream(choices).anyMatch(s->s.isEmpty()||s.is(Items.BARRIER))){problems.add(holder.id()+": unresolved ingredient "+ingredient);valid=false;}
                else input.add(choices[0].copy());
                resolved++;
            }
            Recipe<CraftingInput> recipe=null;int width=0,height=0;
            if(original instanceof ArcaneRecipe arcane){recipe=arcane;width=arcane.pattern().width();height=arcane.pattern().height();}
            else if(original instanceof ShapedRecipe shaped){recipe=shaped;width=shaped.getWidth();height=shaped.getHeight();}
            else if(original instanceof ShapelessRecipe shapeless){recipe=shapeless;width=Math.min(3,input.size());height=(input.size()+2)/3;while(input.size()<width*height)input.add(ItemStack.EMPTY);}
            if(valid&&recipe!=null){var grid=CraftingInput.of(width,height,input);check.accept(recipe.matches(grid,level)&&!recipe.assemble(grid,level.registryAccess()).isEmpty(),"native crafting pattern matches actual registered ingredients: "+holder.id());patterns++;}
        }
        check.accept(problems.isEmpty(),"all loaded Thaumcraft ingredients resolve without barrier placeholders: "+problems);
        check.accept(resolved>500&&patterns>100,"native ingredient audit covers the complete loaded recipe registry ("+resolved+" ingredients, "+patterns+" patterns)");
    }
}
