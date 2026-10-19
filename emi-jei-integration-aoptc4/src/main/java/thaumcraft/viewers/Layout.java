package thaumcraft.viewers;
import java.util.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import thaumcraft.api.crafting.RecipeDisplay;
/** Native book geometry: result above the grid/ritual, costs underneath, research icons in the header. */
public final class Layout {
 public static final int WIDTH=170;
 public record Item(int index,boolean output,int x,int y,int count){public List<ItemStack> stacks(RecipeDisplay r){return output?List.of(r.outputs().get(index)):Arrays.stream(r.inputs().get(index).getItems()).map(s->s.copyWithCount(count)).toList();}}
 public record Essence(AspectValue value,boolean output,int x,int y){}
 public record Card(String research,int x,int y){}
 public final List<Item> items=new ArrayList<>();public final List<Essence> essence=new ArrayList<>();public final List<Card> cards=new ArrayList<>();public final int footer,height,instabilityY;
 public Layout(RecipeDisplay r){int n=0;for(String key:r.research())cards.add(new Card(key,n++%2*40,13+(n-1)/2*20));int y;int instability=-1;
  for(int i=0;i<r.outputs().size();i++)items.add(new Item(i,true,77+i*20,20,1));
  switch(r.kind()){
   case ARCANE->{for(int i=0;i<r.inputs().size();i++)items.add(new Item(i,false,57+i%3*20,50+i/3*20,1));y=112;}
   case INFUSION->{if(!r.inputs().isEmpty())items.add(new Item(0,false,77,72,1));var groups=new ArrayList<int[]>();for(int i=1;i<r.inputs().size();i++){int index=-1;for(int g=0;g<groups.size();g++)if(same(r.inputs().get(i),r.inputs().get(groups.get(g)[0]))){index=g;break;}if(index<0)groups.add(new int[]{i,1});else groups.get(index)[1]++;}for(int i=0;i<groups.size();i++){int[] group=groups.get(i);int xx,yy;if(i<8){double angle=-Math.PI/2+2*Math.PI*i/Math.min(8,groups.size());xx=77+(int)Math.round(Math.cos(angle)*30);yy=72+(int)Math.round(Math.sin(angle)*30);}else{xx=7+(i-8)%8*20;yy=124+(i-8)/8*20;}items.add(new Item(group[0],false,xx,yy,group[1]));}int extra=groups.size()>8?((groups.size()-8+7)/8)*20:0;instability=127+extra;y=142+extra;}
   case CRUCIBLE->{for(int i=0;i<r.inputs().size();i++)items.add(new Item(i,false,77+i*20,50,1));y=86;}
   case CENTRIFUGE->{y=48;}
   case CRYSTALLIZATION->{y=68;}
   default->{for(int i=0;i<r.inputs().size();i++)items.add(new Item(i,false,(WIDTH-Math.min(8,r.inputs().size()-i/8*8)*20)/2+i%8*20,48+i/8*20,1));y=80+((Math.max(1,r.inputs().size())-1)/8)*20;}
  }
  int columns=r.kind()==RecipeDisplay.Kind.CRUCIBLE?3:7;y=append(r.essentia(),false,false,y,columns);y=append(r.vis(),true,false,y,7);if(r.kind()==RecipeDisplay.Kind.CENTRIFUGE)y=Math.max(92,y+14);y=append(r.aspectOutputs(),false,true,y,7);
  footer=Math.max(r.kind()==RecipeDisplay.Kind.CRUCIBLE?134:0,y+5);height=footer+12;instabilityY=instability;
 }
 private int append(Map<String,Integer> values,boolean vis,boolean output,int y,int columns){var tags=new TreeMap<>(values).entrySet().stream().toList();for(int i=0;i<tags.size();i++){int row=i/columns,count=Math.min(columns,tags.size()-row*columns);var entry=tags.get(i);essence.add(new Essence(new AspectValue(entry.getKey(),vis,entry.getValue()),output,(WIDTH-count*24)/2+i%columns*24,y+row*22));}return y+(tags.isEmpty()?0:((tags.size()+columns-1)/columns)*22+3);}
 private static boolean same(Ingredient a,Ingredient b){var aa=a.getItems();var bb=b.getItems();if(aa.length!=bb.length)return false;for(var stack:aa)if(Arrays.stream(bb).noneMatch(s->ItemStack.isSameItemSameComponents(stack,s)))return false;return true;}
}
