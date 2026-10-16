package thaumcraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.world.item.ItemDisplayContext;
import thaumcraft.production.FocalManipulatorBlockEntity;

public final class FocalManipulatorRenderer implements BlockEntityRenderer<FocalManipulatorBlockEntity> {
    public FocalManipulatorRenderer(BlockEntityRendererProvider.Context context){}
    @Override public void render(FocalManipulatorBlockEntity tile,float partial,PoseStack pose,MultiBufferSource buffers,int light,int overlay){var stack=tile.getItem(0);if(stack.isEmpty())return;float time=tile.getLevel().getGameTime()+partial;pose.pushPose();pose.translate(.5,1.2+Math.sin(time/14)*.1,.5);pose.mulPose(Axis.YP.rotationDegrees(time%360));pose.scale(.5f,.5f,.5f);Minecraft.getInstance().getItemRenderer().renderStatic(stack,ItemDisplayContext.GROUND,light,overlay,pose,buffers,tile.getLevel(),0);pose.popPose();if(tile.active()&&tile.getLevel().random.nextBoolean()){var pos=tile.getBlockPos().getCenter().add((tile.getLevel().random.nextFloat()-tile.getLevel().random.nextFloat())*.3,.75,(tile.getLevel().random.nextFloat()-tile.getLevel().random.nextFloat())*.3);LegacyParticles.spark(pos,net.minecraft.world.phys.Vec3.ZERO,0x99ffff,8);}}
}
