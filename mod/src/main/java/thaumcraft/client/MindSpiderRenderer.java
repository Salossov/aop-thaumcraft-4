package thaumcraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.SpiderModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import thaumcraft.entities.MindSpider;

public final class MindSpiderRenderer extends EntityRenderer<MindSpider> {
    private final SpiderModel<MindSpider> model;
    private static final ResourceLocation BODY=ResourceLocation.parse("thaumcraft:textures/models/taint_spider.png"),EYES=ResourceLocation.parse("thaumcraft:textures/models/taint_spider_eyes.png");
    public MindSpiderRenderer(EntityRendererProvider.Context context){super(context);model=new SpiderModel<>(context.bakeLayer(ModelLayers.SPIDER));shadowRadius=0;}
    @Override public ResourceLocation getTextureLocation(MindSpider spider){return BODY;}
    @Override public void render(MindSpider spider,float yaw,float partial,PoseStack pose,MultiBufferSource buffers,int light){if(spider.viewer()!=null&&(Minecraft.getInstance().player==null||!spider.viewer().equals(Minecraft.getInstance().player.getUUID())))return;pose.pushPose();pose.mulPose(Axis.YP.rotationDegrees(180-yaw));if(spider.deathTime>0)pose.mulPose(Axis.ZP.rotationDegrees(Math.min(1,(float)Math.sqrt((spider.deathTime+partial-1)/20f*1.6))*180));pose.scale(.3f,-.3f,-.3f);pose.translate(0,-1.5,0);model.setupAnim(spider,spider.walkAnimation.position(partial),spider.walkAnimation.speed(partial),spider.tickCount+partial,net.minecraft.util.Mth.rotLerp(partial,spider.yHeadRotO,spider.yHeadRot)-yaw,net.minecraft.util.Mth.lerp(partial,spider.xRotO,spider.getXRot()));int alpha=(int)(255*Math.min(.1f,(spider.tickCount+partial)/100f));model.renderToBuffer(pose,buffers.getBuffer(RenderType.entityTranslucent(BODY)),light,OverlayTexture.NO_OVERLAY,alpha<<24|0xffffff);model.renderToBuffer(pose,buffers.getBuffer(RenderType.eyes(EYES)),15728880,OverlayTexture.NO_OVERLAY);pose.popPose();}
}
