package thaumcraft.client;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import thaumcraft.api.client.ParticleApi;

/** Source FXSmokeSpiral: only age/alpha tick; the orbit is evaluated at render time. */
final class SmokeSpiralParticle extends TextureSheetParticle {
    private final ParticleApi.SmokeSpiral effect;
    private final float sourceSize;
    SmokeSpiralParticle(ClientLevel level,ParticleApi.SmokeSpiral effect){
        super(level,effect.x(),effect.y(),effect.z());this.effect=effect;
        sourceSize=(random.nextFloat()*.5f+.5f)*2;
        lifetime=20+level.random.nextInt(10);xd=yd=zd=0;hasPhysics=false;setSize(.01f,.01f);
        setColor((effect.color()>>16&255)/255f,(effect.color()>>8&255)/255f,(effect.color()&255)/255f);
    }
    @Override public ParticleRenderType getRenderType(){return SmokeSpiralRendering.PARTICLES;}
    @Override protected int getLightColor(float partialTick){
        var pos=BlockPos.containing(effect.x(),effect.y(),effect.z());return level.hasChunkAt(pos)?LevelRenderer.getLightColor(level,pos):0;
    }
    @Override public AABB getRenderBoundingBox(float partialTick){
        double pad=sourceSize*.15f,clamp=(double)((float)effect.minY()+.1f);
        return new AABB(effect.x()-effect.radius()-pad,Math.max(effect.y()-effect.radius(),clamp)-pad,effect.z()-effect.radius()-pad,
                effect.x()+effect.radius()+pad,Math.max(effect.y()+effect.radius(),clamp)+pad,effect.z()+effect.radius()+pad);
    }
    @Override public float getQuadSize(float partialTick){return .15f*sourceSize;}
    @Override protected float getU0(){return ((int)(1f+(float)age/lifetime*4)%16)/16f;}
    @Override protected float getU1(){return getU0()+.0624375f;}
    @Override protected float getV0(){return ((int)(1f+(float)age/lifetime*4)/16)/16f;}
    @Override protected float getV1(){return getV0()+.0624375f;}
    @Override public void tick(){
        alpha=(float)(lifetime-age)/lifetime;
        if(age++>=lifetime)remove();
    }
    @Override public void render(VertexConsumer consumer,Camera camera,float partialTick){
        var orbit=SmokeSpiralShape.position(effect,age,lifetime,partialTick);
        x=xo=orbit.x();y=yo=orbit.y();z=zo=orbit.z();float originalAlpha=alpha;alpha*=.66f;
        try{super.render(consumer,camera,partialTick);}
        finally{x=xo=effect.x();y=yo=effect.y();z=zo=effect.z();alpha=originalAlpha;}
    }
}
