package thaumcraft.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Camera;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.phys.Vec3;
import thaumcraft.entities.FocusProjectile;

/** Source focus effects emitted by the native entity tick, independent of render frequency. */
final class FocusProjectileEffects {
    private static ParticleRenderType sheet(String texture){return (tessellator,textures)->{
        RenderSystem.depthMask(false);RenderSystem.enableBlend();RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getParticleShader);RenderSystem.setShaderTexture(0,ResearchGraphics.texture(texture));
        return tessellator.begin(VertexFormat.Mode.QUADS,DefaultVertexFormat.PARTICLE);
    };}
    private static final ParticleRenderType FROST=sheet("blocks/frostshard");

    static void install(){
        FocusProjectile.clientTick=FocusProjectileEffects::tick;
        FocusProjectile.clientImpact=FocusProjectileEffects::impact;
        FocusProjectile.clientCracks=effect->{
            var mc=Minecraft.getInstance();if(mc.level==null)return;var p=effect.position();
            for(var velocity:effect.velocities()){
                mc.particleEngine.add(effect.frost()?new FrostCrack(p,velocity.x,velocity.y,velocity.z):new TerrainParticle(mc.level,p.x,p.y,p.z,velocity.x,velocity.y,velocity.z,effect.state()));
            }
        };
        thaumcraft.blocks.ShockFieldBlock.clientSpark=e->GolemClientEffects.spark(e.position(),e.scale(),e.red(),e.green(),e.blue(),e.alpha());
        thaumcraft.entities.AlumentumProjectile.clientTrail=entity->{
            var level=Minecraft.getInstance().level;if(level==null)return;var r=level.random;
            for(int i=0;i<3;i++){
                VishroomEffects.typed(entity.position().add(offset(r,.3f)),.3f,5,true,true,.02f);
                VishroomEffects.typed(new Vec3((entity.getX()+entity.xo)/2,(entity.getY()+entity.yo)/2,(entity.getZ()+entity.zo)/2).add(offset(r,.3f)),.3f,5,true,true,.02f);
                var delta=offset(r,.1f);FollowingItemEffects.sparkle(new Vec3((float)((float)entity.getX()+(float)delta.x),(float)((float)entity.getY()+(float)delta.y),(float)((float)entity.getZ()+(float)delta.z)),1.5f,6,0);
            }
        };
        thaumcraft.entities.EldritchOrb.clientImpact=orb->{
            var level=Minecraft.getInstance().level;if(level==null)return;
            for(int i=0;i<30;i++){var delta=offset(level.random,.3f);VishroomEffects.travel(orb.position().add(delta),orb.position().add(delta.scale(8)),.3f,5,true,.02f);}
        };
    }
    private static void tick(FocusProjectile entity){
        var mc=Minecraft.getInstance();if(mc.level==null)return;var r=entity.effectRandom();
        switch(entity.kind()){
            case FROST->{
                for(int i=0;i<Math.max(1,entity.upgrade);i++){
                    float spread=entity.damage()/10f;
                    var p=new Vec3((float)entity.getX()-spread+r.nextFloat()*(spread*2),(float)entity.getY()-spread+r.nextFloat()*(spread*2),(float)entity.getZ()-spread+r.nextFloat()*(spread*2));
                    FollowingItemEffects.sparkle(p,.4f,6,.005f);
                }
            }
            case FIREBALL->{
                var p=new Vec3(entity.xo+(r.nextFloat()-r.nextFloat())*.3f,entity.yo+(r.nextFloat()-r.nextFloat())*.3f,entity.zo+(r.nextFloat()-r.nextFloat())*.3f);
                mc.particleEngine.add(new FireTrail(p,7+r.nextInt(5),2+r.nextFloat()));
            }
            case PRIMAL->{
                var worldRandom=mc.level.random;
                // TC4 passes these offsets directly to FXWisp4; it does not add the orb coordinates.
                for(int type=0;type<6;type++)VishroomEffects.targeted(offset(worldRandom,.2f),entity,type);
                VishroomEffects.typed(entity.position().add(offset(worldRandom,.2f)),.1f,r.nextInt(6),true,true,0);
            }
            default->{}
        }
    }
    private static Vec3 offset(net.minecraft.util.RandomSource r,float scale){return new Vec3((r.nextFloat()-r.nextFloat())*scale,(r.nextFloat()-r.nextFloat())*scale,(r.nextFloat()-r.nextFloat())*scale);}
    private static void impact(FocusProjectile entity){
        var level=Minecraft.getInstance().level;if(level==null)return;
        if(entity.kind()==FocusProjectile.Kind.SHOCK){GolemClientEffects.burst(entity.position(),3);return;}
        if(entity.kind()==FocusProjectile.Kind.PRIMAL)for(int a=0;a<6;a++)for(int type=0;type<6;type++){
            var delta=offset(level.random,.5f);VishroomEffects.travel(entity.position().add(delta),entity.position().add(delta.scale(10)),.4f,type,true,.05f);
        }
    }
    private static final class FireTrail extends TextureSheetParticle {
        FireTrail(Vec3 p,int life,float scale){super(Minecraft.getInstance().level,p.x,p.y,p.z);setSize(.1f,.1f);hasPhysics=false;xd=yd=zd=0;lifetime=life;quadSize=.1f*scale;alpha=.8f;}
        @Override public ParticleRenderType getRenderType(){return LegacyParticles.GLOW;}
        private int frame(){return 151+Math.min(8,(int)(9*age/(float)lifetime));}
        @Override protected float getU0(){return frame()%16/16f;}
        @Override protected float getU1(){return getU0()+.0624375f;}
        @Override protected float getV0(){return frame()/16/16f;}
        @Override protected float getV1(){return getV0()+.0624375f;}
        @Override public void tick(){xo=x;yo=y;zo=z;if(age++>=lifetime)remove();}
        @Override public void render(VertexConsumer vertices,Camera camera,float partial){float saved=alpha;if(age<=1||age>=lifetime-1)alpha=saved/2;super.render(vertices,camera,partial);alpha=saved;}
    }
    /** Native debris physics, with the original metadata15 custom-ore sprite. */
    private static final class FrostCrack extends TerrainParticle {
        private final float u,v;
        FrostCrack(Vec3 p,double x,double y,double z){super(Minecraft.getInstance().level,p.x,p.y,p.z,x,y,z,net.minecraft.world.level.block.Blocks.ICE.defaultBlockState());u=random.nextFloat()*.75f;v=random.nextFloat()*.75f;setColor(.6f,.6f,.6f);}
        @Override public ParticleRenderType getRenderType(){return FROST;}
        @Override protected float getU0(){return u+.25f;}
        @Override protected float getU1(){return u;}
        @Override protected float getV0(){return v;}
        @Override protected float getV1(){return v+.25f;}
    }
    private FocusProjectileEffects(){}
}
