package thaumcraft.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import thaumcraft.menu.DeconstructionMenu;

public final class DeconstructionScreen extends AbstractContainerScreen<DeconstructionMenu> {
    private static final ResourceLocation TEXTURE=ResourceLocation.parse("thaumcraft:textures/gui/gui_decontable.png");
    public DeconstructionScreen(DeconstructionMenu menu,Inventory inventory,Component title){super(menu,inventory,title);}
    @Override protected void renderBg(GuiGraphics g,float partial,int mouseX,int mouseY){g.blit(TEXTURE,leftPos,topPos,0,0,imageWidth,imageHeight);int n=menu.progressScaled(46);if(n>0)g.blit(TEXTURE,leftPos+93,topPos+61-n,176,46-n,9,n);var a=menu.pendingAspect();if(a!=null)ResearchGraphics.aspect(g,a,leftPos+64,topPos+48,1);}
    @Override protected void renderLabels(GuiGraphics g,int mouseX,int mouseY){}
    @Override public void render(GuiGraphics g,int mouseX,int mouseY,float partial){super.render(g,mouseX,mouseY,partial);renderTooltip(g,mouseX,mouseY);var a=menu.pendingAspect();if(a!=null&&ResearchGraphics.inside(mouseX,mouseY,leftPos+64,topPos+48,16,16))g.renderComponentTooltip(font,ResearchGraphics.aspectTooltip(a),mouseX,mouseY);}
    @Override public boolean mouseClicked(double x,double y,int button){if(button==0&&menu.pendingAspect()!=null&&ResearchGraphics.inside(x,y,leftPos+64,topPos+48,16,16)){minecraft.gameMode.handleInventoryButtonClick(menu.containerId,1);minecraft.player.playSound(thaumcraft.registry.R2Sounds.ASPECT_CLAIM.get(),.2f,1+minecraft.player.getRandom().nextFloat()*.1f);return true;}return super.mouseClicked(x,y,button);}
}
