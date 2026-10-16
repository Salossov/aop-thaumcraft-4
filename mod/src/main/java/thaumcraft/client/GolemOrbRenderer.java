package thaumcraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import thaumcraft.entities.GolemOrb;

final class GolemOrbRenderer extends EntityRenderer<GolemOrb> {
    private static final ResourceLocation TEXTURE=ResourceLocation.parse("thaumcraft:textures/misc/particles.png");
    GolemOrbRenderer(EntityRendererProvider.Context context){super(context);}
    @Override public ResourceLocation getTextureLocation(GolemOrb orb){return TEXTURE;}
    @Override public void render(GolemOrb orb,float yaw,float partial,PoseStack pose,MultiBufferSource buffers,int light){draw(orb.tickCount,orb.red(),entityRenderDispatcher.camera.getYRot(),entityRenderDispatcher.camera.getXRot(),pose,buffers);super.render(orb,yaw,partial,pose,buffers,light);}
    private void draw(int ticks,boolean red,float viewYaw,float viewPitch,PoseStack pose,MultiBufferSource buffers){pose.pushPose();pose.mulPose(Axis.YP.rotationDegrees(180-viewYaw));pose.mulPose(Axis.XP.rotationDegrees(-viewPitch));float size=1+(net.minecraft.util.Mth.sin(ticks/5f)*.2f+.2f);pose.scale(size,size,size);float u=(1+ticks%6)/8f,v=red?.75f:.875f;var vertices=buffers.getBuffer(ThaumcraftRenderTypes.glow(TEXTURE));float[][] corners={{-.5f,-.5f,u,v+.125f},{.5f,-.5f,u+.125f,v+.125f},{.5f,.5f,u+.125f,v},{-.5f,.5f,u,v}};for(var c:corners)vertices.addVertex(pose.last(),c[0],c[1],0).setColor(255,255,255,204).setUv(c[2],c[3]).setOverlay(OverlayTexture.NO_OVERLAY).setLight(220).setNormal(pose.last(),0,1,0);pose.popPose();}
}
