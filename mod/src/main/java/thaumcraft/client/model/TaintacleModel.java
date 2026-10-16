package thaumcraft.client.model;

import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.*;
import net.minecraft.util.Mth;
import thaumcraft.entities.Taintacle;

/** Original recursive 0.88 taper, ten segments, animated bend and emissive tip. */
public final class TaintacleModel extends HierarchicalModel<Taintacle> {
    private final ModelPart root,base;
    private final ModelPart[] segments;
    public TaintacleModel(ModelPart root,int length) {super(RenderType::entityTranslucent);this.root=root;base=root.getChild("base");segments=new ModelPart[length];var parent=base;for(int i=0;i<length-1;i++){parent=parent.getChild("segment"+i);segments[i]=parent;parent.xScale=parent.yScale=parent.zScale=0.88f;}segments[length-1]=parent.getChild("tip");segments[length-1].xScale=segments[length-1].yScale=segments[length-1].zScale=0.88f;var orb=parent.getChild("orb");orb.xScale=orb.yScale=orb.zScale=0.88f;}
    public static LayerDefinition create(int length) {
        var mesh=new MeshDefinition();var root=mesh.getRoot();var parent=root.addOrReplaceChild("base",CubeListBuilder.create().texOffs(0,0).addBox(-4,-4,-4,8,8,8),PartPose.offset(0,12,0));
        for(int i=0;i<length-1;i++)parent=parent.addOrReplaceChild("segment"+i,CubeListBuilder.create().texOffs(0,16).addBox(-4,-4,-4,8,8,8),PartPose.offset(0,-8,0));
        parent.addOrReplaceChild("orb",CubeListBuilder.create().texOffs(0,56).addBox(-2,-2,-2,4,4,4),PartPose.offset(0,-8,0));
        parent.addOrReplaceChild("tip",CubeListBuilder.create().texOffs(0,32).addBox(-6,-6,-6,12,12,12),PartPose.offset(0,-8,0));return LayerDefinition.create(mesh,64,64);
    }
    @Override public ModelPart root(){return root;}
    @Override public void setupAnim(Taintacle entity,float walk,float amount,float age,float yaw,float pitch) {float speed=entity.agitated()?3:0.9875f;float intensity=Math.max(0.01f,entity.flailIntensity+(entity.hurtTime>0?0.0125f:-0.0125f));base.xRot=0;for(int i=0;i<segments.length-1;i++){segments[i].xRot=0.15f*intensity*Mth.sin(age*0.1f*speed-i/2f);segments[i].zRot=0.1f/intensity*Mth.sin(age*0.15f-i/2f);}}
    @Override public void renderToBuffer(PoseStack pose,VertexConsumer vertices,int light,int overlay,int color) {
        var leaves=java.util.List.of(segments[segments.length-1],segments[segments.length-2].getChild("orb"));leaves.forEach(p->p.skipDraw=true);root.render(pose,vertices,light,overlay,color);root.getAllParts().forEach(p->p.skipDraw=!leaves.contains(p));root.render(pose,vertices,LightTexture.FULL_BRIGHT,overlay,color);root.getAllParts().forEach(p->p.skipDraw=false);
    }
}
