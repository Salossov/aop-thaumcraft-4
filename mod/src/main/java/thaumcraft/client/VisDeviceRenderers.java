package thaumcraft.client;

import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import thaumcraft.registry.R3Content;
import thaumcraft.visnet.*;

final class VisDeviceRenderers {
    private static ResourceLocation texture(String name){return ResourceLocation.parse("thaumcraft:textures/models/"+name+".png");}
    private static float ticks(float partial){return (Minecraft.getInstance().player==null?0:Minecraft.getInstance().player.tickCount)+partial;}
    static final class Stabilizer implements BlockEntityRenderer<NodeStabilizerBlockEntity> {
        Stabilizer(BlockEntityRendererProvider.Context context){}
        @Override public boolean shouldRenderOffScreen(NodeStabilizerBlockEntity tile){return true;}
        @Override public void render(NodeStabilizerBlockEntity tile,float partial,PoseStack pose,MultiBufferSource buffers,int light,int overlay) {
            var mesh=OriginalObjMesh.get("node_stabilizer");boolean advanced=tile.getBlockState().is(R3Content.ADVANCED_NODE_STABILIZER.get());int color=advanced?0xffff3333:0xffffffff;
            pose.pushPose();pose.translate(.5,0,.5);pose.mulPose(Axis.XN.rotationDegrees(90));
            mesh.render("lock",pose,buffers,texture("node_stabilizer"),light,overlay,0xffffffff,false);
            for(int i=0;i<4;i++) {pose.pushPose();pose.mulPose(Axis.ZP.rotationDegrees(i*90));pose.mulPose(Axis.YP.rotationDegrees(45));pose.translate(0,0,tile.extension(partial)/100f);mesh.render("piston",pose,buffers,texture("node_stabilizer"),light,overlay,0xffffffff,false);mesh.render("piston",pose,buffers,texture("node_stabilizer_over"),stabilizerLight(tile.extension(partial),ticks(partial)+i*5),overlay,color,false);pose.popPose();}
            pose.popPose();
            if(tile.extension()>0)billboard(pose,buffers,ResourceLocation.parse("thaumcraft:textures/misc/node_bubble.png"),new Vec3(.5,1.5,.5),.9f,tile.extension(partial)/37f*(Mth.sin(ticks(partial)/8)*.1f+.5f),advanced?0xff4444:0xffffff,0,1);
        }
    }
    static final class Converter implements BlockEntityRenderer<NodeConverterBlockEntity> {
        Converter(BlockEntityRendererProvider.Context context){}
        @Override public boolean shouldRenderOffScreen(NodeConverterBlockEntity tile){return true;}
        @Override public void render(NodeConverterBlockEntity tile,float partial,PoseStack pose,MultiBufferSource buffers,int light,int overlay) {
            var mesh=OriginalObjMesh.get("node_stabilizer");int color=switch(tile.status()){case 2->0xffff004c;case 1->0xffff9919;default->0xff80ff80;};
            float extension=Math.min(50,tile.progress(partial))/137f;
            pose.pushPose();pose.translate(.5,1,.5);pose.mulPose(Axis.XP.rotationDegrees(90));
            mesh.render("lock",pose,buffers,texture("node_converter"),light,overlay,0xffffffff,false);mesh.render("lock",pose,buffers,texture("node_converter_over"),converterLight(extension,ticks(partial)),overlay,color,false);
            for(int i=0;i<4;i++){pose.pushPose();pose.mulPose(Axis.ZP.rotationDegrees(i*90));pose.mulPose(Axis.YP.rotationDegrees(45));pose.translate(0,0,extension);mesh.render("piston",pose,buffers,texture("node_converter"),light,overlay,0xffffffff,false);mesh.render("piston",pose,buffers,texture("node_converter_over"),converterLight(extension,ticks(partial)+i*5),overlay,color,false);pose.popPose();}
            pose.popPose();
        }
    }
    static final class Relay implements BlockEntityRenderer<VisRelayBlockEntity> {
        Relay(BlockEntityRendererProvider.Context context){}
        @Override public boolean shouldRenderOffScreen(VisRelayBlockEntity tile){return true;}
        @Override public int getViewDistance(){return 64;}
        @Override public void render(VisRelayBlockEntity tile,float partial,PoseStack pose,MultiBufferSource buffers,int light,int overlay) {
            var mesh=OriginalObjMesh.get("vis_relay");var facing=tile.getBlockState().getValue(VisDeviceBlock.FACING);float time=ticks(partial);int crystalLight=(tile.getParent()!=null?50:0)+(int)(150*(Math.sin(time/2)*.05+.95));
            if(((VisDeviceBlock)tile.getBlockState().getBlock()).kind==VisDeviceBlock.Kind.CHARGER){
                pose.pushPose();pose.translate(.5,.5,.5);pose.mulPose(Axis.XP.rotationDegrees(90));pose.mulPose(Axis.ZP.rotationDegrees(45));
                mesh.render("RingFloat",pose,buffers,texture("vis_relay"),light,overlay,0xffffffff,false);
                pose.pushPose();pose.mulPose(Axis.XP.rotationDegrees(180));pose.translate(0,0,.5);for(int i=0;i<4;i++){mesh.render("Support",pose,buffers,texture("vis_relay"),light,overlay,0xffffffff,false);pose.mulPose(Axis.ZP.rotationDegrees(90));}pose.popPose();
                mesh.render("Crystal",pose,buffers,texture("vis_relay"),crystalLight,overlay,0xff000000|(tile.getAttunement()<0?0xfff5d0:VisRelayBlockEntity.attunedAspect(tile.getAttunement()).getColor()),false);pose.popPose();connection(tile,pose,buffers,time);return;
            }
            pose.pushPose();pose.translate(.5,.5,.5);orient(pose,facing);pose.mulPose(Axis.XP.rotationDegrees(180));pose.mulPose(Axis.ZP.rotationDegrees(45));
            pose.pushPose();pose.scale(.75f,.75f,.75f);pose.translate(0,0,-.16);mesh.render("RingBase",pose,buffers,texture("vis_relay"),light,overlay,0xffffffff,false);pose.popPose();
            mesh.render("RingFloat",pose,buffers,texture("vis_relay"),light,overlay,0xffffffff,false);
            int color=tile.getAttunement()<0?0xfff5d0:VisRelayBlockEntity.attunedAspect(tile.getAttunement()).getColor();
            mesh.render("Crystal",pose,buffers,texture("vis_relay"),crystalLight,overlay,0xff000000|color,false);pose.popPose();
            connection(tile,pose,buffers,time);
        }
    }
    private static int stabilizerLight(float extension,float time){return LightTexture.pack(Math.clamp((int)((50+170*extension/37f*(Mth.sin(time/3)*.1f+.9f))/16),0,15),0);}
    private static int converterLight(float extension,float time){return LightTexture.pack(Math.clamp((int)((50+170*extension*2.5f*(Mth.sin(time/3)*.1f+.9f))/16),0,15),0);}
    private static void connection(VisRelayBlockEntity tile,PoseStack pose,MultiBufferSource buffers,float time){
        var parent=tile.getLevel()==null?null:tile.getParent();if(parent!=null){var end=parent.getBlockPos().getCenter().subtract(Vec3.atLowerCornerOf(tile.getBlockPos()));int tint=tile.isPulsing()?tile.pulseColor():0xffffff;beam(pose,buffers,new Vec3(.5,.5,.5),end,time,tint,tile.isPulsing()?.065f:.025f,.7f);}
    }
    static final class Energized implements BlockEntityRenderer<EnergizedNodeBlockEntity> {
        Energized(BlockEntityRendererProvider.Context context){}
        @Override public int getViewDistance(){return 64;}
        @Override public boolean shouldRenderOffScreen(EnergizedNodeBlockEntity tile){return true;}
        @Override public void render(EnergizedNodeBlockEntity tile,float partial,PoseStack pose,MultiBufferSource buffers,int light,int overlay) {
            AuraNodeRenderer.renderAura(tile.getBlockPos(),tile.aura(),tile.nodeType(),tile.modifier(),true,pose,buffers);
            int frame=(int)Math.floorMod(System.nanoTime()/40000000L+tile.getBlockPos().getX(),16);
            billboardHorizontal(pose,buffers,ResourceLocation.parse("thaumcraft:textures/items/lightningringv.png"),new Vec3(.5,.5,.5),.33f,.9f,0xffffff,frame,16);
        }
    }
    static void orient(PoseStack pose,Direction facing){switch(facing){case DOWN->pose.mulPose(Axis.XN.rotationDegrees(90));case UP->pose.mulPose(Axis.XP.rotationDegrees(90));case SOUTH->pose.mulPose(Axis.YP.rotationDegrees(180));case WEST->pose.mulPose(Axis.YP.rotationDegrees(90));case EAST->pose.mulPose(Axis.YN.rotationDegrees(90));default->{}}}
    static void beam(PoseStack pose,MultiBufferSource buffers,Vec3 source,Vec3 target,float ticks,int color,float width,float opacity) {
        var vertices=buffers.getBuffer(ThaumcraftRenderTypes.unlit(ResourceLocation.parse("thaumcraft:textures/misc/wispy.png"),true,false));var delta=target.subtract(source);int segments=Math.max(8,(int)(delta.length()*16));Vec3 previous=source;
        for(int i=1;i<=segments;i++){float f=i/(float)segments;float amplitude=Mth.sin(f*Mth.PI)*.05f;var current=source.add(delta.scale(f)).add(Mth.sin(ticks/4f+f*18)*amplitude,Mth.sin(ticks/3f+f*15)*amplitude,Mth.cos(ticks/5f+f*12)*amplitude);float u=f*(float)delta.length()+ticks*.04f,pu=(i-1f)/segments*(float)delta.length()+ticks*.04f;
            for(var cross:new Vec3[]{new Vec3(width,0,0),new Vec3(0,width,0)}){vertex(vertices,pose,previous.subtract(cross),pu,1,color,opacity);vertex(vertices,pose,previous.add(cross),pu,0,color,opacity);vertex(vertices,pose,current.add(cross),u,0,color,opacity);vertex(vertices,pose,current.subtract(cross),u,1,color,opacity);}previous=current;}
    }
    static void billboard(PoseStack pose,MultiBufferSource buffers,ResourceLocation texture,Vec3 center,float size,float alpha,int color,int frame,int frames) {
        pose.pushPose();pose.translate(center.x,center.y,center.z);pose.mulPose(Minecraft.getInstance().gameRenderer.getMainCamera().rotation());var vertices=buffers.getBuffer(ThaumcraftRenderTypes.unlit(texture,true,false));
        float v=frame/(float)frames,end=(frame+1f)/frames;vertex(vertices,pose,new Vec3(-size,-size,0),0,end,color,alpha);vertex(vertices,pose,new Vec3(size,-size,0),1,end,color,alpha);vertex(vertices,pose,new Vec3(size,size,0),1,v,color,alpha);vertex(vertices,pose,new Vec3(-size,size,0),0,v,color,alpha);pose.popPose();
    }
    private static void billboardHorizontal(PoseStack pose,MultiBufferSource buffers,ResourceLocation texture,Vec3 center,float size,float alpha,int color,int frame,int frames){
        pose.pushPose();pose.translate(center.x,center.y,center.z);pose.mulPose(Minecraft.getInstance().gameRenderer.getMainCamera().rotation());var vertices=buffers.getBuffer(ThaumcraftRenderTypes.unlit(texture,true,false));float u=frame/(float)frames,end=(frame+1f)/frames;
        vertex(vertices,pose,new Vec3(-size,-size,0),u,1,color,alpha);vertex(vertices,pose,new Vec3(size,-size,0),end,1,color,alpha);vertex(vertices,pose,new Vec3(size,size,0),end,0,color,alpha);vertex(vertices,pose,new Vec3(-size,size,0),u,0,color,alpha);pose.popPose();
    }
    private static void vertex(VertexConsumer vertices,PoseStack pose,Vec3 point,float u,float v,int color,float alpha){vertices.addVertex(pose.last().pose(),(float)point.x,(float)point.y,(float)point.z).setColor(0xff000000|color).setColor(color>>16&255,color>>8&255,color&255,Math.clamp((int)(alpha*255),0,255)).setUv(u,v);}
    private VisDeviceRenderers(){}
}
