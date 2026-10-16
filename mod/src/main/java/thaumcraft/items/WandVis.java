package thaumcraft.items;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import thaumcraft.api.aspects.Aspect;

/** TC4 stores centivis: 100 units per displayed vis, iron/wood capacity 2500. */
public final class WandVis {
    public static final int CAPACITY=2500;
    public static int capacity(ItemStack stack) { if(stack.getItem() instanceof ThaumicCurioItem curio&&curio.storesVis())return curio.capacity();var rod=WandMaterials.rod(stack);return (rod==null?25:rod.getCapacity())*(WandMaterials.sceptre(stack)?150:100); }
    public static int get(ItemStack stack,Aspect aspect) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getCompound("thaumcraftVis").getInt(aspect.getTag());
    }
    public static void set(ItemStack stack,Aspect aspect,int amount) {
        if(!aspect.isPrimal()||amount<0||amount>capacity(stack)) throw new IllegalArgumentException("Invalid wand vis");
        CustomData.update(DataComponents.CUSTOM_DATA,stack,root->{var values=root.getCompound("thaumcraftVis");values.putInt(aspect.getTag(),amount);root.put("thaumcraftVis",values);});
    }
    public static int add(ItemStack stack,Aspect aspect,int wholeVis) {
        if(!aspect.isPrimal()) return wholeVis;int total=get(stack,aspect)+wholeVis*100;
        set(stack,aspect,Math.min(capacity(stack),total));return Math.max(0,total-capacity(stack))/100;
    }
    public static Map<Aspect,Integer> cost(Map<String,Integer> wholeVis,float discount) {
        var result=new LinkedHashMap<Aspect,Integer>();wholeVis.forEach((tag,amount)->result.put(Aspect.getAspect(tag),(int)(amount*100*Math.max(0.1f,1.1f-discount))));return result;
    }
    public static Map<Aspect,Integer> cost(ItemStack stack,Map<String,Integer> wholeVis,float discount) {
        var result=new LinkedHashMap<Aspect,Integer>();wholeVis.forEach((tag,amount)->{var aspect=Aspect.getAspect(tag);if(aspect==null||!aspect.isPrimal()||amount<=0)throw new IllegalArgumentException("Invalid wand cost");result.put(aspect,(int)(amount*100*WandMaterials.modifier(stack,aspect,discount)));});return result;
    }
    public static boolean consume(ItemStack stack,Map<String,Integer> wholeVis,float discount,boolean commit) {
        if(!(stack.getItem() instanceof CastingWandItem)||wholeVis.isEmpty()) return false;
        var costs=cost(stack,wholeVis,discount);for(var entry:costs.entrySet()) if(get(stack,entry.getKey())<entry.getValue()) return false;
        if(commit) costs.forEach((aspect,amount)->set(stack,aspect,get(stack,aspect)-amount));return true;
    }
    public static boolean consume(ItemStack stack,Map<String,Integer> wholeVis,net.minecraft.world.entity.player.Player player,boolean commit){
        if(!(stack.getItem() instanceof CastingWandItem)||wholeVis.isEmpty())return false;
        var costs=cost(stack,wholeVis,player);
        for(var entry:costs.entrySet())if(get(stack,entry.getKey())<entry.getValue())return false;
        if(commit)costs.forEach((aspect,n)->set(stack,aspect,get(stack,aspect)-n));return true;
    }
    public static Map<Aspect,Integer> cost(ItemStack stack,Map<String,Integer> wholeVis,net.minecraft.world.entity.player.Player player){
        var costs=new LinkedHashMap<Aspect,Integer>();wholeVis.forEach((tag,n)->{var aspect=Aspect.getAspect(tag);if(aspect==null||!aspect.isPrimal()||n<=0)throw new IllegalArgumentException("Invalid wand cost");costs.put(aspect,(int)(n*100*WandMaterials.modifier(stack,aspect,discount(player,aspect))));});return costs;
    }
    public static float discount(net.minecraft.world.entity.player.Player player) {
        return discount(player,null);
    }
    public static float discount(net.minecraft.world.entity.player.Player player,Aspect aspect) {
        float amount=0;
        for(var stack:player.getArmorSlots())if(stack.getItem() instanceof thaumcraft.api.IVisDiscountGear gear)amount+=gear.getVisDiscount(stack,player,aspect)*.01f;
        for(var stack:EquippedCurios.stacks(player))if(stack.getItem() instanceof thaumcraft.api.IVisDiscountGear gear)amount+=gear.getVisDiscount(stack,player,aspect)*.01f;
        var exhausted=player.getEffect(thaumcraft.effects.TaintEffects.VIS_EXHAUST);
        var infectious=player.getEffect(thaumcraft.effects.WarpEffects.INFECTIOUS_EXHAUST);
        int severity=Math.max(exhausted==null?-1:exhausted.getAmplifier(),infectious==null?-1:infectious.getAmplifier());
        return amount-(severity+1)*.1f;
    }
    /** TC4 checks a vis amulet first, then wands from the last inventory slot backwards. Costs are centivis. */
    public static boolean consumeInventory(net.minecraft.world.entity.player.Player player,Map<Aspect,Integer> amounts){
        var sources=new java.util.ArrayList<ItemStack>();
        for(var stack:EquippedCurios.stacks(player))if(stack.getItem() instanceof ThaumicCurioItem curio&&curio.storesVis())sources.add(stack);
        for(int i=player.getInventory().getContainerSize()-1;i>=0;i--){var stack=player.getInventory().getItem(i);if(stack.getItem() instanceof CastingWandItem)sources.add(stack);}
        for(var source:sources){
            var costs=new LinkedHashMap<Aspect,Integer>();boolean enough=true;
            for(var entry:amounts.entrySet()){
                int n=source.getItem() instanceof CastingWandItem?(int)(entry.getValue()*WandMaterials.modifier(source,entry.getKey(),discount(player,entry.getKey()))):entry.getValue();
                if(get(source,entry.getKey())<n){enough=false;break;}costs.put(entry.getKey(),n);
            }
            if(enough){costs.forEach((aspect,n)->set(source,aspect,get(source,aspect)-n));return true;}
        }return false;
    }
    private WandVis() {}
}
