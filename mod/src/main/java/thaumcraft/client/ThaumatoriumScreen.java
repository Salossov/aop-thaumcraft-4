package thaumcraft.client;
import java.util.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.network.PacketDistributor;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.crafting.CrucibleRecipe;
import thaumcraft.crafting.R3Recipes;
import thaumcraft.menu.ThaumatoriumMenu;
import thaumcraft.network.*;
import thaumcraft.production.ThaumatoriumBlockEntity;
public final class ThaumatoriumScreen extends AbstractContainerScreen<ThaumatoriumMenu>{
 private static final ResourceLocation TEXTURE=ResearchGraphics.texture("gui/gui_thaumatorium");
 private int index,startAspect;
 private long sequence;
 public ThaumatoriumScreen(ThaumatoriumMenu menu,Inventory inventory,Component title){super(menu,inventory,title);}
 private ThaumatoriumBlockEntity tile(){return minecraft.level.getBlockEntity(menu.pos) instanceof ThaumatoriumBlockEntity tile?tile:null;}
 private List<RecipeHolder<CrucibleRecipe>> choices(){var tile=tile();var known=ClientKnowledge.snapshot().completedResearch();return minecraft.level.getRecipeManager().getAllRecipesFor(R3Recipes.CRUCIBLE_TYPE.get()).stream().filter(h->tile!=null&&tile.selections.containsKey(h.id())||known.contains(h.value().research())&&h.value().catalyst().test(menu.catalyst())).sorted(Comparator.comparing(h->h.id().toString())).toList();}
 @Override protected void renderBg(GuiGraphics g,float partial,int mouseX,int mouseY){
  g.blit(TEXTURE,leftPos,topPos,0,0,imageWidth,imageHeight);var choices=choices();if(choices.isEmpty()){index=0;return;}index=Math.clamp(index,0,choices.size()-1);var h=choices.get(index);var tile=tile();boolean selected=tile!=null&&tile.selections.containsKey(h.id());
  if(choices.size()>1){g.blit(TEXTURE,leftPos+128,topPos+16,index>0?192:176,16,16,8);g.blit(TEXTURE,leftPos+128,topPos+24,index<choices.size()-1?192:176,24,16,8);}
  if(selected||ResearchGraphics.inside(mouseX,mouseY,leftPos+112,topPos+16,16,16))g.blit(TEXTURE,leftPos+104,topPos+8,176,96,48,48);
  if(tile!=null&&!tile.selections.isEmpty()){g.setColor(1,1,1,(float)Math.min(1,.6+Math.sin(minecraft.player.tickCount/5f)*.4+.4));g.blit(TEXTURE,leftPos+88,topPos+16,176,56,24,24);g.setColor(1,1,1,1);}
  var output=h.value().getRecipeOutput();g.renderItem(output,leftPos+112,topPos+16);g.renderItemDecorations(font,output,leftPos+112,topPos+16);
  var aspects=h.value().aspects().keySet().stream().map(Aspect::getAspect).sorted(Comparator.comparing(Aspect::getName)).toList();startAspect=Math.clamp(startAspect,0,Math.max(0,aspects.size()-1));if(aspects.size()>6){g.blit(TEXTURE,leftPos+32,topPos+40,startAspect>0?192:176,32,8,16);g.blit(TEXTURE,leftPos+136,topPos+40,startAspect<aspects.size()-1?200:184,32,8,16);}else startAspect=0;
  for(int i=startAspect;i<Math.min(aspects.size(),startAspect+6);i++){var a=aspects.get(i);int x=leftPos+40+(i-startAspect)*16,n=h.value().aspects().get(a.getTag());ResearchGraphics.aspect(g,a,x,topPos+40,1);g.pose().pushPose();g.pose().translate(x+16,topPos+50,200);g.pose().scale(.5f,.5f,1);String text=Integer.toString(n);g.drawString(font,text,-font.width(text),0,0xffffffff,true);g.pose().popPose();if(selected){g.blit(TEXTURE,x+1,topPos+57,176,8,14,6);int progress=tile.containerContains(a)*12/Math.max(1,n);if(progress>0)g.fill(x+2,topPos+58,x+2+Math.min(12,progress),topPos+62,0xff000000|a.getColor());}}
  if(tile!=null&&tile.maxRecipes>1){g.pose().pushPose();g.pose().translate(leftPos+136,topPos+33,100);g.pose().scale(.5f,.5f,1);var s=tile.selections.size()+"/"+tile.maxRecipes;g.drawString(font,s,-font.width(s)/2,0,0xffffffff,false);g.pose().popPose();}
 }
 @Override protected void renderLabels(GuiGraphics g,int x,int y){}
 @Override public void render(GuiGraphics g,int x,int y,float partial){super.render(g,x,y,partial);renderTooltip(g,x,y);var choices=choices();if(choices.isEmpty())return;var recipe=choices.get(Math.clamp(index,0,choices.size()-1));if(ResearchGraphics.inside(x,y,leftPos+112,topPos+16,16,16))g.renderTooltip(font,recipe.value().getRecipeOutput(),x,y);var aspects=recipe.value().aspects().keySet().stream().map(Aspect::getAspect).sorted(Comparator.comparing(Aspect::getName)).toList();for(int i=startAspect;i<Math.min(aspects.size(),startAspect+6);i++)if(ResearchGraphics.inside(x,y,leftPos+40+(i-startAspect)*16,topPos+40,16,16))g.renderComponentTooltip(font,ResearchGraphics.aspectTooltip(aspects.get(i)),x,y);}
 @Override public boolean mouseClicked(double x,double y,int button){var choices=choices();if(!choices.isEmpty()&&button==0){index=Math.clamp(index,0,choices.size()-1);if(ResearchGraphics.inside(x,y,leftPos+112,topPos+16,16,16)){PacketDistributor.sendToServer(new ResearchActionPayload(menu.containerId,menu.token(),++sequence,1,choices.get(index).id().toString(),"",0,0));return true;}if(ResearchGraphics.inside(x,y,leftPos+128,topPos+16,16,8)&&index>0){index--;startAspect=0;return true;}if(ResearchGraphics.inside(x,y,leftPos+128,topPos+24,16,8)&&index<choices.size()-1){index++;startAspect=0;return true;}if(ResearchGraphics.inside(x,y,leftPos+32,topPos+40,8,16)){startAspect=Math.max(0,startAspect-1);return true;}if(ResearchGraphics.inside(x,y,leftPos+136,topPos+40,8,16)){startAspect++;return true;}}return super.mouseClicked(x,y,button);}
}
