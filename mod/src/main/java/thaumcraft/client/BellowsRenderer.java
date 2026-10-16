package thaumcraft.client;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import thaumcraft.production.*;
public final class BellowsRenderer implements BlockEntityRenderer<BellowsBlockEntity>{
 public static final ModelLayerLocation LAYER=new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("thaumcraft","bellows"),"main");
 private final ModelPart top,bottom,middle,bag,nozzle;
 public BellowsRenderer(BlockEntityRendererProvider.Context c){var m=c.bakeLayer(LAYER);top=m.getChild("top");bottom=m.getChild("bottom");middle=m.getChild("middle");bag=m.getChild("bag");nozzle=m.getChild("nozzle");}
 public static LayerDefinition layer(){var mesh=new MeshDefinition();var p=mesh.getRoot();p.addOrReplaceChild("bottom",CubeListBuilder.create().texOffs(0,0).mirror().addBox(-6,0,-6,12,2,12),PartPose.offset(0,22,0));p.addOrReplaceChild("top",CubeListBuilder.create().texOffs(0,0).mirror().addBox(-6,0,-6,12,2,12),PartPose.offset(0,8,0));p.addOrReplaceChild("middle",CubeListBuilder.create().texOffs(0,0).mirror().addBox(-6,-1,-6,12,2,12),PartPose.offset(0,16,0));p.addOrReplaceChild("bag",CubeListBuilder.create().texOffs(48,0).mirror().addBox(-10,-12.03333f,-10,20,24,20),PartPose.offset(0,.5f,0));p.addOrReplaceChild("nozzle",CubeListBuilder.create().texOffs(0,36).mirror().addBox(-2,-2,0,4,4,2),PartPose.offset(0,16,6));return LayerDefinition.create(mesh,128,64);}
 @Override public void render(BellowsBlockEntity tile,float partial,PoseStack pose,MultiBufferSource buffers,int light,int overlay){float scale=Mth.lerp(partial,tile.previousInflation,tile.inflation),tscale=.125f+scale*.875f;var buffer=buffers.getBuffer(RenderType.entityCutout(ResearchGraphics.texture("models/bellows")));pose.pushPose();pose.translate(.5,-.5,.5);switch(tile.getBlockState().getValue(BellowsBlock.FACING)){case DOWN->{pose.translate(0,1,-1);pose.mulPose(Axis.XP.rotationDegrees(90));}case UP->{pose.translate(0,1,1);pose.mulPose(Axis.XP.rotationDegrees(270));}case NORTH->pose.mulPose(Axis.YP.rotationDegrees(180));case WEST->pose.mulPose(Axis.YP.rotationDegrees(270));case EAST->pose.mulPose(Axis.YP.rotationDegrees(90));default->{}}
  pose.pushPose();pose.translate(0,1,0);pose.scale(.5f,(scale+.1f)/2,.5f);bag.render(pose,buffer,light,overlay);pose.popPose();pose.pushPose();pose.translate(0,-tscale/2+.5,0);top.render(pose,buffer,light,overlay);pose.popPose();pose.pushPose();pose.translate(0,tscale/2-.5,0);bottom.render(pose,buffer,light,overlay);pose.popPose();middle.render(pose,buffer,light,overlay);nozzle.render(pose,buffer,light,overlay);pose.popPose();
 }
}
