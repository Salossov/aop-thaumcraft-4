package thaumcraft.client;

import java.util.*;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import thaumcraft.api.wands.*;
import thaumcraft.menu.FocalManipulatorMenu;
import thaumcraft.production.FocalManipulatorBlockEntity;

public final class FocalManipulatorScreen extends FittedContainerScreen<FocalManipulatorMenu> {
    private static final ResourceLocation TEXTURE=ResearchGraphics.texture("gui/gui_wandtable");
    private int selected=-1;private net.minecraft.world.item.ItemStack previous=net.minecraft.world.item.ItemStack.EMPTY;
    public FocalManipulatorScreen(FocalManipulatorMenu menu,Inventory inventory,Component title){super(menu,inventory,title);imageWidth=192;imageHeight=233;}
    private List<FocusUpgradeType> choices(){var stack=menu.table.getItem(0);int rank=menu.table.nextRank();return stack.getItem() instanceof ItemFocusBasic focus&&rank>0?focus.getPossibleUpgradesByRank(stack,rank).stream().filter(choice->focus instanceof thaumcraft.items.ThaumicFocusItem item?item.canApplyForPlayer(stack,minecraft.player,choice,rank):focus.canApplyUpgrade(stack,choice,rank)).toList():List.of();}
    @Override protected void renderBg(GuiGraphics g,float partial,int mouseX,int mouseY){
        com.mojang.blaze3d.systems.RenderSystem.enableBlend();com.mojang.blaze3d.systems.RenderSystem.defaultBlendFunc();
        g.blit(TEXTURE,leftPos,topPos,0,0,imageWidth,imageHeight);var stack=menu.table.getItem(0);if(!net.minecraft.world.item.ItemStack.isSameItemSameComponents(previous,stack)){previous=stack.copy();selected=-1;}
        if(menu.table.active())selected=menu.table.upgrade();var choices=choices();for(int i=0;i<choices.size();i++){var choice=choices.get(i);if(choice.id()==selected)g.blit(TEXTURE,leftPos+48+i*16,topPos+104,200,0,16,16);ResearchGraphics.icon(g,choice.icon(),leftPos+48+i*16,topPos+104,16,16,1);}
        if(stack.getItem() instanceof ItemFocusBasic focus){int index=0;for(int id:focus.getAppliedUpgrades(stack)){var choice=FocusUpgradeType.get(id);if(choice!=null)ResearchGraphics.icon(g,choice.icon(),leftPos+56+index++*16,topPos+32,16,16,1);}}
        var choice=FocusUpgradeType.get(selected);int rank=menu.table.nextRank();if(choice==null||rank==0)return;int xp=rank*8;g.blit(TEXTURE,leftPos+108,topPos+59,200,16,16,16);g.drawString(font,Integer.toString(xp),leftPos+125,topPos+64,minecraft.player.experienceLevel>=xp?0x99ff99:0xff7777,false);
        if(!menu.table.active()&&minecraft.player.experienceLevel>=xp)g.blit(TEXTURE,leftPos+48,topPos+88,8,240,96,8);
        var values=menu.table.active()?menu.table.remaining():FocalManipulatorBlockEntity.cost(choice,rank);int row=0;g.pose().pushPose();g.pose().translate(leftPos+49,topPos+68-values.size()*2.5,0);g.pose().scale(.5f,.5f,1);for(var entry:values.entrySet().stream().sorted(Map.Entry.comparingByKey(Comparator.comparing(thaumcraft.api.aspects.Aspect::getTag))).toList()){g.drawString(font,entry.getKey().getName(),0,row*10,entry.getKey().getColor(),false);g.drawString(font,new java.text.DecimalFormat("#######.#").format(entry.getValue()/100f),48,row++*10,entry.getKey().getColor(),false);}g.pose().popPose();
        if(menu.table.active()){int start=0;for(var entry:values.entrySet()){int width=(int)((double)entry.getValue()/menu.table.total()*96);g.fill(leftPos+48+start,topPos+88,leftPos+48+start+width,topPos+96,0xdd000000|entry.getKey().getColor());start+=width;}}
    }
    @Override protected void renderLabels(GuiGraphics g,int x,int y){}
    @Override protected void renderFitted(GuiGraphics g,int x,int y,float partial){super.renderFitted(g,x,y,partial);renderTooltip(g,x,y);var choices=choices();for(int i=0;i<choices.size();i++)if(ResearchGraphics.inside(x,y,leftPos+48+i*16,topPos+104,16,16)){var choice=choices.get(i);var lines=new ArrayList<Component>();lines.add(Component.translatable(choice.name()).withStyle(ChatFormatting.DARK_PURPLE,ChatFormatting.UNDERLINE));for(var sequence:font.split(Component.translatable(choice.description()),180)){var text=new StringBuilder();sequence.accept((index,style,codepoint)->{text.appendCodePoint(codepoint);return true;});lines.add(Component.literal(text.toString()));}fittedTooltip(g,lines,x,y);}}
    @Override protected boolean mouseClickedFitted(double x,double y,int button){if(button==0&&!menu.table.active()){var choices=choices();for(int i=0;i<choices.size();i++)if(ResearchGraphics.inside(x,y,leftPos+48+i*16,topPos+104,16,16)){selected=choices.get(i).id();return true;}if(selected>=0&&ResearchGraphics.inside(x,y,leftPos+48,topPos+88,96,8)){minecraft.gameMode.handleInventoryButtonClick(menu.containerId,selected);return true;}}return super.mouseClickedFitted(x,y,button);}
}
