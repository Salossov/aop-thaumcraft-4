package thaumcraft.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import java.util.*;
import net.minecraft.client.*;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.*;
import thaumcraft.entities.*;

/** TC4 FXSpark, FXBurst and crossed FXArc strips in the native particle pass. */
final class GolemClientEffects {
    private static ParticleRenderType additive(String path){return(tessellator,textures)->{RenderSystem.depthMask(false);RenderSystem.enableBlend();RenderSystem.blendFunc(com.mojang.blaze3d.platform.GlStateManager.SourceFactor.SRC_ALPHA,com.mojang.blaze3d.platform.GlStateManager.DestFactor.ONE);RenderSystem.setShader(GameRenderer::getParticleShader);RenderSystem.setShaderTexture(0,ResearchGraphics.texture(path));return tessellator.begin(VertexFormat.Mode.QUADS,DefaultVertexFormat.PARTICLE);};}
    private static final ParticleRenderType SPARK=additive("misc/particles"),BURST=additive("misc/nodes"),ARC=additive("misc/beamh");
    static void spark(Vec3 from,float scale,float red,float green,float blue){Minecraft.getInstance().particleEngine.add(new Sprite(from,scale,red,green,blue,1,0));}
    static void spark(Vec3 from,float scale,float red,float green,float blue,float alpha){Minecraft.getInstance().particleEngine.add(new Sprite(from,scale,red,green,blue,alpha,0));}
    static void arc(Vec3 from,Vec3 to,float red,float green,float blue,double height){Minecraft.getInstance().particleEngine.add(new Arc(from,to,red,green,blue,height));}
    static void impact(GolemOrb orb){Minecraft.getInstance().particleEngine.add(new Sprite(orb.position(),1,1,1,1,1,1));}
    static void burst(Vec3 position,float scale){Minecraft.getInstance().particleEngine.add(new Sprite(position,scale,1,1,1,1,1));}
    static void tick(EldritchGolem golem){var random=golem.getRandom();float yaw=-golem.yBodyRot*(float)Math.PI/180-(float)Math.PI;float vx=-Mth.sin(yaw),vz=-Mth.cos(yaw);
        if(random.nextInt(20)==0){float a=(random.nextFloat()-random.nextFloat())/2,b=(random.nextFloat()-random.nextFloat())/2;Minecraft.getInstance().particleEngine.add(new Sprite(new Vec3((float)(golem.getX()+vx+a),(float)golem.getY()+golem.getEyeHeight()-.25f,(float)(golem.getZ()+vz+b)),.3f,.65f+random.nextFloat()*.1f,1,1,.8f,0));}
        LegacyParticles.vent(new Vec3((double)(float)golem.getX()+vx*.66,(float)golem.getY()+golem.getEyeHeight()-.75,(double)(float)golem.getZ()+vz*.66),new Vec3(0,.001,0),0x555555,4);
        if(golem.arcing>0){var from=golem.position().add(0,golem.getBbHeight()/2f,0);var to=new Vec3(golem.arcX+.5,golem.arcY+1,golem.arcZ+.5);float red=.65f+random.nextFloat()*.1f;Minecraft.getInstance().particleEngine.add(new Sprite(to,3,red,1,1,1,2));Minecraft.getInstance().particleEngine.add(new Arc(from,to,red,1,1,1-golem.arcing/10f));--golem.arcing;}
    }
    /** Modes 0 spark, 1 burst, 2 endpoint sparkle; original extent is not uniformly multiplied by .1. */
    private static final class Sprite extends TextureSheetParticle {
        private final int mode,frame;
        private final boolean flip;
        private final float size;
        Sprite(Vec3 p,float scale,float red,float green,float blue,float opacity,int mode){super(Minecraft.getInstance().level,p.x,p.y,p.z);this.mode=mode;hasPhysics=false;xd=yd=zd=0;setColor(red,green,blue);alpha=opacity;
            if(mode==0){size=scale;lifetime=5+level.random.nextInt(5);frame=level.random.nextInt(3)*8;flip=level.random.nextBoolean();}
            else{size=(random.nextFloat()*.5f+.5f)*2*scale;lifetime=mode==1?31:6;frame=0;flip=false;}
        }
        @Override public ParticleRenderType getRenderType(){return mode==1?BURST:SPARK;}
        @Override public int getLightColor(float partial){return 240;}
        @Override public float getQuadSize(float partial){return mode==2?.1f*size*(lifetime-age+1)/(float)lifetime:size;}
        private float u(){return mode==1?age%32/32f:mode==2?(16+age/2)%4/16f:(frame+(int)(age/(float)lifetime*7))%8/8f;}
        private float width(){return mode==1?.03125f:mode==2?.0624375f:.125f;}
        @Override protected float getU0(){return flip?u()+width():u();}
        @Override protected float getU1(){return flip?u():u()+width();}
        @Override protected float getV0(){return mode==1?.96875f:mode==2?.25f:(frame+(int)(age/(float)lifetime*7))/8/8f;}
        @Override protected float getV1(){return getV0()+(mode==1?.03125f:mode==2?.0624375f:.125f);}
        @Override public void tick(){xo=x;yo=y;zo=z;if(age++>=lifetime)remove();}
    }
    private static final class Arc extends Particle {
        private final List<Vec3> points=new ArrayList<>();
        private final float length;
        Arc(Vec3 from,Vec3 to,float red,float green,float blue,double height){super(Minecraft.getInstance().level,from.x,from.y,from.z);lifetime=1;hasPhysics=false;setColor(red,green,blue);alpha=.8f;Vec3 end=to.subtract(from);length=(float)end.length();double horizontal=Math.sqrt(end.x*end.x+end.z*end.z),max=Math.max(height,end.y+height),a=-horizontal*horizontal/(4*max),slope=-horizontal/(2*a)-Math.sqrt(horizontal*horizontal+4*a*end.y)/(2*a),vy=Math.sqrt(max*.115),vh=vy/slope;Vec3 velocity=new Vec3(vh*end.x/horizontal,vy,vh*end.z/horizontal),position=Vec3.ZERO;double step=velocity.lengthSqr();points.add(position);
            for(int i=0;end.distanceToSqr(position)>step&&i<50;i++){position=position.add(velocity);points.add(position.add((random.nextDouble()-random.nextDouble())*.25,(random.nextDouble()-random.nextDouble())*.25,(random.nextDouble()-random.nextDouble())*.25));velocity=velocity.add(0,-.115/1.9,0);}points.add(end);setBoundingBox(new AABB(from,to).inflate(3));
        }
        @Override public ParticleRenderType getRenderType(){return ARC;}
        @Override public void tick(){if(age++>=lifetime)remove();}
        @Override public void render(VertexConsumer vertices,Camera camera,float partial){var origin=new Vec3(x-camera.getPosition().x,y-camera.getPosition().y,z-camera.getPosition().z);for(int i=1;i<points.size();i++){for(var side:List.of(new Vec3(0,.25,0),new Vec3(.25,0,.25))){var p=points.get(i-1).add(origin);var q=points.get(i).add(origin);emit(vertices,p.subtract(side),(i-1)/length,1);emit(vertices,p.add(side),(i-1)/length,0);emit(vertices,q.add(side),i/length,0);emit(vertices,q.subtract(side),i/length,1);}}}
        private void emit(VertexConsumer v,Vec3 p,float u,float textureV){v.addVertex((float)p.x,(float)p.y,(float)p.z).setUv(u,textureV).setColor(rCol,gCol,bCol,alpha).setLight(200);}
    }
    private GolemClientEffects(){}
}
