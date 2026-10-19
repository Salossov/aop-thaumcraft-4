package thaumcraft.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.BakedModel;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import thaumcraft.api.wands.WandVisuals;

/** A single wand pass; its light replaces incoming world light without affecting other passes. */
public final class WandLayerModel extends BakedModelWrapper<BakedModel> {
    private final WandVisuals.Lighting lighting;
    public WandLayerModel(BakedModel model,WandVisuals.Lighting lighting){super(model);this.lighting=lighting;}
    public int packedLight(int worldLight){
        var player=Minecraft.getInstance().player;
        return lighting==null||player==null?worldLight:lighting.packedLight(worldLight,player.tickCount);
    }
}
