package thaumcraft.api.research;

import java.util.*;
import net.minecraft.resources.ResourceLocation;

/** Startup rules for additional hidden requirements. The core evaluates these
 * after datapacks, configuration and required item tags have selected the active
 * server catalog. Each callback sees the same immutable, unmodified catalog.
 * Rules must be pure and may only add existing requirements. Reloads recompute
 * from the original definitions; requirements never accumulate across worlds.
 */
public final class ResearchCatalogRules {
    @FunctionalInterface public interface Rule {
        Collection<String> additionalHiddenParents(ResearchDefinition definition,Map<String,ResearchDefinition> catalog);
    }
    private static final Map<ResourceLocation,Rule> RULES=new LinkedHashMap<>();
    private static boolean frozen;
    public static synchronized void register(ResourceLocation id,Rule rule) {
        if(frozen)throw new IllegalStateException("Research catalog registration is closed");
        if(RULES.putIfAbsent(Objects.requireNonNull(id),Objects.requireNonNull(rule))!=null)throw new IllegalArgumentException("Duplicate research catalog rule "+id);
    }
    public static synchronized void freeze() {frozen=true;}
    private static synchronized List<Rule> rules() {return List.copyOf(RULES.values());}
    public static Map<String,ResearchDefinition> resolve(Map<String,ResearchDefinition> definitions) {
        var input=Collections.unmodifiableMap(new LinkedHashMap<>(definitions));var result=new LinkedHashMap<String,ResearchDefinition>();
        var rules=rules();
        for(var definition:input.values()) {
            var parents=new LinkedHashSet<>(definition.hiddenParents());
            for(var rule:rules)for(var parent:Objects.requireNonNull(rule.additionalHiddenParents(definition,input),"Null hidden-parent rule result")) {
                if(!input.containsKey(parent))throw new IllegalArgumentException("Unknown dynamic research parent "+parent+" for "+definition.key());
                parents.add(parent);
            }
            result.put(definition.key(),parents.equals(new LinkedHashSet<>(definition.hiddenParents()))?definition:definition.withHiddenParents(List.copyOf(parents)));
        }
        result=new LinkedHashMap<>(ResearchLinks.resolve(result));
        var visiting=new HashSet<String>();var done=new HashSet<String>();
        for(var key:result.keySet())visit(key,result,visiting,done);
        return Collections.unmodifiableMap(result);
    }
    private static void visit(String key,Map<String,ResearchDefinition> definitions,Set<String> visiting,Set<String> done) {
        if(done.contains(key))return;
        if(!visiting.add(key))throw new IllegalArgumentException("Dynamic research parent cycle at "+key);
        var definition=definitions.get(key);if(definition==null)throw new IllegalArgumentException("Unknown research parent "+key);
        for(var parent:definition.parents())visit(parent,definitions,visiting,done);
        for(var parent:definition.hiddenParents())visit(parent,definitions,visiting,done);
        if(!definition.linkedResearch().isEmpty())visit(definition.linkedResearch(),definitions,visiting,done);
        visiting.remove(key);done.add(key);
    }
    private ResearchCatalogRules() {}
}
