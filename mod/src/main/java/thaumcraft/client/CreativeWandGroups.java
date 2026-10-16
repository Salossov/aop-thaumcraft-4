package thaumcraft.client;

import java.util.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import thaumcraft.items.CastingWandItem;
import thaumcraft.items.WandMaterials;

/** Client projection only: original creative stacks keep all their components. */
public final class CreativeWandGroups {
    public enum Mode { CAPACITY, TYPE, ROD, CAP, ALL }
    private Mode mode=Mode.CAPACITY;
    private final Set<String> expanded=new HashSet<>();
    private final Map<String,List<ItemStack>> groups=new LinkedHashMap<>();
    public Component modeName(){return Component.translatable("thaumcraft.creative.group."+mode.name().toLowerCase(Locale.ROOT));}
    public void next(){mode=Mode.values()[(mode.ordinal()+1)%Mode.values().length];expanded.clear();}
    private String key(ItemStack stack){
        if(!(stack.getItem() instanceof CastingWandItem wand)||mode==Mode.ALL)return null;
        var parts=WandMaterials.parts(stack);return switch(mode){case CAPACITY->String.valueOf(thaumcraft.items.WandVis.capacity(stack));case TYPE->wand.kind().name();case ROD->parts.rod();case CAP->parts.cap();default->null;};
    }
    public List<ItemStack> project(Collection<ItemStack> source){
        groups.clear();for(var stack:source){var key=key(stack);if(key!=null)groups.computeIfAbsent(key,k->new ArrayList<>()).add(stack);}
        var emitted=new HashSet<String>();var result=new ArrayList<ItemStack>();
        for(var stack:source){var key=key(stack);if(key==null)result.add(stack);else if(mode==Mode.CAPACITY){if(emitted.isEmpty())for(var capacity:groups.keySet().stream().sorted(Comparator.comparingInt(Integer::parseInt)).toList()){emitted.add(capacity);var group=groups.get(capacity);if(expanded.contains(capacity))result.addAll(group);else result.add(group.getFirst());}}else if(emitted.add(key)){var group=groups.get(key);if(expanded.contains(key))result.addAll(group);else result.add(group.getFirst());}}
        return result;
    }
    public boolean toggle(ItemStack stack){var key=key(stack);if(key==null||!groups.containsKey(key))return false;if(!expanded.remove(key))expanded.add(key);return true;}
    public int count(ItemStack stack){var key=key(stack);return key==null?0:groups.getOrDefault(key,List.of()).size();}
    public boolean sameGroup(ItemStack first,ItemStack second){var key=key(first);return key!=null&&key.equals(key(second));}
    public boolean collapsed(ItemStack stack){var key=key(stack);return key!=null&&!expanded.contains(key)&&count(stack)>1;}
    public Component description(ItemStack stack){var key=key(stack);if(key==null)return Component.empty();var label=switch(mode){case CAPACITY->Component.translatable("thaumcraft.creative.capacity",java.math.BigDecimal.valueOf(Integer.parseInt(key),2).stripTrailingZeros().toPlainString());case TYPE->Component.translatable("thaumcraft.creative.kind."+key.toLowerCase(Locale.ROOT));case ROD->Component.translatable("thaumcraft.wand.rod."+key);case CAP->Component.translatable("thaumcraft.wand.cap."+key);default->Component.empty();};return Component.translatable(expanded.contains(key)?"thaumcraft.creative.collapse":"thaumcraft.creative.expand",label,count(stack));}
}
