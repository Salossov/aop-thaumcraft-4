package thaumcraft.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import thaumcraft.menu.FocusPouchMenu;

public final class FocusPouchScreen extends FittedContainerScreen<FocusPouchMenu> {
    public FocusPouchScreen(FocusPouchMenu menu,Inventory inventory,Component title){super(menu,inventory,title);imageWidth=175;imageHeight=232;}
    @Override protected void renderBg(GuiGraphics graphics,float partial,int x,int y){graphics.blit(ResearchGraphics.texture("gui/gui_focuspouch"),leftPos,topPos,0,0,imageWidth,imageHeight);}
    @Override protected void renderLabels(GuiGraphics graphics,int x,int y){if(menu.lockedHotbar()>=0)graphics.fill(8+18*menu.lockedHotbar(),209,24+18*menu.lockedHotbar(),225,0x40333333);}
    @Override protected void renderFitted(GuiGraphics graphics,int x,int y,float partial){super.renderFitted(graphics,x,y,partial);renderTooltip(graphics,x,y);}
}
