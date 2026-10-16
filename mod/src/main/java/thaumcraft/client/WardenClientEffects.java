package thaumcraft.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.*;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.*;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import thaumcraft.entities.EldritchWarden;
import thaumcraft.network.*;

/** Native particle passes for TC4's field arcs, summoned smoke and sonic hemisphere. */
public final class WardenClientEffects {
    private static final ParticleRenderType SMOKE=(tessellator,textures)->{RenderSystem.depthMask(false);RenderSystem.enableBlend();RenderSystem.defaultBlendFunc();RenderSystem.setShader(GameRenderer::getParticleShader);RenderSystem.setShaderTexture(0,ResearchGraphics.texture("misc/particles"));return tessellator.begin(VertexFormat.Mode.QUADS,DefaultVertexFormat.PARTICLE);};
    private static final ParticleRenderType SONIC=(tessellator,textures)->{RenderSystem.depthMask(false);RenderSystem.enableBlend();RenderSystem.blendFunc(com.mojang.blaze3d.platform.GlStateManager.SourceFactor.SRC_ALPHA,com.mojang.blaze3d.platform.GlStateManager.DestFactor.ONE);RenderSystem.setShader(GameRenderer::getParticleShader);RenderSystem.setShaderTexture(0,ResearchGraphics.texture("models/sonic_frames"));return tessellator.begin(VertexFormat.Mode.QUADS,DefaultVertexFormat.PARTICLE);};
    static void tick(EldritchWarden warden){var r=warden.getRandom();float x=(float)(warden.getX()+(r.nextFloat()-r.nextFloat())*.2f),z=(float)(warden.getZ()+(r.nextFloat()-r.nextFloat())*.2f);Minecraft.getInstance().particleEngine.add(new Wisp(new Vec3(x,(float)(warden.getY()+.25*warden.getBbHeight()),z),warden));if(warden.getSpawnTimer()>0){float radius=Math.max(1,warden.getBbHeight()*((150-warden.getSpawnTimer())/150f));for(int i=0;i<33;i++)Minecraft.getInstance().particleEngine.add(new Smoke(new Vec3(warden.getX(),warden.getBoundingBox().minY+radius/2f,warden.getZ()),radius,r.nextInt(360),Mth.floor(warden.getBoundingBox().minY)-1));}}
    static void field(Level level,BlockPos pos){var r=level.random;float h=r.nextFloat()*.33f;var at=new Vec3(pos.getX()+r.nextFloat(),pos.getY()+.1515+h/2,pos.getZ()+r.nextFloat());GolemClientEffects.spark(at,.33f+h,.3f-r.nextFloat()*.1f,0,.5f+r.nextFloat()*.2f);if(r.nextInt(50)==0)level.playLocalSound(pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5,thaumcraft.registry.R2Sounds.JACOBS.get(),net.minecraft.sounds.SoundSource.BLOCKS,.5f,1+(r.nextFloat()-r.nextFloat())*.2f,false);}
    public static void arc(BlockArcPayload packet){var level=Minecraft.getInstance().level;if(level==null)return;var source=level.getEntity(packet.source());if(source==null)return;var r=level.random;boolean crimson=source instanceof thaumcraft.entities.CultistPortal;float red=crimson?.5f+r.nextFloat()*.2f:.3f-r.nextFloat()*.1f,blue=crimson?0:.5f+r.nextFloat()*.2f;GolemClientEffects.arc(source.position().add(0,source.getBbHeight()/2f,0),new Vec3(packet.position().getX()+.5,packet.position().getY()+1,packet.position().getZ()+.5),red,0,blue,.5);}
    public static void sonic(SonicPayload packet){var level=Minecraft.getInstance().level;if(level!=null&&level.getEntity(packet.source()) instanceof LivingEntity entity)Minecraft.getInstance().particleEngine.add(new Sonic(entity));}
    private static final class Sonic extends Particle {
        private final LivingEntity target;
        private final float yaw,pitch;
        Sonic(LivingEntity target){super(Minecraft.getInstance().level,target.getX(),target.getY(),target.getZ());this.target=target;yaw=target.getYHeadRot();pitch=target.getXRot();lifetime=10+random.nextInt(5);hasPhysics=false;}
        @Override public ParticleRenderType getRenderType(){return SONIC;}
        @Override public void tick(){xo=x;yo=y;zo=z;if(age++>=lifetime)remove();setPos(target.getX(),target.getY()+target.getEyeHeight(),target.getZ());}
        @Override public void render(VertexConsumer vertices,Camera camera,float partial){var pose=new PoseStack();var view=camera.getPosition();pose.translate(xo+(x-xo)*partial-view.x,yo+(y-yo)*partial-view.y,zo+(z-zo)*partial-view.z);pose.mulPose(Axis.YP.rotationDegrees(-yaw));pose.mulPose(Axis.XP.rotationDegrees(pitch));pose.translate(0,0,2*target.getBbHeight()+target.getBbWidth()/2f);pose.scale(.25f*target.getBbHeight(),.25f*target.getBbHeight(),-target.getBbHeight());int frame=Math.min(15,(int)(14*((age+partial)/lifetime))+1);OriginalObjMesh.get("hemis").renderParticles(pose,vertices,frame,220);}
    }
    private static final class Smoke extends TextureSheetParticle {
        private final Vec3 centre;
        private final float radius,size;
        private final int start,minY;
        Smoke(Vec3 centre,float radius,int start,int minY){super(Minecraft.getInstance().level,centre.x,centre.y,centre.z);this.centre=centre;this.radius=radius;this.start=start;this.minY=minY;hasPhysics=false;setSize(.01f,.01f);size=(random.nextFloat()*.5f+.5f)*2;lifetime=20+level.random.nextInt(10);setColor(0x22/255f,0x11/255f,0x2f/255f);alpha=.66f;}
        @Override public ParticleRenderType getRenderType(){return SMOKE;}
        @Override public void tick(){alpha=.66f*(lifetime-age)/(float)lifetime;if(age++>=lifetime)remove();}
        @Override public float getQuadSize(float partial){return .15f*size;}
        @Override protected float getU0(){return ((int)(1+age/(float)lifetime*4)%16)/16f;}@Override protected float getU1(){return getU0()+.0624375f;}@Override protected float getV0(){return 0;}@Override protected float getV1(){return .0624375f;}
        private Vec3 position(float partial){float a=(start+720*((age+partial)/lifetime))/180*(float)Math.PI,b=(90-180*((age+partial)/lifetime))/180*(float)Math.PI;float dx=-Mth.sin(a)*Mth.cos(b)*radius,dz=Mth.cos(a)*Mth.cos(b)*radius,dy=-Mth.sin(b)*radius;return new Vec3(centre.x+dx,Math.max(centre.y+dy,minY+.1f),centre.z+dz);}
        @Override public void render(VertexConsumer vertices,Camera camera,float partial){var at=position(partial);double priorX=x,priorY=y,priorZ=z;x=xo=at.x;y=yo=at.y;z=zo=at.z;super.render(vertices,camera,partial);x=xo=priorX;y=yo=priorY;z=zo=priorZ;}
    }
    private static final class Wisp extends TextureSheetParticle {
        private final LivingEntity target;
        private final float size;
        Wisp(Vec3 position,LivingEntity target){super(Minecraft.getInstance().level,position.x,position.y,position.z);this.target=target;xd=random.nextGaussian()*.03;yd=-.05;zd=random.nextGaussian()*.03;size=(random.nextFloat()*.5f+.5f)*2*.4f;lifetime=(int)(40/(Math.random()*.3+.7));hasPhysics=true;setSize(.01f,.01f);setColor(random.nextFloat()*.05f,random.nextFloat()*.05f,random.nextFloat()*.05f);var view=Minecraft.getInstance().getCameraEntity();if(view!=null&&view.distanceToSqr(position)>(Minecraft.useFancyGraphics()?2500:625))lifetime=0;}
        @Override public ParticleRenderType getRenderType(){return SMOKE;}
        @Override protected int getLightColor(float partial){return 240;}
        @Override public float getQuadSize(float partial){return .5f*size;}
        @Override protected float getU0(){return age%13/16f;}@Override protected float getU1(){return getU0()+.0624375f;}@Override protected float getV0(){return .1875f;}@Override protected float getV1(){return .2499375f;}
        @Override public void render(VertexConsumer vertices,Camera camera,float partial){alpha=.2f*(1-age/(float)Math.max(1,lifetime))*(float)(1-Math.min(1024,camera.getPosition().distanceToSqr(new Vec3(x,y,z)))/1024);super.render(vertices,camera,partial);}
        @Override public void tick(){xo=x;yo=y;zo=z;if(!onGround){x+=target.getDeltaMovement().x;z+=target.getDeltaMovement().z;}escape();move(xd,yd,zd);xd*=(double).98f;yd*=(double).98f;zd*=(double).98f;if(onGround){xd*=.8500000190734863;zd*=.8500000190734863;}if(age++>=lifetime)remove();}
        private void escape(){var p=BlockPos.containing(x,y,z);if(level.isEmptyBlock(p)||!level.getBlockState(p).isSolidRender(level,p)||level.containsAnyLiquid(getBoundingBox()))return;double[] fractions={x-p.getX(),1-(x-p.getX()),y-p.getY(),1-(y-p.getY()),z-p.getZ(),1-(z-p.getZ())};Direction[] faces={Direction.WEST,Direction.EAST,Direction.DOWN,Direction.UP,Direction.NORTH,Direction.SOUTH};int choice=-1;double nearest=9999;for(int i=0;i<6;i++){var adjacent=p.relative(faces[i]);if(!level.getBlockState(adjacent).isSolidRender(level,adjacent)&&fractions[i]<nearest){nearest=fractions[i];choice=i;}}float velocity=random.nextFloat()*.05f+.025f,side=(random.nextFloat()-random.nextFloat())*.1f;switch(choice){case 0->{xd=-velocity;yd=zd=side;}case 1->{xd=velocity;yd=zd=side;}case 2->{yd=-velocity;xd=zd=side;}case 3->{yd=velocity;xd=zd=side;}case 4->{zd=-velocity;xd=yd=side;}case 5->{zd=velocity;xd=yd=side;}default->{}}}
    }
    private WardenClientEffects(){}
}
