package thaumcraft.client;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.world.item.ItemDisplayContext;
import thaumcraft.production.PedestalBlockEntity;
public final class PedestalRenderer implements BlockEntityRenderer<PedestalBlockEntity>{
 public PedestalRenderer(BlockEntityRendererProvider.Context context){}
 @Override public void render(PedestalBlockEntity tile,float partial,PoseStack pose,MultiBufferSource buffers,int light,int overlay){
  if(tile instanceof thaumcraft.visnet.WandPedestalBlockEntity recharge&&recharge.drainSource()!=null){var from=recharge.drainSource().getCenter().subtract(net.minecraft.world.phys.Vec3.atLowerCornerOf(tile.getBlockPos()));float t=(Minecraft.getInstance().player==null?0:Minecraft.getInstance().player.tickCount)+partial;VisDeviceRenderers.beam(pose,buffers,from,new net.minecraft.world.phys.Vec3(.5,1.15,.5),t,recharge.drainColor(),.1f,.85f);}
  if(tile.isEmpty())return;float ticks=(Minecraft.getInstance().player==null?0:Minecraft.getInstance().player.tickCount)+partial;
  pose.pushPose();pose.translate(.5,1.15+Math.sin(ticks/14)*.05,.5);pose.mulPose(Axis.YP.rotationDegrees(ticks%360));
  var stack=tile.getItem(0);if(stack.getItem() instanceof net.minecraft.world.item.BlockItem)pose.scale(2,2,2);
  Minecraft.getInstance().getItemRenderer().renderStatic(stack,ItemDisplayContext.GROUND,light,overlay,pose,buffers,tile.getLevel(),0);pose.popPose();
 }
}
