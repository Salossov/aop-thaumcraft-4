package thaumcraft.client;

import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import thaumcraft.entities.Dart;

/** RenderDart's shorter, grey-blue arrow geometry and owned1.7.10 arrow texture. */
final class DartRenderer extends EntityRenderer<Dart> {
    private static final ResourceLocation TEXTURE=ResearchGraphics.texture("misc/minecraft_arrow_1_7_10");
    DartRenderer(EntityRendererProvider.Context context){super(context);}
    @Override public ResourceLocation getTextureLocation(Dart dart){return TEXTURE;}
    @Override public void render(Dart dart,float yaw,float partial,PoseStack pose,MultiBufferSource buffers,int light){
        pose.pushPose();pose.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partial,dart.yRotO,dart.getYRot())-90));pose.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(partial,dart.xRotO,dart.getXRot())));
        float shake=dart.shakeTime-partial;if(shake>0)pose.mulPose(Axis.ZP.rotationDegrees(-Mth.sin(shake*3)*shake));
        pose.mulPose(Axis.XP.rotationDegrees(45));pose.scale(.025625f*.75f,.025625f,.025625f);pose.translate(-4,0,0);
        var vertices=buffers.getBuffer(RenderType.entityCutout(TEXTURE));var p=pose.last();
        vertex(vertices,p,-7,-2,-2,0,.15625f,1,0,0,light);vertex(vertices,p,-7,-2,2,.15625f,.15625f,1,0,0,light);
        vertex(vertices,p,-7,2,2,.15625f,.3125f,1,0,0,light);vertex(vertices,p,-7,2,-2,0,.3125f,1,0,0,light);
        vertex(vertices,p,-7,2,-2,0,.15625f,-1,0,0,light);vertex(vertices,p,-7,2,2,.15625f,.15625f,-1,0,0,light);
        vertex(vertices,p,-7,-2,2,.15625f,.3125f,-1,0,0,light);vertex(vertices,p,-7,-2,-2,0,.3125f,-1,0,0,light);
        for(int face=0;face<4;face++){
            pose.mulPose(Axis.XP.rotationDegrees(90));p=pose.last();
            vertex(vertices,p,-8,-2,0,0,0,0,0,1,light);vertex(vertices,p,8,-2,0,.5f,0,0,0,1,light);
            vertex(vertices,p,8,2,0,.5f,.15625f,0,0,1,light);vertex(vertices,p,-8,2,0,0,.15625f,0,0,1,light);
        }
        pose.popPose();super.render(dart,yaw,partial,pose,buffers,light);
    }
    private static void vertex(VertexConsumer vertices,PoseStack.Pose pose,float x,float y,float z,float u,float v,float nx,float ny,float nz,int light){
        vertices.addVertex(pose.pose(),x,y,z).setColor(.5f,.5f,.6f,1).setUv(u,v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose,nx,ny,nz);
    }
}
