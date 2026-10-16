package thaumcraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import thaumcraft.production.DeconstructionTableBlockEntity;

/** The table body is baked; the original scanner, rotating input and aspect sit above it. */
public final class DeconstructionRenderer implements BlockEntityRenderer<DeconstructionTableBlockEntity> {
    public DeconstructionRenderer(BlockEntityRendererProvider.Context context){}
    @Override public void render(DeconstructionTableBlockEntity tile,float partial,PoseStack pose,MultiBufferSource buffers,int light,int overlay){
        float time=tile.getLevel().getGameTime()+partial;var renderer=Minecraft.getInstance().getItemRenderer();
        TableScannerGeometry.draw(pose,buffers,light,overlay);
        if(!tile.getItem(0).isEmpty()){pose.pushPose();pose.translate(.5,1.15+Math.sin(time/14)*.05+.05,.5);pose.mulPose(Axis.YP.rotationDegrees(time%360));renderer.renderStatic(tile.getItem(0).copyWithCount(1),ItemDisplayContext.GROUND,light,overlay,pose,buffers,tile.getLevel(),0);pose.popPose();}
        var aspect=tile.pendingAspect();if(aspect==null)return;pose.pushPose();pose.translate(.5,1.081,.5);pose.mulPose(Axis.XP.rotationDegrees(90));pose.mulPose(Axis.ZP.rotationDegrees(time%360));
        var out=buffers.getBuffer(RenderType.entityTranslucent(ResourceLocation.parse(aspect.texture())));float[][] points={{-.192f,-.192f},{-.192f,.192f},{.192f,.192f},{.192f,-.192f}};int color=0xcc000000|aspect.getColor();
        for(int i=0;i<4;i++)out.addVertex(pose.last(),points[i][0],points[i][1],0).setColor(color).setUv(i<2?0:1,i==0||i==3?1:0).setOverlay(overlay).setLight(light).setNormal(pose.last(),0,0,-1);pose.popPose();
    }
}
