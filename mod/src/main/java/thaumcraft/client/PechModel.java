package thaumcraft.client;

import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;
import thaumcraft.entities.Pech;

/** Nine original 128x64 parts, including the pre-addBox mirror state. */
public final class PechModel extends EntityModel<Pech> {
    public final ModelPart body,rightLeg,leftLeg,head,jowls,lowerPack,upperPack,rightArm,leftArm;
    public PechModel(){var root=create().bakeRoot();body=root.getChild("Body");rightLeg=root.getChild("RightLeg");leftLeg=root.getChild("LeftLeg");head=root.getChild("Head");jowls=root.getChild("Jowls");lowerPack=root.getChild("LowerPack");upperPack=root.getChild("UpperPack");rightArm=root.getChild("RightArm");leftArm=root.getChild("LeftArm");}
    public static LayerDefinition create(){var mesh=new MeshDefinition();var root=mesh.getRoot();part(root,"Body",34,12,-3,0,0,6,10,6,0,9,-3,.3129957f,false);part(root,"RightLeg",35,1,-2.9f,0,0,3,6,3,0,18,0,0,true);part(root,"LeftLeg",35,1,-.1f,0,0,3,6,3,0,18,0,0,false);part(root,"Head",2,11,-3.5f,-5,-5,7,5,5,0,8,0,0,false);part(root,"Jowls",1,21,-4,-1,-6,8,3,5,0,8,0,0,false);part(root,"LowerPack",0,0,-5,0,0,10,5,5,0,10,3.5f,.3013602f,false);part(root,"UpperPack",64,1,-7.5f,-14,0,15,14,11,0,10,3,.4537856f,false);part(root,"RightArm",52,2,-2,0,-1,2,6,2,-3,10,-1,0,true);part(root,"LeftArm",52,2,0,0,-1,2,6,2,3,10,-1,0,false);return LayerDefinition.create(mesh,128,64);}
    private static void part(PartDefinition root,String name,int u,int v,float x,float y,float z,float w,float h,float d,float px,float py,float pz,float rot,boolean mirror){root.addOrReplaceChild(name,CubeListBuilder.create().texOffs(u,v).mirror(mirror).addBox(x,y,z,w,h,d),PartPose.offsetAndRotation(px,py,pz,rot,0,0));}
    @Override public void setupAnim(Pech entity,float walk,float amount,float age,float yaw,float pitch){animate(walk,amount,age,yaw,pitch,entity.mumble,riding,entity.isCrouching(),attackTime);}
    public void animate(float walk,float amount,float age,float yaw,float pitch,float mumble,boolean riding,boolean crouching,float attack){head.yRot=yaw/57.295776f;head.xRot=pitch/57.295776f;jowls.yRot=head.yRot;jowls.xRot=head.xRot+(.2617994f+Mth.cos(walk*.6662f)*amount*.25f)+.34906587f*Math.abs(Mth.sin(mumble));rightArm.xRot=Mth.cos(walk*.6662f+(float)Math.PI)*2*amount*.5f;leftArm.xRot=Mth.cos(walk*.6662f)*2*amount*.5f;rightArm.zRot=leftArm.zRot=0;rightLeg.xRot=Mth.cos(walk*.6662f)*1.4f*amount;leftLeg.xRot=Mth.cos(walk*.6662f+(float)Math.PI)*1.4f*amount;rightLeg.yRot=leftLeg.yRot=0;lowerPack.yRot=lowerPack.zRot=Mth.cos(walk*.6662f)*2*amount*.125f;if(riding){rightArm.xRot+=-.62831855f;leftArm.xRot+=-.62831855f;rightLeg.xRot=leftLeg.xRot=-1.2566371f;rightLeg.yRot=.31415927f;leftLeg.yRot=-.31415927f;}rightArm.yRot=leftArm.yRot=0;if(attack>-9990){rightArm.yRot+=body.yRot;leftArm.yRot+=body.yRot;leftArm.xRot+=body.yRot;float phase=1-attack;phase*=phase;phase*=phase;phase=1-phase;float f7=Mth.sin(phase*(float)Math.PI),f8=Mth.sin(attack*(float)Math.PI)*-(head.xRot-.7f)*.75f;rightArm.xRot=(float)((double)rightArm.xRot-((double)f7*1.2+(double)f8));rightArm.yRot+=body.yRot*2;rightArm.zRot=Mth.sin(attack*(float)Math.PI)*-.4f;}if(crouching){rightArm.xRot+=.4f;leftArm.xRot+=.4f;}rightArm.zRot+=Mth.cos(age*.09f)*.05f+.05f;leftArm.zRot-=Mth.cos(age*.09f)*.05f+.05f;rightArm.xRot+=Mth.sin(age*.067f)*.05f;leftArm.xRot-=Mth.sin(age*.067f)*.05f;}
    @Override public void renderToBuffer(PoseStack pose,VertexConsumer vertices,int light,int overlay,int color){for(var part:new ModelPart[]{body,rightLeg,leftLeg,head,jowls,lowerPack,upperPack,rightArm,leftArm})part.render(pose,vertices,light,overlay,color);}
}
