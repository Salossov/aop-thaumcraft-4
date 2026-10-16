package thaumcraft.client;

import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import thaumcraft.production.*;

public final class MirrorRenderer implements BlockEntityRenderer<MirrorBlockEntity> {
    private static final ResourceLocation PANE=ResearchGraphics.texture("blocks/mirrorpane"),TRANSPARENT=ResearchGraphics.texture("blocks/mirrorpanetrans");
    private static final RenderType PORTAL=RenderType.create("thaumcraft_mirror",DefaultVertexFormat.POSITION,VertexFormat.Mode.QUADS,1536,false,false,RenderType.CompositeState.builder().setShaderState(RenderStateShard.RENDERTYPE_END_PORTAL_SHADER).setTextureState(RenderStateShard.MultiTextureStateShard.builder().add(ResearchGraphics.texture("misc/tunnel"),false,false).add(ResearchGraphics.texture("misc/particlefield"),false,false).build()).createCompositeState(false));
    public MirrorRenderer(BlockEntityRendererProvider.Context context){}
    @Override public void render(MirrorBlockEntity tile,float partial,PoseStack pose,MultiBufferSource buffers,int light,int overlay){
        var face=tile.getBlockState().getValue(MirrorBlock.FACING);var centre=net.minecraft.world.phys.Vec3.atLowerCornerOf(face.getNormal()).scale(-.4375).add(.5,.5,.5);
        pose.pushPose();pose.translate(centre.x,centre.y,centre.z);
        switch(face){case UP->pose.mulPose(Axis.XP.rotationDegrees(-90));case DOWN->pose.mulPose(Axis.XP.rotationDegrees(90));case NORTH->pose.mulPose(Axis.YP.rotationDegrees(180));case EAST->pose.mulPose(Axis.YP.rotationDegrees(90));case WEST->pose.mulPose(Axis.YP.rotationDegrees(-90));default->{}}
        Minecraft.getInstance().getItemRenderer().renderStatic(new ItemStack(tile.getBlockState().getBlock()),ItemDisplayContext.NONE,light,overlay,pose,buffers,tile.getLevel(),0);
        var matrix=pose.last().pose();
        if(tile.linked()){var portal=buffers.getBuffer(PORTAL);portal.addVertex(matrix,-.3125f,-.3125f,.041f);portal.addVertex(matrix,.3125f,-.3125f,.041f);portal.addVertex(matrix,.3125f,.3125f,.041f);portal.addVertex(matrix,-.3125f,.3125f,.041f);}
        var pane=buffers.getBuffer(RenderType.entityTranslucent(tile.linked()?TRANSPARENT:PANE));float z=.042f;
        vertex(pane,pose,-.3125f,-.3125f,z,0,1,light,overlay);vertex(pane,pose,.3125f,-.3125f,z,1,1,light,overlay);vertex(pane,pose,.3125f,.3125f,z,1,0,light,overlay);vertex(pane,pose,-.3125f,.3125f,z,0,0,light,overlay);
        pose.popPose();
    }
    private static void vertex(VertexConsumer buffer,PoseStack pose,float x,float y,float z,float u,float v,int light,int overlay){buffer.addVertex(pose.last().pose(),x,y,z).setColor(255,255,255,255).setUv(u,v).setOverlay(overlay).setLight(light).setNormal(pose.last(),0,0,1);}
}
