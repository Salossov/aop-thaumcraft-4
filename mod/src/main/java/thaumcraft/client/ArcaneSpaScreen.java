package thaumcraft.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import thaumcraft.menu.ArcaneSpaMenu;

public final class ArcaneSpaScreen extends AbstractContainerScreen<ArcaneSpaMenu> {
    private static final ResourceLocation TEXTURE=ResourceLocation.parse("thaumcraft:textures/gui/gui_spa.png");
    public ArcaneSpaScreen(ArcaneSpaMenu menu,Inventory inventory,Component title){super(menu,inventory,title);}
    @Override protected void renderLabels(GuiGraphics g,int x,int y){}
    @Override protected void renderBg(GuiGraphics g,float partial,int mouseX,int mouseY){g.blit(TEXTURE,leftPos,topPos,0,0,imageWidth,imageHeight);g.blit(TEXTURE,leftPos+89,topPos+35,208,menu.mixing()?16:32,8,8);var fluid=menu.fluid();if(!fluid.isEmpty()){var ext=IClientFluidTypeExtensions.of(fluid.getFluid());var sprite=minecraft.getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(ext.getStillTexture(fluid));int color=ext.getTintColor(fluid);g.setColor((color>>16&255)/255f,(color>>8&255)/255f,(color&255)/255f,(color>>>24)/255f);for(int y=0;y<6;y++)g.blit(leftPos+107,topPos+15+y*8,0,8,8,sprite);g.setColor(1,1,1,1);int hidden=(int)(48-48*fluid.getAmount()/5000f);g.blit(TEXTURE,leftPos+107,topPos+15,107,15,10,hidden);}g.blit(TEXTURE,leftPos+106,topPos+11,232,0,10,55);}
    @Override public void render(GuiGraphics g,int x,int y,float partial){super.render(g,x,y,partial);renderTooltip(g,x,y);var fluid=menu.fluid();if(!fluid.isEmpty()&&ResearchGraphics.inside(x,y,leftPos+104,topPos+10,10,55))g.renderComponentTooltip(font,java.util.List.of(fluid.getHoverName(),Component.literal(fluid.getAmount()+" mb")),x,y);if(ResearchGraphics.inside(x,y,leftPos+88,topPos+34,10,10))g.renderTooltip(font,Component.translatable("text.spa.mix."+menu.mixing()),x,y);}
    @Override public boolean mouseClicked(double x,double y,int button){if(button==0&&ResearchGraphics.inside(x,y,leftPos+89,topPos+35,8,8)){minecraft.gameMode.handleInventoryButtonClick(menu.containerId,1);minecraft.player.playSound(thaumcraft.registry.R2Sounds.CAMERA_CLACK.get(),.4f,1);return true;}return super.mouseClicked(x,y,button);}
}
