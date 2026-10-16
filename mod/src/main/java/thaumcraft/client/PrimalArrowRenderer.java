package thaumcraft.client;

import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import thaumcraft.entities.PrimalArrow;

/** Original additive arrow mesh and 4x4 animated elemental halo. */
public final class PrimalArrowRenderer extends EntityRenderer<PrimalArrow> {
    private static final ResourceLocation ARROW=ResourceLocation.parse("thaumcraft:textures/misc/minecraft_arrow_1_7_10.png"),WISP=ResourceLocation.parse("thaumcraft:textures/misc/wisp.png");
    private static int haloTileSize;
    private static final int[] COLORS={0xffff7e,16727041,37119,40960,0xeeccff,0x555577};
    public PrimalArrowRenderer(EntityRendererProvider.Context c){super(c);}
    public static void reload(){haloTileSize=0;}
    private static float haloExtent(){
        if(haloTileSize==0){
            try(var stream=net.minecraft.client.Minecraft.getInstance().getResourceManager().getResourceOrThrow(WISP).open();var image=com.mojang.blaze3d.platform.NativeImage.read(stream)){haloTileSize=Math.max(1,image.getWidth()/64);}
            catch(java.io.IOException exception){haloTileSize=16;} // Original UtilsFX fallback.
        }
        return (haloTileSize-.01f)/(haloTileSize*4f);
    }
    @Override public ResourceLocation getTextureLocation(PrimalArrow arrow){return ARROW;}
    @Override public void render(PrimalArrow a,float yaw,float partial,PoseStack p,MultiBufferSource buffers,int light){float alpha=Math.max(0,(100-a.embeddedTicks())/100f);int white=((int)(alpha*255)<<24)|0xffffff;p.pushPose();p.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partial,a.yRotO,a.getYRot())-90));p.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(partial,a.xRotO,a.getXRot())));float shake=a.shakeTime-partial;if(shake>0)p.mulPose(Axis.ZP.rotationDegrees(-Mth.sin(shake*3)*shake));p.mulPose(Axis.XP.rotationDegrees(45));p.scale(.05625f,.05625f,.05625f);p.translate(-4,0,0);var b=buffers.getBuffer(ThaumcraftRenderTypes.projectile(ARROW));
        vertex(b,p,-7,-2,-2,0,.15625f,white,light,1,0,0);vertex(b,p,-7,-2,2,.15625f,.15625f,white,light,1,0,0);vertex(b,p,-7,2,2,.15625f,.3125f,white,light,1,0,0);vertex(b,p,-7,2,-2,0,.3125f,white,light,1,0,0);
        vertex(b,p,-7,2,-2,0,.15625f,white,light,-1,0,0);vertex(b,p,-7,2,2,.15625f,.15625f,white,light,-1,0,0);vertex(b,p,-7,-2,2,.15625f,.3125f,white,light,-1,0,0);vertex(b,p,-7,-2,-2,0,.3125f,white,light,-1,0,0);
        for(int i=0;i<4;i++){p.mulPose(Axis.XP.rotationDegrees(90));vertex(b,p,-8,-2,0,0,0,white,light,0,0,1);vertex(b,p,8,-2,0,.5f,0,white,light,0,0,1);vertex(b,p,8,2,0,.5f,.15625f,white,light,0,0,1);vertex(b,p,-8,2,0,0,.15625f,white,light,0,0,1);}p.popPose();
        p.pushPose();p.mulPose(entityRenderDispatcher.cameraOrientation());int frame=a.tickCount%16,color=((int)(alpha*255)<<24)|COLORS[a.element()];float u=frame%4*.25f,v=frame/4*.25f,extent=haloExtent();var halo=buffers.getBuffer(ThaumcraftRenderTypes.node(WISP,a.element()<5,false));int fullBright=LightTexture.FULL_BRIGHT;vertex(halo,p,-.5f,-.5f,0,u+extent,v+extent,color,fullBright,0,0,1);vertex(halo,p,-.5f,.5f,0,u+extent,v,color,fullBright,0,0,1);vertex(halo,p,.5f,.5f,0,u,v,color,fullBright,0,0,1);vertex(halo,p,.5f,-.5f,0,u,v+extent,color,fullBright,0,0,1);p.popPose();super.render(a,yaw,partial,p,buffers,light);
    }
    private static void vertex(VertexConsumer v,PoseStack p,float x,float y,float z,float u,float t,int color,int light,float nx,float ny,float nz){v.addVertex(p.last(),x,y,z).setColor(color).setUv(u,t).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(p.last(),nx,ny,nz);}
}
