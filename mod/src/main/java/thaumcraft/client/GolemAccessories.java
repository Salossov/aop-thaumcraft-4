package thaumcraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.*;
import thaumcraft.entities.ThaumicGolem;

public final class GolemAccessories {
    public static final ModelLayerLocation LAYER=new ModelLayerLocation(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("thaumcraft","golem"),"accessories");
    private final ModelPart root;
    private static final String[] NAMES={"fez","plate","plate_left","plate_right","hat","glasses","visor","rim","dart","mace","bowtie","jar","brain","evil_head"};
    private static final String[] DECO={"F","P","P","P","H","G","V","H","R","M","B","","",""};
    public GolemAccessories(ModelPart root){this.root=root;}
    public static LayerDefinition layer(){var mesh=new MeshDefinition();var root=mesh.getRoot();
        part(root,"fez",0,94,-4.5f,-15,-6,9,7,9,true,false);part(root,"plate",32,40,-6.5f,-1,-7,13,12,13,false,false);part(root,"plate_left",0,44,-8.5f,-4,-6.5f,3,6,12,false,false);part(root,"plate_right",0,44,5.5f,-4,-6.5f,3,6,12,false,true);part(root,"hat",0,110,-4.5f,-17,-6,9,9,9,true,false);part(root,"glasses",0,80,-4.5f,-8,-6,9,4,9,true,false);part(root,"visor",0,70,-5,-8,-6,10,5,5,true,false);part(root,"rim",36,114,-6.5f,-9,-8,13,1,13,true,false);part(root,"dart",80,80,7.9f,7.5f,-3.5f,6,16,7,false,false);part(root,"mace",80,26,-13,15,-5,6,8,10,false,false);part(root,"bowtie",0,0,-8.5f,-2,-6.5f,17,4,12,false,false);part(root,"jar",96,56,-4,-15,-5.5f,8,4,8,true,false);part(root,"brain",96,70,-3.5f,-14,-5,7,3,7,true,false);part(root,"evil_head",64,65,-4,-9,-5.5f,8,7,8,true,false);return LayerDefinition.create(mesh,128,128);
    }
    private static void part(PartDefinition root,String name,int u,int v,float x,float y,float z,float width,float height,float depth,boolean head,boolean mirror){var cubes=CubeListBuilder.create().texOffs(u,v);if(mirror)cubes.mirror();root.addOrReplaceChild(name,cubes.addBox(x,y,z,width,height,depth),PartPose.offset(0,30,head?-2:0));}
    public void render(ThaumicGolem golem,GolemModel model,PoseStack pose,MultiBufferSource buffers,int light,int overlay){pose.pushPose();pose.scale(.4f,.4f,.4f);for(int i=0;i<NAMES.length;i++){if(i<11&&!golem.decoration().contains(DECO[i])||i>=11&&!golem.advanced()||i==13&&golem.core()<0)continue;var part=root.getChild(NAMES[i]);part.resetPose();var base=i==8?model.leftArm:i==9?model.rightArm:i==1||i==2||i==3||i==10?model.body:model.head;part.xRot=base.xRot;part.yRot=base.yRot;part.zRot=base.zRot;part.render(pose,buffers.getBuffer(RenderType.entityTranslucent(ResearchGraphics.texture("models/golem_decoration"))),light,overlay);}pose.popPose();}
}
