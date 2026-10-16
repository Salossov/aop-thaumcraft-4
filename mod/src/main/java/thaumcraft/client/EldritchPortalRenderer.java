package thaumcraft.client;

import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector3f;
import thaumcraft.blocks.EldritchPortalBlockEntity;

/** Original sixteen-frame billboard, opening across five / thirty client ticks. */
public final class EldritchPortalRenderer implements BlockEntityRenderer<EldritchPortalBlockEntity> {
    private static final RenderType PORTAL=RenderType.create("thaumcraft_eldritch_portal",DefaultVertexFormat.PARTICLE,VertexFormat.Mode.QUADS,256,false,true,RenderType.CompositeState.builder().setShaderState(new RenderStateShard.ShaderStateShard(GameRenderer::getParticleShader)).setTextureState(new RenderStateShard.TextureStateShard(ResourceLocation.parse("thaumcraft:textures/misc/eldritch_portal.png"),false,false)).setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY).setLightmapState(RenderStateShard.LIGHTMAP).setCullState(RenderStateShard.NO_CULL).setWriteMaskState(RenderStateShard.COLOR_WRITE).createCompositeState(false));
    public EldritchPortalRenderer(BlockEntityRendererProvider.Context context){}
    @Override public int getViewDistance(){return 96;}
    @Override public net.minecraft.world.phys.AABB getRenderBoundingBox(EldritchPortalBlockEntity tile){return tile.renderBounds();}
    @Override public void render(EldritchPortalBlockEntity tile,float partial,PoseStack pose,MultiBufferSource buffers,int light,int overlay){
        if(!(Minecraft.getInstance().getCameraEntity() instanceof net.minecraft.world.entity.player.Player))return;
        draw(tile,partial,pose,buffers,Minecraft.getInstance().gameRenderer.getMainCamera().rotation(),System.nanoTime());
    }
    static void draw(EldritchPortalBlockEntity tile,float partial,PoseStack pose,MultiBufferSource buffers,org.joml.Quaternionf camera,long nanoTime){
        float horizontal=(int)Math.min(5,tile.openCount+partial)/5f,vertical=(int)Math.min(30,tile.openCount+partial)/30f;
        int frame=(int)(nanoTime/50_000_000L%16);float u=frame/16f;
        var consumer=buffers.getBuffer(PORTAL);var matrix=pose.last();
        float[][] corners={{-1,-1},{-1,1},{1,1},{1,-1}};float[][] uv={{u,1},{u+.0625f,1},{u+.0625f,0},{u,0}};
        for(int i=0;i<4;i++){var vertex=new Vector3f(corners[i][0],corners[i][1],0).rotate(camera);vertex.mul(horizontal,vertical,horizontal).add(.5f,.5f,.5f);consumer.addVertex(matrix,vertex.x,vertex.y,vertex.z).setColor(0xffffffff).setUv(uv[i][0],uv[i][1]).setLight(220);}
    }
}
