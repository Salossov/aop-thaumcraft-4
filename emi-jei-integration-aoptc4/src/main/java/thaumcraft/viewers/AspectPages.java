package thaumcraft.viewers;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import thaumcraft.api.aspects.Aspect;
/** The aspect index, combination and scanned-source pages share identical geometry in both viewers. */
public final class AspectPages {
 public record Slot(AspectValue aspect,int x,int y,boolean output){}
 public static int height(AspectRecipes.Entry e){return e.kind()==AspectRecipes.Kind.INDEX?12+((AspectRecipes.values().size()+7)/8)*20:e.kind()==AspectRecipes.Kind.SOURCES?146:100;}
 public static List<Slot> slots(AspectRecipes.Entry e){var slots=new ArrayList<Slot>();if(e.kind()==AspectRecipes.Kind.INDEX){int n=0;for(var value:AspectRecipes.values()){slots.add(new Slot(value,5+n%8*20,8+n/8*20,false));n++;}}else{slots.add(new Slot(new AspectValue(e.aspect(),false,1),77,14,true));if(e.kind()==AspectRecipes.Kind.COMBINATION){var components=Aspect.getAspect(e.aspect()).getComponents();for(int i=0;i<components.size();i++)slots.add(new Slot(new AspectValue(components.get(i).getTag(),false,1),49+i*56,66,false));}}return slots;}
 public static Component title(AspectRecipes.Kind kind){return Component.translatable("thaumcraft_viewers.aspect_category."+kind.name().toLowerCase(Locale.ROOT));}
 public static void draw(AspectRecipes.Entry e,GuiGraphics g){var font=Minecraft.getInstance().font;if(e.kind()==AspectRecipes.Kind.COMBINATION){var a=Aspect.getAspect(e.aspect());RecipeArt.overlay(g,73,10,20,3,16,16,1.5f,.7f);if(a.isPrimal())RecipeArt.center(g,Component.translatable("thaumcraft_viewers.primal"),85,64,0xff444444);else{RecipeArt.center(g,Component.literal("+"),85,70,0xff444444);RecipeArt.center(g,Component.literal("↑"),85,45,0xff444444);}}else if(e.kind()==AspectRecipes.Kind.SOURCES){if(AspectRecipes.page(e).isEmpty())g.drawWordWrap(font,Component.translatable("thaumcraft_viewers.no_scanned_sources"),5,54,160,0xff666666);else{int n=0;for(var source:AspectRecipes.page(e)){int x=5+n%8*20,y=48+n/8*20;n++;g.pose().pushPose();g.pose().translate(x+17,y+13,200);g.pose().scale(.65f,.65f,1);String amount=""+source.amount();g.drawString(font,amount,-font.width(amount),0,0xffffffff,true);g.pose().popPose();}}}}
 private AspectPages(){}
}
