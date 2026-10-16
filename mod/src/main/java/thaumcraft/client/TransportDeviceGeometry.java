package thaumcraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.*;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import thaumcraft.production.ProductionBlock;

/** Original OBJ part selection plus the reservoir's separate block-rendered glass. */
final class TransportDeviceGeometry {
    private static ResourceLocation texture(String name){return ResourceLocation.parse("thaumcraft:textures/models/"+name+".png");}
    static void draw(ProductionBlock.Kind kind,Direction facing,PoseStack pose,MultiBufferSource buffers,int light,int overlay,float spin){
        draw(kind,facing,pose,buffers,light,overlay,spin,0,false);
    }
    static void draw(ProductionBlock.Kind kind,Direction facing,PoseStack pose,MultiBufferSource buffers,int light,int overlay,float spin,int support,boolean inventory){
        if(kind==ProductionBlock.Kind.RESERVOIR&&inventory){
            // The original inventory draws blended glass first, with depth writes enabled.
            // Flush it before the OBJ so the inner mesh cannot show through the glass.
            glass(pose,buffers,light,overlay,true);
            if(buffers instanceof MultiBufferSource.BufferSource source)source.endBatch(reservoirInventoryGlass());
        }
        pose.pushPose();
        if(kind==ProductionBlock.Kind.ALEMBIC){
            pose.translate(.5,0,.5);pose.mulPose(Axis.XN.rotationDegrees(90));
            if(inventory)pose.translate(0,0,-.4);
            else pose.mulPose(Axis.ZP.rotationDegrees(switch(facing){case EAST->180;case SOUTH->90;case NORTH->270;default->0;}));
            var mesh=OriginalObjMesh.get("alembic");
            if(support>0)mesh.render("TubeMain",pose,buffers,texture("alembic"),light,overlay,-1,false);
            mesh.render(support==2?"TubeSmall":"Legs",pose,buffers,texture("alembic"),light,overlay,-1,false);
            for(var part:new String[]{"Pot","Panel"})mesh.render(part,pose,buffers,texture("alembic"),light,overlay,-1,false);
        }else{
            pose.translate(.5,.5,.5);
            switch(facing){case DOWN->pose.mulPose(Axis.XN.rotationDegrees(90));case UP->pose.mulPose(Axis.XP.rotationDegrees(90));case SOUTH->pose.mulPose(Axis.YP.rotationDegrees(180));case WEST->pose.mulPose(Axis.YP.rotationDegrees(90));case EAST->pose.mulPose(Axis.YN.rotationDegrees(90));default->{}}
            pose.translate(0,0,-.5);
            if(kind==ProductionBlock.Kind.RESERVOIR)OriginalObjMesh.get("reservoir").renderAll(pose,buffers,texture("reservoir"),light,overlay,-1,false);
            else if(kind==ProductionBlock.Kind.CRYSTALLIZER){
                OriginalObjMesh.get("crystalizer").renderAll(pose,buffers,texture("crystalizer"),light,overlay,-1,false);
                pose.scale(.75f,.75f,.75f);
                for(int i=0;i<4;i++){pose.pushPose();pose.mulPose(Axis.ZP.rotationDegrees(90*i));pose.translate(.34,0,1.2125);pose.mulPose(Axis.ZP.rotationDegrees(spin));OriginalObjMesh.get("vis_relay").render("Crystal",pose,buffers,texture("vis_relay"),LightTexture.pack(13,0),overlay,-1,false);pose.popPose();}
            }
        }
        pose.popPose();
        if(kind==ProductionBlock.Kind.RESERVOIR&&!inventory)glass(pose,buffers,light,overlay,false);
    }
    private static RenderType reservoirInventoryGlass(){return ThaumcraftRenderTypes.reservoirInventoryGlass();}
    private static void glass(PoseStack pose,MultiBufferSource buffers,int light,int overlay,boolean inventory){
        var texture=ResourceLocation.parse("thaumcraft:textures/blocks/essentiareservoir.png");
        float a=.125f,b=.875f;
        float[][][] faces={{{a,a,a},{a,b,a},{b,b,a},{b,a,a}},{{b,a,b},{b,b,b},{a,b,b},{a,a,b}},{{a,a,b},{a,b,b},{a,b,a},{a,a,a}},{{b,a,a},{b,b,a},{b,b,b},{b,a,b}},{{a,b,a},{a,b,b},{b,b,b},{b,b,a}},{{a,a,b},{a,a,a},{b,a,a},{b,a,b}}};
        float[][] normals={{0,0,-1},{0,0,1},{-1,0,0},{1,0,0},{0,1,0},{0,-1,0}};
        for(int side=0;side<6;side++){
            var type=inventory?reservoirInventoryGlass():RenderType.entityTranslucent(texture);
            var out=buffers.getBuffer(type);
            float shade=inventory?(side==4?1:side==5?.5f:.7f):1;
            // RenderBlocks crops UVs to the 2/16..14/16 inset cube rather than stretching them.
            for(int i=0;i<4;i++){var p=faces[side][i];var n=normals[side];
                out.addVertex(pose.last(),p[0],p[1],p[2]).setColor(shade,shade,shade,1).setUv(i<2?a:b,i==0||i==3?b:a).setOverlay(overlay).setLight(light).setNormal(pose.last(),n[0],n[1],n[2]);}
        }
    }
}
