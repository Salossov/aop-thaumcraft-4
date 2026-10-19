package thaumcraft.viewers.emi;
import java.util.*;
import dev.emi.emi.api.recipe.*;
import dev.emi.emi.api.stack.*;
import dev.emi.emi.api.widget.*;
import thaumcraft.api.crafting.RecipeDisplay;
import thaumcraft.viewers.*;
public final class EmiDisplay implements EmiRecipe {
 public final RecipeDisplay display;private final EmiRecipeCategory category;
 public EmiDisplay(RecipeDisplay r,EmiRecipeCategory category){display=r;this.category=category;}
 public EmiRecipeCategory getCategory(){return category;}
 public net.minecraft.resources.ResourceLocation getId(){return display.id().withPrefix("/thaumcraft_viewers/");}
 public List<EmiIngredient> getInputs(){var list=new ArrayList<EmiIngredient>();for(var i:display.inputs())if(!i.isEmpty())list.add(EmiIngredient.of(i));for(var e:new Layout(display).essence)if(!e.output())list.add(new EmiAspect(e.value()));return list;}
 public List<EmiStack> getOutputs(){var list=new ArrayList<EmiStack>();for(var stack:display.outputs())list.add(EmiStack.of(stack));for(var e:new Layout(display).essence)if(e.output())list.add(new EmiAspect(e.value()));return list;}
 public List<EmiIngredient> getCatalysts(){return display.catalysts().stream().map(s->(EmiIngredient)EmiStack.of(s)).toList();}
 public int getDisplayWidth(){return Layout.WIDTH;}
 public int getDisplayHeight(){return FittedWidgets.height(new Layout(display).height);}
 public boolean supportsRecipeTree(){return display.kind()!=RecipeDisplay.Kind.INFERNAL;}
 public void addWidgets(WidgetHolder widgets){var layout=new Layout(display);var fit=FittedWidgets.of(widgets,layout.height);widgets.addDrawable(0,0,getDisplayWidth(),getDisplayHeight(),(g,mx,my,delta)->{fit.begin(g);RecipeArt.draw(display,g);fit.end(g);});for(var item:layout.items){var values=item.stacks(display);if(values.isEmpty())continue;var stack=item.output()?EmiStack.of(values.getFirst()):EmiIngredient.of(values.stream().map(EmiStack::of).toList());var slot=fit.slot(widgets,stack,item.x(),item.y());if(item.output())slot.recipeContext(this);}for(var e:layout.essence){var slot=fit.slot(widgets,new EmiAspect(e.value()),e.x(),e.y());if(e.output())slot.recipeContext(this);}
  var footer=fit.bounds(0,layout.footer,Layout.WIDTH,12);widgets.addTooltipText(Catalog.details(display),footer.x(),footer.y(),footer.width(),footer.height());
  for(var card:layout.cards)widgets.add(new Widget(){public Bounds getBounds(){return fit.bounds(card.x(),card.y(),36,18);}public void render(net.minecraft.client.gui.GuiGraphics g,int mx,int my,float delta){}public List<net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent> getTooltip(int x,int y){if(!getBounds().contains(x,y))return List.of();return RecipeArt.researchTooltip(card.research()).stream().map(c->net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent.create(c.getVisualOrderText())).toList();}public boolean mouseClicked(int x,int y,int button){if(button==0&&getBounds().contains(x,y)){thaumcraft.api.client.BookNavigation.open(card.research());return true;}return false;}});
 }
}
