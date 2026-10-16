package thaumcraft.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.world.phys.Vec3;
import thaumcraft.entities.FollowingItem;

/** Source FXSparkle / FXBubble trails, using the owned TC4 particle atlas. */
final class FollowingItemEffects {
    static void install(){FollowingItem.clientTrail=FollowingItemEffects::trail;}
    private static void trail(FollowingItem.Trail trail){
        var minecraft=Minecraft.getInstance();var level=minecraft.level;if(level==null)return;
        if(trail.style()==10)minecraft.particleEngine.add(new Bubble(level,trail.position()));
        else if(level.random.nextInt(6)<LegacyParticles.particleCount(2))minecraft.particleEngine.add(new Sparkle(level,trail.position(),trail.style()));
    }
    static void sparkle(Vec3 position,float scale,int style,float gravity){
        var mc=Minecraft.getInstance();if(mc.level==null||mc.level.random.nextInt(6)>=LegacyParticles.particleCount(2))return;
        mc.particleEngine.add(new Sparkle(mc.level,position,style,scale,gravity));
    }
    private abstract static class AtlasParticle extends TextureSheetParticle {
        AtlasParticle(ClientLevel level,Vec3 position){super(level,position.x,position.y,position.z);hasPhysics=false;}
        @Override protected float getU1(){return getU0()+.0624375f;}
        @Override protected float getV1(){return getV0()+.0624375f;}
        @Override protected int getLightColor(float partial){return 0xf000f0;}
    }
    private static final class Sparkle extends AtlasParticle {
        private final boolean dark;
        Sparkle(ClientLevel level,Vec3 position,int style){
            this(level,position,style,1.5f,0);
        }
        Sparkle(ClientLevel level,Vec3 position,int style,float scale,float gravity){
            super(level,position);lifetime=18;quadSize=(1+random.nextFloat())*scale*.1f;this.gravity=gravity;dark=style==5;
            var r=level.random;setColor(1,0,0);
            switch(style){
                case 0->setColor(.75f+r.nextFloat()*.25f,.25f+r.nextFloat()*.25f,.75f+r.nextFloat()*.25f);
                case 1->setColor(.5f+r.nextFloat()*.3f,.5f+r.nextFloat()*.3f,.2f);
                case 2->setColor(.2f,.2f,.7f+r.nextFloat()*.3f);
                case 3->setColor(.2f,.7f+r.nextFloat()*.3f,.2f);
                case 4->setColor(.7f+r.nextFloat()*.3f,.2f,.2f);
                case 5->setColor(r.nextFloat()*.1f,r.nextFloat()*.1f,r.nextFloat()*.1f);
                case 6->setColor(.8f+r.nextFloat()*.2f,.8f+r.nextFloat()*.2f,.8f+r.nextFloat()*.2f);
                case 7->setColor(.2f,.5f+r.nextFloat()*.3f,.6f+r.nextFloat()*.3f);
            }
        }
        @Override public ParticleRenderType getRenderType(){return dark?LegacyParticles.TRAIL:LegacyParticles.GLOW;}
        @Override protected float getU0(){return (age/6%4)/16f;}
        @Override protected float getV0(){return .25f;}
        @Override public float getQuadSize(float partial){return quadSize*(lifetime-age+1)/(float)lifetime;}
        @Override public void tick(){xo=x;yo=y;zo=z;if(age++>=lifetime)remove();yd-=.04*(double)gravity;x+=xd;y+=yd;z+=zd;xd*=.9080000019073486;yd*=.9080000019073486;zd*=.9080000019073486;}
    }
    private static final class Bubble extends AtlasParticle {
        private int frame=16;
        Bubble(ClientLevel level,Vec3 position){
            super(level,position);setColor(.33f,.33f,1);quadSize=(1+random.nextFloat())*(random.nextFloat()*.3f+.2f)*.1f;
            xd=(float)(Math.random()*2-1)*.02f;yd=(float)Math.random()*.02f;zd=(float)(Math.random()*2-1)*.02f;
            lifetime=(int)(3+8/(Math.random()*.8+.2));
            var camera=Minecraft.getInstance().getCameraEntity();double distance=Minecraft.getInstance().options.graphicsMode().get()==net.minecraft.client.GraphicsStatus.FAST?25:50;
            if(camera!=null&&camera.distanceToSqr(position)>distance*distance)lifetime=0;
        }
        @Override public ParticleRenderType getRenderType(){return LegacyParticles.TRAIL;}
        @Override protected float getU0(){return frame%16/16f;}
        @Override protected float getV0(){return frame/16/16f;}
        @Override public void tick(){
            xo=x;yo=y;zo=z;yd+=.002;xd+=(level.random.nextFloat()-level.random.nextFloat())*.01f;zd+=(level.random.nextFloat()-level.random.nextFloat())*.01f;
            x+=xd;y+=yd;z+=zd;xd*=(double).85f;yd*=(double).85f;zd*=(double).85f;
            if(lifetime--<=0)remove();else if(lifetime<=2)++frame;
        }
    }
    private FollowingItemEffects(){}
}
