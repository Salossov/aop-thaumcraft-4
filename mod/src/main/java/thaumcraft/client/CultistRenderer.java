package thaumcraft.client;

import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import thaumcraft.entities.*;

/** Original 64x32 biped and the ritualist's two crossed, flowing ribbons. */
public final class CultistRenderer<T extends net.minecraft.world.entity.Mob> extends HumanoidMobRenderer<T,HumanoidModel<T>> {
    public CultistRenderer(EntityRendererProvider.Context context){super(context,new HumanoidModel<>(legacyBody().bakeRoot()),.5f);addLayer(new HumanoidArmorLayer<>(this,new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)),new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)),context.getModelManager()));}
    public static LayerDefinition legacyBody(){var mesh=HumanoidModel.createMesh(CubeDeformation.NONE,0);var root=mesh.getRoot();root.addOrReplaceChild("left_arm",CubeListBuilder.create().texOffs(40,16).mirror().addBox(-1,-2,-2,4,12,4),PartPose.offset(5,2,0));root.addOrReplaceChild("left_leg",CubeListBuilder.create().texOffs(0,16).mirror().addBox(-2,0,-2,4,12,4),PartPose.offset(1.9f,12,0));return LayerDefinition.create(mesh,64,32);}
    @Override public ResourceLocation getTextureLocation(T entity){return ResearchGraphics.texture("models/cultist");}
    @Override protected void scale(T entity,PoseStack pose,float partial){if(entity.getType()==R5Entities.CULTIST_LEADER.get())pose.scale(1.25f,1.25f,1.25f);}
    public static float ritualBob(int id,int ticks,float partial){int phase=new java.util.Random(id).nextInt(1000);return Mth.sin((ticks+partial+phase)/9)*.1f+.21f;}
    @Override public void render(T entity,float yaw,float partial,PoseStack pose,MultiBufferSource buffers,int light){pose.pushPose();boolean ritual=entity instanceof CultistCleric cleric&&cleric.isRitualist();float bob=ritual?ritualBob(entity.getId(),entity.tickCount,partial):0;if(ritual)pose.translate(0,bob,0);super.render(entity,yaw,partial,pose,buffers,light);if(ritual){var cleric=(CultistCleric)entity;var home=cleric.ritualHome();pose.pushPose();pose.translate(home.getX()+.5-entity.getX(),home.getY()+1.5-bob-entity.getY(),home.getZ()+.5-entity.getZ());ribbon(pose,buffers,entity.getX(),entity.getY()+entity.getEyeHeight()*1.2f,entity.getZ(),home.getX()+.5,home.getY()+1.5-bob,home.getZ()+.5,(float)(System.nanoTime()/30000000L),Math.min(entity.tickCount,10)/10f);pose.popPose();}pose.popPose();}
    public static void ribbon(PoseStack pose,MultiBufferSource buffers,double x,double y,double z,double tx,double ty,double tz,float time,float growth){var vertex=buffers.getBuffer(RenderType.entityTranslucent(ResearchGraphics.texture("misc/wispy")));double cx=(float)(x-tx),cy=(float)(y-ty),cz=(float)(z-tz);float distance=Mth.sqrt((float)((x-tx)*(x-tx)+(y-ty)*(y-ty)+(z-tz)*(z-tz))),length=Math.round(distance)*4f;if(length<=0)return;for(int strip=0;strip<2;strip++){float[] previous=null;for(int i=0;i<=length*growth;i++){float phase=i/length,pulse=1-Math.abs(i-length/2)/(length/2);double dx=cx+Mth.sin((float)((z%16+distance*(1-phase)*4-time%32767/5)/4))*.5f*pulse,dy=cy+Mth.sin((float)((x%16+distance*(1-phase)*4-time%32767/5)/3))*.5f*pulse,dz=cz+Mth.sin((float)((y%16+distance*(1-phase)*4-time%32767/5)/2))*.5f*pulse;float px=(float)(dx*phase),py=(float)(dy*phase),pz=(float)(dz*phase),u=(1-phase)*distance-time*-.03f;float[] current={px-(strip==1?.25f:0),py-(strip==0?.25f:0),pz,u,px+(strip==1?.25f:0),py+(strip==0?.25f:0),pz};if(previous!=null){point(vertex,pose,previous[0],previous[1],previous[2],previous[3],1);point(vertex,pose,current[0],current[1],current[2],current[3],1);point(vertex,pose,current[4],current[5],current[6],current[3],0);point(vertex,pose,previous[4],previous[5],previous[6],previous[3],0);}previous=current;}}}
    private static void point(VertexConsumer vertices,PoseStack pose,float x,float y,float z,float u,float v){vertices.addVertex(pose.last().pose(),x,y,z).setColor(17,0,17,204).setUv(u,v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xf000f0).setNormal(pose.last(),0,1,0);}
}
