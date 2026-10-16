package thaumcraft.client;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.world.item.ItemDisplayContext;
import thaumcraft.production.ThaumatoriumBlockEntity;
/** TC4 cycles the selected recipe outputs on its front every forty player ticks. */
public final class ThaumatoriumRenderer implements BlockEntityRenderer<ThaumatoriumBlockEntity> {
 public ThaumatoriumRenderer(BlockEntityRendererProvider.Context context){}
 @Override public void render(ThaumatoriumBlockEntity tile,float partial,PoseStack pose,MultiBufferSource buffers,int light,int overlay){
  if(tile.getLevel()==null||tile.selections.isEmpty())return;var client=Minecraft.getInstance();var ids=java.util.List.copyOf(tile.selections.keySet());int ticks=client.player==null?0:client.player.tickCount;var id=ids.get(Math.floorMod(ticks/40,ids.size()));var recipe=tile.getLevel().getRecipeManager().byKey(id);if(recipe.isEmpty()||!(recipe.get().value() instanceof thaumcraft.api.crafting.CrucibleRecipe crucible))return;
  var output=crucible.getRecipeOutput().copy();output.setCount(1);var face=tile.facing();pose.pushPose();pose.translate(.5+face.getStepX()/1.99,1.325,.5+face.getStepZ()/1.99);float angle=switch(face){case EAST->90;case WEST->270;case NORTH->180;default->0;};pose.mulPose(Axis.YP.rotationDegrees(angle));pose.scale(.75f,.75f,.75f);client.getItemRenderer().renderStatic(output,ItemDisplayContext.FIXED,light,overlay,pose,buffers,tile.getLevel(),0);pose.popPose();
 }
}
