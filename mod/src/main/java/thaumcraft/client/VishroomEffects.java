package thaumcraft.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.phys.Vec3;

/** Original small shrinking purple FXWisp, including its atlas region and gentle gravity. */
public final class VishroomEffects {
    private static final ParticleRenderType WISP=(tessellator,textures)->{
        RenderSystem.depthMask(false);RenderSystem.enableBlend();RenderSystem.blendFunc(org.lwjgl.opengl.GL11.GL_SRC_ALPHA,org.lwjgl.opengl.GL11.GL_ONE);
        RenderSystem.setShader(GameRenderer::getParticleShader);RenderSystem.setShaderTexture(0,ResearchGraphics.texture("misc/particles"));
        return tessellator.begin(VertexFormat.Mode.QUADS,DefaultVertexFormat.PARTICLE);
    };
    public static void install(){
        thaumcraft.blocks.VishroomBlock.clientWisp=p->Minecraft.getInstance().particleEngine.add(new Wisp(p,.1f,.5f,.3f,.8f,true,.015f));
        thaumcraft.blocks.MagicalFlowerBlock.clientWisp=e->Minecraft.getInstance().particleEngine.add(new Wisp(e.position(),.2f,e.red(),e.green(),e.blue(),false,0));
    }
    static void nitor(Vec3 from,Vec3 toward,boolean outer){
        var mc=Minecraft.getInstance();if(mc.level==null)return;var random=mc.level.random;
        float red=outer?.7f+random.nextFloat()*.3f:.5f+random.nextFloat()*.3f,green=outer?.2f:.5f+random.nextFloat()*.3f;
        var wisp=new Wisp(from,outer?.5f:.25f,red,green,.2f,true,outer?-.025f:-.02f);
        wisp.aim(from,toward);
        mc.particleEngine.add(wisp);
    }
    static void typed(Vec3 from,float scale,int type,boolean shrink,boolean clip,float gravity){
        var wisp=typedWisp(from,scale,type,shrink,gravity);wisp.clip(clip);Minecraft.getInstance().particleEngine.add(wisp);
    }
    static void travel(Vec3 from,Vec3 toward,float scale,int type,boolean shrink,float gravity){
        var wisp=typedWisp(from,scale,type,shrink,gravity);wisp.aim(from,toward);Minecraft.getInstance().particleEngine.add(wisp);
    }
    static void targeted(Vec3 from,net.minecraft.world.entity.Entity target,int type){
        var wisp=typedWisp(from,.4f,type,true,0);wisp.target=target;Minecraft.getInstance().particleEngine.add(wisp);
    }
    private static Wisp typedWisp(Vec3 from,float scale,int type,boolean shrink,float gravity){
        var wisp=new Wisp(from,scale,0,0,0,shrink,gravity);var r=Minecraft.getInstance().level.random;
        switch(type){
            case 0->wisp.setColor(.75f+r.nextFloat()*.25f,.25f+r.nextFloat()*.25f,.75f+r.nextFloat()*.25f);
            case 1->wisp.setColor(.5f+r.nextFloat()*.3f,.5f+r.nextFloat()*.3f,.2f);
            case 2->wisp.setColor(.2f,.2f,.7f+r.nextFloat()*.3f);
            case 3->wisp.setColor(.2f,.7f+r.nextFloat()*.3f,.2f);
            case 4->wisp.setColor(.7f+r.nextFloat()*.3f,.2f,.2f);
            case 5->{wisp.normalBlend=true;wisp.setColor(r.nextFloat()*.1f,r.nextFloat()*.1f,r.nextFloat()*.1f);}
            case 6->wisp.setColor(.8f+r.nextFloat()*.2f,.8f+r.nextFloat()*.2f,.8f+r.nextFloat()*.2f);
            case 7->wisp.setColor(.7f+r.nextFloat()*.3f,.5f+r.nextFloat()*.2f,.3f+r.nextFloat()*.1f);
        }
        return wisp;
    }
    private static final class Wisp extends TextureSheetParticle {
        private final boolean shrink;
        private boolean normalBlend;
        private net.minecraft.world.entity.Entity target;
        Wisp(Vec3 p,float scale,float red,float green,float blue,boolean shrink,float gravity){
            super(Minecraft.getInstance().level,p.x,p.y,p.z);setSize(.1f,.1f);hasPhysics=true;xd=yd=zd=0;
            this.shrink=shrink;this.gravity=gravity;setColor(red==0?1:red,green,blue);alpha=.5f;quadSize=(random.nextFloat()*.5f+.5f)*scale;lifetime=(int)(36/(Math.random()*.3+.7));
            var view=Minecraft.getInstance().getCameraEntity();if(view!=null&&view.distanceToSqr(p)>(Minecraft.useFancyGraphics()?2500:625))lifetime=0;
        }
        void aim(Vec3 from,Vec3 toward){if(lifetime>0){xd=(toward.x-from.x)/lifetime;yd=(toward.y-from.y)/lifetime;zd=(toward.z-from.z)/lifetime;}}
        void clip(boolean clip){hasPhysics=!clip;}
        @Override public ParticleRenderType getRenderType(){return normalBlend?LegacyParticles.TRAIL:WISP;}
        @Override public int getLightColor(float partial){return 240;}
        @Override public float getQuadSize(float partial){if(lifetime<=0)return 0;float fraction=shrink?(lifetime-age)/(float)lifetime:age/(float)(lifetime/2);if(!shrink&&fraction>1)fraction=2-fraction;return quadSize*fraction;}
        @Override protected float getU0(){return 0;}@Override protected float getU1(){return .125f;}
        @Override protected float getV0(){return .875f;}@Override protected float getV1(){return 1;}
        @Override public void tick(){
            xo=x;yo=y;zo=z;if(age++>=lifetime)remove();yd-=.04*(double)gravity;
            if(hasPhysics){var motion=LegacyWispPhysics.pushOut(level,random,new Vec3(x,y,z),getBoundingBox(),new Vec3(xd,yd,zd));xd=motion.x;yd=motion.y;zd=motion.z;}
            move(xd,yd,zd);
            if(target!=null){
                xd*=.985;yd*=.985;zd*=.985;var delta=target.position().add(0,target.getBbHeight()/2f,0).subtract(x,y,z);double length=(float)Math.sqrt(delta.lengthSqr());
                if(length>0){xd+=delta.x/length*.2;yd+=delta.y/length*.2;zd+=delta.z/length*.2;}
                xd=net.minecraft.util.Mth.clamp((float)xd,-.2f,.2f);yd=net.minecraft.util.Mth.clamp((float)yd,-.2f,.2f);zd=net.minecraft.util.Mth.clamp((float)zd,-.2f,.2f);
            }else{xd*=(double).98f;yd*=(double).98f;zd*=(double).98f;if(onGround){xd*=(double).7f;zd*=(double).7f;}}
        }
    }
    private VishroomEffects(){}
}
