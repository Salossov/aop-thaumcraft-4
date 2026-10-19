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
    @Override protected void scale(FocusFireBat entity,PoseStack pose,float partial){float scale=entity.devil()||entity.vampire()?.6f:.35f;pose.scale(scale,scale,scale);}
    @Override protected int getBlockLightLevel(FocusFireBat entity,net.minecraft.core.BlockPos pos){return 15;}
    @Override protected int getSkyLightLevel(FocusFireBat entity,net.minecraft.core.BlockPos pos){return 15;}
    @Override protected void setupRotations(FocusFireBat entity,PoseStack pose,float age,float yaw,float partial,float scale){
        pose.translate(0,entity.isResting()?-.1:Math.cos(age*.3)*.1,0);
        super.setupRotations(entity,pose,age,yaw,partial,scale);
    }
    public static LayerDefinition layer(){var mesh=new MeshDefinition();var root=mesh.getRoot();var head=root.addOrReplaceChild("head",CubeListBuilder.create().texOffs(0,0).addBox(-3,-3,-3,6,6,6),PartPose.ZERO);head.addOrReplaceChild("right_ear",CubeListBuilder.create().texOffs(24,0).addBox(-4,-6,-2,3,4,1),PartPose.ZERO);head.addOrReplaceChild("left_ear",CubeListBuilder.create().texOffs(24,0).mirror().addBox(1,-6,-2,3,4,1),PartPose.ZERO);var body=root.addOrReplaceChild("body",CubeListBuilder.create().texOffs(0,16).addBox(-3,4,-3,6,12,6).texOffs(0,34).addBox(-5,16,0,10,6,1),PartPose.ZERO);var right=body.addOrReplaceChild("right_wing",CubeListBuilder.create().texOffs(42,0).addBox(-12,1,1.5f,10,16,1),PartPose.ZERO);right.addOrReplaceChild("right_tip",CubeListBuilder.create().texOffs(24,16).addBox(-8,1,0,8,12,1),PartPose.offset(-12,1,1.5f));var left=body.addOrReplaceChild("left_wing",CubeListBuilder.create().texOffs(42,0).mirror().addBox(2,1,1.5f,10,16,1),PartPose.ZERO);left.addOrReplaceChild("left_tip",CubeListBuilder.create().texOffs(24,16).mirror().addBox(0,1,0,8,12,1),PartPose.offset(12,1,1.5f));return LayerDefinition.create(mesh,64,64);}
    public static final class Model extends HierarchicalModel<FocusFireBat> {
        private final ModelPart root,head,body,right,left,rightTip,leftTip;
        Model(ModelPart root){super(RenderType::entityCutoutNoCull);this.root=root;head=root.getChild("head");body=root.getChild("body");right=body.getChild("right_wing");left=body.getChild("left_wing");rightTip=right.getChild("right_tip");leftTip=left.getChild("left_tip");}
        @Override public ModelPart root(){return root;}
        @Override public void setupAnim(FocusFireBat entity,float limbSwing,float limbAmount,float age,float yaw,float pitch){
            head.xRot=pitch/57.295776f;head.zRot=0;body.yRot=0;
            if(entity.isResting()){
                head.yRot=(float)Math.PI-yaw/57.295776f;head.zRot=(float)Math.PI;head.setPos(0,-2,0);
                right.setPos(-3,0,3);left.setPos(3,0,3);body.xRot=(float)Math.PI;
                right.xRot=-.15707964f;right.yRot=-1.2566371f;rightTip.yRot=-1.7278761f;
                left.xRot=right.xRot;left.yRot=-right.yRot;leftTip.yRot=-rightTip.yRot;
            }else{
                head.yRot=yaw/57.295776f;head.setPos(0,0,0);right.setPos(0,0,0);left.setPos(0,0,0);
                body.xRot=(float)Math.PI/4+(float)Math.cos(age*.1)*.15f;
                right.xRot=left.xRot=0;right.yRot=(float)Math.cos(age*1.3)*(float)Math.PI*.25f;left.yRot=-right.yRot;
                rightTip.yRot=right.yRot*.5f;leftTip.yRot=-rightTip.yRot;
            }
        }
    }
}
