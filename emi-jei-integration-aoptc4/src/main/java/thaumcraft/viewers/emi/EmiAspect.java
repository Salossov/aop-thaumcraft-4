package thaumcraft.viewers.emi;
import java.util.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.component.DataComponentPatch;
import dev.emi.emi.api.stack.*;
import thaumcraft.viewers.AspectValue;
public final class EmiAspect extends EmiStack {
 private final String aspect;private final boolean vis;
 public EmiAspect(AspectValue value){aspect=value.aspect();vis=value.vis();amount=value.amount();}
 private AspectValue value(){return new AspectValue(aspect,vis,Math.toIntExact(amount));}
 public EmiStack copy(){var copy=new EmiAspect(value());copy.setChance(chance);return copy;}
 public boolean isEmpty(){return false;}
 public DataComponentPatch getComponentChanges(){return DataComponentPatch.EMPTY;}
 public Object getKey(){return value().key();}
 public ResourceLocation getId(){return value().id();}
 public List<Component> getTooltipText(){return value().tooltip();}
 public List<net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent> getTooltip(){var lines=new ArrayList<net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent>();for(var line:getTooltipText())lines.add(net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent.create(line.getVisualOrderText()));lines.addAll(super.getTooltip());return lines;}
 public Component getName(){return value().name();}
 public void render(net.minecraft.client.gui.GuiGraphics g,int x,int y,float delta,int flags){value().draw(g,x,y);}
 public static final class Serializer implements dev.emi.emi.api.stack.serializer.EmiIngredientSerializer<EmiAspect>{
  public String getType(){return "thaumcraft_viewers:aspect";}
  public EmiIngredient deserialize(com.google.gson.JsonElement json){try{var o=json.getAsJsonObject();return new EmiAspect(new AspectValue(o.get("aspect").getAsString(),o.get("vis").getAsBoolean(),o.get("amount").getAsInt()));}catch(RuntimeException invalid){return EmiStack.EMPTY;}}
  public com.google.gson.JsonElement serialize(EmiAspect stack){var o=new com.google.gson.JsonObject();o.addProperty("type",getType());o.addProperty("aspect",stack.aspect);o.addProperty("vis",stack.vis);o.addProperty("amount",stack.amount);return o;}
 }
}
