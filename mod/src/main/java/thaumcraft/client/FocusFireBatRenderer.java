package thaumcraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.resources.ResourceLocation;
import thaumcraft.entities.FocusFireBat;

public final class FocusFireBatRenderer extends MobRenderer<FocusFireBat,FocusFireBatRenderer.Model> {
    public static final ModelLayerLocation LAYER=new ModelLayerLocation(ResourceLocation.parse("thaumcraft:firebat"),"main");
    public FocusFireBatRenderer(EntityRendererProvider.Context context){super(context,new Model(context.bakeLayer(LAYER)),.25f);}
    @Override public ResourceLocation getTextureLocation(FocusFireBat entity){return ResearchGraphics.texture(entity.vampire()?"models/vampirebat":"models/firebat");}
    @Override protected void scale(FocusFireBat entity,PoseStack pose,float partial){float scale=entity.devil()||entity.vampire()?.6f:.35f;pose.scale(scale,scale,scale);pose.translate(0,Math.cos((entity.tickCount+partial)*.3)*.1,0);}
    public static LayerDefinition layer(){var mesh=new MeshDefinition();var root=mesh.getRoot();var head=root.addOrReplaceChild("head",CubeListBuilder.create().texOffs(0,0).addBox(-3,-3,-3,6,6,6),PartPose.ZERO);head.addOrReplaceChild("right_ear",CubeListBuilder.create().texOffs(24,0).addBox(-4,-6,-2,3,4,1),PartPose.ZERO);head.addOrReplaceChild("left_ear",CubeListBuilder.create().texOffs(24,0).mirror().addBox(1,-6,-2,3,4,1),PartPose.ZERO);var body=root.addOrReplaceChild("body",CubeListBuilder.create().texOffs(0,16).addBox(-3,4,-3,6,12,6).texOffs(0,34).addBox(-5,16,0,10,6,1),PartPose.ZERO);var right=body.addOrReplaceChild("right_wing",CubeListBuilder.create().texOffs(42,0).addBox(-12,1,1.5f,10,16,1),PartPose.ZERO);right.addOrReplaceChild("right_tip",CubeListBuilder.create().texOffs(24,16).addBox(-8,1,0,8,12,1),PartPose.offset(-12,1,1.5f));var left=body.addOrReplaceChild("left_wing",CubeListBuilder.create().texOffs(42,0).mirror().addBox(2,1,1.5f,10,16,1),PartPose.ZERO);left.addOrReplaceChild("left_tip",CubeListBuilder.create().texOffs(24,16).mirror().addBox(0,1,0,8,12,1),PartPose.offset(12,1,1.5f));return LayerDefinition.create(mesh,64,64);}
    public static final class Model extends HierarchicalModel<FocusFireBat> {
        private final ModelPart root,head,body,right,left,rightTip,leftTip;
        Model(ModelPart root){super(RenderType::entityCutoutNoCull);this.root=root;head=root.getChild("head");body=root.getChild("body");right=body.getChild("right_wing");left=body.getChild("left_wing");rightTip=right.getChild("right_tip");leftTip=left.getChild("left_tip");}
        @Override public ModelPart root(){return root;}
        @Override public void setupAnim(FocusFireBat entity,float limbSwing,float limbAmount,float age,float yaw,float pitch){head.xRot=pitch/(180f/(float)Math.PI);head.yRot=yaw/(180f/(float)Math.PI);body.xRot=(float)Math.PI/4+(float)Math.cos(age*.1)*.15f;right.yRot=(float)Math.cos(age*1.3)*(float)Math.PI*.25f;left.yRot=-right.yRot;rightTip.yRot=right.yRot*.5f;leftTip.yRot=-rightTip.yRot;}
    }
}
