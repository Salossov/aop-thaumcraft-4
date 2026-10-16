package thaumcraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.minecraft.world.phys.Vec3;
import thaumcraft.production.*;

public final class FluxScrubberRenderer implements BlockEntityRenderer<FluxScrubberBlockEntity> {
    private static final ResourceLocation TEXTURE=ResourceLocation.parse("thaumcraft:textures/models/fluxscrubber.png");
    public FluxScrubberRenderer(BlockEntityRendererProvider.Context context){}
    @Override public void render(FluxScrubberBlockEntity tile,float partial,PoseStack pose,MultiBufferSource buffers,int light,int overlay){draw(tile.getBlockState().getValue(FluxScrubberBlock.FACING),tile.animationTicks+partial,pose,buffers,light,overlay);}
    private static void draw(Direction direction,float time,PoseStack pose,MultiBufferSource buffers,int light,int overlay){
        pose.pushPose();pose.translate(.5,.5,.5);
        switch(direction){case DOWN->pose.mulPose(Axis.XN.rotationDegrees(90));case UP->pose.mulPose(Axis.XP.rotationDegrees(90));case SOUTH->pose.mulPose(Axis.YP.rotationDegrees(180));case WEST->pose.mulPose(Axis.YP.rotationDegrees(90));case EAST->pose.mulPose(Axis.YN.rotationDegrees(90));default->{}}
        pose.translate(0,0,-.5);var mesh=OriginalObjMesh.get("obelisk_cap");mesh.render("Cap",pose,buffers,TEXTURE,light,overlay,0xffffffff,false);
        pose.translate(0,0,-(Math.sin(time/8)*.075+.075));mesh.render("Tip",pose,buffers,TEXTURE,light,overlay,0xffffffff,false);pose.popPose();
    }
    public static void sparkle(thaumcraft.network.FluxScrubPayload packet){if(Minecraft.getInstance().level!=null)LegacyParticles.spark(packet.position().getCenter(),new Vec3(0,.005,0),0xdd00ff,20);}
    private static final class ItemRenderer extends BlockEntityWithoutLevelRenderer {
        ItemRenderer(){super(Minecraft.getInstance().getBlockEntityRenderDispatcher(),Minecraft.getInstance().getEntityModels());}
        @Override public void renderByItem(ItemStack stack,ItemDisplayContext context,PoseStack pose,MultiBufferSource buffers,int light,int overlay){draw(Direction.DOWN,0,pose,buffers,light,overlay);}
    }
    public static final class Extension implements net.neoforged.neoforge.client.extensions.common.IClientItemExtensions {
        private ItemRenderer renderer;
        @Override public BlockEntityWithoutLevelRenderer getCustomRenderer(){if(renderer==null)renderer=new ItemRenderer();return renderer;}
    }
}
