package thaumcraft.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import thaumcraft.addon.ReplacementEffects;

/** Defer only native item/orb insertion while a protected replacement owns the scope. */
@Mixin(ServerLevel.class)
public abstract class ServerLevelReplacementDropsMixin {
    @Inject(method="addEntity",at=@At("HEAD"),cancellable=true)
    private void thaumcraft$deferReplacementDrops(Entity entity,CallbackInfoReturnable<Boolean> callback){
        if(ReplacementEffects.captureEntity((ServerLevel)(Object)this,entity))callback.setReturnValue(true);
    }
}
