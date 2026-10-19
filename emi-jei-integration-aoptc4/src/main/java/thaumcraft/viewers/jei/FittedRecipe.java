package thaumcraft.viewers.jei;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import mezz.jei.api.ingredients.IIngredientRenderer;
import thaumcraft.viewers.AspectValue;

/** Keeps native diagrams and JEI ingredient hitboxes in the same fitted coordinate space. */
public record FittedRecipe(float scale,int offsetX) {
 public static int height(int natural){return Math.min(natural,Math.max(90,Minecraft.getInstance().getWindow().getGuiScaledHeight()-100));}
 public static FittedRecipe of(int height,int natural){float s=Math.min(1,(height-2f)/natural);return new FittedRecipe(s,Math.round((170-170*s)/2));}
 public int x(int x){return offsetX+Math.round(x*scale);}public int y(int y){return Math.round(y*scale);}
 public double mouseX(double x){return (x-offsetX)/scale;}public double mouseY(double y){return y/scale;}
 public void begin(GuiGraphics g){g.pose().pushPose();g.pose().translate(offsetX,0,0);g.pose().scale(scale,scale,1);}
 public void end(GuiGraphics g){g.flush();g.pose().popPose();}
 public IIngredientRenderer<ItemStack> itemRenderer(){return new IIngredientRenderer<>(){
  public int getWidth(){return (int)Math.ceil(16*scale);}public int getHeight(){return getWidth();}
  public void render(GuiGraphics g,ItemStack stack){g.pose().pushPose();g.pose().scale(scale,scale,1);g.renderItem(stack,0,0);g.renderItemDecorations(Minecraft.getInstance().font,stack,0,0);g.flush();g.pose().popPose();}
  public List<Component> getTooltip(ItemStack stack,TooltipFlag flag){return net.minecraft.client.gui.screens.Screen.getTooltipFromItem(Minecraft.getInstance(),stack);}
 };}
 public IIngredientRenderer<AspectValue> aspectRenderer(){return new IIngredientRenderer<>(){
  public int getWidth(){return (int)Math.ceil(16*scale);}public int getHeight(){return getWidth();}
  public void render(GuiGraphics g,AspectValue value){g.pose().pushPose();g.pose().scale(scale,scale,1);value.draw(g,0,0);g.flush();g.pose().popPose();}
  public List<Component> getTooltip(AspectValue value,TooltipFlag flag){return value.tooltip();}
 };}
}
