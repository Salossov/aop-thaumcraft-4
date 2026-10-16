package thaumcraft.client;

import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import thaumcraft.entities.FocusProjectile;

public final class FocusProjectileRenderer extends EntityRenderer<FocusProjectile> {
    private static final int[] PRIMAL_COLOURS={0xffff7e,16727041,37119,40960,0xeeccff,0x555577};
    public FocusProjectileRenderer(EntityRendererProvider.Context context){super(context);}
    @Override public ResourceLocation getTextureLocation(FocusProjectile entity){return ResearchGraphics.texture(entity.kind()==FocusProjectile.Kind.FIREBALL?"misc/particles2":entity.kind()==FocusProjectile.Kind.FROST?"blocks/frostshard":"misc/particles");}
    @Override public void render(FocusProjectile entity,float yaw,float partial,PoseStack pose,MultiBufferSource buffers,int light){
        pose.pushPose();
        if(entity.kind()==FocusProjectile.Kind.FROST){
            pose.mulPose(Axis.YP.rotationDegrees(Mth.rotLerp(partial,entity.yRotO,entity.getYRot())));pose.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(partial,entity.xRotO,entity.getXRot())));
            var random=new java.util.Random(entity.getId());float scale=entity.damage()*.1f;pose.scale(scale+random.nextFloat()*.1f,scale+random.nextFloat()*.1f,scale+random.nextFloat()*.1f);
            OriginalObjMesh.get("orb").renderAll(pose,buffers,getTextureLocation(entity),light,OverlayTexture.NO_OVERLAY,0xffffffff,false);
        }else{
            if(entity.kind()==FocusProjectile.Kind.PRIMAL)primalRays(entity,pose,buffers);
            pose.mulPose(entityRenderDispatcher.cameraOrientation());
            float half,u,v,du,alpha=.8f;boolean additive=true;
            switch(entity.kind()){
                case FIREBALL->{half=.35f;u=entity.tickCount%4/16f;v=.8125f;du=.0625f;additive=false;}
                case SHOCK->{half=(1+Mth.sin(entity.tickCount/5f)*.2f+.2f)/2;u=(1+entity.tickCount%6)/8f;v=.875f;du=.125f;}
                case EMBER->{float age=entity.tickCount/(float)Math.max(1,entity.duration);half=(.25f+age)/2;u=(7+(int)(8*age))/16f;v=.5625f;du=.0625f;alpha=.9f;}
                default->{half=.25f;u=entity.tickCount%13/16f;v=.125f;du=.0624375f;}
            }
            var vertex=buffers.getBuffer(ThaumcraftRenderTypes.node(getTextureLocation(entity),additive,false));var transform=pose.last();float[][] corners={{-half,-half},{half,-half},{half,half},{-half,half}};
            for(int i=0;i<4;i++)vertex.addVertex(transform,corners[i][0],corners[i][1],0).setColor(1f,1f,1f,alpha).setUv(i==0||i==3?u:u+du,i<2?v+du:v).setLight(entity.kind()==FocusProjectile.Kind.PRIMAL?light:220).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(transform,0,1,0);
        }
        pose.popPose();
    }
    private static void primalRays(FocusProjectile entity,PoseStack pose,MultiBufferSource buffers){
        var random=new java.util.Random(entity.getId());var vertices=buffers.getBuffer(ThaumcraftRenderTypes.infusionHalo());pose.pushPose();
        // TC4 rotates cumulatively between rays, seeded only by the entity ID.
        for(int i=0;i<12;i++){
            pose.mulPose(Axis.XP.rotationDegrees(random.nextFloat()*360));pose.mulPose(Axis.YP.rotationDegrees(random.nextFloat()*360));pose.mulPose(Axis.ZP.rotationDegrees(random.nextFloat()*360));
            pose.mulPose(Axis.XP.rotationDegrees(random.nextFloat()*360));pose.mulPose(Axis.YP.rotationDegrees(random.nextFloat()*360));pose.mulPose(Axis.ZP.rotationDegrees(random.nextFloat()*360+entity.tickCount/80f*360));
            float factor=Math.min(entity.tickCount,10)/300f,height=(random.nextFloat()*20+5)*factor,width=(random.nextFloat()*2+1)*factor;
            int color=PRIMAL_COLOURS[i/2];float[][] corners={{-.866f*width,height,-.5f*width},{.866f*width,height,-.5f*width},{0,height,width}};
            for(int triangle=0;triangle<3;triangle++){
                vertices.addVertex(pose.last().pose(),0,0,0).setColor(0xffffffff);
                for(int c=0;c<2;c++){var point=corners[(triangle+c)%3];vertices.addVertex(pose.last().pose(),point[0],point[1],point[2]).setColor(color);}
            }
        }
        pose.popPose();
    }
}
