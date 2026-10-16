package thaumcraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.minecraft.world.phys.*;
import org.joml.Quaternionf;
import thaumcraft.blocks.*;
import thaumcraft.registry.R5Content;

/** Original sixteen pulsing cube pieces, mounted key and five-block void plane. */
public final class EldritchLockRenderer implements BlockEntityRenderer<EldritchLockBlockEntity> {
    private static final ResourceLocation TEXTURE=ResourceLocation.parse("thaumcraft:textures/models/eldritch_cube.png");
    private final ModelPart cube;
    // TC4 sets ModelRenderer.mirror AFTER addBox: the already-created ModelBox stays unmirrored.
    public EldritchLockRenderer(BlockEntityRendererProvider.Context context){var mesh=new MeshDefinition();mesh.getRoot().addOrReplaceChild("cube",CubeListBuilder.create().texOffs(0,0).addBox(-8,-8,-8,16,16,16),PartPose.ZERO);cube=LayerDefinition.create(mesh,64,64).bakeRoot().getChild("cube");}
    @Override public AABB getRenderBoundingBox(EldritchLockBlockEntity tile){return tile.renderBounds();}
    @Override public void render(EldritchLockBlockEntity tile,float partial,PoseStack pose,MultiBufferSource buffers,int light,int overlay){
        var mc=Minecraft.getInstance();var camera=mc.getCameraEntity();var facing=tile.facing>=0&&tile.facing<6?Direction.from3DDataValue(tile.facing):null;
        drawArms(tile.count,facing,camera==null?0:camera.tickCount,partial,pose,buffers,light,overlay);
        if(tile.count>=0&&tile.getLevel()!=null){pose.pushPose();keyPose(pose,facing);mc.getItemRenderer().renderStatic(new ItemStack(R5Content.RUNIC_KEY.get()),ItemDisplayContext.GROUND,light,overlay,pose,buffers,tile.getLevel(),0);pose.popPose();}
        if(facing!=null&&facing.getAxis().isHorizontal())EldritchVoidRenderer.lockPlane(pose,buffers,facing,camera!=null&&EldritchVoidGeometry.near(camera.distanceToSqr(Vec3.atCenterOf(tile.getBlockPos()))));
    }
    void drawArms(int count,Direction facing,int cameraTicks,float partial,PoseStack pose,MultiBufferSource buffers,int light,int overlay){
        for(var piece:EldritchLockGeometry.cubes(count,cameraTicks,partial)){
            pose.pushPose();armPose(pose,facing,piece);cube.render(pose,buffers.getBuffer(RenderType.entityCutoutNoCull(TEXTURE)),light,overlay);pose.popPose();
        }
    }
    static void armPose(PoseStack pose,Direction facing,EldritchLockGeometry.Cube cube){
        pose.translate(.5,.5,.5);if(facing!=null)pose.mulPose(new Quaternionf().rotationAxis((float)Math.toRadians(cube.arm()*90),facing.getStepX(),facing.getStepY(),facing.getStepZ()));pose.translate(0,cube.offsetY(),0);pose.scale((float)cube.width(),(float)cube.height(),(float)cube.width());
    }
    static void keyPose(PoseStack pose,Direction facing){
        pose.translate(.5f+(facing==null?0:facing.getStepX())*.525f,.285f,.5f+(facing==null?0:facing.getStepZ())*.525f);
        if(facing!=null)switch(facing){case EAST->pose.mulPose(Axis.YP.rotationDegrees(90));case WEST->pose.mulPose(Axis.YP.rotationDegrees(270));case NORTH->pose.mulPose(Axis.YP.rotationDegrees(180));default->{}}
    }
}
