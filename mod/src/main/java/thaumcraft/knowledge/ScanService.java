package thaumcraft.knowledge;

import java.util.Map;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.knowledge.PlayerKnowledge;
import thaumcraft.config.ThaumcraftConfig;

public final class ScanService {
    public static boolean scan(ServerPlayer player,PlayerKnowledge.ScanKind kind,String key,Map<String,Integer> aspects) {
        var current=KnowledgeService.get(player);
        if(current.scanned(kind).contains(key)) { thaumcraft.network.Notify.send(player,"tc.alreadyscanned","",0xffffff,false);return false; }
        if(aspects.isEmpty()) { thaumcraft.network.Notify.send(player,"tc.unknownobject","",0xff5555,false);return false; }
        for(var tag:aspects.keySet()) {
            var aspect=Aspect.getAspect(tag);if(!current.knowsParents(aspect)) {
                var missing=aspect.getComponents().stream().filter(a->!current.knows(a)).findFirst().orElseThrow();
                thaumcraft.network.Notify.send(player,"tc.discoveryerror","tc.aspect.help."+missing.getTag(),0xaa00aa,true);return false;
            }
        }
        boolean ok=KnowledgeService.mutate(player,k->{if(!k.recordScan(kind,key)) return false;aspects.forEach((tag,amount)->reward(k,Aspect.getAspect(tag),amount));discoverHidden(player,k,kind,key,aspects);return true;});
        return ok;
    }
    private static void discoverHidden(ServerPlayer player,PlayerKnowledge knowledge,PlayerKnowledge.ScanKind kind,String key,Map<String,Integer> aspects){
        var matches=thaumcraft.data.ThaumcraftData.get(player.server).research().values().stream()
            .filter(definition->definition.hidden()&&!definition.aspects().isEmpty()&&!knowledge.completedResearch().contains(definition.key())&&!knowledge.completedResearch().contains("@"+definition.key()))
            .filter(definition->definition.aspectTriggers().stream().anyMatch(tag->aspects.getOrDefault(tag,0)>0)
                ||definition.itemTriggers().contains(key)||definition.entityTriggers().contains(key)).map(thaumcraft.api.research.ResearchDefinition::key).sorted().toList();
        if(!matches.isEmpty()){String found=matches.get(player.getRandom().nextInt(matches.size()));knowledge.completeResearch("@"+found);thaumcraft.network.Notify.send(player,"tc.addclue","",0x00aa00,true);}
    }
    public static int reward(PlayerKnowledge knowledge,Aspect aspect,int amount) {
        if(!knowledge.knows(aspect)) amount+=2;
        int cap=ThaumcraftConfig.ASPECT_TOTAL_CAP.get();
        if(knowledge.pool(aspect)>=cap) amount=(int)Math.sqrt(amount);
        if(amount>1&&knowledge.pool(aspect)>=cap*1.25f) amount=1;
        knowledge.addPool(aspect,amount);return amount;
    }
    /** Original: an aspect appearing in a scan it wasn't known from posts a discovery notice. */
    public static void notifyDiscoveries(ServerPlayer player,PlayerKnowledge before,PlayerKnowledge after){
        for(var tag:after.aspectPools().keySet())if(!before.aspectPools().containsKey(tag)){
            var aspect=Aspect.getAspect(tag);if(aspect!=null)thaumcraft.network.Notify.discovery(player,aspect);
        }
    }
    private ScanService() {}
}
