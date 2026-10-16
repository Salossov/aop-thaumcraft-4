package thaumcraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import thaumcraft.entities.Pech;

public final class PechRenderer extends MobRenderer<Pech,PechModel> {
    public PechRenderer(EntityRendererProvider.Context context){super(context,new PechModel(),.5f);addLayer(new RenderLayer<>(this){@Override public void render(PoseStack pose,MultiBufferSource buffers,int light,Pech entity,float walk,float amount,float partial,float age,float yaw,float pitch){var stack=entity.getMainHandItem();if(stack.isEmpty())return;pose.pushPose();getParentModel().rightArm.translateAndRotate(pose);pose.translate(-.0625,.3375,.0625);if(stack.getItem() instanceof BlockItem){pose.translate(0,.1875,-.3125);pose.mulPose(Axis.XP.rotationDegrees(20));pose.mulPose(Axis.YP.rotationDegrees(45));pose.scale(-.375f,-.375f,.375f);}else if(stack.is(Items.BOW)){pose.translate(0,.125,.3125);pose.mulPose(Axis.YP.rotationDegrees(-20));pose.scale(.625f,-.625f,.625f);pose.mulPose(Axis.XP.rotationDegrees(-100));pose.mulPose(Axis.YP.rotationDegrees(45));}else if(stack.getItem() instanceof SwordItem||stack.getItem() instanceof DiggerItem||stack.getItem() instanceof thaumcraft.items.CastingWandItem){if(stack.getItem() instanceof thaumcraft.items.CastingWandItem)pose.translate(0,-.125,0);pose.translate(0,.1875,0);pose.scale(.625f,-.625f,.625f);pose.mulPose(Axis.XP.rotationDegrees(-100));pose.mulPose(Axis.YP.rotationDegrees(45));}else{pose.translate(.25,.1875,-.1875);pose.scale(.375f,.375f,.375f);pose.mulPose(Axis.ZP.rotationDegrees(60));pose.mulPose(Axis.XP.rotationDegrees(-90));pose.mulPose(Axis.ZP.rotationDegrees(20));}context.getItemRenderer().renderStatic(entity,stack,ItemDisplayContext.NONE,false,pose,buffers,entity.level(),light,net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY,entity.getId());pose.popPose();}});}
    @Override public ResourceLocation getTextureLocation(Pech entity){return ResearchGraphics.texture("models/"+new String[]{"pech_forage","pech_thaum","pech_stalker"}[Math.floorMod(entity.variant(),3)]);}
    @Override public void render(Pech entity,float yaw,float partial,PoseStack pose,MultiBufferSource buffers,int light){pose.pushPose();if(entity.isCrouching())pose.translate(0,-.125,0);super.render(entity,yaw,partial,pose,buffers,light);pose.popPose();}
}
