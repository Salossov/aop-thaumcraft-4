package thaumcraft.client;

import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.phys.Vec3;
import thaumcraft.blocks.AuraNodeBlockEntity;
import thaumcraft.items.CastingWandItem;

/** TC4's two crossed, scrolling wispy ribbons, submitted through a normal RenderType. */
final class NodeChargeBeam {
    private static final ResourceLocation TEXTURE=ResearchGraphics.texture("misc/wispy");
    static void render(AuraNodeBlockEntity node,float partial,PoseStack pose,MultiBufferSource buffers){
        if(node.drainer()==null||node.getLevel()==null)return;var player=node.getLevel().getPlayerByUUID(node.drainer());
        if(player==null||!player.isUsingItem()||!(player.getUseItem().getItem() instanceof CastingWandItem))return;
        float pitch=Mth.lerp(partial,player.xRotO,player.getXRot()),yaw=Mth.rotLerp(partial,player.yRotO,player.getYRot());
        var arm=player.getUsedItemHand()==net.minecraft.world.InteractionHand.MAIN_HAND?player.getMainArm():player.getMainArm().getOpposite();
        float sway=Mth.sin((player.getTicksUsingItem()+partial)/10f)*10;var offset=new Vec3(arm==HumanoidArm.RIGHT?-0.1:0.1,-0.1,0.5).xRot(-pitch*Mth.DEG_TO_RAD).yRot(-yaw*Mth.DEG_TO_RAD).yRot(-sway*0.01f).xRot(-sway*0.015f);
        var source=player instanceof net.minecraft.client.player.LocalPlayer local&&Minecraft.getInstance().options.getCameraType().isFirstPerson()?WandClientEffects.chargingTip(local,partial):player.getEyePosition(partial).add(offset);var end=node.getBlockPos().getCenter();var delta=source.subtract(end);float distance=(float)delta.length();
        int segments=Math.max(1,Math.round(distance)*8),last=Math.min(segments,(int)(segments*Math.min(player.getTicksUsingItem(),10)/10f));if(last==0)return;
        var vertices=buffers.getBuffer(ThaumcraftRenderTypes.node(TEXTURE,true,false));float clock=node.getLevel().getGameTime()+partial,phase=clock/3f,time=clock*5/3f;
        int color=node.beamColor(partial);Vec3 previous=point(source,delta,distance,0,segments,phase);float previousAlpha=0,previousU=distance+time*0.02f;
        for(int i=1;i<=last;i++){float f=i/(float)segments,alpha=1-Math.abs(i-segments/2f)/(segments/2f),u=(1-f)*distance+time*0.02f;var current=point(source,delta,distance,i,segments,phase);
            for(int axis=0;axis<2;axis++){var width=axis==0?new Vec3(0,0.15,0):new Vec3(0.15,0,0);vertex(vertices,pose,previous.subtract(width),previousU,1,color,previousAlpha);vertex(vertices,pose,previous.add(width),previousU,0,color,previousAlpha);vertex(vertices,pose,current.add(width),u,0,color,alpha);vertex(vertices,pose,current.subtract(width),u,1,color,alpha);}
            previous=current;previousAlpha=alpha;previousU=u;
        }
    }
    private static Vec3 point(Vec3 source,Vec3 delta,float distance,int i,int segments,float phase){float f=i/(float)segments,alpha=1-Math.abs(i-segments/2f)/(segments/2f);double along=distance*(1-f)*8-phase;
        return delta.add(Mth.sin((float)((source.z%16+along)/4))*0.5f*alpha,Mth.sin((float)((source.x%16+along)/3))*0.5f*alpha,Mth.sin((float)((source.y%16+along)/2))*0.5f*alpha).scale(f).add(0.5,0.5,0.5);
    }
    private static void vertex(VertexConsumer v,PoseStack p,Vec3 at,float u,float uv,int color,float alpha){v.addVertex(p.last().pose(),(float)at.x,(float)at.y,(float)at.z).setColor(color>>16&255,color>>8&255,color&255,Math.clamp((int)(alpha*255),0,255)).setUv(u,uv).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(p.last(),0,1,0);}
    private NodeChargeBeam(){}
}
