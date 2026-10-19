package thaumcraft.viewers.jei;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import thaumcraft.api.crafting.RecipeDisplay;
import thaumcraft.viewers.*;
import mezz.jei.api.recipe.*;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
public final class JeiCategory implements IRecipeCategory<RecipeDisplay>{
 private final RecipeDisplay.Kind kind;
 public JeiCategory(RecipeDisplay.Kind kind){this.kind=kind;}
 public RecipeType<RecipeDisplay> getRecipeType(){return new RecipeType<>(Catalog.id(kind),RecipeDisplay.class);}
 public Component getTitle(){return Catalog.title(kind);}
 public int getWidth(){return Layout.WIDTH;}
 public int getHeight(){return FittedRecipe.height(Math.max(90,Catalog.all().stream().filter(r->r.kind()==kind).mapToInt(r->new Layout(r).height).max().orElse(90)));}
 public IDrawable getBackground(){return new IDrawable(){public int getWidth(){return JeiCategory.this.getWidth();}public int getHeight(){return JeiCategory.this.getHeight();}public void draw(GuiGraphics g,int x,int y){}};}
 public IDrawable getIcon(){return new IDrawable(){public int getWidth(){return 16;}public int getHeight(){return 16;}public void draw(GuiGraphics g,int x,int y){var station=Catalog.workstations().getOrDefault(kind,List.of());if(station.isEmpty())new AspectValue("aer",false,1).draw(g,x,y);else g.renderItem(station.getFirst(),x,y);}};}
 public void setRecipe(IRecipeLayoutBuilder b,RecipeDisplay r,IFocusGroup focus){var l=new Layout(r);var fit=FittedRecipe.of(getHeight(),l.height);for(var s:l.items)if(!s.stacks(r).isEmpty())b.addSlot(s.output()?RecipeIngredientRole.OUTPUT:RecipeIngredientRole.INPUT,fit.x(s.x()),fit.y(s.y())).addItemStacks(s.stacks(r)).setCustomRenderer(mezz.jei.api.constants.VanillaTypes.ITEM_STACK,fit.itemRenderer());for(var s:l.essence)b.addSlot(s.output()?RecipeIngredientRole.OUTPUT:RecipeIngredientRole.INPUT,fit.x(s.x()),fit.y(s.y())).addIngredient(JeiAspects.TYPE,s.value()).setCustomRenderer(JeiAspects.TYPE,fit.aspectRenderer());}
 public void draw(RecipeDisplay r,IRecipeSlotsView slots,GuiGraphics g,double mx,double my){var fit=FittedRecipe.of(getHeight(),new Layout(r).height);fit.begin(g);RecipeArt.draw(r,g);fit.end(g);}
 public List<Component> getTooltipStrings(RecipeDisplay r,IRecipeSlotsView slots,double mx,double my){var fit=FittedRecipe.of(getHeight(),new Layout(r).height);var research=RecipeArt.hit(r,fit.mouseX(mx),fit.mouseY(my));if(research.isPresent())return RecipeArt.researchTooltip(research.get());return fit.mouseY(my)>=new Layout(r).footer?Catalog.details(r):List.of();}
 public boolean handleInput(RecipeDisplay r,double mx,double my,com.mojang.blaze3d.platform.InputConstants.Key key){var fit=FittedRecipe.of(getHeight(),new Layout(r).height);var research=RecipeArt.hit(r,fit.mouseX(mx),fit.mouseY(my));if(key.getValue()==0&&research.isPresent()){thaumcraft.api.client.BookNavigation.open(research.get());return true;}return false;}
 public net.minecraft.resources.ResourceLocation getRegistryName(RecipeDisplay r){return r.id();}
}
