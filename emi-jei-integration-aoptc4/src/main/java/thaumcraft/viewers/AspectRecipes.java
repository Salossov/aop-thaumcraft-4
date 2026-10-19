package thaumcraft.viewers;
import java.util.*;
import net.minecraft.world.item.ItemStack;
import thaumcraft.api.aspects.*;
import thaumcraft.api.research.ResearchApi;
/** Discovery-aware catalog shared by JEI and EMI. Sources always require a scan, even in reveal-all config. */
public final class AspectRecipes {
 public enum Kind {INDEX,COMBINATION,SOURCES}
 public record Entry(Kind kind,String aspect,int page,long revision){public Entry(Kind kind,String aspect,int page){this(kind,aspect,page,0);}}
 public record Source(ItemStack stack,int amount){}
 private static final Map<String,List<Source>> SOURCES=new HashMap<>();
 public static final int PAGE_SIZE=32;
 public static synchronized void invalidate(){SOURCES.clear();}
 public static List<AspectValue> values(){return Aspect.all().keySet().stream().map(a->new AspectValue(a,false,1)).toList();}
 public static synchronized List<Source> sources(String tag){if(!ResearchApi.clientKnowledge().knows(Aspect.getAspect(tag)))return List.of();return SOURCES.computeIfAbsent(tag,key->collectSources(key,Catalog.all(),AspectApi::scanned));}
 static List<Source> collectSources(String tag,List<thaumcraft.api.crafting.RecipeDisplay> recipes,java.util.function.Predicate<ItemStack> scanned){
  var found=new ArrayList<Source>();
  for(var r:recipes)if(r.kind()==thaumcraft.api.crafting.RecipeDisplay.Kind.DISTILLATION&&r.aspectOutputs().getOrDefault(tag,0)>0)
   for(var ingredient:r.inputs())for(var stack:ingredient.getItems())if(scanned.test(stack)&&found.stream().noneMatch(s->ItemStack.isSameItemSameComponents(s.stack(),stack)))found.add(new Source(stack.copyWithCount(1),r.aspectOutputs().get(tag)));
  found.sort(Comparator.comparingInt(Source::amount).reversed().thenComparing(s->net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(s.stack().getItem()).toString()));return List.copyOf(found);
 }
 public static synchronized List<Entry> entries(){var entries=new ArrayList<Entry>();entries.add(new Entry(Kind.INDEX,"",0));for(var a:Aspect.all().values())if(ResearchApi.clientKnowledge().knows(a)){entries.add(new Entry(Kind.COMBINATION,a.getTag(),0));int n=Math.max(1,(sources(a.getTag()).size()+PAGE_SIZE-1)/PAGE_SIZE);for(int p=0;p<n;p++)entries.add(new Entry(Kind.SOURCES,a.getTag(),p,sourceRevision(a.getTag())));}return List.copyOf(entries);}
 public static List<Source> page(Entry e){var all=sources(e.aspect());int start=Math.min(all.size(),e.page()*PAGE_SIZE);return all.subList(start,Math.min(all.size(),start+PAGE_SIZE));}
 private static long sourceRevision(String tag){long hash=1;for(var s:sources(tag))hash=31*hash+Objects.hash(net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(s.stack().getItem()),s.stack().getComponentsPatch(),s.amount());return hash;}
 public static String id(Entry e){return e.kind().name().toLowerCase(Locale.ROOT)+"/"+e.aspect().replace(':','/')+"/"+e.page()+"/"+Long.toUnsignedString(e.revision());}
 private AspectRecipes(){}
}
