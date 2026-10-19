package thaumcraft.client;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.GameRenderer;
import thaumcraft.api.client.ParticleApi;

/** Original FXBurst: stationary, no shrink, node-atlas row 31, 32 frames. */
final class LegacyBurstParticle extends TextureSheetParticle {
    private static final ParticleRenderType TYPE=(tessellator,textures)->{
        RenderSystem.depthMask(false);RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA,GlStateManager.DestFactor.ONE);
        RenderSystem.setShader(GameRenderer::getParticleShader);RenderSystem.setShaderTexture(0,ResearchGraphics.texture("misc/nodes"));
        return tessellator.begin(VertexFormat.Mode.QUADS,DefaultVertexFormat.PARTICLE);
    };
    private final float scale;
    LegacyBurstParticle(ClientLevel level,ParticleApi.Effect effect){
        super(level,effect.x(),effect.y(),effect.z());scale=(random.nextFloat()*.5f+.5f)*2*effect.size();
        xd=yd=zd=0;lifetime=31;hasPhysics=true;gravity=0;setSize(.01f,.01f);setColor(1,1,1);
    }
    @Override public ParticleRenderType getRenderType(){return TYPE;}
    @Override protected int getLightColor(float partial){return 0xf000f0;}
    @Override public float getQuadSize(float partial){return scale;}
    @Override protected float getU0(){return (age%32)/32f;}
    @Override protected float getU1(){return getU0()+1/32f;}
    @Override protected float getV0(){return 31/32f;}
    @Override protected float getV1(){return 1;}
    @Override public void tick(){xo=x;yo=y;zo=z;if(age++>=lifetime)remove();}
}
