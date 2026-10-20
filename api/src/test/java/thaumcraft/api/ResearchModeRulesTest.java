package thaumcraft.api;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.resources.ResourceLocation;
import thaumcraft.api.book.BookCatalog;
import thaumcraft.api.knowledge.PlayerKnowledge;
import thaumcraft.api.research.ResearchApi;
import thaumcraft.api.research.ResearchDefinition;

public final class ResearchModeRulesTest {
    private static int checks;
    private static void check(boolean value,String label){checks++;if(!value)throw new AssertionError(label);}
    private static ResearchDefinition definition(String key,boolean secondary,Map<String,Integer> aspects){
        return new ResearchDefinition(key,"BASICS",List.of(),aspects,1,false,false,0,0,List.of(),List.of(),"",false,false,false,secondary,List.of(),List.of());
    }
    public static void main(String[] args){
        check(!ResearchApi.serverEasyMode()&&!ResearchApi.clientEasyMode(),"normal mode is the default on both sides");
        var first=new AtomicBoolean();var second=new AtomicBoolean();
        ResearchApi.registerEasyMode(ResourceLocation.parse("first:easy"),first::get);
        ResearchApi.registerEasyMode(ResourceLocation.parse("second:easy"),second::get);
        check(!ResearchApi.serverEasyMode(),"disabled addon requests preserve normal mode");
        first.set(true);check(ResearchApi.serverEasyMode(),"one enabled request selects global mode");
        second.set(true);first.set(false);check(ResearchApi.serverEasyMode(),"a disabled addon cannot veto another enabled request");
        var primary=definition("GOGGLES",false,Map.of("aer",2));
        var other=definition("other:primary",false,Map.of("aer",2));
        var secondary=definition("SECONDARY",true,Map.of("aer",2));
        check(!ResearchApi.usesAspectPurchase(primary,false),"normal primary research still needs a note");
        check(ResearchApi.usesAspectPurchase(secondary,false),"normal secondary research remains purchasable");
        check(ResearchApi.usesAspectPurchase(primary,true)&&ResearchApi.usesAspectPurchase(other,true),"easy mode covers core and other addon namespaces");
        check(!ResearchApi.usesAspectPurchase(definition("EMPTY",false,Map.of()),true),"empty aspect lists do not become free purchases");
        check(!primary.secondary(),"mode selection never rewrites research definitions");
        ResearchApi.accept(Map.of(primary.key(),primary),BookCatalog.EMPTY,new PlayerKnowledge(),false);
        check(!ResearchApi.clientEasyMode()&&ResearchApi.serverEasyMode(),"client uses its received server mode, not local addon requests");
        second.set(false);
        ResearchApi.accept(Map.of(primary.key(),primary),BookCatalog.EMPTY,new PlayerKnowledge(),true);
        check(ResearchApi.clientEasyMode()&&!ResearchApi.serverEasyMode(),"received easy mode also wins over locally disabled requests");
        ResearchApi.accept(Map.of(),BookCatalog.EMPTY,new PlayerKnowledge());
        check(!ResearchApi.clientEasyMode(),"legacy snapshot/reset clears mode between servers");
        boolean duplicate=false;
        try{ResearchApi.registerEasyMode(ResourceLocation.parse("first:easy"),()->false);}catch(IllegalArgumentException expected){duplicate=true;}
        check(duplicate,"duplicate registration rejected");
        ResearchApi.freezeModes();boolean late=false;
        try{ResearchApi.registerEasyMode(ResourceLocation.parse("late:easy"),()->true);}catch(IllegalStateException expected){late=true;}
        check(late,"startup registration closes with the addon catalog");
        System.out.println("TC4_RESEARCH_MODE_RULES_PASS checks="+checks);
    }
}
