package thaumcraft.client;

import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import thaumcraft.blocks.EtherealBloomBlockEntity;

public final class EtherealBloomRenderer implements BlockEntityRenderer<EtherealBloomBlockEntity> {
    private final ModelPart seed;
    public EtherealBloomRenderer(BlockEntityRendererProvider.Context c){var mesh=new MeshDefinition();mesh.getRoot().addOrReplaceChild("seed",CubeListBuilder.create().texOffs(0,0).addBox(-8,-8,-8,16,16,16),PartPose.ZERO);seed=LayerDefinition.create(mesh,64,32).bakeRoot().getChild("seed");}
    @Override public void render(EtherealBloomBlockEntity tile,float partial,PoseStack pose,MultiBufferSource buffers,int light,int overlay){
        float t=Math.min(100,tile.growth+partial),s1=t/100,s2=Math.min(50,t)/60+.1666666f,s3=Math.clamp((t-33)/33,0,1),s4=Math.clamp((t-66)/33,0,1)*.7f;
        leaves(pose,buffers,.25f,s3,s1,0,"purifier_leaves");leaves(pose,buffers,.6f,s4,s1*.7f,45,"purifier_leaves");leaves(pose,buffers,.5f-(1-s1)/2,s2,s1,0,"purifier_stalk");
        pose.pushPose();pose.translate(.5-s4/8,s1-s4/6,.5-s4/8);pose.scale(s4/4,s4/3,s4/4);seed.render(pose,buffers.getBuffer(RenderType.entityTranslucent(ResourceLocation.parse("thaumcraft:textures/models/crystalcapacitor.png"))),LightTexture.FULL_BRIGHT,overlay);pose.popPose();
        pose.pushPose();pose.translate(.5,s1,.5);pose.mulPose(Minecraft.getInstance().gameRenderer.getMainCamera().rotation());var texture=ResourceLocation.parse("thaumcraft:textures/misc/nodes.png");int frame=(int)(System.nanoTime()/40000000%32);quad(pose,buffers.getBuffer(ThaumcraftRenderTypes.node(texture,true,false)),s1*.5f,s1*.5f,frame/32f,6/32f,(frame+1)/32f,7/32f,0xffaaddff);pose.popPose();
    }
    private static void leaves(PoseStack pose,MultiBufferSource buffers,float height,float width,float size,float angle,String texture){for(int i=0;i<4;i++){pose.pushPose();pose.translate(.5,height,.5);pose.mulPose(Axis.YP.rotationDegrees(angle+90*i));quad(pose,buffers.getBuffer(RenderType.entityTranslucent(ResourceLocation.parse("thaumcraft:textures/blocks/"+texture+".png"))),width*.5f,size*.5f,0,0,1,1,0xffffffff);pose.popPose();}}
    private static void quad(PoseStack pose,VertexConsumer vertices,float x,float y,float u0,float v0,float u1,float v1,int color){var p=pose.last();for(int i=0;i<4;i++)vertices.addVertex(p.pose(),i<2?-x:x,i==0||i==3?-y:y,0).setColor(color).setUv(i<2?u1:u0,i==0||i==3?v1:v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(p,0,0,-1);}
}
