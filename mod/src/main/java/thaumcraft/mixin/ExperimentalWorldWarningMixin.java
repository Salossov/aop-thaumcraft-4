package thaumcraft.mixin;

import com.mojang.serialization.Lifecycle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldOpenFlows;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Custom TC4 dimensions trigger this generic creation dialog even without experimental game rules. */
@Mixin(WorldOpenFlows.class)
public abstract class ExperimentalWorldWarningMixin {
    @Inject(method="confirmWorldCreation",at=@At("HEAD"),cancellable=true)
    private static void thaumcraft$skipExperimentalCreation(Minecraft minecraft,CreateWorldScreen parent,Lifecycle lifecycle,Runnable create,boolean skip,CallbackInfo callback){
        if(lifecycle==Lifecycle.experimental()){create.run();callback.cancel();}
    }
}
