package thaumcraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import thaumcraft.items.*;

public final class WandClientEffects implements IClientItemExtensions {
    static net.minecraft.world.phys.Vec3 chargingTip(LocalPlayer player,float partial){
        var stack=player.getUseItem();var arm=player.getUsedItemHand()==net.minecraft.world.InteractionHand.MAIN_HAND?player.getMainArm():player.getMainArm().getOpposite();var pose=new PoseStack();usePose(pose,player,arm,stack,partial,0);pose.mulPose(Axis.YP.rotationDegrees(arm==HumanoidArm.RIGHT?-45:45));pose.translate(0,.8/16,0);pose.scale(.55f,.55f,.55f);
        float top=stack.getItem() instanceof CastingWandItem item?switch(item.kind()){case STAFF->27.8f/16f-.5f;case SCEPTRE->15.91f/16-.5f;default->19/16f-.5f;}:.6875f;
        var point=pose.last().pose().transformPosition(new org.joml.Vector3f(0,top,0));net.minecraft.client.Minecraft.getInstance().gameRenderer.getMainCamera().rotation().transform(point);return player.getEyePosition(partial).add(point.x,point.y,point.z);
    }
    public static boolean expandedTooltip(){return net.minecraft.client.gui.screens.Screen.hasShiftDown();}
    public static net.minecraft.world.entity.player.Player tooltipPlayer(){return net.minecraft.client.Minecraft.getInstance().player;}
    @Override public boolean applyForgeHandTransform(PoseStack pose,LocalPlayer player,HumanoidArm arm,ItemStack stack,float partial,float equip,float swing) {
        if(!player.isUsingItem()||player.getUseItem().getItem()!=stack.getItem()||arm!=(player.getUsedItemHand()==net.minecraft.world.InteractionHand.MAIN_HAND?player.getMainArm():player.getMainArm().getOpposite()))return false;
        usePose(pose,player,arm,stack,partial,equip);return true;
    }
    private static void usePose(PoseStack pose,LocalPlayer player,HumanoidArm arm,ItemStack stack,float partial,float equip){
        int sign=arm==HumanoidArm.RIGHT?1:-1;float ticks=player.getTicksUsingItem()+partial;
        float raise=Math.min(1,ticks/3);
        pose.translate(sign*net.minecraft.util.Mth.lerp(raise,.56f,.18f),net.minecraft.util.Mth.lerp(raise,-.52f,-.5f)-equip*.6f,net.minecraft.util.Mth.lerp(raise,-.72f,-1.2f));
        pose.mulPose(Axis.YP.rotationDegrees(sign*45));
        pose.translate(0,.35f,0);
        boolean charge=chargeAnimation(player,stack,partial);
        pose.mulPose(Axis.XP.rotationDegrees(10));pose.mulPose(Axis.ZP.rotationDegrees(sign*10));pose.mulPose(Axis.XN.rotationDegrees(60*Math.min(1,ticks/3)));
        pose.mulPose(Axis.ZP.rotationDegrees(sign*(float)Math.sin(ticks/(charge?.8f:10))*(charge?1:10)));
        pose.mulPose(Axis.XP.rotationDegrees((float)Math.sin(ticks/(charge?.7f:15))*(charge?1:10)));
        pose.translate(0,-.35f,0);
    }
    static boolean chargeAnimation(LivingEntity wielder,ItemStack stack,float partial){
        var focus=WandFocus.get(stack);boolean charge=focus.getItem() instanceof ThaumicFocusItem item&&switch(item.kind()){case EXCAVATION->true;case FIRE->!item.isUpgradedWith(focus,thaumcraft.api.wands.FocusUpgradeType.fireball);case SHOCK->!item.isUpgradedWith(focus,thaumcraft.api.wands.FocusUpgradeType.earthshock);default->false;};
        if(!charge)return false;
        var mc=net.minecraft.client.Minecraft.getInstance();
        var hit=wielder==mc.player?mc.hitResult:wielder.pick(5,partial,false);
        return !(hit instanceof net.minecraft.world.phys.BlockHitResult block&&wielder.level().getBlockEntity(block.getBlockPos()) instanceof thaumcraft.blocks.AuraNodeBlockEntity);
    }
}
