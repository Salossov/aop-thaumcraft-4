package thaumcraft.client;

import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;

/** Exact TC4 ModelBanner boxes, pivots and UV offsets, on its 128x64 texture. */
final class ThaumicBannerModel {
    final ModelPart pole,beam,tabs,cloth;
    ThaumicBannerModel(ModelPart root){pole=root.getChild("pole");beam=root.getChild("beam");tabs=root.getChild("tabs");cloth=root.getChild("cloth");}
    static LayerDefinition layer(){var mesh=new MeshDefinition();var root=mesh.getRoot();root.addOrReplaceChild("pole",CubeListBuilder.create().mirror().texOffs(62,0).addBox(0,0,-1,2,31,2),PartPose.offset(-1,-7,-2));root.addOrReplaceChild("beam",CubeListBuilder.create().mirror().texOffs(30,0).addBox(-7,-7,-1,14,2,2),PartPose.ZERO);root.addOrReplaceChild("tabs",CubeListBuilder.create().mirror().texOffs(0,29).addBox(-5,-7.5f,-1.5f,2,3,3).addBox(3,-7.5f,-1.5f,2,3,3),PartPose.ZERO);root.addOrReplaceChild("cloth",CubeListBuilder.create().mirror().texOffs(0,0).addBox(-7,0,-.5f,14,28,1),PartPose.offset(0,-5,0));return LayerDefinition.create(mesh,128,64);}
}
