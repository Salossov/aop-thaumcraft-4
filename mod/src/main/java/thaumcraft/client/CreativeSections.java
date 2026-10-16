package thaumcraft.client;

import java.util.*;
import net.minecraft.world.item.ItemStack;

/** Banner rows exist only in the client menu projection, never in the creative registry/search. */
public final class CreativeSections {
    private final NavigableMap<Integer,Integer> headers=new TreeMap<>();
    private final BitSet placeholders=new BitSet();
    public Map<Integer,Integer> headers(){return Collections.unmodifiableMap(headers);}
    public boolean placeholder(int index){return index>=0&&placeholders.get(index);}
    public List<ItemStack> project(List<ItemStack> items,boolean searching){
        headers.clear();placeholders.clear();if(searching||items.isEmpty())return items;
        var sections=new TreeMap<Integer,List<ItemStack>>();for(var stack:items)sections.computeIfAbsent(thaumcraft.registry.ThaumcraftContent.section(stack),key->new ArrayList<>()).add(stack);
        var result=new ArrayList<ItemStack>();for(var section:sections.entrySet()){
            headers.put(result.size()/9,section.getKey());for(int i=0;i<9;i++)blank(result);
            result.addAll(section.getValue());while(result.size()%9!=0)blank(result);
        }return result;
    }
    private void blank(List<ItemStack> items){placeholders.set(items.size());items.add(ItemStack.EMPTY);}
}
