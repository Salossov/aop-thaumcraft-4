package thaumcraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import thaumcraft.client.model.*;
import thaumcraft.entities.EldritchWarden;

/** Original Warden body scale/summoning sink and a separately additive pulsing eye. */
final class EldritchWardenRenderer extends MobRenderer<EldritchWarden,GuardianModel<EldritchWarden>> {
    private static final ResourceLocation TEXTURE=ResourceLocation.parse("thaumcraft:textures/models/eldritch_warden.png");
    EldritchWardenRenderer(EntityRendererProvider.Context context){super(context,new GuardianModel<>(GuardianMesh.create().bakeRoot()),.5f);addLayer(new RenderLayer<>(this){@Override public void render(PoseStack pose,MultiBufferSource buffers,int light,EldritchWarden entity,float walk,float amount,float partial,float age,float yaw,float pitch){pose.pushPose();pose.scale(1.01f,1.01f,1.01f);int glow=(int)(195+Mth.sin(entity.tickCount/3f)*15+15);getParentModel().renderEye(pose,buffers.getBuffer(ThaumcraftRenderTypes.projectile(TEXTURE)),glow,OverlayTexture.NO_OVERLAY);pose.popPose();}});}
    @Override public ResourceLocation getTextureLocation(EldritchWarden entity){return TEXTURE;}
    @Override protected void scale(EldritchWarden entity,PoseStack pose,float partial){pose.scale(1.5f,1.5f,1.5f);}
    @Override public void render(EldritchWarden entity,float yaw,float partial,PoseStack pose,MultiBufferSource buffers,int light){pose.pushPose();pose.translate(0,-entity.getBbHeight()*entity.getSpawnTimer()/150f,0);super.render(entity,yaw,partial,pose,buffers,light);pose.popPose();}
}
