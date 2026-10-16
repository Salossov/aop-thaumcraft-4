package thaumcraft.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.*;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;
import thaumcraft.blocks.PavingStoneBlock;

/** FXBlockRunes: cardinal face rotation, two in-plane offsets, three-times-duration lifetime. */
final class PavingRuneParticle extends TextureSheetParticle {
    private static final ParticleRenderType GLOW=(tessellator,textures)->{
        RenderSystem.depthMask(false);RenderSystem.enableBlend();
        RenderSystem.blendFunc(com.mojang.blaze3d.platform.GlStateManager.SourceFactor.SRC_ALPHA,com.mojang.blaze3d.platform.GlStateManager.DestFactor.ONE);
        RenderSystem.setShader(GameRenderer::getParticleShader);RenderSystem.setShaderTexture(0,ResearchGraphics.texture("misc/particles"));
        return tessellator.begin(VertexFormat.Mode.QUADS,DefaultVertexFormat.PARTICLE);
    };
    private final int rotation,rune;
    private final double offsetX,offsetY;
    private final float scale;
    PavingRuneParticle(PavingStoneBlock.Rune effect){
        super(Minecraft.getInstance().level,effect.x()+.5,effect.y()+.5,effect.z()+.5);
        rotation=random.nextInt(4)*90;
        setColor(effect.red()==0?1:effect.red(),effect.green(),effect.blue());
        gravity=effect.gravity();xd=yd=zd=0;lifetime=3*effect.duration();hasPhysics=false;setSize(.01f,.01f);
        rune=(int)(Math.random()*16);offsetX=random.nextFloat()*.2;offsetY=-.3+random.nextFloat()*.6;
        scale=(float)(1+random.nextGaussian()*(double).1f);alpha=0;
    }
    @Override public ParticleRenderType getRenderType(){return GLOW;}
    @Override protected int getLightColor(float partial){return 0xf000f0;}
    @Override public void tick(){
        xo=x;yo=y;zo=z;float threshold=lifetime/5f;
        alpha=(age<=threshold?age/threshold:(lifetime-age)/(float)lifetime)/2f;
        if(age++>=lifetime)remove();yd-=.04*(double)gravity;x+=xd;y+=yd;z+=zd;
    }
    @Override public void render(VertexConsumer vertices,Camera camera,float partial){
        var eye=camera.getPosition();float px=(float)(Mth.lerp(partial,xo,x)-eye.x),py=(float)(Mth.lerp(partial,yo,y)-eye.y),pz=(float)(Mth.lerp(partial,zo,z)-eye.z);
        double angle=Math.toRadians(rotation),sin=Math.sin(angle),cos=Math.cos(angle);double half=.5*(double)(.3f*scale);
        float u=rune/16f,v=.375f,span=.0624375f;
        double[][] corners={{-half,half,u+span,v+span},{half,half,u+span,v},{half,-half,u,v},{-half,-half,u,v+span}};
        // The original in-plane translation occurs after glRotate(90,Z), so it rotates too.
        for(var point:corners){double horizontal=-offsetY-point[1];vertices.addVertex((float)(px+horizontal*cos-.51*sin),(float)(py+offsetX+point[0]),(float)(pz-horizontal*sin-.51*cos)).setUv((float)point[2],(float)point[3]).setColor(rCol,gCol,bCol,alpha).setLight(0xf000f0);}
    }
}
