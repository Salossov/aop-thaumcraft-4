package thaumcraft.client;

import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import thaumcraft.entities.CultistPortal;

/** Source sixteen-frame billboard, damage wobble and event16 pulse. */
public final class CultistPortalRenderer extends EntityRenderer<CultistPortal> {
    private static final ResourceLocation TEXTURE=ResourceLocation.parse("thaumcraft:textures/misc/cultist_portal.png");
    public CultistPortalRenderer(EntityRendererProvider.Context context){super(context);shadowRadius=.1f;}
    @Override public ResourceLocation getTextureLocation(CultistPortal entity){return TEXTURE;}
    public record Shape(float horizontal,float vertical,float alpha,int frame){}
    public static Shape shape(int ticks,int hurt,int pulse,float health,float max,float partial,long nanos){float height=1.5f;int e=(int)Math.min(50,ticks+partial);if(hurt>0){double d=Math.sin(hurt*72*Math.PI/180);height=(float)(height-d/4);e=(int)(e+6*d);}if(pulse>0){double d=Math.sin(pulse*36*Math.PI/180);height=(float)(height+d/4);e=(int)(e+12*d);}float width=e/50f*1.3f,m=(1-health/max)/3f,bob=Mth.sin(ticks/(5-12*m))*m+m,bob2=Mth.sin(ticks/(6-15*m))*m+m;return new Shape(width-bob2/3,height-bob/4,1-bob,15-(int)(nanos/50000000L)%16);}
    @Override public void render(CultistPortal portal,float yaw,float partial,PoseStack pose,MultiBufferSource buffers,int light){var shape=shape(portal.tickCount,portal.hurtTime,portal.pulse,portal.getHealth(),portal.getMaxHealth(),partial,System.nanoTime());pose.pushPose();pose.translate(0,portal.getBbHeight()/2f,0);var camera=entityRenderDispatcher.camera;float yawR=camera.getYRot()*(float)Math.PI/180,pitchR=camera.getXRot()*(float)Math.PI/180;float x=Mth.cos(yawR),z=Mth.sin(yawR),yz=-z*Mth.sin(pitchR),xy=x*Mth.sin(pitchR),xz=Mth.cos(pitchR);float[][] corners={{-x-yz,-xz,-z-xy},{-x+yz,xz,-z+xy},{x+yz,xz,z+xy},{x-yz,-xz,z-xy}};float u=shape.frame/16f;float[][] uv={{u+.0625f,0},{u+.0625f,1},{u,1},{u,0}};var vertices=buffers.getBuffer(ThaumcraftRenderTypes.invisibleCrabOverlay(TEXTURE));for(int i=0;i<4;i++)vertices.addVertex(pose.last(),corners[i][0]*shape.horizontal,corners[i][1]*shape.vertical,corners[i][2]*shape.horizontal).setColor(1f,1f,1f,shape.alpha).setUv(uv[i][0],uv[i][1]).setOverlay(OverlayTexture.NO_OVERLAY).setLight(220).setNormal(pose.last(),0,0,-1);pose.popPose();super.render(portal,yaw,partial,pose,buffers,light);}
}
