package thaumcraft.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.joml.Quaternionf;

/** TC4 particle motion in Minecraft's particle engine; no world-renderer hooks. */
public final class LegacyParticles {
    static final ParticleRenderType GLOW=(tessellator,textures)->{
        RenderSystem.depthMask(false);RenderSystem.enableBlend();
        RenderSystem.blendFunc(com.mojang.blaze3d.platform.GlStateManager.SourceFactor.SRC_ALPHA,com.mojang.blaze3d.platform.GlStateManager.DestFactor.ONE);
        RenderSystem.setShader(GameRenderer::getParticleShader);RenderSystem.setShaderTexture(0,ResearchGraphics.texture("misc/particles"));
        return tessellator.begin(VertexFormat.Mode.QUADS,DefaultVertexFormat.PARTICLE);
    };
    static final ParticleRenderType TRAIL=(tessellator,textures)->{RenderSystem.depthMask(false);RenderSystem.enableBlend();RenderSystem.defaultBlendFunc();RenderSystem.setShader(GameRenderer::getParticleShader);RenderSystem.setShaderTexture(0,ResearchGraphics.texture("misc/particles"));return tessellator.begin(VertexFormat.Mode.QUADS,DefaultVertexFormat.PARTICLE);};
    static void essentia(Vec3 from,Vec3 target,float count,int color){add(new Mote(from,target,count,color,0,ItemStack.EMPTY,null));}
    static void ingredient(Vec3 from,Vec3 target,ItemStack stack){add(new Mote(from,target,0,0xffffff,1,stack,null));}
    static void infusionSparkle(Vec3 from,Vec3 target){add(new Mote(from,target,0,0x8033cc,6,ItemStack.EMPTY,null));}
    static void nitor(Vec3 from,boolean outer){add(new Mote(from,null,0,0xffffff,outer?4:5,ItemStack.EMPTY,null));}
    public static void vent(Vec3 from,Vec3 velocity,int color,float scale){Minecraft.getInstance().particleEngine.add(new Vent(from,velocity,color,scale));}
    static void obeliskWisp(Vec3 from,net.minecraft.world.entity.LivingEntity target){Minecraft.getInstance().particleEngine.add(new ObeliskWisp(from,target));}
    static void rune(Vec3 centre,Direction face,int color,int duration,float gravity){rune(centre,face,color,Vec3.ZERO,duration,gravity);}
    static void rune(Vec3 centre,Direction face,int color,Vec3 velocity,int duration,float gravity){var p=new Mote(centre,null,0,color,2,ItemStack.EMPTY,face);p.timing(duration,gravity,velocity);add(p);}
    static int particleCount(int base){int setting=Minecraft.getInstance().options.particles().get().getId();return setting==2?0:setting==1?base:base*2;}
    static void blockRune(thaumcraft.blocks.PavingStoneBlock.Rune effect){Minecraft.getInstance().particleEngine.add(new PavingRuneParticle(effect));}
    static void blockSparkles(net.minecraft.core.BlockPos pos,int color,int base){
        var l=Minecraft.getInstance().level;if(l==null)return;
        thaumcraft.blocks.PavingSparkles.emit(pos,color,particleCount(base),l.random,e->{
            var p=new Mote(e.position(),null,0,color,8,ItemStack.EMPTY,null);p.setColor(e.red(),e.green(),e.blue());p.delay=e.delay();p.size=e.scale();p.timing(e.lifetime()+e.delay(),0,new Vec3(0,e.upward(),0));add(p);
        });
    }
    static void spark(Vec3 from,Vec3 velocity,int color,int lifetime){var p=new Mote(from,null,0,color,3,ItemStack.EMPTY,null);p.timing(lifetime,0,velocity);add(p);}
    static void liftSparkle(Vec3 from){var l=Minecraft.getInstance().level;if(l==null||l.random.nextInt(6)>=2-Minecraft.getInstance().options.particles().get().getId())return;var p=new Mote(from,null,0,0x33cc33,7,ItemStack.EMPTY,null);p.setColor(.2f,.7f+l.random.nextFloat()*.3f,.2f);p.size=(.5f+l.random.nextFloat()*.5f)*2;p.timing(18,-.3f,Vec3.ZERO);add(p);}
    static void blockSparkles(net.minecraft.core.BlockPos pos,int color){var l=Minecraft.getInstance().level;if(l==null)return;var r=l.random;int setting=Minecraft.getInstance().options.particles().get().getId(),count=setting==2?0:setting==1?7:14;for(int i=0;i<count;i++){var p=new Mote(new Vec3(pos.getX()-.1+r.nextFloat()*1.2,pos.getY()-.1+r.nextFloat()*1.2,pos.getZ()-.1+r.nextFloat()*1.2),null,0,color,8,ItemStack.EMPTY,null);float red=color==-9999?.33f+r.nextFloat()*.67f:(color>>16&255)/255f,green=color==-9999?.33f+r.nextFloat()*.67f:(color>>8&255)/255f,blue=color==-9999?.33f+r.nextFloat()*.67f:(color&255)/255f;p.setColor(red-.2f+r.nextFloat()*.4f,green-.2f+r.nextFloat()*.4f,blue-.2f+r.nextFloat()*.4f);p.delay=r.nextInt(10);p.size=.7f+r.nextFloat()*.4f;p.timing(5+r.nextInt(8)+p.delay,0,new Vec3(0,r.nextFloat()*.02,0));add(p);}}
    private static void add(Mote mote){Minecraft.getInstance().particleEngine.add(mote);}
    /** Original FXVent grows until its maximum scale, then expires; no age-based fade. */
    private static final class ObeliskWisp extends TextureSheetParticle {
        private final net.minecraft.world.entity.LivingEntity target;
        private final float size;
        ObeliskWisp(Vec3 from,net.minecraft.world.entity.LivingEntity target){super(Minecraft.getInstance().level,from.x,from.y,from.z);this.target=target;setSize(.1f,.1f);hasPhysics=true;xd=yd=zd=0;size=(random.nextFloat()*.5f+.5f)*2*.4f;lifetime=(int)(36/(Math.random()*.3+.7));alpha=.5f;setColor(level.random.nextFloat()*.1f,level.random.nextFloat()*.1f,level.random.nextFloat()*.1f);var camera=Minecraft.getInstance().getCameraEntity();if(camera!=null&&camera.distanceToSqr(from)>(Minecraft.useFancyGraphics()?2500:625))lifetime=0;}
        @Override public ParticleRenderType getRenderType(){return TRAIL;}
        @Override protected int getLightColor(float partial){return 0xf000f0;}
        @Override public float getQuadSize(float partial){return .5f*size*(lifetime-age)/(float)Math.max(1,lifetime);}
        @Override protected float getU0(){return 0;}@Override protected float getU1(){return .125f;}@Override protected float getV0(){return .875f;}@Override protected float getV1(){return 1;}
        @Override public void tick(){xo=x;yo=y;zo=z;if(age++>=lifetime)remove();yd-=.04;var motion=LegacyWispPhysics.pushOut(level,random,new Vec3(x,y,z),getBoundingBox(),new Vec3(xd,yd,zd));xd=motion.x;yd=motion.y;zd=motion.z;move(xd,yd,zd);xd*=.985;yd*=.985;zd*=.985;var direction=target.position().add(0,target.getBbHeight()/2f,0).subtract(x,y,z).normalize().scale(.2);xd=net.minecraft.util.Mth.clamp((float)(xd+direction.x),-.2f,.2f);yd=net.minecraft.util.Mth.clamp((float)(yd+direction.y),-.2f,.2f);zd=net.minecraft.util.Mth.clamp((float)(zd+direction.z),-.2f,.2f);}
    }
    private static final class Vent extends TextureSheetParticle {
        private float size;private final float maximum;
        Vent(Vec3 from,Vec3 velocity,int color,float scale){super(Minecraft.getInstance().level,from.x,from.y,from.z);hasPhysics=false;size=(random.nextFloat()*.1f+.05f)*scale;maximum=scale;alpha=.4f;setColor((color>>16&255)/255f,(color>>8&255)/255f,(color&255)/255f);var heading=velocity.normalize();xd=noise(heading.x)*.125f;yd=noise(heading.y)*.125f;zd=noise(heading.z)*.125f;var player=Minecraft.getInstance().getCameraEntity();if(player!=null&&player.distanceToSqr(from)>(Minecraft.useFancyGraphics()?2500:625))remove();}
        private double noise(double value){return value+random.nextGaussian()*(random.nextBoolean()?-1:1)*.0075f*5;}
        @Override public ParticleRenderType getRenderType(){return TRAIL;}
        @Override public void tick(){xo=x;yo=y;zo=z;++age;if(size>maximum){remove();return;}yd+=.0025;move(xd,yd,zd);xd*=.8500000190734863;yd*=.8500000190734863;zd*=.8500000190734863;if(size<maximum)size*=1.15f;}
        @Override public float getQuadSize(float partial){return .3f*size;}
        @Override protected float getU0(){return (int)(1+size/maximum*4)%16/16f;}
        @Override protected float getU1(){return getU0()+.0624375f;}
        @Override protected float getV0(){return 0;}
        @Override protected float getV1(){return .0624375f;}
        @Override public void render(VertexConsumer vertices,Camera camera,float partial){float saved=alpha;alpha=saved*(maximum-size)/maximum;super.render(vertices,camera,partial);alpha=saved;}
    }
    private static final class Mote extends TextureSheetParticle {
        private final Vec3 target;private final int kind,rune;private int delay;private final float count;private final Direction face;private final float jitterU,jitterV;private float size;
        Mote(Vec3 from,Vec3 target,float count,int color,int kind,ItemStack stack,Direction face){
            super(Minecraft.getInstance().level,from.x,from.y,from.z);this.target=target;this.kind=kind;this.count=count;this.face=face;hasPhysics=false;
            rune=random.nextInt(16);jitterU=random.nextFloat()*.75f;jitterV=random.nextFloat()*.75f;size=kind==1?(random.nextFloat()*.3f+.4f):kind==2?1:1.2f;
            setColor((color>>16&255)/255f,(color>>8&255)/255f,(color&255)/255f);alpha=.5f;gravity=.2f;
            if(kind==7)alpha=1;if(kind==8)alpha=.9f;
            int base=target==null?30:Math.max(1,(int)(from.distanceTo(target)*(kind==0?30:3)));lifetime=base/2+random.nextInt(base);
            if(kind==0){var velocity=thaumcraft.effects.EssentiaFlow.initialVelocity(count);xd=velocity.x;yd=velocity.y;zd=velocity.z;size=1;lifetime=thaumcraft.effects.EssentiaFlow.flightTicks(from,target,count)+1;}
            else if(kind==1){setSprite(Minecraft.getInstance().getItemRenderer().getModel(stack,level,null,0).getParticleIcon());xd=random.nextGaussian()*.03;yd=random.nextGaussian()*.03;zd=random.nextGaussian()*.03;/* TC4 render uses setColorOpaque_F, ignoring particleAlpha. */alpha=1;setColor(.6f,.6f,.6f);}
            else if(kind==6){size=random.nextFloat()*.5f+.5f;alpha=1;xd=random.nextGaussian()*.01;yd=random.nextGaussian()*.01;zd=random.nextGaussian()*.01;}
            else if(kind==4||kind==5){boolean outer=kind==4;lifetime=(int)(36/(random.nextDouble()*.3+.7));size=(1+random.nextFloat())*(outer?.5f:.25f);float spread=outer?.4f:.2f;xd=(random.nextFloat()-.5)*spread/lifetime;zd=(random.nextFloat()-.5)*spread/lifetime;yd=0;gravity=outer?-.025f:-.02f;setColor(outer?.7f+random.nextFloat()*.3f:.5f+random.nextFloat()*.3f,outer?.2f:.5f+random.nextFloat()*.3f,.2f);}
        }
        void timing(int life,float grav,Vec3 velocity){lifetime=life;gravity=grav;xd=velocity.x;yd=velocity.y;zd=velocity.z;}
        @Override public ParticleRenderType getRenderType(){return kind==1?ParticleRenderType.TERRAIN_SHEET:kind==0?TRAIL:GLOW;}
        @Override protected int getLightColor(float partial){return 0xf000f0;}
        @Override public float getQuadSize(float partial){return kind==8?.1f*size:kind==7?.1f*size*(lifetime-age+1)/(float)lifetime:(kind==4||kind==5)?.5f*size*Math.max(0,1-(age+partial)/lifetime):kind==2?.15f*size:kind==1?.1f*size:kind==0?.1f*size*(1+.25f*(float)Math.sin((age+partial-count)/5)):.08f*size;}
        @Override protected float getU0(){return kind==8?(112+Math.min(8,(int)(9*age/(float)lifetime)))%16/16f:kind==7?(age/6%4)/16f:kind==1?sprite.getU(jitterU):(kind==4||kind==5)?0:kind==2?rune/16f:kind==0?.5625f:(age%(kind==6?4:16))/16f;}
        @Override protected float getU1(){return kind==1?sprite.getU(jitterU+.25f):getU0()+((kind==4||kind==5)?.125f:kind==0?.0625f:.0624375f);}
        @Override protected float getV0(){return kind==8?(112+Math.min(8,(int)(9*age/(float)lifetime)))/16/16f:kind==1?sprite.getV(jitterV):(kind==4||kind==5)?.875f:kind==2?.375f:kind==0?.0625f:.25f;}
        @Override protected float getV1(){return kind==1?sprite.getV(jitterV+.25f):getV0()+((kind==4||kind==5)?.125f:.0624375f);}
        @Override public void tick(){
            xo=x;yo=y;zo=z;if(age++>=lifetime){remove();return;}
            if(kind==8){x+=xd;y+=yd;z+=zd;xd*=.98;yd*=.98;zd*=.98;return;}
            if(kind==7){yd-=.04*gravity;x+=xd;y+=yd;z+=zd;xd*=.908;yd*=.908;zd*=.908;return;}
            if(kind==4||kind==5){yd-=.04*gravity;x+=xd;y+=yd;z+=zd;xd*=.98;yd*=.98;zd*=.98;return;}
            if(kind==2){float threshold=lifetime/5f;alpha=(age<=threshold?age/threshold:(lifetime-age)/(float)lifetime)*.5f;yd-=.04*gravity;x+=xd;y+=yd;z+=zd;return;}
            x+=xd;y+=yd;z+=zd;if(kind==0){var position=new Vec3(x,y,z);double distance=position.distanceTo(target);if(distance<.12){remove();return;}if(distance<2)size*=.98f;var velocity=thaumcraft.effects.EssentiaFlow.nextVelocity(position,target,new Vec3(xd,yd,zd));xd=velocity.x;yd=velocity.y;zd=velocity.z;return;}xd*=.985;yd*=.985;zd*=.985;
            if(target!=null){var delta=target.subtract(x,y,z);double distance=delta.length();if(distance<.12){remove();return;}
                double acceleration=kind==0?.01/Math.min(1,distance):distance<4?.6:.3;
                if(distance<(kind==0?2:4))size*=kind==0?.98f:.9f;if(size<.02f){remove();return;}
                double limit=kind==0?.05:.35;xd=Math.clamp(xd+delta.x/distance*acceleration,-limit,limit);yd=Math.clamp(yd+delta.y/distance*acceleration+(kind==0?.002:0),-limit,limit);zd=Math.clamp(zd+delta.z/distance*acceleration,-limit,limit);
            }else if(kind==3){alpha=Math.max(0,1-age/(float)lifetime);size*=.98f;}
        }
        @Override public void render(VertexConsumer vertices,Camera camera,float partial){
            if(kind==8){if(age<delay)return;float old=alpha;if(age<=1||age>=lifetime-1)alpha=old/2;super.render(vertices,camera,partial);alpha=old;return;}
            if(kind!=2){super.render(vertices,camera,partial);return;}
            var centre=new Vector3f((float)(xo+(x-xo)*partial-camera.getPosition().x),(float)(yo+(y-yo)*partial-camera.getPosition().y),(float)(zo+(z-zo)*partial-camera.getPosition().z));
            var rotation=new Quaternionf();switch(face){case NORTH->rotation.rotateY((float)Math.PI);case EAST->rotation.rotateY((float)Math.PI/2);case WEST->rotation.rotateY(-(float)Math.PI/2);case UP->rotation.rotateX(-(float)Math.PI/2);case DOWN->rotation.rotateX((float)Math.PI/2);default->{}}
            float extent=getQuadSize(partial);float[][] corners={{1,-1},{1,1},{-1,1},{-1,-1}};for(int i=0;i<4;i++){var point=new Vector3f(corners[i][0]*extent,corners[i][1]*extent,0).rotate(rotation).add(centre);vertices.addVertex(point.x,point.y,point.z).setUv(i<2?getU1():getU0(),i==0||i==3?getV1():getV0()).setColor(rCol,gCol,bCol,alpha).setLight(0xf000f0);}
        }
    }
    private LegacyParticles(){}
}
