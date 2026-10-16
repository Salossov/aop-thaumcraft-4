package thaumcraft.client;

import com.mojang.blaze3d.pipeline.*;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL30;
import thaumcraft.Thaumcraft;

/** Own post chain after world composition; leaves the vanilla/renderer-owned chain alone. */
final class GuardianFogPostProcessor {
    private static PostChain chain;
    private static PostPass fog;
    private static TextureTarget preservedDepth;
    private static RenderTarget main;
    private static int width,height;
    private static boolean failed;
    static boolean failed() {return failed;}
    static void reload() {close();failed=false;}
    static void close() {
        if(chain!=null)chain.close();if(preservedDepth!=null)preservedDepth.destroyBuffers();
        chain=null;fog=null;preservedDepth=null;main=null;width=height=0;
    }
    private static void prepare(Minecraft minecraft) throws java.io.IOException {
        var target=minecraft.getMainRenderTarget();
        if(chain!=null&&target==main&&width==target.width&&height==target.height)return;
        close();main=target;width=target.width;height=target.height;
        if(target.getDepthTextureId()<0)throw new java.io.IOException("World framebuffer has no depth texture");
        chain=new PostChain(minecraft.getTextureManager(),minecraft.getResourceManager(),target,ResourceLocation.parse("thaumcraft:shaders/post/guardian_mist.json"));
        var swap=chain.getTempTarget("guardian_swap");
        fog=chain.addPass("thaumcraft:guardian_mist",target,swap,false);
        fog.addAuxAsset("DepthSampler",target::getDepthTextureId,width,height);
        chain.addPass("thaumcraft:guardian_copy",swap,target,false);chain.resize(width,height);
        preservedDepth=new TextureTarget(width,height,true,Minecraft.ON_OSX);
    }
    static void render(Matrix4f projection,float density,float partialTick) {
        if(failed||density<=0)return;var minecraft=Minecraft.getInstance();
        boolean blend=GL11.glIsEnabled(GL11.GL_BLEND),depth=GL11.glIsEnabled(GL11.GL_DEPTH_TEST),cull=GL11.glIsEnabled(GL11.GL_CULL_FACE);
        boolean depthMask=GL11.glGetBoolean(GL11.GL_DEPTH_WRITEMASK);int depthFunction=GL11.glGetInteger(GL11.GL_DEPTH_FUNC);
        int srcRgb=GL11.glGetInteger(GL14.GL_BLEND_SRC_RGB),dstRgb=GL11.glGetInteger(GL14.GL_BLEND_DST_RGB),srcAlpha=GL11.glGetInteger(GL14.GL_BLEND_SRC_ALPHA),dstAlpha=GL11.glGetInteger(GL14.GL_BLEND_DST_ALPHA);
        boolean backedUp=false;
        try {
            prepare(minecraft);copyFrame(main,preservedDepth);backedUp=true;
            var effect=fog.getEffect();effect.safeGetUniform("MistDensity").set(density);
            effect.safeGetUniform("InverseProjection").set(new Matrix4f(projection).invert());
            var color=RenderSystem.getShaderFogColor();effect.safeGetUniform("MistColor").set(color[0],color[1],color[2]);
            RenderSystem.disableBlend();RenderSystem.disableCull();RenderSystem.depthMask(true);chain.process(partialTick);
            main.copyDepthFrom(preservedDepth);
        } catch(java.io.IOException|RuntimeException error) {
            if(backedUp)try{copyFrame(preservedDepth,main);}catch(RuntimeException restoreError){error.addSuppressed(restoreError);}
            failed=true;Thaumcraft.LOGGER.error("Guardian mist shader unavailable; using native fog until resource reload",error);close();
        } finally {
            minecraft.getMainRenderTarget().bindWrite(true);
            RenderSystem.depthFunc(depthFunction);RenderSystem.depthMask(depthMask);RenderSystem.blendFuncSeparate(srcRgb,dstRgb,srcAlpha,dstAlpha);
            if(blend)RenderSystem.enableBlend();else RenderSystem.disableBlend();
            if(depth)RenderSystem.enableDepthTest();else RenderSystem.disableDepthTest();
            if(cull)RenderSystem.enableCull();else RenderSystem.disableCull();
        }
    }
    private static void copyFrame(RenderTarget source,RenderTarget target){GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER,source.frameBufferId);GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER,target.frameBufferId);GL30.glBlitFramebuffer(0,0,source.width,source.height,0,0,target.width,target.height,GL11.GL_COLOR_BUFFER_BIT|GL11.GL_DEPTH_BUFFER_BIT,GL11.GL_NEAREST);}
    private GuardianFogPostProcessor() {}
}
