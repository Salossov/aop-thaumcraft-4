package thaumcraft.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

/** TC4 FXSlimyBubble's seven frames, 15..19 tick lifetime and rising motion. */
public final class CleansingParticles {
    private static final ParticleRenderType TYPE=(tessellator,textures)->{RenderSystem.depthMask(false);RenderSystem.enableBlend();RenderSystem.defaultBlendFunc();RenderSystem.setShader(GameRenderer::getParticleShader);RenderSystem.setShaderTexture(0,ResourceLocation.parse("thaumcraft:textures/misc/particles.png"));return tessellator.begin(VertexFormat.Mode.QUADS,DefaultVertexFormat.PARTICLE);};
    public static void bubble(double x,double y,double z,float size,float r,float g,float b,float alpha){var level=Minecraft.getInstance().level;if(level!=null)Minecraft.getInstance().particleEngine.add(new Bubble(level,x,y,z,size,r,g,b,alpha));}
    public static void soap(LivingEntity user,int count){var random=user.getRandom();for(int i=0;i<count;i++)bubble(user.getX()-.5+random.nextFloat(),user.getBoundingBox().minY+random.nextFloat()*user.getBbHeight(),user.getZ()-.5+random.nextFloat(),.075f+random.nextFloat()*.075f,1,.8f,.9f,.45f);}
    private static final class Bubble extends TextureSheetParticle {
        private int frame=144;
        Bubble(net.minecraft.client.multiplayer.ClientLevel level,double x,double y,double z,float size,float r,float g,float b,float a){super(level,x,y,z);xd=yd=zd=0;gravity=0;quadSize=size;lifetime=15+random.nextInt(5);hasPhysics=false;setColor(r,g,b);setAlpha(a);}
        @Override public ParticleRenderType getRenderType(){return TYPE;}
        @Override protected float getU0(){return frame%16/16f;}
        @Override protected float getU1(){return getU0()+.0625f;}
        @Override protected float getV0(){return frame/16/16f;}
        @Override protected float getV1(){return getV0()+.0625f;}
        @Override public void tick(){xo=x;yo=y;zo=z;if(age++>=lifetime){remove();return;}if(age-1<6){frame=144+age/2;if(age==5)y+=.1;}else if(age<lifetime-4){yd+=.005;frame=147+age%4/2;}else{yd/=2;frame=150-(lifetime-age)/2;}y+=yd;}
    }
    public static void pureBubble(double x,double y,double z){var level=Minecraft.getInstance().level;if(level!=null)Minecraft.getInstance().particleEngine.add(new PureBubble(level,x,y,z));}
    private static final class PureBubble extends TextureSheetParticle {
        PureBubble(net.minecraft.client.multiplayer.ClientLevel level,double x,double y,double z){super(level,x,y,z);quadSize=.035f+random.nextFloat()*.025f;lifetime=20+random.nextInt(25);gravity=0;hasPhysics=false;xd=zd=0;yd=.005;setColor(1,1,1);setAlpha(.7f);}
        @Override public ParticleRenderType getRenderType(){return TYPE;}
        @Override protected float getU0(){return 0;}
        @Override protected float getU1(){return .0625f;}
        @Override protected float getV0(){return .0625f;}
        @Override protected float getV1(){return .125f;}
        @Override protected int getLightColor(float partial){return net.minecraft.client.renderer.LightTexture.FULL_BRIGHT;}
        @Override public void tick(){xo=x;yo=y;zo=z;if(age++>=lifetime){remove();return;}y+=yd;yd+=.0005;alpha=.7f*(1-age/(float)lifetime);}
    }
    private CleansingParticles(){}
}
