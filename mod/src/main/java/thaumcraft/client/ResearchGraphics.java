package thaumcraft.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import thaumcraft.api.aspects.Aspect;

final class ResearchGraphics {
    static final int INK=0xff3b2618,WHITE=0xffffffff,LINK=0xe0ffffef,AVAILABLE=0xffe3c564,LOCKED=0xff777777;
    static ResourceLocation texture(String name) {return ResourceLocation.fromNamespaceAndPath("thaumcraft","textures/"+name+".png");}
    /** Flush font/fill batches before restoring alpha blending for an immediate GUI blit. */
    static void blit(GuiGraphics g,ResourceLocation texture,int x,int y,float u,float v,int w,int h,int tw,int th) {
        float[] color=RenderSystem.getShaderColor().clone();g.flush();RenderSystem.enableBlend();RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(color[0],color[1],color[2],color[3]);g.blit(texture,x,y,u,v,w,h,tw,th);
    }
    static void blit(GuiGraphics g,ResourceLocation texture,int x,int y,int w,int h,float u,float v,int rw,int rh,int tw,int th) {
        float[] color=RenderSystem.getShaderColor().clone();g.flush();RenderSystem.enableBlend();RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(color[0],color[1],color[2],color[3]);g.blit(texture,x,y,w,h,u,v,rw,rh,tw,th);
    }
    static void aspect(GuiGraphics graphics,Aspect aspect,int x,int y,float alpha) {
        if(aspect==null) return;int color=aspect.getColor();RenderSystem.enableBlend();RenderSystem.defaultBlendFunc();
        graphics.setColor(((color>>16)&255)/255f,((color>>8)&255)/255f,(color&255)/255f,alpha);
        // Aspect sprites are 32x32; blit the whole texture into a 16x16 slot.
        blit(graphics,ResourceLocation.parse(aspect.texture()),x,y,16,16,0,0,32,32,32,32);graphics.setColor(1,1,1,1);
    }
    static void icon(GuiGraphics g,ResourceLocation texture,int x,int y,int w,int h,float brightness){
        g.setColor(1,1,1,1);var v=g.bufferSource().getBuffer(ThaumcraftRenderTypes.guiIcon(texture));var matrix=g.pose().last().pose();int shade=Math.clamp(Math.round(brightness*255),0,255);
        v.addVertex(matrix,x,y+h,0).setUv(0,1).setColor(shade,shade,shade,255);v.addVertex(matrix,x+w,y+h,0).setUv(1,1).setColor(shade,shade,shade,255);v.addVertex(matrix,x+w,y,0).setUv(1,0).setColor(shade,shade,shade,255);v.addVertex(matrix,x,y,0).setUv(0,0).setColor(shade,shade,shade,255);g.flush();RenderSystem.enableBlend();RenderSystem.defaultBlendFunc();
    }
    /** Original Item Grate inventory geometry: a 3/16-high plate with its hatch layer. */
    static void grateIcon(GuiGraphics g,int x,int y,float brightness) {
        g.flush();g.pose().pushPose();g.pose().translate(x+8,y+9,150);g.pose().scale(11.313708f,-11.313708f,11.313708f);g.pose().mulPose(com.mojang.math.Axis.XP.rotationDegrees(30));g.pose().mulPose(com.mojang.math.Axis.YP.rotationDegrees(45));g.pose().translate(-.5,-.9,-.5);
        float[][][] faces={{{0,1,0},{0,1,1},{1,1,1},{1,1,0}},{{0,.8125f,0},{1,.8125f,0},{1,.8125f,1},{0,.8125f,1}},{{0,.8125f,0},{0,1,0},{1,1,0},{1,.8125f,0}},{{1,.8125f,1},{1,1,1},{0,1,1},{0,.8125f,1}},{{1,.8125f,0},{1,1,0},{1,1,1},{1,.8125f,1}},{{0,.8125f,1},{0,1,1},{0,1,0},{0,.8125f,0}}};
        for(String layer:java.util.List.of("grate","grate_hatch")){var vertices=g.bufferSource().getBuffer(ThaumcraftRenderTypes.guiIcon(texture("blocks/"+layer)));var matrix=g.pose().last().pose();
            for(int face=0;face<faces.length;face++){int shade=Math.clamp(Math.round(brightness*(face==0?1:face<4?.8f:.6f)*255),0,255);for(int i=0;i<4;i++){var point=faces[face][i];vertices.addVertex(matrix,point[0],point[1],point[2]).setUv(i<2?0:1,i==0||i==3?1:0).setColor(shade,shade,shade,255);}}g.flush();}
        g.pose().popPose();RenderSystem.enableBlend();RenderSystem.defaultBlendFunc();
    }
    /** TC4's empty-space block texture, projected at the same size as a vanilla GUI block item. */
    static void emptySpaceIcon(GuiGraphics g,int x,int y){
        g.flush();g.pose().pushPose();g.pose().translate(x+8,y+8,150);g.pose().scale(10,-10,10);
        g.pose().mulPose(com.mojang.math.Axis.XP.rotationDegrees(30));g.pose().mulPose(com.mojang.math.Axis.YP.rotationDegrees(225));g.pose().translate(-.5,-.5,-.5);
        float[][][] faces={{{0,1,0},{0,1,1},{1,1,1},{1,1,0}},{{0,0,0},{0,1,0},{1,1,0},{1,0,0}},{{1,0,0},{1,1,0},{1,1,1},{1,0,1}}};
        var vertices=g.bufferSource().getBuffer(ThaumcraftRenderTypes.guiIcon(texture("blocks/empty")));var matrix=g.pose().last().pose();
        for(var face:faces)for(int i=0;i<4;i++){var p=face[i];vertices.addVertex(matrix,p[0],p[1],p[2]).setUv(i<2?0:1,i==0||i==3?1:0).setColor(255,255,255,255);}
        g.flush();g.pose().popPose();RenderSystem.enableBlend();RenderSystem.defaultBlendFunc();
    }
    static void glowingIcon(GuiGraphics g,ResourceLocation texture,int x,int y,int u,int v,int w,int h,int tw,int th,float alpha) {
        additiveSprite(g,texture,x,y,w,h,u/(float)tw,v/(float)th,(u+w)/(float)tw,(v+h)/(float)th,0xffffff,alpha);
    }
    private static void additiveSprite(GuiGraphics g,ResourceLocation texture,int x,int y,int w,int h,float u0,float v0,float u1,float v1,int color,float alpha){
        g.flush();g.setColor(1,1,1,1);
        var out=g.bufferSource().getBuffer(ThaumcraftRenderTypes.unlit(texture,true,true));var matrix=g.pose().last().pose();int a=Math.clamp(Math.round(alpha*255),0,255),r=color>>16&255,green=color>>8&255,b=color&255;
        out.addVertex(matrix,x,y+h,0).setUv(u0,v1).setColor(r,green,b,a);
        out.addVertex(matrix,x+w,y+h,0).setUv(u1,v1).setColor(r,green,b,a);
        out.addVertex(matrix,x+w,y,0).setUv(u1,v0).setColor(r,green,b,a);
        out.addVertex(matrix,x,y,0).setUv(u0,v0).setColor(r,green,b,a);
        g.flush();g.setColor(1,1,1,1);RenderSystem.enableBlend();RenderSystem.defaultBlendFunc();
    }
    static void aspectHighlight(GuiGraphics g,int x,int y){
        // _back is a dark backing, not an additive light: additive blending erased it on parchment.
        blit(g,texture("aspects/_back"),x,y,20,20,0,0,64,64,64,64);
    }
    static void discoveryFlame(GuiGraphics g,int x,int y,float alpha) {
        int frame=net.minecraft.client.Minecraft.getInstance().player==null?0:net.minecraft.client.Minecraft.getInstance().player.tickCount%16;
        glowingIcon(g,texture("misc/particles"),x-4,y-4,frame*16,80,16,16,256,256,alpha);
    }
    static void researchOrb(GuiGraphics g,int x,int y,int color,float alpha){
        int tick=net.minecraft.client.Minecraft.getInstance().player==null?0:net.minecraft.client.Minecraft.getInstance().player.tickCount;
        additiveSprite(g,texture("misc/particles"),x,y,16,16,(128+(tick%8)*16)/256f,.5f,(144+(tick%8)*16)/256f,144/256f,color,alpha);
    }
    static void forbidden(GuiGraphics g,int x,int y,int tick) {
        RenderSystem.enableBlend();RenderSystem.defaultBlendFunc();g.setColor(.267f,0,.333f,.66f);
        blit(g,texture("misc/nodes"),x-40,y-40,80,80,(31-tick%32)*32,5*32,32,32,1024,1024);g.setColor(1,1,1,1);
    }
    static Component aspectName(Aspect aspect) {if(aspect.translationKey()!=null)return Component.translatable(aspect.translationKey());return Component.literal(aspect.getTag().substring(0,1).toUpperCase(java.util.Locale.ROOT)+aspect.getTag().substring(1));}
    static boolean inside(double x,double y,int left,int top,int width,int height) {return x>=left&&x<left+width&&y>=top&&y<top+height;}
    static void outlinedNumber(GuiGraphics g,net.minecraft.client.gui.Font font,String value,int x,int y){for(int[] offset:new int[][]{{-1,0},{1,0},{0,-1},{0,1}})g.drawString(font,value,x+offset[0],y+offset[1],0xff000000,false);g.drawString(font,value,x,y,WHITE,false);}
    static void aspectCount(GuiGraphics g,net.minecraft.client.gui.Font font,String value,int x,int y){g.pose().pushPose();g.pose().translate(x+16-font.width(value)*.5f,y+11,150);g.pose().scale(.5f,.5f,1);outlinedNumber(g,font,value,0,0);g.pose().popPose();}
    static void line(GuiGraphics graphics,int x,int y,int endX,int endY,int color) {
        int steps=Math.max(Math.abs(endX-x),Math.abs(endY-y));for(int i=0;i<=steps;i++) {int px=x+(steps==0?0:(endX-x)*i/steps),py=y+(steps==0?0:(endY-y)*i/steps);graphics.fill(px,py,px+1,py+1,color);}
    }
    static void researchLink(GuiGraphics g,int x,int y,int targetX,int targetY,boolean complete,boolean parentComplete,float time) {
        float distance=(float)Math.hypot(x-targetX,y-targetY);int steps=Math.max(1,(int)(distance/2));
        float dx=(x-targetX)/(float)steps,dy=(y-targetY)/(float)steps;boolean horizontal=Math.abs(dx)>Math.abs(dy);
        if(horizontal)dx*=2;else dy*=2;var points=new java.util.ArrayList<org.joml.Vector2f>();
        for(int i=0;i<=steps;i++){float phase=i/(float)steps,wiggle=complete?0:1-phase;points.add(new org.joml.Vector2f(x-dx*i+(float)Math.sin((time+i)/7)*5*wiggle,y-dy*i+(float)Math.sin((time+i)/5)*5*wiggle));if(horizontal)dx*=1-1/(steps*1.5f);else dy*=1-1/(steps*1.5f);}
        g.flush();var vertices=g.bufferSource().getBuffer(net.minecraft.client.renderer.RenderType.gui());var matrix=g.pose().last().pose();float half=1.5f/(float)net.minecraft.client.Minecraft.getInstance().getWindow().getGuiScale();
        for(int i=1;i<points.size();i++){
            var first=points.get(i-1);var next=points.get(i);float length=first.distance(next);if(length<0.001f)continue;float nx=(next.y-first.y)/length*half,ny=(first.x-next.x)/length*half;
            int firstColor=linkColor((i-1)/(float)steps,complete,parentComplete),nextColor=linkColor(i/(float)steps,complete,parentComplete);
            // GUI quads use clockwise vertex order in the projected XY plane.
            vertices.addVertex(matrix,first.x+nx,first.y+ny,0).setColor(firstColor);vertices.addVertex(matrix,first.x-nx,first.y-ny,0).setColor(firstColor);vertices.addVertex(matrix,next.x-nx,next.y-ny,0).setColor(nextColor);vertices.addVertex(matrix,next.x+nx,next.y+ny,0).setColor(nextColor);
        }g.flush();
    }
    private static int linkColor(float phase,boolean complete,boolean parentComplete){int shade=complete?26:Math.round(255*(1-phase)),alpha=Math.round(153*(complete?1:phase));return alpha<<24|(complete?0x1a1a1a:parentComplete?shade<<8:shade);}
    static java.util.List<Component> aspectTooltip(Aspect aspect) {
        return java.util.List.of(aspectName(aspect).copy().withStyle(net.minecraft.ChatFormatting.AQUA),Component.translatable("tc.aspect."+aspect.getTag()).withStyle(net.minecraft.ChatFormatting.GRAY));
    }
    private ResearchGraphics() {}
}
