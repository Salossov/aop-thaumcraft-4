package thaumcraft.api;

import java.util.*;
import thaumcraft.api.knowledge.PlayerKnowledge;
import thaumcraft.api.research.*;

final class LinkedResearchTest {
    private static int checks;
    private static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
    private static void rejects(Runnable run,String message){try{run.run();throw new AssertionError(message);}catch(IllegalArgumentException expected){checks++;}}
    private static ResearchDefinition ordinary(String key,List<String> parents,List<String> hidden){
        return new ResearchDefinition(key,"BASICS",parents,Map.of("aer",3),2,false,false,1,2,List.of("tc.native.text"),List.of("NativeRecipe"),"minecraft:diamond",false,true,false,true,List.of(),List.of("tc.native.text","group"),false,3,List.of(),List.of(),List.of(),hidden,Map.of("tc.native.text","SECRET"),true,Map.of("group",List.of("NativeRecipe")));
    }
    private static ResearchDefinition alias(String key,String target){
        return new ResearchDefinition(key,"example:links",List.of(),Map.of(),0,false,false,-2,4,List.of(),List.of(),"minecraft:air",false,false,true,false,List.of(),List.of(),true,0,List.of(),List.of(),List.of(),List.of(),Map.of(),false,Map.of(),target);
    }
    static void run(){
        var target=ordinary("NATIVE",List.of("BASE"),List.of("SECRET"));
        var raw=new LinkedHashMap<String,ResearchDefinition>();raw.put("BASE",ordinary("BASE",List.of(),List.of()));raw.put("SECRET",ordinary("SECRET",List.of(),List.of()));raw.put("NATIVE",target);raw.put("example:alias",alias("example:alias","NATIVE").withLost(true));raw.put("example:chain",alias("example:chain","example:alias"));
        var resolved=ResearchCatalogRules.resolve(raw);var link=resolved.get("example:alias");
        check(resolved.get("NATIVE")==target&&target.category().equals("BASICS"),"native node is unchanged in its own category");
        check(link.category().equals("example:links")&&link.column()==-2&&link.row()==4&&link.stub()&&link.hidden(),"alias retains identity, map placement and flags");
        check(link.lost()&&!target.lost(),"linked presentation keeps the alias lost flag independently of its target");
        check(link.icon().equals(target.icon())&&link.textPages().equals(target.textPages())&&link.recipePages().equals(target.recipePages())&&link.pageOrder().equals(target.pageOrder())&&link.pageRequirements().equals(target.pageRequirements())&&link.recipeGroups().equals(target.recipeGroups()),"all current target presentation and page gates delegate");
        check(ResearchLinks.target(resolved.get("example:chain"),resolved)==target,"translations and special icons use ultimate native identity");
        check(link.withHiddenParents(List.of("BASE")).linkedResearch().equals("NATIVE"),"dynamic parent copy preserves link");
        check(raw.get("example:alias").icon().equals("minecraft:air"),"resolver never mutates raw alias");
        var state=new PlayerKnowledge();state.setPool("aer",11);state.setWarp(2,3,4,9);state.completeResearch("BASE");
        check(!target.available(state),"native hidden requirement still enforced");state.completeResearch("SECRET");check(target.available(state),"ordinary research becomes available through original prerequisites");
        check(!link.available(state),"alias cannot issue an independent note or purchase");
        var child=ordinary("CHILD",List.of("example:alias"),List.of("NATIVE"));state.completeResearch("example:alias");
        check(!child.available(state),"a forged alias completion cannot bypass child's real hidden prerequisite");
        state.completeResearch("NATIVE");check(ResearchLinks.completeAliases(state,resolved)&&state.completedResearch().contains("example:chain"),"native completion mirrors entire alias chain");
        check(state.pool(thaumcraft.api.aspects.Aspects.AIR)==11&&state.totalWarp()==9,"mirroring consumes no points and adds no warp");
        check(!ResearchLinks.completeAliases(state,resolved),"reconciliation is idempotent");
        var existing=new PlayerKnowledge();existing.completeResearch("NATIVE");check(ResearchLinks.completeAliases(existing,resolved)&&existing.completedResearch().contains("example:alias"),"completion from an old save reconciles without completing native research again");
        check(target.linkedResearch().isEmpty(),"existing constructor defaults to ordinary research");
        rejects(()->ResearchCatalogRules.resolve(Map.of("A",alias("A","MISSING"))),"missing active target rejected");
        rejects(()->ResearchCatalogRules.resolve(Map.of("A",alias("A","A"))),"self link rejected");
        rejects(()->ResearchCatalogRules.resolve(Map.of("A",alias("A","B"),"B",alias("B","A"))),"alias cycle rejected");
        rejects(()->ResearchCatalogRules.resolve(Map.of("A",alias("A","B"),"B",ordinary("B",List.of("A"),List.of()))),"mixed parent/alias dependency cycle rejected");
        check(ResearchLinks.resolve(resolved).equals(resolved),"resolving current immutable catalog twice is stable");
        System.out.println("TC4_LINKED_RESEARCH_RULES_PASS checks="+checks);
    }
}
