package thaumcraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.resources.ResourceLocation;
import thaumcraft.production.BrainJarBlockEntity;

public final class BrainJarRenderer implements BlockEntityRenderer<BrainJarBlockEntity> {
    private final ModelPart brain;
    private final ModelPart brine=LayerDefinition.create(brineMesh(),64,32).bakeRoot();
    private static MeshDefinition brineMesh(){var mesh=new MeshDefinition();mesh.getRoot().addOrReplaceChild("brine",CubeListBuilder.create().texOffs(0,0).mirror().addBox(-4,-11,-4,8,10,8),PartPose.ZERO);return mesh;}
    public BrainJarRenderer(BlockEntityRendererProvider.Context context){var mesh=new MeshDefinition();var root=mesh.getRoot();root.addOrReplaceChild("brain",CubeListBuilder.create().texOffs(0,0).mirror().addBox(0,0,0,12,10,16),PartPose.offset(-6,8,-8));root.addOrReplaceChild("lower",CubeListBuilder.create().texOffs(64,0).mirror().addBox(0,0,0,8,3,7),PartPose.offset(-4,18,0));root.addOrReplaceChild("stem",CubeListBuilder.create().texOffs(0,32).mirror().addBox(0,0,0,2,6,2),PartPose.offsetAndRotation(-1,18,-2,.4089647f,0,0));brain=LayerDefinition.create(mesh,128,64).bakeRoot();}
    @Override public void render(BrainJarBlockEntity tile,float partial,PoseStack pose,MultiBufferSource buffers,int light,int overlay){float ticks=(Minecraft.getInstance().player==null?0:Minecraft.getInstance().player.tickCount)+partial;
        pose.pushPose();pose.translate(.5,.81-(float)Math.sin(ticks/14)*.03f-.03f,.5);pose.mulPose(Axis.XP.rotationDegrees(180));pose.mulPose(Axis.YP.rotationDegrees(tile.rotation(partial)-90));pose.scale(.4f,.4f,.4f);brain.render(pose,buffers.getBuffer(RenderType.entityCutoutNoCull(ResourceLocation.parse("thaumcraft:textures/models/brain2.png"))),light,overlay);pose.popPose();
        pose.pushPose();pose.translate(.5,.01,.5);pose.mulPose(Axis.XP.rotationDegrees(180));brine.render(pose,buffers.getBuffer(RenderType.entityTranslucent(ResourceLocation.parse("thaumcraft:textures/models/jarbrine.png"))),light,overlay);pose.popPose();
    }
}
