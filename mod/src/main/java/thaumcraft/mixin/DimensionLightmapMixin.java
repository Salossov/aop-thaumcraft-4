package thaumcraft.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.world.effect.MobEffects;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import thaumcraft.api.client.DimensionLightmap;

/** One final pixel hook; dimensions without a registered policy retain vanilla. */
@Mixin(LightTexture.class)
public abstract class DimensionLightmapMixin {
    @Shadow @Final private Minecraft minecraft;
    @Shadow @Final private GameRenderer renderer;
    @Shadow private float blockLightRedFlicker;
    @Unique private DimensionLightmap.Context thaumcraft$context;
    @Inject(method="updateLightTexture",at=@At("HEAD"))
    private void thaumcraft$begin(float partialTicks,CallbackInfo info){
        var level=minecraft.level;var player=minecraft.player;
        thaumcraft$context=level==null||player==null||!DimensionLightmap.hasPolicy(level.dimensionType().effectsLocation())?null:
                new DimensionLightmap.Context(level,partialTicks,level.getSkyDarken(1.0f),blockLightRedFlicker+1.5f,
                        level.getSkyFlashTime()>0,renderer.getDarkenWorldAmount(1f),renderer.getDarkenWorldAmount(partialTicks),
                        player.hasEffect(MobEffects.NIGHT_VISION)?GameRenderer.getNightVisionScale(player,partialTicks):0f,minecraft.options.gamma().get().floatValue());
    }
    @WrapOperation(method="updateLightTexture",at=@At(value="INVOKE",target="Lcom/mojang/blaze3d/platform/NativeImage;setPixelRGBA(III)V"))
    private void thaumcraft$pixel(NativeImage image,int x,int y,int color,Operation<Void> original){
        original.call(image,x,y,thaumcraft$context==null?color:DimensionLightmap.pixel(thaumcraft$context,x,y,color));
    }
}
