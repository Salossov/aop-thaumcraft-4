package thaumcraft.mixin;

import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import thaumcraft.api.entities.PlayerAirControlEvent;

@Mixin(Player.class)
public abstract class PlayerAirControlMixin {
    @Inject(method="getFlyingSpeed",at=@At("RETURN"),cancellable=true)
    private void thaumcraft$airControl(CallbackInfoReturnable<Float> result) {
        var event=new PlayerAirControlEvent((Player)(Object)this,result.getReturnValueF());
        PlayerAirControlEvent.BUS.post(event);
        result.setReturnValue(event.speed());
    }
}
