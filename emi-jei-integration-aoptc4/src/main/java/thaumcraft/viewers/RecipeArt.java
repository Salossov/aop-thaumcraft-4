package thaumcraft.viewers;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.ChatFormatting;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.BuiltInRegistries;
import thaumcraft.api.research.ResearchApi;
import thaumcraft.api.crafting.RecipeDisplay;
/** Reuses the core's native research-book artwork; no third-party UI assets are copied. */
public final class RecipeArt {
 public static void overlay(GuiGraphics g,int x,int y,int u,int v,int w,int h,float scale,float alpha){g.flush();com.mojang.blaze3d.systems.RenderSystem.enableBlend();com.mojang.blaze3d.systems.RenderSystem.defaultBlendFunc();g.setColor(1,1,1,alpha);g.pose().pushPose();g.pose().translate(x,y,0);g.pose().scale(scale,scale,1);g.blit(ResourceLocation.parse("thaumcraft:textures/gui/gui_researchbook_overlay.png"),0,0,u,v,w,h,256,256);g.pose().popPose();g.setColor(1,1,1,1);}
 public static void draw(RecipeDisplay r,GuiGraphics g){var l=new Layout(r);var font=Minecraft.getInstance().font;if(!l.cards.isEmpty())g.drawString(font,Component.translatable("thaumcraft_viewers.research"),0,1,0xff444444,false);for(var card:l.cards)card(g,card);if(!r.outputs().isEmpty())overlay(g,73,16,20,3,16,16,1.5f,.8f);
  switch(r.kind()){
   case ARCANE->overlay(g,51,44,112,15,52,52,1.3f,.75f);
   case INFUSION->overlay(g,43,49,200,77,60,44,1.4f,.75f);
   case CRUCIBLE->overlay(g,43,70,0,20,56,48,1.5f,.75f);
   case CENTRIFUGE->center(g,Component.literal("↓"),85,78,0xff444444);
   case CRYSTALLIZATION->center(g,Component.literal("↑"),85,48,0xff444444);
   default->{}
  }
  if(l.instabilityY>=0)center(g,Component.translatable("thaumcraft_viewers.instability",r.instability()),85,l.instabilityY,0xff704000);
  if(r.experience()>0)g.drawString(font,Component.translatable("thaumcraft_viewers.xp",r.experience()),2,l.footer,0xff285f20,false);
  else if(!r.notes().isEmpty())g.drawString(font,Component.translatable("thaumcraft_viewers.recipe_notes"),2,l.footer,0xff666666,false);
 }
 public static Component researchName(String key){var d=ResearchApi.definitions().get(key);if(d==null)return Component.translatable("tc.research_name."+key);var category=ResearchApi.books().categories().get(d.category());return (category==null?Component.empty():Component.translatable(category.name()).append(" : ")).append(Component.translatable("tc.research_name."+key));}
 public static List<Component> researchTooltip(String key){var lines=new ArrayList<Component>();lines.add(researchName(key).copy().withStyle(ResearchApi.clientKnowledge().completedResearch().contains(key)?ChatFormatting.GREEN:ChatFormatting.RED));var d=ResearchApi.definitions().get(key);if(d!=null){var parents=java.util.stream.Stream.concat(d.parents().stream(),d.hiddenParents().stream()).distinct().toList();if(!parents.isEmpty()){lines.add(Component.empty());lines.add(Component.translatable("thaumcraft_viewers.parents"));for(String parent:parents)lines.add(Component.literal("  ").append(researchName(parent)).withStyle(ResearchApi.clientKnowledge().completedResearch().contains(parent)?ChatFormatting.GREEN:ChatFormatting.RED));}}var navigation=thaumcraft.api.research.ResearchNavigation.resolve(key,ResearchApi.definitions(),ResearchApi.books(),ResearchApi.clientKnowledge());if(navigation.reason()==thaumcraft.api.research.ResearchNavigation.Reason.UNDISCOVERED)lines.add(Component.translatable("thaumcraft_viewers.undiscovered_research").withStyle(ChatFormatting.GRAY));else if(navigation.reason()==thaumcraft.api.research.ResearchNavigation.Reason.PREREQUISITES)lines.add(Component.translatable("thaumcraft_viewers.complete_parents").withStyle(ChatFormatting.GOLD));lines.add(Component.translatable("thaumcraft_viewers.open_research").withStyle(ChatFormatting.DARK_GRAY));return lines;}
 private static void card(GuiGraphics g,Layout.Card card){var d=ResearchApi.definitions().get(card.research());if(d!=null){var id=ResourceLocation.tryParse(d.icon());if(id!=null){if(id.getPath().startsWith("textures/")){g.flush();com.mojang.blaze3d.systems.RenderSystem.enableBlend();com.mojang.blaze3d.systems.RenderSystem.defaultBlendFunc();g.blit(id,card.x(),card.y(),0,0,16,16,16,16);}else BuiltInRegistries.ITEM.getOptional(id).ifPresent(item->g.renderItem(new ItemStack(item),card.x(),card.y()));}}status(g,card.x()+20,card.y()+2,ResearchApi.clientKnowledge().completedResearch().contains(card.research()));}
 public static void center(GuiGraphics g,Component text,int x,int y,int color){var font=Minecraft.getInstance().font;g.drawString(font,text,x-font.width(text)/2,y,color,false);}
 private static void status(GuiGraphics g,int x,int y,boolean known){int color=known?0xff267627:0xffb32222;if(known){for(int i=0;i<5;i++)g.fill(x+i,y+5+i,x+i+2,y+7+i,color);for(int i=0;i<9;i++)g.fill(x+4+i,y+9-i,x+6+i,y+11-i,color);}else for(int i=0;i<12;i++){g.fill(x+i,y+i,x+i+2,y+i+2,color);g.fill(x+11-i,y+i,x+13-i,y+i+2,color);}}

 public static Optional<String> hit(RecipeDisplay r,double x,double y){return new Layout(r).cards.stream().filter(c->x>=c.x()&&x<c.x()+36&&y>=c.y()&&y<c.y()+18).map(Layout.Card::research).findFirst();}
 private RecipeArt(){}
}
