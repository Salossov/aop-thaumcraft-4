package thaumcraft.data;

import java.util.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.crafting.*;
import thaumcraft.crafting.ArcaneRecipe;

/** TC4 recipe decay, output division, arcane vis and a bounded, explicit ID allowlist. */
final class RecipeAspectResolver {
    record Rule(String inherit,Map<String,Integer> add,Map<String,Integer> merge,List<String> remove) {}
    private final MinecraftServer server;
    private final Map<String,Rule> rules;
    private final Map<String,Map<String,Integer>> values;
    private final Map<String,List<Recipe<?>>> normal=new LinkedHashMap<>(),arcane=new LinkedHashMap<>(),crucible=new LinkedHashMap<>(),infusion=new LinkedHashMap<>();
    private final Set<String> path=new HashSet<>();
    RecipeAspectResolver(MinecraftServer server,Map<String,Map<String,Integer>> explicit,Map<String,Rule> rules) {
        this.server=server;this.rules=rules;values=new LinkedHashMap<>(explicit);
        for(var holder:server.getRecipeManager().getRecipes().stream().sorted(Comparator.comparing(h->h.id().toString())).toList()) {
            var recipe=holder.value();boolean magic=recipe instanceof thaumcraft.api.crafting.IArcaneRecipe,alchemy=recipe instanceof thaumcraft.api.crafting.CrucibleRecipe;
            boolean ritual=recipe instanceof thaumcraft.api.crafting.InfusionRecipe;
            if(!magic&&!alchemy&&!ritual&&!(recipe instanceof ShapedRecipe)&&!(recipe instanceof ShapelessRecipe))continue;
            var output=recipe.getResultItem(server.registryAccess());if(output.isEmpty())continue;
            String id=BuiltInRegistries.ITEM.getKey(output.getItem()).toString();if(rules.containsKey(id))(alchemy?crucible:magic?arcane:ritual?infusion:normal).computeIfAbsent(id,k->new ArrayList<>()).add(recipe);
        }
    }
    Map<String,Map<String,Integer>> resolve() {for(var id:rules.keySet().stream().sorted().toList())value(id);return Map.copyOf(values);}
    private Map<String,Integer> value(String id) {
        var cached=values.get(id);if(cached!=null)return cached;
        var rule=rules.get(id);if(rule==null||path.size()>=100||!path.add(id))return Map.of();
        try {
            var result=new LinkedHashMap<String,Integer>();
            if(!rule.inherit.isEmpty())result.putAll(value(rule.inherit));
            else {result.putAll(best(crucible.getOrDefault(id,List.of())));if(result.isEmpty())result.putAll(best(arcane.getOrDefault(id,List.of())));if(result.isEmpty())result.putAll(best(infusion.getOrDefault(id,List.of())));if(result.isEmpty())result.putAll(best(normal.getOrDefault(id,List.of())));}
            rule.add.forEach((tag,amount)->result.merge(tag,amount,Integer::sum));rule.merge.forEach((tag,amount)->result.merge(tag,amount,Math::max));rule.remove.forEach(result::remove);
            result.entrySet().removeIf(e->e.getValue()<=0);result.replaceAll((tag,amount)->Math.min(64,amount));
            if(!result.isEmpty())values.put(id,Map.copyOf(result));return result;
        } finally {path.remove(id);}
    }
    private Map<String,Integer> best(List<Recipe<?>> recipes) {
        Map<String,Integer> best=Map.of();int smallest=Integer.MAX_VALUE;
        for(var recipe:recipes) {
            var sum=new LinkedHashMap<String,Integer>();
            for(var ingredient:recipe.getIngredients()) {
                if(ingredient.isEmpty())continue;
                for(var option:ingredient.getItems()) {
                    var tags=value(BuiltInRegistries.ITEM.getKey(option.getItem()).toString());
                    if(tags.isEmpty())continue;tags.forEach((tag,amount)->sum.merge(tag,amount,Integer::sum));break;
                }
            }
            int count=recipe.getResultItem(server.registryAccess()).getCount();if(count<=0)continue;
            if(!(recipe instanceof thaumcraft.api.crafting.CrucibleRecipe))sum.replaceAll((tag,amount)->(int)(amount*0.75f/count));
            if(recipe instanceof thaumcraft.api.crafting.CrucibleRecipe alchemy)alchemy.aspects().forEach((tag,amount)->sum.merge(tag,(int)(Math.sqrt(amount)/count),Integer::sum));
            if(recipe instanceof thaumcraft.api.crafting.InfusionRecipe ritual)ritual.aspects().forEach((tag,amount)->sum.merge(tag,(int)(Math.sqrt(amount)/count),Integer::sum));
            if(recipe instanceof thaumcraft.api.crafting.IArcaneRecipe magic)magic.vis().forEach((tag,amount)->sum.merge(tag,(int)(Math.sqrt(amount)/count),Integer::sum));
            sum.entrySet().removeIf(e->e.getValue()<=0);int size=sum.values().stream().mapToInt(Integer::intValue).sum();
            if(size>0&&size<smallest){best=Map.copyOf(sum);smallest=size;}
        }
        return best;
    }
}
