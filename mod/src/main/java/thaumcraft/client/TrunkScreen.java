package thaumcraft.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import thaumcraft.menu.TrunkMenu;

public final class TrunkScreen extends AbstractContainerScreen<TrunkMenu> {
    private static final ResourceLocation TEXTURE=ResourceLocation.fromNamespaceAndPath("thaumcraft","textures/gui/guitrunkbase.png");
    public TrunkScreen(TrunkMenu menu,Inventory inventory,Component title){super(menu,inventory,title);imageHeight=200;inventoryLabelY=106;}
    @Override protected void renderBg(GuiGraphics graphics,float partial,int mouseX,int mouseY){graphics.blit(TEXTURE,leftPos,topPos,0,0,176,200);if(menu.trunk.rows()==4)graphics.blit(TEXTURE,leftPos,topPos+80,0,206,176,27);graphics.blit(TEXTURE,leftPos+134,topPos+2,176,16,Math.round(menu.trunk.getHealth()/menu.trunk.getMaxHealth()*39),6);graphics.blit(TEXTURE,leftPos+112,topPos,176,menu.trunk.stay()?0:6,10,10);}
    @Override protected void renderLabels(GuiGraphics graphics,int x,int y){graphics.drawString(font,playerInventoryTitle,8,106,0x404040,false);}
    @Override public void render(GuiGraphics graphics,int x,int y,float partial){super.render(graphics,x,y,partial);renderTooltip(graphics,x,y);if(x>=leftPos+112&&x<leftPos+122&&y>=topPos&&y<topPos+10)graphics.renderTooltip(font,Component.translatable(menu.trunk.stay()?"thaumcraft.trunk.staying":"thaumcraft.trunk.following"),x,y);}
    @Override public boolean mouseClicked(double x,double y,int button){if(button==0&&x>=leftPos+112&&x<leftPos+122&&y>=topPos&&y<topPos+10){minecraft.gameMode.handleInventoryButtonClick(menu.containerId,1);return true;}return super.mouseClicked(x,y,button);}
}
