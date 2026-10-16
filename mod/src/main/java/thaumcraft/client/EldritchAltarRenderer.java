package thaumcraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import thaumcraft.production.EldritchAltarBlockEntity;

public final class EldritchAltarRenderer implements BlockEntityRenderer<EldritchAltarBlockEntity> {
    public EldritchAltarRenderer(BlockEntityRendererProvider.Context context){}
    @Override public void render(EldritchAltarBlockEntity tile,float partial,PoseStack pose,MultiBufferSource buffers,int light,int overlay){pose.pushPose();pose.translate(.5,0,.5);pose.mulPose(Axis.XN.rotationDegrees(90));OriginalObjMesh.get("obelisk_cap").render("Cap",pose,buffers,ResourceLocation.parse("thaumcraft:textures/models/obelisk_cap.png"),light,overlay,0xffffffff,false);pose.popPose();for(int i=0;i<tile.eyes;i++){pose.pushPose();pose.translate(.5,0,.5);pose.mulPose(Axis.YP.rotationDegrees(i*90));pose.translate(.46,.2,0);pose.mulPose(Axis.YP.rotationDegrees(90));pose.mulPose(Axis.XN.rotationDegrees(18));pose.scale(.5f,.5f,.5f);Minecraft.getInstance().getItemRenderer().renderStatic(new ItemStack(thaumcraft.registry.R3Equipment.ELDRITCH_EYE.get()),ItemDisplayContext.NONE,light,overlay,pose,buffers,tile.getLevel(),i);pose.popPose();}}
}
