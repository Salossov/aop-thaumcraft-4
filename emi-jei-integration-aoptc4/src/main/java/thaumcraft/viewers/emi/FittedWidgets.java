package thaumcraft.viewers.emi;

import dev.emi.emi.api.widget.*;
import dev.emi.emi.api.stack.EmiIngredient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

/** EMI can clamp recipe cards at large GUI scales. Drawing and hitboxes use the same transform. */
public record FittedWidgets(float scale,int offsetX) {
 public static int height(int natural){return Math.min(natural,Math.max(70,Minecraft.getInstance().getWindow().getGuiScaledHeight()-145));}
 public static FittedWidgets of(WidgetHolder holder,int naturalHeight){float scale=Math.min(1,Math.min(holder.getWidth()/170f,(holder.getHeight()-2f)/naturalHeight));return new FittedWidgets(scale,Math.round((holder.getWidth()-170*scale)/2));}
 public Bounds bounds(int x,int y,int w,int h){return new Bounds(offsetX+(int)Math.floor(x*scale),(int)Math.floor(y*scale),(int)Math.ceil(w*scale),(int)Math.ceil(h*scale));}
 public void begin(GuiGraphics g){g.pose().pushPose();g.pose().translate(offsetX,0,0);g.pose().scale(scale,scale,1);}
 public void end(GuiGraphics g){g.flush();g.pose().popPose();}
 public SlotWidget slot(WidgetHolder widgets,EmiIngredient ingredient,int px,int py){return widgets.add(new SlotWidget(ingredient,px-1,py-1){
  @Override public Bounds getBounds(){return bounds(px-1,py-1,18,18);}
  @Override public void render(GuiGraphics g,int mx,int my,float delta){begin(g);getStack().render(g,px,py,delta);end(g);if(getBounds().contains(mx,my)){var b=getBounds();g.fill(b.x()+1,b.y()+1,b.x()+b.width()-1,b.y()+b.height()-1,0x40ffffff);}}
 }.drawBack(false));}
}
