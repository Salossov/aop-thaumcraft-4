package thaumcraft.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import thaumcraft.menu.HarnessMenu;

public final class HarnessScreen extends AbstractContainerScreen<HarnessMenu>{
    private static final ResourceLocation TEXTURE=ResourceLocation.parse("thaumcraft:textures/gui/guihoverharness.png");
    public HarnessScreen(HarnessMenu menu,Inventory inventory,Component title){super(menu,inventory,title);}
    @Override protected void renderBg(GuiGraphics g,float partial,int x,int y){g.blit(TEXTURE,leftPos,topPos,0,0,imageWidth,imageHeight);}
    @Override protected void renderLabels(GuiGraphics g,int x,int y){if(menu.lockedHotbarSlot()>=0)g.blit(TEXTURE,8+18*menu.lockedHotbarSlot(),142,240,0,16,16);}
    @Override public void render(GuiGraphics g,int x,int y,float partial){super.render(g,x,y,partial);renderTooltip(g,x,y);}
}
