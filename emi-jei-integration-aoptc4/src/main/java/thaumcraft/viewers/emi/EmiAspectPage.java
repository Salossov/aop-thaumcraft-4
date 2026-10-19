package thaumcraft.viewers.emi;
import java.util.*;
import dev.emi.emi.api.recipe.*;
import dev.emi.emi.api.stack.*;
import dev.emi.emi.api.widget.*;
import thaumcraft.viewers.*;
public final class EmiAspectPage implements EmiRecipe {
 private final AspectRecipes.Entry entry;private final EmiRecipeCategory category;
 public EmiAspectPage(AspectRecipes.Entry e,EmiRecipeCategory category){entry=e;this.category=category;}
 public EmiRecipeCategory getCategory(){return category;}
 public net.minecraft.resources.ResourceLocation getId(){return net.minecraft.resources.ResourceLocation.parse("thaumcraft_viewers:/aspects/"+AspectRecipes.id(entry));}
 public List<EmiIngredient> getInputs(){var list=new ArrayList<EmiIngredient>();if(entry.kind()==AspectRecipes.Kind.COMBINATION)for(var s:AspectPages.slots(entry))if(!s.output())list.add(new EmiAspect(s.aspect()));if(entry.kind()==AspectRecipes.Kind.SOURCES)for(var s:AspectRecipes.page(entry))list.add(EmiStack.of(s.stack()));return list;}
 public List<EmiStack> getOutputs(){return entry.kind()==AspectRecipes.Kind.INDEX?List.of():List.of(new EmiAspect(new AspectValue(entry.aspect(),false,1)));}
 public int getDisplayWidth(){return 170;}
 public int getDisplayHeight(){return FittedWidgets.height(AspectPages.height(entry));}
 public boolean supportsRecipeTree(){return false;}
 public void addWidgets(WidgetHolder widgets){var fit=FittedWidgets.of(widgets,AspectPages.height(entry));widgets.addDrawable(0,0,170,getDisplayHeight(),(g,mx,my,delta)->{fit.begin(g);AspectPages.draw(entry,g);fit.end(g);});for(var s:AspectPages.slots(entry)){var slot=fit.slot(widgets,new EmiAspect(s.aspect()),s.x(),s.y());if(s.output())slot.recipeContext(this);}if(entry.kind()==AspectRecipes.Kind.SOURCES){int n=0;for(var source:AspectRecipes.page(entry)){fit.slot(widgets,EmiStack.of(source.stack()),5+n%8*20,48+n/8*20).appendTooltip(net.minecraft.network.chat.Component.translatable("thaumcraft_viewers.source_yield",source.amount()));n++;}}}
}
