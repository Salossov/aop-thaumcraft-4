package thaumcraft.mixin;

import net.minecraft.client.renderer.entity.ItemRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import thaumcraft.client.WandLayerModel;

@Mixin(ItemRenderer.class)
public abstract class WandLayerLightMixin {
    @ModifyArgs(method="render",at=@At(value="INVOKE",target="Lnet/minecraft/client/renderer/entity/ItemRenderer;renderModelLists(Lnet/minecraft/client/resources/model/BakedModel;Lnet/minecraft/world/item/ItemStack;IILcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;)V"))
    private void thaumcraft$wandLayerLighting(Args args){if(args.get(0) instanceof WandLayerModel layer)args.set(2,layer.packedLight(args.get(2)));}
}
