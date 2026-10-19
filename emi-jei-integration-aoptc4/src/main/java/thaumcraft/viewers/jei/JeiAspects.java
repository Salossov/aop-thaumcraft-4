package thaumcraft.viewers.jei;
import java.util.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import thaumcraft.viewers.AspectValue;
import mezz.jei.api.ingredients.*;
public final class JeiAspects implements IIngredientHelper<AspectValue>,IIngredientRenderer<AspectValue>{
 public static final IIngredientType<AspectValue> TYPE=()->AspectValue.class;
 public IIngredientType<AspectValue> getIngredientType(){return TYPE;}
 public String getDisplayName(AspectValue a){return a.name().getString();}
 public String getUniqueId(AspectValue a,mezz.jei.api.ingredients.subtypes.UidContext c){return a.key();}
 public ResourceLocation getResourceLocation(AspectValue a){return a.id();}
 public AspectValue copyIngredient(AspectValue a){return a;}
 public AspectValue normalizeIngredient(AspectValue a){return new AspectValue(a.aspect(),false,1);}
 public boolean isIngredientOnServer(AspectValue a){return true;}
 public String getErrorInfo(AspectValue a){return a.toString();}
 public void render(net.minecraft.client.gui.GuiGraphics g,AspectValue a){a.draw(g,0,0);}
 public List<Component> getTooltip(AspectValue a,net.minecraft.world.item.TooltipFlag f){return a.tooltip();}
}
