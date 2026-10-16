package thaumcraft.items;

import java.util.*;
import net.minecraft.world.item.ItemStack;
import thaumcraft.api.aspects.*;
import thaumcraft.api.wands.*;

public final class ThaumicFocusItem extends ItemFocusBasic {
    public enum Kind { FIRE,FROST,SHOCK,EXCAVATION,TRADE,HELLBAT,PORTABLE_HOLE,WARDING,PRIMAL }
    private final Kind kind;
    public ThaumicFocusItem(Kind kind){this.kind=kind;}
    public Kind kind(){return kind;}
    private Map<Aspect,Integer> cost(Object... values){var result=new LinkedHashMap<Aspect,Integer>();for(int i=0;i<values.length;i+=2)result.put(Aspect.getAspect((String)values[i]),(Integer)values[i+1]);return result;}
    @Override public Map<Aspect,Integer> getVisCost(ItemStack stack){return switch(kind){
        case FIRE->isUpgradedWith(stack,FocusUpgradeType.fireball)?cost("ignis",66,"perditio",33):isUpgradedWith(stack,FocusUpgradeType.firebeam)?cost("ignis",10,"ordo",3):cost("ignis",10);
        case FROST->isUpgradedWith(stack,FocusUpgradeType.scattershot)?cost("aqua",20,"ignis",2,"perditio",2,"aer",5):isUpgradedWith(stack,FocusUpgradeType.iceboulder)?cost("aqua",20,"ignis",2,"perditio",2,"terra",5):cost("aqua",5,"ignis",2,"perditio",2);
        case SHOCK->isUpgradedWith(stack,FocusUpgradeType.chainlightning)?cost("aer",40,"aqua",10):isUpgradedWith(stack,FocusUpgradeType.earthshock)?cost("aer",75,"terra",25):cost("aer",25);
        case EXCAVATION->isUpgradedWith(stack,FocusUpgradeType.silktouch)?cost("terra",16,"aer",1,"ignis",1,"aqua",1,"ordo",1,"perditio",1):isUpgradedWith(stack,FocusUpgradeType.dowsing)?cost("terra",15,"ignis",2,"ordo",2):cost("terra",15);
        case TRADE->isUpgradedWith(stack,FocusUpgradeType.silktouch)?cost("perditio",6,"terra",6,"ordo",6,"aer",1,"ignis",1,"aqua",1):cost("perditio",5,"terra",5,"ordo",5);
        case HELLBAT->isUpgradedWith(stack,FocusUpgradeType.batbombs)?cost("ignis",100,"perditio",200,"aer",100):isUpgradedWith(stack,FocusUpgradeType.devilbats)?cost("ignis",100,"perditio",100,"aer",100,"terra",100):cost("ignis",200,"perditio",100,"aer",100);
        case PORTABLE_HOLE->cost("perditio",10,"aer",10);case WARDING->cost("terra",25,"ordo",25,"aqua",10);
        case PRIMAL->primalCost(System.currentTimeMillis());
    };}
    /** TC4 uses one 200 ms wall-clock seed for both the visible quote and actual cast. */
    public static Map<Aspect,Integer> primalCost(long timeMillis){
        var random=new java.util.Random(timeMillis/200L);var result=new LinkedHashMap<Aspect,Integer>();
        for(var aspect:List.of(Aspects.WATER,Aspects.AIR,Aspects.EARTH,Aspects.FIRE,Aspects.ORDER,Aspects.ENTROPY))result.put(aspect,50+random.nextInt(5)*50);
        return result;
    }
    public Map<Aspect,Integer> activationCost(ItemStack stack,net.minecraft.util.RandomSource random){return getVisCost(stack);}
    @Override public boolean isVisCostPerTick(ItemStack stack){return kind==Kind.FIRE&&!isUpgradedWith(stack,FocusUpgradeType.fireball)||kind==Kind.EXCAVATION;}
    @Override public int getActivationCooldown(ItemStack stack){return switch(kind){case HELLBAT->1000;case PRIMAL->500;case FROST->isUpgradedWith(stack,FocusUpgradeType.scattershot)||isUpgradedWith(stack,FocusUpgradeType.iceboulder)?500:200;case SHOCK->isUpgradedWith(stack,FocusUpgradeType.chainlightning)?500:isUpgradedWith(stack,FocusUpgradeType.earthshock)?1000:250;case FIRE->isUpgradedWith(stack,FocusUpgradeType.fireball)?1000:0;default->0;};}
    @Override public int getFocusColor(ItemStack stack){return switch(kind){case FIRE->15028484;case FROST->5204428;case SHOCK->10466239;case EXCAVATION->409606;case TRADE->8747923;case HELLBAT->14431746;case PORTABLE_HOLE->594985;case WARDING->0xfc9e00;case PRIMAL->10854849;};}
    @Override public List<FocusUpgradeType> getPossibleUpgradesByRank(ItemStack stack,int rank){if(rank<1||rank>5)return List.of();var choices=new ArrayList<FocusUpgradeType>();choices.add(FocusUpgradeType.frugal);switch(kind){
        case PORTABLE_HOLE->{choices.add(FocusUpgradeType.enlarge);choices.add(FocusUpgradeType.extend);}
        case PRIMAL->{if(rank==3)choices.add(FocusUpgradeType.seeker);}
        case WARDING->{if(rank==2)choices.add(FocusUpgradeType.architect);if(rank>=3)choices.add(FocusUpgradeType.enlarge);}
        case HELLBAT->{choices.add(FocusUpgradeType.potency);if(rank==3){choices.add(FocusUpgradeType.batbombs);choices.add(FocusUpgradeType.devilbats);}if(rank==5)choices.add(FocusUpgradeType.vampirebats);}
        case FIRE->{choices.add(FocusUpgradeType.potency);if(rank==3){choices.add(FocusUpgradeType.fireball);choices.add(FocusUpgradeType.firebeam);}if(rank==2||rank==4)choices.add(FocusUpgradeType.alchemistsfire);}
        case FROST->{choices.add(FocusUpgradeType.potency);if(rank==3){choices.add(FocusUpgradeType.scattershot);choices.add(FocusUpgradeType.iceboulder);}if(rank%2==1)choices.add(FocusUpgradeType.alchemistsfrost);}
        case SHOCK->{choices.add(FocusUpgradeType.potency);if(rank==3){choices.add(FocusUpgradeType.chainlightning);choices.add(FocusUpgradeType.earthshock);}if(rank>=4)choices.add(FocusUpgradeType.enlarge);}
        case EXCAVATION->{choices.add(FocusUpgradeType.potency);choices.add(rank%2==1?FocusUpgradeType.treasure:FocusUpgradeType.enlarge);if(rank==3)choices.add(FocusUpgradeType.dowsing);if(rank==5)choices.add(FocusUpgradeType.silktouch);}
        case TRADE->{choices.add(FocusUpgradeType.enlarge);if(rank==3){choices.add(FocusUpgradeType.treasure);choices.add(FocusUpgradeType.architect);}if(rank==5)choices.add(FocusUpgradeType.silktouch);}
    }return choices;}
    @Override public boolean canApplyUpgrade(ItemStack stack,FocusUpgradeType upgrade,int rank){
        if(!super.canApplyUpgrade(stack,upgrade,rank))return false;
        if(upgrade==FocusUpgradeType.treasure&&isUpgradedWith(stack,FocusUpgradeType.silktouch)||upgrade==FocusUpgradeType.silktouch&&isUpgradedWith(stack,FocusUpgradeType.treasure))return false;
        if(kind==Kind.FIRE&&upgrade==FocusUpgradeType.alchemistsfire&&isUpgradedWith(stack,FocusUpgradeType.fireball)&&isUpgradedWith(stack,FocusUpgradeType.alchemistsfire))return false;
        if(upgrade==FocusUpgradeType.enlarge){
            if(kind==Kind.SHOCK&&!isUpgradedWith(stack,FocusUpgradeType.chainlightning)&&!isUpgradedWith(stack,FocusUpgradeType.earthshock))return false;
            if(kind==Kind.WARDING&&!isUpgradedWith(stack,FocusUpgradeType.architect))return false;
        }
        return true;
    }
    public boolean canApplyForPlayer(ItemStack stack,net.minecraft.world.entity.player.Player player,FocusUpgradeType upgrade,int rank){
        if(!canApplyUpgrade(stack,upgrade,rank))return false;
        if(kind!=Kind.HELLBAT||upgrade!=FocusUpgradeType.vampirebats)return true;
        if(player.level().isClientSide)return thaumcraft.network.ClientKnowledge.snapshot().completedResearch().contains("VAMPBAT");
        return player instanceof net.minecraft.server.level.ServerPlayer server&&thaumcraft.knowledge.KnowledgeService.get(server).completedResearch().contains("VAMPBAT");
    }
}
