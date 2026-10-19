package thaumcraft.api.crafting;

import java.util.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

/** Providers operate on the live recipe manager, not a second copied recipe database. */
public final class RecipeDisplays {
    @FunctionalInterface public interface Provider {Collection<RecipeDisplay> recipes(Level level);}
    private static final Map<ResourceLocation,Provider> PROVIDERS=new LinkedHashMap<>();
    private static final Map<Level,List<RecipeDisplay>> CACHE=new WeakHashMap<>();
    public static synchronized void register(ResourceLocation id,Provider provider){if(PROVIDERS.putIfAbsent(id,Objects.requireNonNull(provider))!=null)throw new IllegalArgumentException("Duplicate recipe display provider "+id);CACHE.clear();}
    public static synchronized List<RecipeDisplay> recipes(Level level){if(level==null)return List.of();return CACHE.computeIfAbsent(level,l->{var result=new LinkedHashMap<ResourceLocation,RecipeDisplay>();for(var provider:PROVIDERS.values())for(var recipe:provider.recipes(l))if(result.putIfAbsent(recipe.id(),recipe)!=null)throw new IllegalArgumentException("Duplicate recipe display "+recipe.id());return List.copyOf(result.values());});}
    public static synchronized void invalidate(){CACHE.clear();}
    private RecipeDisplays(){}
}
