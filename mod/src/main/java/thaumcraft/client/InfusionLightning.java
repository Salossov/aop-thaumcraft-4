package thaumcraft.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.*;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.phys.*;
import thaumcraft.network.InfusionZapPayload;

/** Short branched discharges in the native particle pass, also used with Sodium. */
public final class InfusionLightning {
    private static final ParticleRenderType TYPE=(tessellator,textures)->{
        RenderSystem.depthMask(false);RenderSystem.enableBlend();RenderSystem.blendFunc(com.mojang.blaze3d.platform.GlStateManager.SourceFactor.SRC_ALPHA,com.mojang.blaze3d.platform.GlStateManager.DestFactor.ONE);
        RenderSystem.setShader(GameRenderer::getParticleShader);RenderSystem.setShaderTexture(0,ResearchGraphics.texture("misc/p_large"));return tessellator.begin(VertexFormat.Mode.QUADS,DefaultVertexFormat.PARTICLE);
    };
    public static void accept(InfusionZapPayload packet){var level=Minecraft.getInstance().level;if(level!=null){bolt(packet.from(),packet.to());level.playLocalSound(packet.from().x,packet.from().y,packet.from().z,thaumcraft.registry.R2Sounds.ZAP.get(),net.minecraft.sounds.SoundSource.BLOCKS,.1f,1+level.random.nextFloat()*.2f,false);}}
    static void bolt(Vec3 from,Vec3 to){if(from.distanceToSqr(to)>4096)return;Minecraft.getInstance().particleEngine.add(new Bolt(from,to));}
    private static final class Bolt extends Particle {
        private final Vec3[] points;
        Bolt(Vec3 from,Vec3 to){
            super(Minecraft.getInstance().level,from.x,from.y,from.z);lifetime=10;hasPhysics=false;
            int count=Math.max(4,(int)(from.distanceTo(to)*6));points=new Vec3[count+1];
            for(int i=0;i<=count;i++){double t=i/(double)count,spread=Math.sin(t*Math.PI)*.18;points[i]=from.lerp(to,t).add((random.nextDouble()-.5)*spread,(random.nextDouble()-.5)*spread,(random.nextDouble()-.5)*spread);}
            setBoundingBox(new AABB(from,to).inflate(.5));
        }
        @Override public ParticleRenderType getRenderType(){return TYPE;}
        @Override public void tick(){if(age++>=lifetime)remove();}
        @Override public void render(VertexConsumer vertices,Camera camera,float partial){
            float alpha=Math.max(0,1-(age+partial)/lifetime);var eye=camera.getPosition();
            for(int i=1;i<points.length;i++){var a=points[i-1].subtract(eye);var b=points[i].subtract(eye);var side=b.subtract(a).cross(a.add(b).scale(-.5)).normalize();
                ribbon(vertices,a,b,side.scale(.045),.55f,.15f,.8f,alpha*.6f);ribbon(vertices,a,b,side.scale(.012),1,.85f,1,alpha);
                if(i%4==0&&i<points.length-1){var branch=b.add(side.scale(.16));ribbon(vertices,b,branch,side.cross(b).normalize().scale(.008),.65f,.3f,1,alpha*.5f);}
            }
        }
        private void ribbon(VertexConsumer vertices,Vec3 a,Vec3 b,Vec3 side,float r,float g,float blue,float alpha){
            Vec3[] corners={a.add(side),a.subtract(side),b.subtract(side),b.add(side)};float[][] uv={{0,0},{0,1},{1,1},{1,0}};
            for(int i=0;i<4;i++)vertices.addVertex((float)corners[i].x,(float)corners[i].y,(float)corners[i].z).setUv(uv[i][0],uv[i][1]).setColor(r,g,blue,alpha).setLight(0xf000f0);
        }
    }
    private InfusionLightning(){}
}
