package thaumcraft.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import thaumcraft.menu.TrunkMenu;

public final class TrunkScreen extends AbstractContainerScreen<TrunkMenu> {
    private static final ResourceLocation TEXTURE=ResourceLocation.fromNamespaceAndPath("thaumcraft","textures/gui/guitrunkbase.png");
    public TrunkScreen(TrunkMenu menu,Inventory inventory,Component title){super(menu,inventory,title);imageHeight=200;}
    @Override protected void renderBg(GuiGraphics graphics,float partial,int mouseX,int mouseY){ResearchGraphics.blit(graphics,TEXTURE,leftPos,topPos,0,0,176,200,256,256);if(menu.trunk.rows()==4)ResearchGraphics.blit(graphics,TEXTURE,leftPos,topPos+80,0,206,176,27,256,256);ResearchGraphics.blit(graphics,TEXTURE,leftPos+134,topPos+2,176,16,Math.round(menu.trunk.getHealth()/menu.trunk.getMaxHealth()*39),6,256,256);if(menu.trunk.stay())ResearchGraphics.blit(graphics,TEXTURE,leftPos+112,topPos,176,0,10,10,256,256);}
    @Override protected void renderLabels(GuiGraphics graphics,int x,int y){var owner=menu.trunk.owner();var caption=owner==null?title:owner.getName().copy().append(Component.translatable("entity.trunk.guiname"));graphics.pose().pushPose();graphics.pose().scale(.5f,.5f,1);graphics.drawString(font,caption,8,4,0xff000000|12624112,false);graphics.pose().popPose();}
    @Override public void render(GuiGraphics graphics,int x,int y,float partial){super.render(graphics,x,y,partial);renderTooltip(graphics,x,y);if(x>=leftPos+112&&x<leftPos+122&&y>=topPos&&y<topPos+10)graphics.renderTooltip(font,Component.translatable(menu.trunk.stay()?"thaumcraft.trunk.staying":"thaumcraft.trunk.following"),x,y);}
    @Override public boolean mouseClicked(double x,double y,int button){if(button==0&&x>=leftPos+112&&x<leftPos+122&&y>=topPos&&y<topPos+10){minecraft.gameMode.handleInventoryButtonClick(menu.containerId,1);return true;}return super.mouseClicked(x,y,button);}
}
