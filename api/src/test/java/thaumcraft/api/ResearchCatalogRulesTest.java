package thaumcraft.api;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.resources.ResourceLocation;
import thaumcraft.api.research.ResearchCatalogRules;
import thaumcraft.api.research.ResearchDefinition;

public final class ResearchCatalogRulesTest {
    private static int checks;
    private static void check(boolean value,String label) {checks++;if(!value)throw new AssertionError(label);}
    private static ResearchDefinition definition(String key,List<String> parents) {return new ResearchDefinition(key,"BASICS",parents,Map.of(),1,false,false,0,0,List.of(),List.of(),"");}
    private static void rejects(Runnable action,String label) {boolean failed=false;try{action.run();}catch(IllegalArgumentException|IllegalStateException|UnsupportedOperationException expected){failed=true;}check(failed,label);}
    public static void main(String[] args) {
        var mode=new AtomicInteger();var observations=new ArrayList<List<String>>();
        ResearchCatalogRules.register(ResourceLocation.parse("test:dynamic"),(definition,catalog)->{
            if(!definition.key().equals("KAMI"))return List.of();
            if(mode.get()==1)return List.of("MISSING");
            return catalog.containsKey("LATE")?List.of("LATE","BASE","LATE"):List.of("BASE");
        });
        ResearchCatalogRules.register(ResourceLocation.parse("test:observer"),(definition,catalog)->{
            if(definition.key().equals("KAMI"))observations.add(catalog.get("KAMI").hiddenParents());return List.of();
        });
        rejects(()->ResearchCatalogRules.register(ResourceLocation.parse("test:dynamic"),(d,c)->List.of()),"duplicate registration rejected");
        var base=definition("BASE",List.of());
        var kami=new ResearchDefinition("KAMI","ARTIFICE",List.of("BASE"),Map.of("ordo",2),5,false,true,14,2,List.of("page:text"),List.of("recipe:a","recipe:b"),"item:icon",false,true,false,true,List.of("SIBLING"),List.of("page:text","group:pair"),true,2,List.of("minecraft:stone"),List.of("minecraft:zombie"),List.of("ignis"),List.of("BASE"),Map.of("page:text","BASE"),true,Map.of("group:pair",List.of("recipe:a","recipe:b")));
        var input=new LinkedHashMap<String,ResearchDefinition>();input.put("BASE",base);input.put("KAMI",kami);input.put("LATE",definition("LATE",List.of("BASE")));
        var resolved=ResearchCatalogRules.resolve(input);
        check(resolved.get("KAMI").hiddenParents().equals(List.of("BASE","LATE")),"existing order and deduplicated dynamic requirements");
        check(observations.getFirst().equals(List.of("BASE")),"later rule sees original catalog rather than another rule's output");
        check(input.get("KAMI")==kami&&kami.hiddenParents().equals(List.of("BASE")),"input definitions are never mutated");
        check(resolved.get("KAMI").withHiddenParents(kami.hiddenParents()).equals(kami),"copy preserves every non-parent record field");
        check(new ArrayList<>(resolved.keySet()).equals(new ArrayList<>(input.keySet())),"catalog registration order retained");
        rejects(()->resolved.put("NEW",base),"output catalog immutable");
        check(ResearchCatalogRules.resolve(resolved).get("KAMI").equals(resolved.get("KAMI")),"reapplying additive rule does not duplicate requirements");
        input.remove("LATE");check(ResearchCatalogRules.resolve(input).get("KAMI").hiddenParents().equals(List.of("BASE")),"reload/other world drops requirements for removed definitions");
        mode.set(1);rejects(()->ResearchCatalogRules.resolve(input),"missing dynamic requirement rejected transactionally");
        check(kami.hiddenParents().equals(List.of("BASE")),"failed resolution leaves original intact");mode.set(0);
        rejects(()->ResearchCatalogRules.resolve(Map.of("A",definition("A",List.of("B")),"B",definition("B",List.of("A")))),"cycles rejected");
        rejects(()->ResearchCatalogRules.resolve(Map.of("A",definition("A",List.of("MISSING")))),"unknown original parent rejected");
        LinkedResearchTest.run();
        ResearchCatalogRules.freeze();rejects(()->ResearchCatalogRules.register(ResourceLocation.parse("test:late"),(d,c)->List.of()),"registration closes with addon registry");
        System.out.println("TC4_RESEARCH_CATALOG_RULES_PASS checks="+checks);
    }
}
