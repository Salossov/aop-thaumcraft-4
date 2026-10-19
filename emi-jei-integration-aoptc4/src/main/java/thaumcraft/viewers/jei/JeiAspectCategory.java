package thaumcraft.viewers.jei;
import java.util.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import thaumcraft.viewers.*;
import mezz.jei.api.recipe.*;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
public final class JeiAspectCategory implements mezz.jei.api.recipe.category.IRecipeCategory<AspectRecipes.Entry>{
 private final AspectRecipes.Kind kind;
 public JeiAspectCategory(AspectRecipes.Kind kind){this.kind=kind;}
 public static RecipeType<AspectRecipes.Entry> type(AspectRecipes.Kind kind){return new RecipeType<>(ResourceLocation.parse("thaumcraft_viewers:aspects/"+kind.name().toLowerCase(Locale.ROOT)),AspectRecipes.Entry.class);}
 public RecipeType<AspectRecipes.Entry> getRecipeType(){return type(kind);}
 public Component getTitle(){return AspectPages.title(kind);}
 public int getWidth(){return 170;}
 public int getHeight(){return AspectPages.height(new AspectRecipes.Entry(kind,"aer",0));}
 public IDrawable getBackground(){return new IDrawable(){public int getWidth(){return 170;}public int getHeight(){return JeiAspectCategory.this.getHeight();}public void draw(GuiGraphics g,int x,int y){}};}
 public IDrawable getIcon(){return new IDrawable(){public int getWidth(){return 16;}public int getHeight(){return 16;}public void draw(GuiGraphics g,int x,int y){new AspectValue("aer",false,1).draw(g,x,y);}};}
 public void setRecipe(IRecipeLayoutBuilder b,AspectRecipes.Entry e,IFocusGroup focus){for(var slot:AspectPages.slots(e))b.addSlot(e.kind()==AspectRecipes.Kind.INDEX?RecipeIngredientRole.RENDER_ONLY:slot.output()?RecipeIngredientRole.OUTPUT:RecipeIngredientRole.INPUT,slot.x(),slot.y()).addIngredient(JeiAspects.TYPE,slot.aspect());if(e.kind()==AspectRecipes.Kind.SOURCES){int n=0;for(var source:AspectRecipes.page(e)){b.addSlot(RecipeIngredientRole.INPUT,5+n%8*20,48+n/8*20).addItemStack(source.stack()).addTooltipCallback((view,lines)->lines.add(Component.translatable("thaumcraft_viewers.source_yield",source.amount())));n++;}}}
 public void draw(AspectRecipes.Entry e,IRecipeSlotsView slots,GuiGraphics g,double mx,double my){AspectPages.draw(e,g);}
 public ResourceLocation getRegistryName(AspectRecipes.Entry e){return ResourceLocation.parse("thaumcraft_viewers:aspects/"+AspectRecipes.id(e));}
}
