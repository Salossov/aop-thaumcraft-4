package thaumcraft.client;

import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import thaumcraft.entities.GolemBobber;

final class GolemBobberRenderer extends EntityRenderer<GolemBobber> {
    private static final ResourceLocation TEXTURE=ResearchGraphics.texture("misc/minecraft_particles_1_7_10");
    GolemBobberRenderer(EntityRendererProvider.Context context){super(context);}
    @Override public ResourceLocation getTextureLocation(GolemBobber entity){return TEXTURE;}
    @Override public void render(GolemBobber bobber,float yaw,float partial,PoseStack pose,MultiBufferSource buffers,int light){
        pose.pushPose();pose.mulPose(entityRenderDispatcher.cameraOrientation());var p=pose.last();var vertices=buffers.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
        vertex(vertices,p,-.25f,-.25f,.0625f,.1875f,light);vertex(vertices,p,.25f,-.25f,.125f,.1875f,light);
        vertex(vertices,p,.25f,.25f,.125f,.125f,light);vertex(vertices,p,-.25f,.25f,.0625f,.125f,light);pose.popPose();
        var fisher=bobber.fisher();
        if(fisher!=null){
            var origin=bobber.getPosition(partial);
            var hand=GolemFishingPose.tip(fisher,partial);
            var difference=hand.subtract(origin.add(0,.25,0));
            // The source casts the three deltas to float before building its 16-segment parabola.
            double x=(float)difference.x,y=(float)difference.y,z=(float)difference.z;
            var lines=buffers.getBuffer(RenderType.lines());
            for(int segment=0;segment<16;segment++){
                Vec3 a=point(x,y,z,segment/16f),b=point(x,y,z,(segment+1)/16f),normal=b.subtract(a).normalize();
                line(lines,pose.last(),a,normal);line(lines,pose.last(),b,normal);
            }
        }
        super.render(bobber,yaw,partial,pose,buffers,light);
    }
    private static Vec3 point(double x,double y,double z,float fraction){return new Vec3(x*fraction,y*(fraction*fraction+fraction)*.5+.25,z*fraction);}
    private static void line(VertexConsumer vertices,PoseStack.Pose pose,Vec3 point,Vec3 normal){vertices.addVertex(pose.pose(),(float)point.x,(float)point.y,(float)point.z).setColor(0,0,0,255).setNormal(pose,(float)normal.x,(float)normal.y,(float)normal.z);}
    private static void vertex(VertexConsumer vertices,PoseStack.Pose pose,float x,float y,float u,float v,int light){vertices.addVertex(pose.pose(),x,y,0).setColor(255,255,255,255).setUv(u,v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose,0,1,0);}
}
