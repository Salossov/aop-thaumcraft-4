package thaumcraft.client.model;

import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.*;
import net.minecraft.util.Mth;
import thaumcraft.entities.TaintSpore;

public final class SporeModel extends HierarchicalModel<TaintSpore> {
    private final ModelPart root,cube,base;private final boolean swarmer;private float size,pulse;
    public SporeModel(ModelPart root,boolean swarmer) {super(RenderType::entityTranslucent);this.root=root;this.swarmer=swarmer;cube=root.getChild("cube");base=swarmer?root.getChild("base"):null;}
    public static LayerDefinition create(boolean swarmer) {
        var mesh=new MeshDefinition();var root=mesh.getRoot();
        if(swarmer) {root.addOrReplaceChild("cube",CubeListBuilder.create().texOffs(0,0).addBox(-8,0,-8,16,16,16),PartPose.ZERO);root.addOrReplaceChild("base",CubeListBuilder.create().texOffs(0,32).addBox(-8,-8,-8,16,16,16),PartPose.offset(0,16,0));}
        else root.addOrReplaceChild("cube",CubeListBuilder.create().texOffs(0,0).addBox(-6,2,-6,12,12,12).addBox(-8,0,-8,16,16,16),PartPose.offset(0,24,0));
        return LayerDefinition.create(mesh,64,64);
    }
    @Override public ModelPart root(){return root;}
    @Override public void setupAnim(TaintSpore entity,float walk,float amount,float age,float yaw,float pitch) {float intensity=entity.hurtTime>0?0.04f:0.02f;cube.xRot=intensity*Mth.sin(age*0.05f);cube.zRot=intensity*Mth.sin(age*0.1f);size=entity.displaySize;pulse=0.025f*Mth.sin(entity.tickCount*0.075f);}
    @Override public void renderToBuffer(PoseStack pose,VertexConsumer vertices,int light,int overlay,int color) {if(!swarmer){super.renderToBuffer(pose,vertices,light,overlay,color);return;}pose.pushPose();pose.translate(0,1.6,0);pose.scale(-0.07f*size-pulse,-0.07f*size+pulse,-0.07f*size-pulse);pose.translate(0,-(-0.07f*size+pulse)/2,0);cube.render(pose,vertices,LightTexture.FULL_BRIGHT,overlay,color);pose.popPose();base.render(pose,vertices,light,overlay,color);}
}
