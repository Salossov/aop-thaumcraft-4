package thaumcraft.client.model;

import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;
import thaumcraft.entities.TaintedCreature;

/** TC4 sheep body and wool UVs, without casts to the vanilla Sheep entity. */
public final class TaintedSheepModel extends HierarchicalModel<TaintedCreature> {
    private final ModelPart root,head;private final ModelPart[] legs;
    public TaintedSheepModel(ModelPart root){this.root=root;head=root.getChild("head");legs=new ModelPart[]{root.getChild("leg0"),root.getChild("leg1"),root.getChild("leg2"),root.getChild("leg3")};}
    public static LayerDefinition create(boolean wool){
        var mesh=new MeshDefinition();var root=mesh.getRoot();
        root.addOrReplaceChild("head",CubeListBuilder.create().texOffs(0,0).addBox(-3,-4,wool?-4:-6,6,6,wool?6:8,new CubeDeformation(wool?0.6f:0)),PartPose.offset(0,6,-8));
        root.addOrReplaceChild("body",CubeListBuilder.create().texOffs(28,8).addBox(-4,-10,-7,8,16,6,new CubeDeformation(wool?1.75f:0)),PartPose.offsetAndRotation(0,5,2,Mth.HALF_PI,0,0));
        for(int i=0;i<4;i++)root.addOrReplaceChild("leg"+i,CubeListBuilder.create().texOffs(0,16).addBox(-2,0,-2,4,wool?6:12,4,new CubeDeformation(wool?0.5f:0)),PartPose.offset(i%2==0?-3:3,12,i<2?7:-5));
        return LayerDefinition.create(mesh,64,32);
    }
    @Override public ModelPart root(){return root;}
    @Override public void setupAnim(TaintedCreature entity,float walk,float amount,float age,float yaw,float pitch){head.yRot=yaw*Mth.DEG_TO_RAD;head.xRot=pitch*Mth.DEG_TO_RAD;for(int i=0;i<4;i++)legs[i].xRot=Mth.cos(walk*0.6662f+(i==0||i==3?0:Mth.PI))*1.4f*amount;}
}
