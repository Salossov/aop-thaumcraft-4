package thaumcraft.client.model;

import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import thaumcraft.entities.EldritchGuardian;
import thaumcraft.entities.EldritchWarden;

public final class GuardianModel<T extends net.minecraft.world.entity.Mob> extends HierarchicalModel<T> {
    private final ModelPart root,hood,leftArm,rightArm,front1,front2,front3,cloak1,cloak2,cloak3,left1,left2,left3,right1,right2,right3;
    private static final String[] ORDER={"BeltR","Mbelt","MbeltL","MbeltR","BeltL","Chestplate","Hood1","Backplate","ShoulderplateTopR","ShoulderplateR1","ShoulderplateR2","ShoulderplateR3","ShoulderR","ArmL1","ArmR1","ShoulderL","ShoulderplateLtop","ShoulderplateL1","ShoulderplateL2","ShoulderplateL3","LegpanelR4","LegpanelR5","LegpanelR6","BackpanelR1","BackpanelR2","BackpanelR3","BackpanelL3","LegpanelL4","LegpanelL5","LegpanelL6","BackpanelL1","BackpanelL2","Cloak1","SidepanelR1","SidepanelL1","SidepanelL2","SidepanelR2","LegpanelC1"};
    private static final java.util.Map<String,String[]> CHILDREN=java.util.Map.of("Hood1",new String[]{"Hood2","Hood3","Hood4"},"ArmL1",new String[]{"ArmL2","ArmL3"},"ArmR1",new String[]{"ArmR2","ArmR3"});
    private float alpha=0.6f;
    private float partialTicks;
    public GuardianModel(ModelPart root) {
        super(RenderType::entityTranslucent);this.root=root;hood=root.getChild("Hood1");leftArm=root.getChild("ArmL1");rightArm=root.getChild("ArmR1");
        front1=root.getChild("LegpanelC1");front2=front1.getChild("LegpanelC2");front3=front2.getChild("LegpanelC3");
        cloak1=root.getChild("Cloak1");cloak2=cloak1.getChild("Cloak2");cloak3=cloak2.getChild("Cloak3");
        left1=root.getChild("SidepanelL2");left2=left1.getChild("SidepanelL3");left3=left2.getChild("SidepanelL4");right1=root.getChild("SidepanelR2");right2=right1.getChild("SidepanelR3");right3=right2.getChild("SidepanelR4");
    }
    @Override public ModelPart root(){return root;}
    @Override public void prepareMobModel(T entity,float walk,float amount,float partial){partialTicks=partial;}
    @Override public void setupAnim(T entity,float walk,float amount,float age,float yaw,float pitch) {
        float left=entity instanceof EldritchWarden w?w.armLiftL:entity instanceof EldritchGuardian g?g.armLiftL:0;
        float right=entity instanceof EldritchWarden w?w.armLiftR:entity instanceof EldritchGuardian g?g.armLiftR:0;
        animate(entity.tickCount,partialTicks,left,right,yaw,pitch);
        var camera=Minecraft.getInstance().getCameraEntity();double distance=camera==null?0:entity.distanceToSqr(camera);float far=entity.level().getDifficulty()==Difficulty.HARD?576:1024;
        alpha=entity instanceof EldritchWarden||entity.level().dimension().location().toString().equals("thaumcraft:outer_lands")?1:(float)(1-Math.clamp((distance-256)/(far-256),0,1))*0.6f;
    }
    private void animate(int ticks,float partial,float left,float right,float yaw,float pitch){
        float age=ticks+partial;
        hood.yRot=yaw/57.295776f;hood.xRot=pitch/57.295776f;var eye=root.getChild("HoodEye");eye.xRot=hood.xRot;eye.yRot=hood.yRot;
        leftArm.xRot=-1-left+Mth.sin((ticks+20+partial)/10)*0.08f;rightArm.xRot=-1-right+Mth.sin(age/10)*0.08f;
        front1.xRot=-0.15f+Mth.sin(age/8)*0.12f;front2.xRot=Mth.sin((age-5)/8)*0.13f;front3.xRot=Mth.sin((age-10)/8)*0.14f;
        cloak1.xRot=0.2f+Mth.sin(age/7)*0.08f;cloak2.xRot=Mth.sin((age-5)/7)*0.1f;cloak3.xRot=Mth.sin((age-10)/7)*0.12f;
        left1.zRot=-0.2f+Mth.sin((ticks+10+partial)/8)*0.12f;left2.zRot=Mth.sin((age+5)/8)*0.13f;left3.zRot=Mth.sin(age/8)*0.14f;
        right1.zRot=0.2f+Mth.sin((ticks-5+partial)/8)*0.12f;right2.zRot=Mth.sin((age-10)/8)*0.13f;right3.zRot=Mth.sin((age-15)/8)*0.14f;
    }
    @Override public void renderToBuffer(PoseStack pose,VertexConsumer vertices,int light,int overlay,int color){int tint=(Math.round(((color>>>24)&255)*alpha)<<24)|(color&0xffffff);for(String name:ORDER)ordered(root.getChild(name),name,pose,vertices,light,overlay,tint);}
    private void ordered(ModelPart part,String name,PoseStack pose,VertexConsumer vertices,int light,int overlay,int color){var children=CHILDREN.get(name);if(children==null){part.render(pose,vertices,light,overlay,color);return;}if(!part.visible)return;boolean[] visibility=new boolean[children.length];for(int i=0;i<children.length;i++){var child=part.getChild(children[i]);visibility[i]=child.visible;child.visible=false;}try{part.render(pose,vertices,light,overlay,color);}finally{for(int i=0;i<children.length;i++)part.getChild(children[i]).visible=visibility[i];}pose.pushPose();part.translateAndRotate(pose);for(String child:children)part.getChild(child).render(pose,vertices,light,overlay,color);pose.popPose();}
    public void renderEye(PoseStack pose,VertexConsumer vertices,int light,int overlay){root.getChild("HoodEye").render(pose,vertices,light,overlay,-1);}
}
