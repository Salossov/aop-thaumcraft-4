package thaumcraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import thaumcraft.production.ArcaneBoreBlockEntity;
import thaumcraft.production.ArcaneLampBlock;
import thaumcraft.production.ArcaneLampBlockEntity;

/** The original lamp mount shares the bore base's two nozzle cuboids. */
public final class ArcaneLampRenderer implements BlockEntityRenderer<ArcaneLampBlockEntity> {
    private static final ResourceLocation TEXTURE=ResourceLocation.parse("thaumcraft:textures/models/bore.png");
    private final ModelPart model=OriginalDeviceGeometry.bake("bore_base");
    public ArcaneLampRenderer(BlockEntityRendererProvider.Context context){}

    @Override public void render(ArcaneLampBlockEntity tile,float partial,PoseStack pose,MultiBufferSource buffers,int light,int overlay){
        var direction=tile.getBlockState().getValue(ArcaneLampBlock.FACING);
        draw(direction,pose,buffers,light,overlay);
        if(tile.getLevel()!=null&&tile.getLevel().getBlockEntity(tile.getBlockPos().relative(direction)) instanceof ArcaneBoreBlockEntity bore&&bore.base()){
            pose.pushPose();pose.translate(direction.getStepX(),direction.getStepY(),direction.getStepZ());
            draw(direction.getOpposite(),pose,buffers,light,overlay);pose.popPose();
        }
    }

    private void draw(Direction direction,PoseStack pose,MultiBufferSource buffers,int light,int overlay){
        pose.pushPose();pose.translate(.5,0,.5);
        switch(direction){
            case DOWN->{pose.translate(-.5,.5,0);pose.mulPose(Axis.ZN.rotationDegrees(90));}
            case UP->{pose.translate(.5,.5,0);pose.mulPose(Axis.ZP.rotationDegrees(90));}
            case NORTH->pose.mulPose(Axis.YP.rotationDegrees(90));
            case SOUTH->pose.mulPose(Axis.YP.rotationDegrees(270));
            case WEST->pose.mulPose(Axis.YP.rotationDegrees(180));
            default->{}
        }
        var vertices=buffers.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
        model.getChild("Nozzle1").render(pose,vertices,light,overlay);
        model.getChild("Nozzle2").render(pose,vertices,light,overlay);
        pose.popPose();
    }
}
