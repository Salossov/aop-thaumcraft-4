package thaumcraft.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.*;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.*;
import net.minecraft.world.phys.Vec3;
import thaumcraft.entities.ChampionSystem;

/** Original modifier-specific FXGeneric atlas sequences and FXSpark. */
public final class ChampionClientEffects {
    private static final ParticleRenderType GENERIC=(tessellator,textures)->{RenderSystem.depthMask(false);RenderSystem.enableBlend();RenderSystem.defaultBlendFunc();RenderSystem.setShader(GameRenderer::getParticleShader);RenderSystem.setShaderTexture(0,ResearchGraphics.texture("misc/particles"));return tessellator.begin(VertexFormat.Mode.QUADS,DefaultVertexFormat.PARTICLE);};
    public static void install(){ChampionSystem.clientEffects=ChampionClientEffects::tick;}
    private static void tick(Monster mob){int type=ChampionSystem.modifier(mob);var r=mob.level().random;
        if(type==12){if(r.nextBoolean()){r.nextFloat();r.nextFloat();Minecraft.getInstance().particleEngine.add(new Breaking(mob.position().add(0,mob.getBbHeight()/2,0),(int)(66/(r.nextFloat()*.9f+.1f))));}return;}
        if(type==2){if(r.nextInt(4)!=0)return;}else if(type==3){if(r.nextFloat()>.3f)return;}else if(type==11){if(r.nextFloat()>.2f)return;}else if(type!=8&&r.nextBoolean())return;
        float w=r.nextFloat()*mob.getBbWidth(),d=r.nextFloat()*mob.getBbWidth(),h=r.nextFloat()*mob.getBbHeight()/(type==0?3:1);Vec3 pos=new Vec3(mob.getBoundingBox().minX+w,mob.getBoundingBox().minY+h,mob.getBoundingBox().minZ+d);
        if(type==0){GolemClientEffects.spark(pos,.2f,.3f-r.nextFloat()*.1f,0,.8f+r.nextFloat()*.2f);return;}
        float red=0,green=0,blue=0,alpha=.7f,size=1;double vy=0;boolean loop=false;int frame=0,count=1,inc=1,life=1;
        switch(type){
            case 1->{frame=176+r.nextInt(4)*3;red=.5f+r.nextFloat()*.2f;green=.1f+r.nextFloat()*.2f;blue=.1f+r.nextFloat()*.2f;count=3;life=3;size=1.2f+r.nextFloat()*.3f;}
            case 2->{red=.9f;green=.9f;blue=.9f+r.nextFloat()*.1f;frame=112;count=9;life=5+r.nextInt(4);size=.6f+r.nextFloat()*.2f;}
            case 3->{frame=176+r.nextInt(4)*3;red=.8f+r.nextFloat()*.2f;green=.8f+r.nextFloat()*.2f;blue=.8f+r.nextFloat()*.2f;count=3;life=4+r.nextInt(3);size=1+r.nextFloat()*.3f;}
            case 4->{red=r.nextFloat()*.2f;green=r.nextFloat()*.2f;blue=r.nextFloat()*.2f;alpha=.8f;frame=160;count=10;life=8+r.nextInt(4);size=.6f+r.nextFloat()*.4f;vy=-.02;}
            case 5->{red=.5f+r.nextFloat()*.1f;green=.5f+r.nextFloat()*.1f;blue=.5f+r.nextFloat()*.1f;alpha=.6f;loop=true;frame=21;count=4;life=4+r.nextInt(4);size=.8f+r.nextFloat()*.3f;}
            case 6->{red=.8f+r.nextFloat()*.2f;green=0;blue=.9f+r.nextFloat()*.1f;loop=true;frame=72;count=8;life=10+r.nextInt(4);size=.6f+r.nextFloat()*.4f;}
            case 7->{red=.1f+r.nextFloat()*.1f;green=.8f+r.nextFloat()*.2f;blue=.1f+r.nextFloat()*.1f;alpha=.9f;loop=true;frame=21;count=4;life=4+r.nextInt(4);size=.5f+r.nextFloat()*.2f;vy=.03;}
            case 8->{red=.9f+r.nextFloat()*.1f;green=blue=1;frame=160;count=10;life=8+r.nextInt(4);size=.7f+r.nextFloat()*.4f;vy=.03;}
            case 9->{red=.2f;green=.6f+r.nextFloat()*.1f;blue=.2f+r.nextFloat()*.1f;alpha=.5f;frame=1;count=4;inc=2;life=5+r.nextInt(4);size=.9f+r.nextFloat()*.3f;vy=-.02;}
            case 10->{red=.2f;green=.6f+r.nextFloat()*.1f;blue=.2f+r.nextFloat()*.1f;frame=147;count=4;life=8+r.nextInt(4);size=.5f+r.nextFloat()*.2f;vy=.02;}
            case 11->{red=.9f+r.nextFloat()*.1f;green=blue=0;alpha=.9f;frame=147;count=4;life=8+r.nextInt(4);size=.5f+r.nextFloat()*.2f;}
            default->{return;}
        }
        Minecraft.getInstance().particleEngine.add(new Generic(pos,vy,red,green,blue,alpha,loop,frame,count,inc,life,size));
    }
    private static final class Generic extends TextureSheetParticle {
        private final boolean loop;private final int start,count,inc;
        Generic(Vec3 p,double vy,float red,float green,float blue,float alpha,boolean loop,int start,int count,int inc,int life,float size){super(Minecraft.getInstance().level,p.x,p.y,p.z);setSize(.1f,.1f);hasPhysics=false;xd=zd=0;yd=vy;setColor(red,green,blue);this.alpha=alpha;this.loop=loop;this.start=start;this.count=count;this.inc=inc;lifetime=life;quadSize=.1f*size;}
        @Override public ParticleRenderType getRenderType(){return GENERIC;}
        private int frame(){return loop?start+age/inc%count:(int)(start+Math.min(count*age/(float)lifetime,count-1));}
        @Override protected float getU0(){return frame()%16/16f;}@Override protected float getU1(){return getU0()+.0624375f;}@Override protected float getV0(){return frame()/16/16f;}@Override protected float getV1(){return getV0()+.0624375f;}
        @Override public void tick(){xo=x;yo=y;zo=z;if(age++>=lifetime)remove();move(xd,yd,zd);xd*=.9800000190734863;yd*=.9800000190734863;zd*=.9800000190734863;}
        @Override public void render(VertexConsumer vertices,Camera camera,float partial){float saved=alpha;if(age<=1||age>=lifetime-1)alpha=saved/2;super.render(vertices,camera,partial);alpha=saved;}
    }
    private static final class Breaking extends TextureSheetParticle {
        private final float jitterU,jitterV;
        Breaking(Vec3 p,int life){super(Minecraft.getInstance().level,p.x,p.y,p.z,0,0,0);setSprite(Minecraft.getInstance().getItemRenderer().getModel(new ItemStack(Items.SLIME_BALL),level,null,0).getParticleIcon());jitterU=random.nextFloat()*3;jitterV=random.nextFloat()*3;gravity=1;quadSize*=.5f;setColor(.7f,0,1);alpha=.4f;lifetime=life;}
        @Override public ParticleRenderType getRenderType(){return ParticleRenderType.TERRAIN_SHEET;}
        @Override protected float getU0(){return sprite.getU(jitterU/4);} @Override protected float getU1(){return sprite.getU((jitterU+1)/4);}@Override protected float getV0(){return sprite.getV(jitterV/4);}@Override protected float getV1(){return sprite.getV((jitterV+1)/4);}
        @Override public float getQuadSize(float partial){return quadSize*(1-age/(float)lifetime);}
        @Override public void render(VertexConsumer vertices,Camera camera,float partial){float saved=alpha;alpha*=1-age/(float)lifetime;super.render(vertices,camera,partial);alpha=saved;}
    }
    private ChampionClientEffects(){}
}
