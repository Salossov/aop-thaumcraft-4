package thaumcraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.model.BakedModelWrapper;

/** Per-resolution use animation; cached composites never retain their last wielder. */
final class WandUseModel extends BakedModelWrapper<BakedModel> {
    private final ItemStack stack;
    private final LivingEntity wielder;
    private final float pivot;

    static BakedModel wrap(BakedModel model, ItemStack stack, LivingEntity wielder, String kind) {
        return wielder != null && wielder.isUsingItem() && wielder.getUseItem() == stack
                ? new WandUseModel(model, stack, wielder, kind) : model;
    }

    private WandUseModel(BakedModel model, ItemStack stack, LivingEntity wielder, String kind) {
        super(model);
        this.stack = stack;
        this.wielder = wielder;
        // Source T(0,1,0) pivot, mapped through each model's Rx(180) frame and item centering.
        this.pivot = switch (kind) { case "staff" -> .36875f; case "sceptre" -> -.2625f; default -> -.375f; };
    }

    @Override public BakedModel applyTransform(ItemDisplayContext context, PoseStack pose, boolean left) {
        originalModel.applyTransform(context, pose, left);
        if (context != ItemDisplayContext.THIRD_PERSON_LEFT_HAND && context != ItemDisplayContext.THIRD_PERSON_RIGHT_HAND)
            return this;
        var arm = wielder.getUsedItemHand() == net.minecraft.world.InteractionHand.MAIN_HAND
                ? wielder.getMainArm() : wielder.getMainArm().getOpposite();
        if (!wielder.isUsingItem() || wielder.getUseItem() != stack
                || (context == ItemDisplayContext.THIRD_PERSON_LEFT_HAND) != (arm == HumanoidArm.LEFT))
            return this;
        float partial = Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(false);
        float ticks = wielder.getTicksUsingItem() + partial;
        boolean charge = WandClientEffects.chargeAnimation(wielder, stack, partial);
        int sign = arm == HumanoidArm.RIGHT ? 1 : -1;
        pose.translate(0, pivot, 0);
        // Rx(180) reverses the source Z rotation; mirror it for the modern left hand.
        pose.mulPose(Axis.ZP.rotationDegrees(-sign * Mth.sin(ticks / (charge ? .8f : 10f)) * (charge ? 1f : 10f)));
        pose.mulPose(Axis.XP.rotationDegrees(Mth.sin(ticks / (charge ? .7f : 15f)) * (charge ? 1f : 10f)));
        pose.translate(0, -pivot, 0);
        return this;
    }
}
