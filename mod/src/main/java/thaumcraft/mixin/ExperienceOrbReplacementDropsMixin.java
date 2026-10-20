package thaumcraft.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import thaumcraft.addon.ReplacementEffects;

/** Capture the full grant before award can increment an existing nearby orb. */
@Mixin(ExperienceOrb.class)
public abstract class ExperienceOrbReplacementDropsMixin {
    @Inject(method="award",at=@At("HEAD"),cancellable=true)
    private static void thaumcraft$deferReplacementExperience(ServerLevel level,Vec3 pos,int amount,CallbackInfo callback){
        if(ReplacementEffects.captureExperience(level,pos,amount))callback.cancel();
    }
}
