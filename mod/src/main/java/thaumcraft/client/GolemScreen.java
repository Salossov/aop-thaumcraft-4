package thaumcraft.client;

import java.util.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import thaumcraft.menu.GolemMenu;

public final class GolemScreen extends FittedContainerScreen<GolemMenu> {
    private record Toggle(int id,int x,int y,String key,boolean checked){}
    public GolemScreen(GolemMenu menu,Inventory inventory,Component title){super(menu,inventory,title);imageWidth=menu.upgrade(5)>0&&menu.golem.core()!=10?276:176;imageHeight=166;}
    @Override protected int fittingWidth(){return menu.upgrade(5)>0&&menu.golem.core()!=10?276:176;}
    private List<Toggle> controls(){var result=new ArrayList<Toggle>();int core=menu.golem.core();if(core==0)result.add(new Toggle(50,62,54,"precise_amount",!menu.toggle(0)));if(core==8){result.add(new Toggle(50,42,40,menu.toggle(0)?"empty_space":"block",!menu.toggle(0)));result.add(new Toggle(51,42,50,menu.toggle(1)?"left_click":"right_click",!menu.toggle(1)));result.add(new Toggle(52,42,60,"sneaking",menu.toggle(2)));}if(core==4&&menu.upgrade(4)>0)for(int i=0;i<4;i++)result.add(new Toggle(51+i,104,5+16*i,new String[]{"monsters","animals","players","creepers"}[i],!menu.toggle(i+1)));if(menu.upgrade(5)>0&&Set.of(0,1,2,8,10).contains(core)){int x=core==10?66:180,y=core==10?36:24;for(int i=0;i<3;i++)result.add(new Toggle(55+i,x,y+10*i,new String[]{"common_tags","ignore_damage","ignore_components"}[i],menu.toggle(5+i)));}return result;}
    @Override protected void renderBg(GuiGraphics graphics,float partial,int mouseX,int mouseY){var texture=ResearchGraphics.texture("gui/guigolem");graphics.blit(texture,leftPos,topPos,0,0,176,imageHeight);
        if(menu.hasFilters())for(int i=0;i<6&&i+menu.page()*6<menu.count();i++){int x=leftPos+96+i/2*28,y=topPos+12+i%2*31;graphics.blit(texture,x,y,184,menu.golem.material().ordinal()*24,24,24);if(menu.upgrade(4)>0){graphics.blit(texture,x,y-8,72,168,24,12);int color=menu.color(i+menu.page()*6);if(color>=0)graphics.fill(x+9,y-5,x+15,y+1,0xff000000|net.minecraft.world.item.DyeColor.byId(color).getTextureDiffuseColor());}}
        if(menu.count()>6&&menu.hasFilters()){graphics.blit(texture,leftPos+111,topPos+68,0,menu.page()>0?200:208,24,8);graphics.blit(texture,leftPos+135,topPos+68,24,menu.page()<(menu.count()-1)/6?200:208,24,8);}
        for(var toggle:controls()){ResearchGraphics.blit(graphics,texture,leftPos+toggle.x,topPos+toggle.y,8,168,8,8,256,256);if(toggle.checked)ResearchGraphics.blit(graphics,texture,leftPos+toggle.x,topPos+toggle.y,8,176,8,8,256,256);graphics.pose().pushPose();graphics.pose().translate(leftPos+toggle.x+10,topPos+toggle.y+2,0);graphics.pose().scale(.5f,.5f,1);graphics.drawString(font,Component.translatable("gui.thaumcraft.golem."+toggle.key),0,0,0xfffdfdfd,false);graphics.pose().popPose();}
        InventoryScreen.renderEntityInInventoryFollowsMouse(graphics,leftPos+6,topPos+4,leftPos+38,topPos+75,48,0,mouseX,mouseY,menu.golem);
    }
    @Override protected boolean mouseClickedFitted(double x,double y,int button){int px=(int)x-leftPos,py=(int)y-topPos;for(var toggle:controls())if(px>=toggle.x&&px<toggle.x+8&&py>=toggle.y&&py<toggle.y+8){minecraft.gameMode.handleInventoryButtonClick(menu.containerId,toggle.id);return true;}if(menu.hasFilters()){for(int i=0;i<6&&i+menu.page()*6<menu.count();i++){int sx=96+i/2*28,sy=4+i%2*31;if(menu.upgrade(4)>0&&py>=sy&&py<sy+12&&(px>=sx&&px<sx+8||px>=sx+16&&px<sx+24)){minecraft.gameMode.handleInventoryButtonClick(menu.containerId,i+menu.page()*6+(px>=sx+16?18:0));return true;}}if(py>=68&&py<76&&px>=111&&px<159){minecraft.gameMode.handleInventoryButtonClick(menu.containerId,px<135?66:67);return true;}}return super.mouseClickedFitted(x,y,button);}
    @Override protected void renderLabels(GuiGraphics graphics,int x,int y){}
    @Override protected void renderFitted(GuiGraphics graphics,int x,int y,float partial){super.renderFitted(graphics,x,y,partial);renderTooltip(graphics,x,y);}
}
