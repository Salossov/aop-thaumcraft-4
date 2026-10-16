package thaumcraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import thaumcraft.entities.FallingTaint;

final class FallingTaintRenderer extends EntityRenderer<FallingTaint> {
    private final net.minecraft.client.renderer.block.BlockRenderDispatcher blocks;
    FallingTaintRenderer(EntityRendererProvider.Context context){super(context);blocks=context.getBlockRenderDispatcher();shadowRadius=.5f;}
    @Override public ResourceLocation getTextureLocation(FallingTaint entity){return TextureAtlas.LOCATION_BLOCKS;}
    @Override public void render(FallingTaint entity,float yaw,float partial,PoseStack pose,MultiBufferSource buffers,int light){
        pose.pushPose();pose.translate(-.5,entity.renderOffsetY(),-.5);
        blocks.renderSingleBlock(entity.blockState(),pose,buffers,light,OverlayTexture.NO_OVERLAY);
        pose.popPose();super.render(entity,yaw,partial,pose,buffers,light);
    }
}
