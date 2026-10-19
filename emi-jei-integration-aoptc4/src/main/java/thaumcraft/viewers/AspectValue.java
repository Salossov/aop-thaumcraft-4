package thaumcraft.viewers;
import java.util.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.research.ResearchApi;
/** One searchable aspect identity; vis/essentia are amounts/units of a particular recipe slot. */
public record AspectValue(String aspect,boolean vis,int amount){
 public AspectValue{if(Aspect.getAspect(aspect)==null||amount<0)throw new IllegalArgumentException("Unknown aspect or negative amount");}
 public String key(){return aspect;}
 public ResourceLocation id(){var a=ResourceLocation.parse(aspect.contains(":")?aspect:"thaumcraft:"+aspect);return a.withPrefix("aspect/");}
 public boolean discovered(){return ResearchApi.clientKnowledge().knows(Aspect.getAspect(aspect));}
 public Component name(){if(!discovered())return Component.translatable("thaumcraft_viewers.unknown_aspect");var a=Aspect.getAspect(aspect);return a.translationKey()==null?Component.literal(a.getName()):Component.translatable(a.translationKey());}
 public Component amountText(){return Component.translatable(vis?"thaumcraft_viewers.vis_amount":"thaumcraft_viewers.essentia_amount",vis?new java.text.DecimalFormat("0.##").format(amount/100.0):amount);}
 public List<Component> tooltip(){var lines=new ArrayList<Component>();lines.add(name().copy().withStyle(net.minecraft.ChatFormatting.GOLD));lines.add(amountText().copy().withStyle(net.minecraft.ChatFormatting.AQUA));if(!discovered()){lines.add(Component.translatable("thaumcraft_viewers.discover_first").withStyle(net.minecraft.ChatFormatting.GRAY));return lines;}var components=Aspect.getAspect(aspect).getComponents();if(components.isEmpty())lines.add(Component.translatable("thaumcraft_viewers.primal").withStyle(net.minecraft.ChatFormatting.GRAY));else lines.add(Component.translatable("thaumcraft_viewers.combination",new AspectValue(components.get(0).getTag(),false,1).name(),new AspectValue(components.get(1).getTag(),false,1).name()).withStyle(net.minecraft.ChatFormatting.GRAY));var sources=AspectRecipes.sources(aspect);lines.add(Component.translatable(sources.isEmpty()?"thaumcraft_viewers.no_scanned_sources":"thaumcraft_viewers.scanned_sources").withStyle(s->s.withColor(0x909090)));for(var source:sources.stream().limit(5).toList())lines.add(Component.literal("  ").append(source.stack().getHoverName()).append(Component.literal(" ×"+source.amount()).withStyle(net.minecraft.ChatFormatting.AQUA)).withStyle(net.minecraft.ChatFormatting.GRAY));if(sources.size()>5)lines.add(Component.translatable("thaumcraft_viewers.more_sources",sources.size()-5).withStyle(s->s.withColor(0x909090)));lines.add(Component.translatable("thaumcraft_viewers.aspect_navigation").withStyle(s->s.withColor(0x909090)));return List.copyOf(lines);}
 public static String countLabel(double amount){if(amount>=1_000_000_000)return compact(amount/1_000_000_000)+"G";if(amount>=1_000_000)return compact(amount/1_000_000)+"M";if(amount>=10_000)return compact(amount/1_000)+"k";return new java.text.DecimalFormat("0.##").format(amount);}
 private static String compact(double value){return String.format(java.util.Locale.ROOT,value<10?"%.1f":"%.0f",value);}
 public void draw(GuiGraphics g,int x,int y){g.flush();com.mojang.blaze3d.systems.RenderSystem.enableBlend();com.mojang.blaze3d.systems.RenderSystem.defaultBlendFunc();var a=Aspect.getAspect(aspect);boolean known=discovered();int color=known?a.getColor():0xffffff;g.setColor(((color>>16)&255)/255f,((color>>8)&255)/255f,(color&255)/255f,1);g.blit(ResourceLocation.parse(known?a.texture():"thaumcraft:textures/aspects/_unknown.png"),x,y,16,16,0,0,32,32,32,32);g.setColor(1,1,1,1);if(amount>1||vis){g.pose().pushPose();g.pose().translate(x+17,y+12,200);g.pose().scale(.65f,.65f,1);String n=countLabel(vis?amount/100.0:amount);g.drawString(Minecraft.getInstance().font,n,-Minecraft.getInstance().font.width(n),0,0xffffffff,true);g.pose().popPose();}}
}
