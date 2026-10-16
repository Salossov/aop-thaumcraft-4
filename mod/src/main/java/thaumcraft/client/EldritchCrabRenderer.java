package thaumcraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import thaumcraft.entities.EldritchCrab;

final class EldritchCrabRenderer extends MobRenderer<EldritchCrab,EldritchCrabModel> {
    private static final ResourceLocation BODY=ResourceLocation.parse("thaumcraft:textures/models/crab.png"),OVERLAY=ResourceLocation.parse("thaumcraft:textures/models/craboverlay.png");
    EldritchCrabRenderer(EntityRendererProvider.Context context){super(context,new EldritchCrabModel(),1);addLayer(new RenderLayer<>(this){@Override public void render(PoseStack pose,MultiBufferSource buffers,int light,EldritchCrab crab,float walk,float amount,float partial,float age,float yaw,float pitch){getParentModel().renderToBuffer(pose,buffers.getBuffer(crab.isInvisible()?ThaumcraftRenderTypes.invisibleCrabOverlay(OVERLAY):RenderType.entityTranslucent(OVERLAY)),200,OverlayTexture.NO_OVERLAY);}});}
    @Override public ResourceLocation getTextureLocation(EldritchCrab crab){return BODY;}
}
