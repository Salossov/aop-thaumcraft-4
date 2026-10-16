package thaumcraft.client;

import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import thaumcraft.entities.ThaumicGolem;

public final class GolemModel extends EntityModel<ThaumicGolem> {
    public static final ModelLayerLocation LAYER=new ModelLayerLocation(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("thaumcraft","golem"),"main");
    public final ModelPart root,head,body,rightArm,leftArm,rightLeg,leftLeg;
    public GolemModel(ModelPart root){this.root=root;head=root.getChild("head");body=root.getChild("body");rightArm=root.getChild("right_arm");leftArm=root.getChild("left_arm");rightLeg=root.getChild("right_leg");leftLeg=root.getChild("left_leg");}
    public static LayerDefinition layer(){var mesh=new MeshDefinition();var root=mesh.getRoot();root.addOrReplaceChild("head",CubeListBuilder.create().texOffs(0,0).addBox(-4,-11,-5.5f,8,9,8),PartPose.offset(0,30,-2));root.addOrReplaceChild("body",CubeListBuilder.create().texOffs(0,40).addBox(-8,-2,-6,16,12,11).texOffs(0,70).addBox(-4.5f,10,-3,9,5,6,new CubeDeformation(.5f)),PartPose.offset(0,30,0));root.addOrReplaceChild("right_arm",CubeListBuilder.create().texOffs(60,21).addBox(-12,-2.5f,-3,4,25,6),PartPose.offset(0,30,0));root.addOrReplaceChild("left_arm",CubeListBuilder.create().texOffs(60,21).mirror().addBox(8,-2.5f,-3,4,25,6),PartPose.offset(0,30,0));root.addOrReplaceChild("right_leg",CubeListBuilder.create().texOffs(37,0).addBox(-3.5f,-3,-3,6,16,5),PartPose.offset(-4,48,0));root.addOrReplaceChild("left_leg",CubeListBuilder.create().texOffs(37,0).mirror().addBox(-3.5f,-3,-3,6,16,5),PartPose.offset(5,48,0));return LayerDefinition.create(mesh,128,128);}
    private static float wave(float phase,float period){return (Math.abs(phase%period-period*.5f)-period*.25f)/(period*.25f);}
    @Override public void setupAnim(ThaumicGolem golem,float swing,float amount,float age,float yaw,float pitch){root.getAllParts().forEach(ModelPart::resetPose);head.yRot=yaw*(float)Math.PI/180;head.xRot=pitch*(float)Math.PI/180;if(golem.core()<0){head.yRot=0;head.xRot=.57595867f;return;}rightLeg.xRot=-1.5f*wave(swing,13)*amount;leftLeg.xRot=1.5f*wave(swing,13)*amount;if(golem.action()>0){rightArm.xRot=leftArm.xRot=-2+1.5f*wave(3-Math.abs(Math.max(0,golem.action()-(age-golem.tickCount))-3),5);}else if(golem.core()==5||!golem.displayCarried().isEmpty()){rightArm.xRot=leftArm.xRot=-1;}else{rightArm.xRot=(-.2f+1.5f*wave(swing,13))*amount;leftArm.xRot=(-.2f-1.5f*wave(swing,13))*amount;}}
    @Override public void renderToBuffer(PoseStack pose,VertexConsumer buffer,int light,int overlay,int color){pose.pushPose();pose.scale(.4f,.4f,.4f);root.render(pose,buffer,light,overlay,color);pose.popPose();}
}
