package thaumcraft.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import thaumcraft.menu.HandMirrorMenu;

public final class HandMirrorScreen extends FittedContainerScreen<HandMirrorMenu> {
    public HandMirrorScreen(HandMirrorMenu menu,Inventory inventory,Component title){super(menu,inventory,title);imageWidth=176;imageHeight=166;}
    @Override protected void renderBg(GuiGraphics graphics,float partial,int x,int y){graphics.blit(ResearchGraphics.texture("gui/guihandmirror"),leftPos,topPos,0,0,imageWidth,imageHeight);}
    @Override protected void renderLabels(GuiGraphics graphics,int x,int y){}
    @Override protected void renderFitted(GuiGraphics graphics,int x,int y,float partial){super.renderFitted(graphics,x,y,partial);renderTooltip(graphics,x,y);}
}
