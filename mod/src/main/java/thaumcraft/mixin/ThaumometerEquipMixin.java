package thaumcraft.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.world.InteractionHand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import thaumcraft.items.ThaumometerItem;

/** Successful RMB use resets equip height independently of stack-change animation hooks. */
@Mixin(ItemInHandRenderer.class)
public abstract class ThaumometerEquipMixin {
    @Inject(method="itemUsed",at=@At("HEAD"),cancellable=true)
    private void thaumcraft$keepScannerEquipped(InteractionHand hand,CallbackInfo ci){
        var player=Minecraft.getInstance().player;
        if(player!=null&&player.getItemInHand(hand).getItem() instanceof ThaumometerItem)ci.cancel();
    }
}
