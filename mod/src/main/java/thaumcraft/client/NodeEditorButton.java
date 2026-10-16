package thaumcraft.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/** Hand-drawn pixel brass frame; labels stay native, localized and keyboard accessible. */
final class NodeEditorButton extends Button {
    private static final ResourceLocation TEXTURE=ResourceLocation.parse("thaumcraft:textures/gui/node_button.png");
    NodeEditorButton(int x,int y,int width,int height,Component label,OnPress action){super(x,y,width,height,label,action,DEFAULT_NARRATION);}
    @Override protected void renderWidget(GuiGraphics g,int mouseX,int mouseY,float partial){
        RenderSystem.enableBlend();RenderSystem.defaultBlendFunc();int state=!active?2:isHoveredOrFocused()?1:0;
        g.setColor(1,1,1,alpha);int edge=5,w=getWidth(),h=getHeight(),x=getX(),y=getY();
        for(int row=0;row<3;row++)for(int col=0;col<3;col++){
            int dx=col==0?x:col==1?x+edge:x+w-edge,dy=row==0?y:row==1?y+edge:y+h-edge;
            int dw=col==1?w-edge*2:edge,dh=row==1?h-edge*2:edge;
            int u=col==0?0:col==1?5:123,v=state*20+(row==0?0:row==1?5:15),sw=col==1?118:5,sh=row==1?10:5;
            g.blit(TEXTURE,dx,dy,dw,dh,u,v,sw,sh,128,60);
        }
        g.setColor(1,1,1,1);var font=Minecraft.getInstance().font;float scale=Math.min(1,(w-14f)/Math.max(1,font.width(getMessage())));
        g.pose().pushPose();g.pose().translate(x+(w-font.width(getMessage())*scale)/2,y+(h-font.lineHeight*scale)/2+1,0);g.pose().scale(scale,scale,1);g.drawString(font,getMessage(),0,0,active?0xfff2ddb0:0xffa59d8d,true);g.pose().popPose();
        if(isFocused())g.renderOutline(x-1,y-1,w+2,h+2,0xffe4c275);
    }
}
