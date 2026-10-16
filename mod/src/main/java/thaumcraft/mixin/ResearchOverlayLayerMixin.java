package thaumcraft.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Final GUI pass, after screens, their tooltips, saving indicator and vanilla toasts. */
@Mixin(GameRenderer.class)
public abstract class ResearchOverlayLayerMixin {
    @Inject(method="render",at=@At(value="INVOKE",target="Lnet/minecraft/client/gui/GuiGraphics;flush()V"))
    private void tcResearchOnTop(DeltaTracker tracker,boolean renderLevel,CallbackInfo ci,@Local GuiGraphics graphics){thaumcraft.client.ResearchCompletionOverlay.drawTopmost(graphics);}
}
