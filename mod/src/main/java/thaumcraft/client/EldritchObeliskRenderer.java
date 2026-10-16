package thaumcraft.client;

import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import thaumcraft.blocks.*;

/** Original three-block floating side panels and two OBJ caps. */
public final class EldritchObeliskRenderer implements BlockEntityRenderer<EldritchObeliskBlockEntity> {
    public EldritchObeliskRenderer(BlockEntityRendererProvider.Context context){}
    static ResourceLocation texture(BlockEntity tile,String name){boolean outer=tile.getLevel()!=null&&tile.getLevel().dimension().location().toString().equals("thaumcraft:outer_lands");return ResourceLocation.parse("thaumcraft:textures/models/obelisk_"+name+(outer?"_2":"")+".png");}
    static void cap(BlockEntity tile,PoseStack pose,MultiBufferSource buffers,int light,int overlay){pose.pushPose();pose.translate(.5,0,.5);pose.mulPose(Axis.XN.rotationDegrees(90));OriginalObjMesh.get("obelisk_cap").render("Cap",pose,buffers,texture(tile,"cap"),light,overlay,-1,false);pose.popPose();}
    @Override public int getViewDistance(){return 96;}
    @Override public net.minecraft.world.phys.AABB getRenderBoundingBox(EldritchObeliskBlockEntity tile){return tile.renderBounds();}
    @Override public void render(EldritchObeliskBlockEntity tile,float partial,PoseStack pose,MultiBufferSource buffers,int light,int overlay){
        var camera=Minecraft.getInstance().getCameraEntity();float age=camera==null?0:camera.tickCount+partial,bob=net.minecraft.util.Mth.sin(age/10)*.1f+.1f;
        boolean near=camera!=null&&camera.distanceToSqr(Vec3.atCenterOf(tile.getBlockPos()))<512;
        pose.pushPose();pose.translate(0,1+bob,0);EldritchVoidRenderer.verticalCore(pose,buffers,near,3);pose.popPose();
        if(tile.getLevel()!=null)light=LevelRenderer.getLightColor(tile.getLevel(),tile.getBlockPos().above(5));
        pose.pushPose();pose.translate(.5,1+bob,.5);
        for(int i=0;i<4;i++){pose.pushPose();pose.mulPose(Axis.YP.rotationDegrees(i*90));pose.translate(0,0,-.5);var vertices=buffers.getBuffer(RenderType.entityTranslucent(texture(tile,"side")));vertex(vertices,pose,-.5f,3,0,0,1,light,overlay);vertex(vertices,pose,.5f,3,0,1,1,light,overlay);vertex(vertices,pose,.5f,0,0,1,0,light,overlay);vertex(vertices,pose,-.5f,0,0,0,0,light,overlay);pose.popPose();}
        pose.pushPose();pose.mulPose(Axis.XP.rotationDegrees(90));OriginalObjMesh.get("obelisk_cap").render("Cap",pose,buffers,texture(tile,"cap"),light,overlay,-1,false);pose.popPose();
        pose.translate(0,3,0);pose.mulPose(Axis.XN.rotationDegrees(90));OriginalObjMesh.get("obelisk_cap").render("Cap",pose,buffers,texture(tile,"cap"),light,overlay,-1,false);pose.popPose();
    }
    private static void vertex(VertexConsumer vertices,PoseStack pose,float x,float y,float z,float u,float v,int light,int overlay){vertices.addVertex(pose.last().pose(),x,y,z).setColor(-1).setUv(u,v).setOverlay(overlay).setLight(light).setNormal(pose.last(),0,0,-1);}
    public static final class Cap implements BlockEntityRenderer<EldritchCapBlockEntity>{public Cap(BlockEntityRendererProvider.Context context){}@Override public int getViewDistance(){return 96;}@Override public void render(EldritchCapBlockEntity tile,float partial,PoseStack pose,MultiBufferSource buffers,int light,int overlay){cap(tile,pose,buffers,light,overlay);}}
}
