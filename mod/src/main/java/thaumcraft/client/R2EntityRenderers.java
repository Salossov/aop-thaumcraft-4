package thaumcraft.client;

import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import java.util.Random;
import net.minecraft.client.model.*;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.layers.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.*;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import thaumcraft.client.model.*;
import thaumcraft.entities.*;

public final class R2EntityRenderers {
    private static ResourceLocation texture(String name){return ResourceLocation.fromNamespaceAndPath("thaumcraft","textures/"+name+".png");}
    private static ModelLayerLocation layer(String name){return new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("thaumcraft",name),"main");}
    private static final ModelLayerLocation GUARDIAN=layer("guardian"),TENTACLE=layer("tentacle"),TINY=layer("tiny_tentacle"),SPORE=layer("spore"),SWARMER=layer("swarmer"),SHEEP=layer("taint_sheep"),WOOL=layer("taint_sheep_wool");
    public static void layers(EntityRenderersEvent.RegisterLayerDefinitions event){event.registerLayerDefinition(GUARDIAN,GuardianMesh::create);event.registerLayerDefinition(TENTACLE,()->TaintacleModel.create(10));event.registerLayerDefinition(TINY,()->TaintacleModel.create(6));event.registerLayerDefinition(SPORE,()->SporeModel.create(false));event.registerLayerDefinition(SWARMER,()->SporeModel.create(true));event.registerLayerDefinition(SHEEP,()->TaintedSheepModel.create(false));event.registerLayerDefinition(WOOL,()->TaintedSheepModel.create(true));}
    public static void register(EntityRenderersEvent.RegisterRenderers event){
        event.registerEntityRenderer(R2Entities.FALLING_TAINT.get(),FallingTaintRenderer::new);
        for(var type:java.util.List.of(R2Entities.BRAINY,R2Entities.GIANT_BRAINY))event.registerEntityRenderer(type.get(),context->new ZombieRenderer(context){@Override public ResourceLocation getTextureLocation(Zombie entity){return texture("models/bzombie");}@Override protected void scale(Zombie entity,PoseStack pose,float partial){float size=entity.getType()==R2Entities.GIANT_BRAINY.get()?((BrainyZombie)entity).anger():1;pose.scale(size,size,size);}});
        event.registerEntityRenderer(R2Entities.TAINT_PIG.get(),context->new Skin<>(context,new PigModel<>(context.bakeLayer(ModelLayers.PIG)),"pig",0.7f));
        event.registerEntityRenderer(R2Entities.TAINT_COW.get(),context->new Skin<>(context,new CowModel<>(context.bakeLayer(ModelLayers.COW)),"cow",0.7f));
        event.registerEntityRenderer(R2Entities.TAINT_CHICKEN.get(),context->new Skin<>(context,new ChickenModel<>(context.bakeLayer(ModelLayers.CHICKEN)),"chicken",0.3f));
        event.registerEntityRenderer(R2Entities.TAINT_VILLAGER.get(),context->new Skin<>(context,new VillagerModel<>(context.bakeLayer(ModelLayers.VILLAGER)),"villager",0.5f){@Override protected void scale(TaintedCreature entity,PoseStack pose,float partial){pose.scale(0.9375f,0.9375f,0.9375f);}});
        event.registerEntityRenderer(R2Entities.TAINT_SHEEP.get(),SheepSkin::new);
        event.registerEntityRenderer(R2Entities.TAINT_CREEPER.get(),context->new Skin<TaintCreeper>(context,new CreeperModel<>(context.bakeLayer(ModelLayers.CREEPER)),"creeper",0.5f){
            @Override protected void scale(TaintCreeper entity,PoseStack pose,float partial){float flash=entity.flash(partial),pulse=1+Mth.sin(flash*100)*flash*0.01f;flash=Mth.clamp(flash,0,1);flash*=flash;flash*=flash;float x=(1+flash*0.4f)*pulse,y=(1+flash*0.1f)/pulse;pose.scale(x,y,x);}
            @Override protected float getWhiteOverlayProgress(TaintCreeper entity,float partial){float flash=entity.flash(partial);return (int)(flash*10)%2==0?0:Mth.clamp(flash*0.2f,0,1);}
        });
        event.registerEntityRenderer(R2Entities.TAINT_SPIDER.get(),SpiderSkin::new);
        event.registerEntityRenderer(R2Entities.THAUMIC_SLIME.get(),SlimeSkin::new);
        event.registerEntityRenderer(R2Entities.TAINT_SPORE.get(),context->new SporeSkin(context,false));event.registerEntityRenderer(R2Entities.TAINT_SWARMER.get(),context->new SporeSkin(context,true));
        event.registerEntityRenderer(R2Entities.TAINTACLE.get(),context->new TentacleSkin(context,false));event.registerEntityRenderer(R2Entities.TAINTACLE_SMALL.get(),context->new TentacleSkin(context,true));
        event.registerEntityRenderer(R2Entities.ELDRITCH_GUARDIAN.get(),context->new Skin<>(context,new thaumcraft.client.model.GuardianModel(context.bakeLayer(GUARDIAN)),"eldritch_guardian",0.5f));
        event.registerEntityRenderer(R2Entities.WISP.get(),context->new Sprite<>(context));event.registerEntityRenderer(R2Entities.ASPECT_ORB.get(),context->new Sprite<>(context));event.registerEntityRenderer(R2Entities.ELDRITCH_ORB.get(),context->new Sprite<>(context));event.registerEntityRenderer(R2Entities.TAINT_SWARM.get(),context->new Sprite<>(context));
    }
    private static class Skin<T extends Mob> extends MobRenderer<T,EntityModel<T>>{
        private final ResourceLocation skin;
        Skin(EntityRendererProvider.Context context,EntityModel<T> model,String skin,float shadow){super(context,model,shadow);this.skin=texture("models/"+skin);}
        @Override public ResourceLocation getTextureLocation(T entity){return skin;}
    }
    private static final class SheepSkin extends Skin<TaintedCreature>{
        SheepSkin(EntityRendererProvider.Context context){super(context,new TaintedSheepModel(context.bakeLayer(SHEEP)),"sheep",0.7f);var wool=new TaintedSheepModel(context.bakeLayer(WOOL));addLayer(new RenderLayer<>(this){@Override public void render(PoseStack pose,MultiBufferSource buffers,int light,TaintedCreature entity,float walk,float amount,float partial,float age,float yaw,float pitch){if(entity.isInvisible())return;getParentModel().copyPropertiesTo(wool);wool.prepareMobModel(entity,walk,amount,partial);wool.setupAnim(entity,walk,amount,age,yaw,pitch);wool.renderToBuffer(pose,buffers.getBuffer(RenderType.entityCutoutNoCull(texture("models/sheep_fur"))),light,LivingEntityRenderer.getOverlayCoords(entity,0));}});}
    }
    private static final class SpiderSkin extends Skin<TaintSpider>{
        SpiderSkin(EntityRendererProvider.Context context){super(context,new SpiderModel<>(context.bakeLayer(ModelLayers.SPIDER)),"taint_spider",0.5f);addLayer(new EyesLayer<>(this){@Override public RenderType renderType(){return RenderType.eyes(texture("models/taint_spider_eyes"));}});}
        @Override protected void scale(TaintSpider entity,PoseStack pose,float partial){pose.scale(0.4f,0.5f,0.4f);}
        @Override protected float getFlipDegrees(TaintSpider entity){return 180;}
    }
    private static final class SlimeSkin extends Skin<ThaumicSlime>{
        SlimeSkin(EntityRendererProvider.Context context){super(context,new SlimeModel<>(context.bakeLayer(ModelLayers.SLIME)),"tslime",0.25f);var outer=new SlimeModel<ThaumicSlime>(context.bakeLayer(ModelLayers.SLIME_OUTER));addLayer(new RenderLayer<>(this){@Override public void render(PoseStack pose,MultiBufferSource buffers,int light,ThaumicSlime entity,float walk,float amount,float partial,float age,float yaw,float pitch){if(entity.isInvisible())return;getParentModel().copyPropertiesTo(outer);outer.setupAnim(entity,walk,amount,age,yaw,pitch);outer.renderToBuffer(pose,buffers.getBuffer(RenderType.entityTranslucent(getTextureLocation(entity))),light,LivingEntityRenderer.getOverlayCoords(entity,0));}});}
        @Override protected void scale(ThaumicSlime entity,PoseStack pose,float partial){float size=(float)Math.sqrt(entity.size());float squish=Mth.lerp(partial,entity.oldSquish,entity.squish)/(size*0.25f+1),factor=1/(squish+1);pose.scale(factor*size+0.1f,size/factor+0.1f,factor*size+0.1f);}
    }
    private static final class SporeSkin extends Skin<TaintSpore>{
        private final boolean swarmer;
        SporeSkin(EntityRendererProvider.Context context,boolean swarmer){super(context,new SporeModel(context.bakeLayer(swarmer?SWARMER:SPORE),swarmer),"taint_spore",0.25f);this.swarmer=swarmer;}
        @Override protected void scale(TaintSpore entity,PoseStack pose,float partial){if(!swarmer){float size=entity.displaySize+(entity.displaySize<entity.size()?0.02f*partial:0),pulse=0.025f*Mth.sin(entity.tickCount*0.075f);pose.scale(-0.12f*size-pulse,-0.12f*size+pulse,-0.12f*size-pulse);}}
        @Override protected float getFlipDegrees(TaintSpore entity){return 0;}
    }
    private static final class TentacleSkin extends Skin<Taintacle>{
        TentacleSkin(EntityRendererProvider.Context context,boolean small){super(context,new TaintacleModel(context.bakeLayer(small?TINY:TENTACLE),small?6:10),"taintacle",small?0.2f:0.6f);}
        @Override protected void scale(Taintacle entity,PoseStack pose,float partial){float age=entity.tickCount+partial,h=entity.getBbHeight(),emergence=Math.max(0,(h*10-age)/(h*10))*h;pose.translate(0,(entity.small()?1.2:0.6)+emergence,0);pose.scale(h/3,h/3,h/3);}
    }
    private static final class Sprite<T extends Entity> extends EntityRenderer<T>{
        Sprite(EntityRendererProvider.Context context){super(context);}
        @Override public ResourceLocation getTextureLocation(T entity){return texture("misc/particles");}
        @Override public void render(T entity,float yaw,float partial,PoseStack pose,MultiBufferSource buffers,int light){
            if(entity instanceof Wisp wisp){if(!wisp.isAlive())return;int frame=entity.tickCount%16,color=wisp.aspect()==null?0:wisp.aspect().getColor();if(wisp.hurtTime>0)color=0xff0000|((color>>8&255)*255/300<<8)|(color&255)*255/300;quad(pose,buffers,texture("misc/wisp"),frame%4/4f,frame/4/4f,0.24996f,0.24996f,1,0.45f,color,255,true);quad(pose,buffers,getTextureLocation(entity),frame/16f,5/16f,1/16f,1/16f,0.4f+Mth.sin((entity.tickCount+partial)/10)*0.1f,0.45f,0xffffff,255,true);}
            else if(entity instanceof AspectOrb orb){float size=0.1f+0.3f*(150-orb.age())/150f;quad(pose,buffers,getTextureLocation(entity),(entity.tickCount*2%16)/16f,8/16f,1/16f,1/16f,size/2,size/4,orb.aspect()==null?0xffffff:orb.aspect().getColor(),128,orb.aspect()==null||orb.aspect().getBlend()==1);}
            else if(entity instanceof EldritchOrb){rays(entity,partial,pose,buffers);quad(pose,buffers,getTextureLocation(entity),(entity.tickCount%13)/16f,3/16f,0.0624375f,0.0624375f,0.5f,0,0xffffff,255,false);}
            // The tainted swarm's body is invisible in TC4; SwarmParticles owns its motes.
            super.render(entity,yaw,partial,pose,buffers,light);
        }
        private void quad(PoseStack pose,MultiBufferSource buffers,ResourceLocation texture,float u,float v,float du,float dv,float size,float height,int color,int alpha,boolean glow){pose.pushPose();pose.translate(0,height,0);pose.mulPose(entityRenderDispatcher.cameraOrientation());var vertices=buffers.getBuffer(glow?ThaumcraftRenderTypes.glow(texture):RenderType.entityTranslucentEmissive(texture));var p=pose.last();vertex(vertices,p,-size,-size,u+du,v+dv,color,alpha);vertex(vertices,p,size,-size,u,v+dv,color,alpha);vertex(vertices,p,size,size,u,v,color,alpha);vertex(vertices,p,-size,size,u+du,v,color,alpha);pose.popPose();}
        private static void vertex(VertexConsumer vertices,PoseStack.Pose pose,float x,float y,float u,float v,int color,int alpha){vertices.addVertex(pose.pose(),x,y,0).setColor(color>>16&255,color>>8&255,color&255,alpha).setUv(u,v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(pose,0,1,0);}
        private void rays(Entity entity,float partial,PoseStack pose,MultiBufferSource buffers){
            pose.pushPose();var random=new Random(entity.getId());var vertices=buffers.getBuffer(ThaumcraftRenderTypes.infusionHalo());float growth=Math.min(entity.tickCount,10)/300f;
            for(int i=0;i<12;i++){
                for(int repeat=0;repeat<2;repeat++){
                    pose.mulPose(Axis.XP.rotationDegrees(random.nextFloat()*360));pose.mulPose(Axis.YP.rotationDegrees(random.nextFloat()*360));
                    pose.mulPose(Axis.ZP.rotationDegrees(random.nextFloat()*360+(repeat==1?entity.tickCount/80f*360:0)));
                }
                float length=(random.nextFloat()*20+5)*growth,width=(random.nextFloat()*2+1)*growth;var matrix=pose.last().pose();
                float[][] ring={{-.866f*width,-.5f*width},{.866f*width,-.5f*width},{0,width}};
                for(int side=0;side<3;side++){
                    float[] a=ring[side],b=ring[(side+1)%3];vertices.addVertex(matrix,0,0,0).setColor(255,255,255,255);
                    vertices.addVertex(matrix,a[0],length,a[1]).setColor(238,204,255,0);vertices.addVertex(matrix,b[0],length,b[1]).setColor(238,204,255,0);
                }
            }
            pose.popPose();
        }
    }
    private R2EntityRenderers(){}
}
