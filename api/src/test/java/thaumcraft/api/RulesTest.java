package thaumcraft.api;

import java.util.*;
import thaumcraft.api.aspects.*;
import thaumcraft.api.knowledge.PlayerKnowledge;
import thaumcraft.api.nodes.NodeState;
import thaumcraft.api.research.ResearchNote;

/** Headless checks of observable TC4 rules, with no Minecraft client or test framework. */
public final class RulesTest {
    private static int checks;
    private static void check(boolean value,String label) { checks++;if(!value) throw new AssertionError(label); }
    public static void main(String[] args) {
        check(Aspect.all().size()==48,"48 original aspects");check(Aspects.PRIMALS.size()==6,"six primals");
        check(Aspects.AIR.getTag().equals("aer")&&Aspects.AIR.getColor()==16777086,"original aer identity and color");
        check(Aspect.combine(Aspects.AIR,Aspects.FIRE)==Aspects.LIGHT,"aer + ignis = lux");
        check(Aspect.combine(Aspects.FIRE,Aspects.AIR)==Aspects.LIGHT,"combination is symmetric");
        check(Aspect.combine(Aspects.VOID,Aspects.ENERGY)==Aspects.MAGIC,"vacuos + potentia = praecantatio");
        check(Aspect.combine(Aspects.MAGIC,Aspects.AIR)==Aspects.AURA,"praecantatio + aer = auram");
        check(Aspect.combine(Aspects.AIR,Aspects.SOUL)==Aspects.SENSES,"aer + spiritus = sensus");
        for(var aspect:Aspect.all().values()) {
            check(aspect.getComponents().size()==(aspect.isPrimal()?0:2),"component count "+aspect);
            if(!aspect.isPrimal()) check(Aspect.combine(aspect.getComponents().get(0),aspect.getComponents().get(1))==aspect,"canonical combination "+aspect);
            check(!aspect.connects(aspect),"identical adjacent aspects are not a connection "+aspect);
        }
        var list=new AspectList().add(Aspects.AIR,3).merge(Aspects.AIR,2);check(list.getAmount(Aspects.AIR)==3,"merge uses maximum");
        list.add(Aspects.AIR,2);check(list.getAmount(Aspects.AIR)==5,"add sums");check(!list.reduce(Aspects.AIR,6),"insufficient reduction rejected");
        check(list.reduce(Aspects.AIR,5)&&list.size()==1&&list.getAmount(Aspects.AIR)==0,"zero discovery retained");
        list.add(Aspects.FIRE,3).remove(Aspects.FIRE,1);check(list.getAmount(Aspects.FIRE)==2,"remove decrements populated aspect");
        list.remove(Aspects.FIRE,2);check(list.size()==1,"remove deletes an exhausted entry while preserving other discovered zero");
        list.remove(Aspects.AIR);check(list.size()==0,"explicit remove deletes discovered zero");
        list.remove(Aspects.WATER,1);check(list.size()==0,"remove missing aspect does not introduce a negative entry");
        var knowledge=new PlayerKnowledge();check(knowledge.knows(Aspects.AIR)&&knowledge.pool(Aspects.AIR)==0,"primal discovery has no free points");
        check(knowledge.knowsParents(Aspects.LIGHT)&&!knowledge.knowsParents(Aspects.MAGIC),"scan component prerequisites");
        knowledge.addPool(Aspects.AIR,1);var copy=knowledge.copy();check(copy.spend(Aspects.AIR,1),"can spend final point");
        check(copy.knows(Aspects.AIR)&&knowledge.pool(Aspects.AIR)==1,"copy isolation and discovered zero");
        knowledge.recordScan(PlayerKnowledge.ScanKind.OBJECT,"stone");check(!knowledge.recordScan(PlayerKnowledge.ScanKind.OBJECT,"stone"),"scan identity deduplicated");
        check(knowledge.recordScan(PlayerKnowledge.ScanKind.ENTITY,"stone"),"scan classes independent");
        knowledge.setWarp(-1,2,-3,7);check(knowledge.totalWarp()==2&&knowledge.warpCounter()==7,"warp components and raw counter");
        var grid=new LinkedHashMap<ResearchNote.Hex,ResearchNote.Entry>();
        grid.put(new ResearchNote.Hex(0,0),new ResearchNote.Entry("aer",1));grid.put(new ResearchNote.Hex(1,0),new ResearchNote.Entry("aer",1));
        var note=new ResearchNote("TEST",grid,false,0);check(!note.checkComplete(Set.of("aer")),"same-aspect anchors fail");
        grid.put(new ResearchNote.Hex(1,0),new ResearchNote.Entry("lux",1));note=new ResearchNote("TEST",grid,false,0);
        check(!note.checkComplete(Set.of("aer")),"unknown compound fails");check(note.checkComplete(Set.of("aer","lux")),"direct parent connection succeeds");
        var generated=ResearchNote.create("GOGGLES",1,List.of(Aspects.SENSES,Aspects.AURA,Aspects.MAGIC),new Random(42));
        check(generated.grid.size()==19,"complexity one has radius two and nineteen cells");
        check(generated.grid.values().stream().filter(e->e.type()==1).count()==3,"one anchor per research aspect");
        var second=generated.copy();second.grid.clear();check(generated.grid.size()==19,"note copy isolation");
        for(var modifier:NodeState.Modifier.values()) {
            var node=new NodeState();node.modifier=modifier;node.initialize(Map.of("aer",4));node.take(Aspects.AIR,1);
            int interval=node.rechargeInterval();for(int i=0;i<(interval==0?600:interval-1);i++) node.recharge(new Random(i),1000);
            check(node.amount(Aspects.AIR)==3,"no early recharge "+modifier);
            if(interval>0) {node.recharge(new Random(1),1000);check(node.amount(Aspects.AIR)==4,"recharge exactly at interval "+modifier);}
        }
        var node=new NodeState();node.initialize(Map.of("aer",4));node.take(Aspects.AIR,4);node.ticks=1199;node.recharge(new Random(7),2000);
        check(node.base.get("aer")==3,"exhaustion permanently damages base vis before recharge");
        node=new NodeState();node.initialize(Map.of("aer",10));node.take(Aspects.AIR,3);node.lastActive=0;node.catchUp(new Random(1),600*75*2L);
        check(node.amount(Aspects.AIR)==9,"offline catch-up uses original 75ms multiplier");
        var columns=new thaumcraft.api.world.BiomeColumns();
        check(columns.set(0,0,"thaumcraft:tainted_land"),"exact column added");
        check(columns.get(1,0)==null&&columns.get(0,1)==null,"1x1 change leaves neighbours untouched inside the same quart");
        check(!columns.set(0,0,"thaumcraft:tainted_land"),"idempotent column change");
        check(columns.set(-1,-1,"thaumcraft:eerie"),"negative coordinates supported");
        check(columns.get(-2,-1)==null&&columns.get(-1,-1).equals("thaumcraft:eerie"),"negative chunk border precision");
        check(columns.chunk(-1,-1).cells().get(255).equals("thaumcraft:eerie"),"negative local coordinates wrap to 15");
        columns.set(16,0,"thaumcraft:magical_forest");
        check(columns.chunk(1,0).cells().size()==1&&columns.chunk(0,0).cells().size()==1,"chunk snapshots independent");
        var mirror=new thaumcraft.api.world.BiomeColumns();
        check(mirror.accept(0,0,columns.chunk(0,0)),"first full snapshot accepted");
        columns.set(0,0,"thaumcraft:eerie");mirror.accept(0,0,columns.chunk(0,0));
        check(!mirror.accept(0,0,new thaumcraft.api.world.BiomeColumns.Chunk(1,Map.of(0,"thaumcraft:tainted_land")))&&mirror.get(0,0).equals("thaumcraft:eerie"),"stale snapshot rejected");
        mirror.forget(0,0);check(mirror.get(0,0)==null,"unwatched chunk removed");
        mirror.accept(0,0,columns.chunk(0,0));mirror.clear();check(mirror.chunks().isEmpty(),"disconnect clears world mirror");
        System.out.println("TC4_R2_API_PASS checks="+checks);
    }
}
