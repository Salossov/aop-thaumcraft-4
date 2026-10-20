package thaumcraft.api.addon;

import java.util.*;
import net.minecraft.resources.ResourceLocation;
import thaumcraft.api.book.*;
import thaumcraft.api.research.ResearchDefinition;

/** Startup registrations; immutable reload snapshots are built from these defaults. */
public final class AddonRegistry {
    public static final int API_VERSION=1;
    private static final Map<String,BookCategory> CATEGORIES=new LinkedHashMap<>();
    private static final Map<String,BookPage> PAGES=new LinkedHashMap<>();
    private static final Map<String,ResearchDefinition> RESEARCH=new LinkedHashMap<>();
    private static final Map<String,java.util.function.BooleanSupplier> RESEARCH_CONDITIONS=new LinkedHashMap<>();
    private static boolean frozen;
    public static String id(String value){var id=ResourceLocation.parse(value);if(!value.contains(":"))throw new IllegalArgumentException("Addon identifiers must be namespaced: "+value);if(value.length()>128)throw new IllegalArgumentException("Identifier too long");return id.toString();}
    private static <T> void put(Map<String,T> values,String key,T value){if(frozen)throw new IllegalStateException("Addon registration is closed");id(key);if(values.putIfAbsent(key,Objects.requireNonNull(value))!=null)throw new IllegalArgumentException("Duplicate addon identifier "+key);}
    public static synchronized void category(BookCategory value){put(CATEGORIES,value.id(),value);}
    public static synchronized void page(BookPage value){put(PAGES,value.id(),value);}
    public static synchronized void research(ResearchDefinition value){research(value,()->true);}
    /** Register all model/book identities at startup; evaluate a pure config condition when building each server catalog. */
    public static synchronized void research(ResearchDefinition value,java.util.function.BooleanSupplier enabled){Objects.requireNonNull(enabled);put(RESEARCH,value.key(),value);RESEARCH_CONDITIONS.put(value.key(),enabled);}
    public static synchronized Set<String> disabledResearch(){var disabled=new HashSet<String>();RESEARCH_CONDITIONS.forEach((key,condition)->{if(!condition.getAsBoolean())disabled.add(key);});return Set.copyOf(disabled);}
    public static synchronized Map<String,BookCategory> categories(){return Map.copyOf(CATEGORIES);}
    public static synchronized Map<String,BookPage> pages(){return Map.copyOf(PAGES);}
    public static synchronized Map<String,ResearchDefinition> research(){return Map.copyOf(RESEARCH);}
    public static synchronized void freeze(){thaumcraft.api.research.ResearchCatalogRules.freeze();thaumcraft.api.research.ResearchApi.freezeModes();frozen=true;}
    private AddonRegistry(){}
}
