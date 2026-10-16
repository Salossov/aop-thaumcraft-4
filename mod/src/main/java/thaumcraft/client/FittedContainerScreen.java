package thaumcraft.client;

import java.util.List;
import java.util.Optional;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import thaumcraft.config.ThaumcraftClientConfig;

/** Keeps TC4's coordinates and slot geometry while fitting oversized menus to the window. */
abstract class FittedContainerScreen<M extends AbstractContainerMenu> extends AbstractContainerScreen<M> {
    private double interfaceScale=1;

    protected FittedContainerScreen(M menu,Inventory inventory,Component title) {super(menu,inventory,title);}
    protected int fittingWidth() {return imageWidth;}
    protected int fittingHeight() {return imageHeight;}
    protected void fittedScissor(GuiGraphics g,int left,int top,int right,int bottom){g.flush();g.enableScissor((int)Math.floor(left*interfaceScale),(int)Math.floor(top*interfaceScale),(int)Math.ceil(right*interfaceScale),(int)Math.ceil(bottom*interfaceScale));}
    @Override protected void init() {
        int actualWidth=minecraft.getWindow().getGuiScaledWidth(),actualHeight=minecraft.getWindow().getGuiScaledHeight();
        interfaceScale=ThaumcraftClientConfig.FIT_LARGE_SCREENS.get()
            ?Math.min(1,Math.min(Math.max(1,actualWidth-4d)/fittingWidth(),Math.max(1,actualHeight-4d)/fittingHeight())):1;
        width=(int)Math.ceil(actualWidth/interfaceScale);height=(int)Math.ceil(actualHeight/interfaceScale);
        super.init();
    }
    @Override public final void render(GuiGraphics g,int x,int y,float partial) {
        g.pose().pushPose();g.pose().scale((float)interfaceScale,(float)interfaceScale,1);
        RenderSystem.enableBlend();RenderSystem.defaultBlendFunc();
        renderFitted(g,(int)(x/interfaceScale),(int)(y/interfaceScale),partial);
        g.flush();g.pose().popPose();
    }
    protected void renderFitted(GuiGraphics g,int x,int y,float partial) {super.render(g,x,y,partial);}
    // Tooltips retain vanilla font size and use actual window bounds.
    @Override protected void renderTooltip(GuiGraphics g,int x,int y) {
        g.pose().pushPose();g.pose().scale((float)(1/interfaceScale),(float)(1/interfaceScale),1);
        super.renderTooltip(g,(int)Math.round(x*interfaceScale),(int)Math.round(y*interfaceScale));
        g.flush();g.pose().popPose();
    }
    protected void fittedTooltip(GuiGraphics g,Component text,int x,int y) {fittedTooltip(g,List.of(text),x,y);}
    protected void fittedTooltip(GuiGraphics g,List<Component> text,int x,int y) {
        g.pose().pushPose();g.pose().scale((float)(1/interfaceScale),(float)(1/interfaceScale),1);
        g.renderTooltip(font,text,Optional.empty(),(int)Math.round(x*interfaceScale),(int)Math.round(y*interfaceScale));
        g.flush();g.pose().popPose();
    }
    @Override public final boolean mouseClicked(double x,double y,int button) {return mouseClickedFitted(x/interfaceScale,y/interfaceScale,button);}
    protected boolean mouseClickedFitted(double x,double y,int button) {return super.mouseClicked(x,y,button);}
    @Override public final boolean mouseReleased(double x,double y,int button) {return mouseReleasedFitted(x/interfaceScale,y/interfaceScale,button);}
    protected boolean mouseReleasedFitted(double x,double y,int button) {return super.mouseReleased(x,y,button);}
    @Override public final boolean mouseDragged(double x,double y,int button,double dx,double dy) {return mouseDraggedFitted(x/interfaceScale,y/interfaceScale,button,dx/interfaceScale,dy/interfaceScale);}
    protected boolean mouseDraggedFitted(double x,double y,int button,double dx,double dy) {return super.mouseDragged(x,y,button,dx,dy);}
    @Override public final boolean mouseScrolled(double x,double y,double horizontal,double vertical) {return mouseScrolledFitted(x/interfaceScale,y/interfaceScale,horizontal,vertical);}
    protected boolean mouseScrolledFitted(double x,double y,double horizontal,double vertical) {return super.mouseScrolled(x,y,horizontal,vertical);}
    @Override public void mouseMoved(double x,double y) {super.mouseMoved(x/interfaceScale,y/interfaceScale);}
}
