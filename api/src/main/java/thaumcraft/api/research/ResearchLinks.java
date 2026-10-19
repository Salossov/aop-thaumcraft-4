package thaumcraft.api.research;

import java.util.*;
import thaumcraft.api.knowledge.PlayerKnowledge;

/** Cross-category display links. Links mirror completion; they are never independent purchases. */
public final class ResearchLinks {
    /** Resolve presentation from this catalog only, without mutating native definitions. */
    public static Map<String,ResearchDefinition> resolve(Map<String,ResearchDefinition> catalog) {
        var result=new LinkedHashMap<String,ResearchDefinition>();
        for(var key:catalog.keySet())resolve(key,catalog,result,new HashSet<>());
        var ordered=new LinkedHashMap<String,ResearchDefinition>();
        for(var key:catalog.keySet())ordered.put(key,result.get(key));
        return Collections.unmodifiableMap(ordered);
    }
    private static ResearchDefinition resolve(String key,Map<String,ResearchDefinition> catalog,Map<String,ResearchDefinition> result,Set<String> visiting) {
        if(result.containsKey(key))return result.get(key);
        if(!visiting.add(key))throw new IllegalArgumentException("Linked research cycle at "+key);
        var definition=catalog.get(key);if(definition==null)throw new IllegalArgumentException("Unknown linked research "+key);
        var resolved=definition.linkedResearch().isEmpty()?definition:definition.withLinkedDisplay(resolve(definition.linkedResearch(),catalog,result,visiting));
        visiting.remove(key);result.put(key,resolved);return resolved;
    }
    /** Native identity for translations, special icons and pages; alias identity remains unchanged. */
    public static ResearchDefinition target(ResearchDefinition definition,Map<String,ResearchDefinition> catalog) {
        var seen=new HashSet<String>();
        while(!definition.linkedResearch().isEmpty()) {
            if(!seen.add(definition.key()))throw new IllegalArgumentException("Linked research cycle at "+definition.key());
            String target=definition.linkedResearch();definition=catalog.get(target);
            if(definition==null)throw new IllegalArgumentException("Unknown linked research "+target);
        }
        return definition;
    }
    /** Called inside the server's ordinary knowledge transaction, including login/reload reconciliation. */
    public static boolean completeAliases(PlayerKnowledge state,Map<String,ResearchDefinition> catalog) {
        boolean changed=false,added;
        do {added=false;for(var definition:catalog.values())if(!definition.linkedResearch().isEmpty()
                &&state.completedResearch().contains(definition.linkedResearch()))added|=state.completeResearch(definition.key());changed|=added;}while(added);
        return changed;
    }
    private ResearchLinks() {}
}
