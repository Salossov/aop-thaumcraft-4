package thaumcraft.api.research;

import java.util.*;
import thaumcraft.api.book.BookCatalog;
import thaumcraft.api.knowledge.PlayerKnowledge;

/** Resolves a book link without revealing or unlocking a hidden research node. */
public final class ResearchNavigation {
    public enum Reason { KNOWN, AVAILABLE, PREREQUISITES, UNDISCOVERED, MISSING }
    public record Target(String requested,String focus,Reason reason) {}
    public static Target resolve(String key,Map<String,ResearchDefinition> definitions,BookCatalog books,PlayerKnowledge state) {
        var target=definitions.get(key);
        if(target==null)return new Target(key,"",Reason.MISSING);
        if(state.completedResearch().contains(key))return new Target(key,key,Reason.KNOWN);
        var pending=new ArrayDeque<String>();pending.add(key);var seen=new HashSet<String>();String completedAncestor="";
        while(!pending.isEmpty()){
            var current=pending.removeFirst();if(!seen.add(current))continue;var d=definitions.get(current);if(d==null)continue;
            var category=books.categories().get(d.category());String gate=category==null?"":category.requiredResearch();
            boolean categoryVisible=gate.isEmpty()||state.completedResearch().contains(gate);
            if(categoryVisible&&d.available(state))return new Target(key,current,current.equals(key)?Reason.AVAILABLE:Reason.PREREQUISITES);
            if(categoryVisible&&!d.virtual()&&state.completedResearch().contains(current)&&completedAncestor.isEmpty())completedAncestor=current;
            for(String parent:d.parents())if(!state.completedResearch().contains(parent))pending.addLast(parent);
            for(String parent:d.hiddenParents())if(!state.completedResearch().contains(parent))pending.addLast(parent);
            if(!categoryVisible)pending.addLast(gate);
        }
        // A hidden/trigger-gated node with no reachable unfinished parents stays undiscovered.
        return new Target(key,completedAncestor,Reason.UNDISCOVERED);
    }
    private ResearchNavigation(){}
}
