package thaumcraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.*;
import thaumcraft.production.NitorBlockEntity;

/** TC4's rising, shrinking wisps run in the native particle engine. */
public final class NitorRenderer implements BlockEntityRenderer<NitorBlockEntity> {
    public NitorRenderer(BlockEntityRendererProvider.Context context){}
    @Override public void render(NitorBlockEntity tile,float partial,PoseStack pose,MultiBufferSource buffers,int light,int overlay){
        // FXWisp emission belongs to TileNitor's client tick, independent of visible frames.
    }
}
