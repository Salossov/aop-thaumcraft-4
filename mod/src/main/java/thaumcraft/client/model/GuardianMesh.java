package thaumcraft.client.model;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

/** Generated from TC4 ModelEldritchGuardian; pivots, boxes, UVs and hierarchy preserved. */
public final class GuardianMesh {
    public static LayerDefinition create() {
        var mesh=new MeshDefinition();var root=mesh.getRoot();
        var BeltR=root.addOrReplaceChild("BeltR",CubeListBuilder.create().texOffs(76,44).mirror(false).addBox(-5.0f, 4.0f, -3.0f, 1, 3, 6),PartPose.offsetAndRotation(0.0f, -6.0f, 0.0f, 0.0f, 0.0f, 0.0f));
        var Mbelt=root.addOrReplaceChild("Mbelt",CubeListBuilder.create().texOffs(56,55).mirror(false).addBox(-4.0f, 8.0f, -3.0f, 8, 4, 1),PartPose.offsetAndRotation(0.0f, -6.0f, 0.0f, 0.0f, 0.0f, 0.0f));
        var MbeltL=root.addOrReplaceChild("MbeltL",CubeListBuilder.create().texOffs(76,44).mirror(false).addBox(4.0f, 8.0f, -3.0f, 1, 3, 6),PartPose.offsetAndRotation(0.0f, -6.0f, 0.0f, 0.0f, 0.0f, 0.0f));
        var MbeltR=root.addOrReplaceChild("MbeltR",CubeListBuilder.create().texOffs(76,44).mirror(false).addBox(-5.0f, 8.0f, -3.0f, 1, 3, 6),PartPose.offsetAndRotation(0.0f, -6.0f, 0.0f, 0.0f, 0.0f, 0.0f));
        var BeltL=root.addOrReplaceChild("BeltL",CubeListBuilder.create().texOffs(76,44).mirror(false).addBox(4.0f, 4.0f, -3.0f, 1, 3, 6),PartPose.offsetAndRotation(0.0f, -6.0f, 0.0f, 0.0f, 0.0f, 0.0f));
        var Chestplate=root.addOrReplaceChild("Chestplate",CubeListBuilder.create().texOffs(56,45).mirror(false).addBox(-4.0f, 1.0f, -4.0f, 8, 7, 2),PartPose.offsetAndRotation(0.0f, -6.0f, 0.0f, 0.0f, 0.0f, 0.0f));
        var Hood1=root.addOrReplaceChild("Hood1",CubeListBuilder.create().texOffs(40,12).mirror(false).addBox(-4.0f, -8.0f, -4.0f, 8, 8, 8),PartPose.offsetAndRotation(0.0f, -6.0f, 0.0f, 0.0f, 0.0f, 0.0f));
        var Hood2=Hood1.addOrReplaceChild("Hood2",CubeListBuilder.create().texOffs(36,28).mirror(false).addBox(-3.5f, -8.7f, 2.0f, 7, 7, 3),PartPose.offsetAndRotation(0, 0, 0, -0.2268928f, 0.0f, 0.0f));
        var Hood3=Hood1.addOrReplaceChild("Hood3",CubeListBuilder.create().texOffs(22,19).mirror(false).addBox(-3.0f, -9.0f, 2.5f, 6, 6, 3),PartPose.offsetAndRotation(0, 0, 0, -0.3490659f, 0.0f, 0.0f));
        var Hood4=Hood1.addOrReplaceChild("Hood4",CubeListBuilder.create().texOffs(40,4).mirror(false).addBox(-2.5f, -9.7f, 3.5f, 5, 5, 3),PartPose.offsetAndRotation(0, 0, 0, -0.5759587f, 0.0f, 0.0f));
        var Backplate=root.addOrReplaceChild("Backplate",CubeListBuilder.create().texOffs(36,45).mirror(false).addBox(-4.0f, 1.0f, 2.0f, 8, 11, 2),PartPose.offsetAndRotation(0.0f, -6.0f, 0.0f, 0.0f, 0.0f, 0.0f));
        var ShoulderplateTopR=root.addOrReplaceChild("ShoulderplateTopR",CubeListBuilder.create().texOffs(110,37).mirror(false).addBox(-5.5f, -2.5f, -3.5f, 2, 1, 7),PartPose.offsetAndRotation(-5.0f, -4.0f, 0.0f, -0.3665191f, 0.3141593f, 0.4363323f));
        var ShoulderplateR1=root.addOrReplaceChild("ShoulderplateR1",CubeListBuilder.create().texOffs(110,45).mirror(false).addBox(3.5f, -1.5f, -3.5f, 1, 4, 7),PartPose.offsetAndRotation(5.0f, -4.0f, 0.0f, -0.3665191f, -0.3141593f, -0.4363323f));
        var ShoulderplateR2=root.addOrReplaceChild("ShoulderplateR2",CubeListBuilder.create().texOffs(94,45).mirror(false).addBox(-3.5f, 1.5f, -3.5f, 1, 3, 7),PartPose.offsetAndRotation(-5.0f, -4.0f, 0.0f, -0.3665191f, 0.3141593f, 0.4363323f));
        var ShoulderplateR3=root.addOrReplaceChild("ShoulderplateR3",CubeListBuilder.create().texOffs(94,45).mirror(false).addBox(-2.5f, 3.5f, -3.5f, 1, 3, 7),PartPose.offsetAndRotation(-5.0f, -4.0f, 0.0f, -0.3665191f, 0.3141593f, 0.4363323f));
        var ShoulderR=root.addOrReplaceChild("ShoulderR",CubeListBuilder.create().texOffs(56,35).mirror(false).addBox(-3.5f, -2.5f, -2.5f, 5, 5, 5),PartPose.offsetAndRotation(-5.0f, -4.0f, 0.0f, -0.3665191f, 0.122173f, 0.0349066f));
        var ArmL1=root.addOrReplaceChild("ArmL1",CubeListBuilder.create().texOffs(72,8).mirror(false).addBox(-1.0f, 2.5f, -1.5f, 4, 10, 5),PartPose.offsetAndRotation(5.0f, -4.0f, 0.0f, -0.9599311f, -0.1047198f, -0.1919862f));
        var ArmL2=ArmL1.addOrReplaceChild("ArmL2",CubeListBuilder.create().texOffs(76,28).mirror(false).addBox(-1.0f, 9.5f, 3.5f, 4, 3, 3),PartPose.offsetAndRotation(0, 0, 0, 0, 0, 0));
        var ArmL3=ArmL1.addOrReplaceChild("ArmL3",CubeListBuilder.create().texOffs(76,23).mirror(false).addBox(-1.0f, 6.5f, 3.5f, 4, 3, 2),PartPose.offsetAndRotation(0, 0, 0, 0, 0, 0));
        var ArmR1=root.addOrReplaceChild("ArmR1",CubeListBuilder.create().texOffs(72,8).mirror(false).addBox(-3.0f, 2.5f, -1.5f, 4, 10, 5),PartPose.offsetAndRotation(-5.0f, -4.0f, 0.0f, -0.9599311f, 0.1047198f, 0.1919862f));
        var ArmR2=ArmR1.addOrReplaceChild("ArmR2",CubeListBuilder.create().texOffs(76,28).mirror(false).addBox(-3.0f, 9.5f, 3.5f, 4, 3, 3),PartPose.offsetAndRotation(0, 0, 0, 0, 0, 0));
        var ArmR3=ArmR1.addOrReplaceChild("ArmR3",CubeListBuilder.create().texOffs(76,23).mirror(false).addBox(-3.0f, 6.5f, 3.5f, 4, 3, 2),PartPose.offsetAndRotation(0, 0, 0, 0, 0, 0));
        var ShoulderL=root.addOrReplaceChild("ShoulderL",CubeListBuilder.create().texOffs(56,35).mirror(true).addBox(-1.5f, -2.5f, -2.5f, 5, 5, 5),PartPose.offsetAndRotation(5.0f, -4.0f, 0.0f, -0.3665191f, -0.122173f, -0.0349066f));
        var ShoulderplateLtop=root.addOrReplaceChild("ShoulderplateLtop",CubeListBuilder.create().texOffs(110,37).mirror(false).addBox(3.5f, -2.5f, -3.5f, 2, 1, 7),PartPose.offsetAndRotation(5.0f, -4.0f, 0.0f, -0.3665191f, -0.3141593f, -0.4363323f));
        var ShoulderplateL1=root.addOrReplaceChild("ShoulderplateL1",CubeListBuilder.create().texOffs(110,45).mirror(false).addBox(-4.5f, -1.5f, -3.5f, 1, 4, 7),PartPose.offsetAndRotation(-5.0f, -4.0f, 0.0f, -0.3665191f, 0.3141593f, 0.4363323f));
        var ShoulderplateL2=root.addOrReplaceChild("ShoulderplateL2",CubeListBuilder.create().texOffs(94,45).mirror(false).addBox(2.5f, 1.5f, -3.5f, 1, 3, 7),PartPose.offsetAndRotation(5.0f, -4.0f, 0.0f, -0.3665191f, -0.3141593f, -0.4363323f));
        var ShoulderplateL3=root.addOrReplaceChild("ShoulderplateL3",CubeListBuilder.create().texOffs(94,45).mirror(false).addBox(1.5f, 3.5f, -3.5f, 1, 3, 7),PartPose.offsetAndRotation(5.0f, -4.0f, 0.0f, -0.3665191f, -0.3141593f, -0.4363323f));
        var LegpanelR4=root.addOrReplaceChild("LegpanelR4",CubeListBuilder.create().texOffs(0,43).mirror(false).addBox(-3.0f, 0.5f, -3.5f, 2, 3, 1),PartPose.offsetAndRotation(-2.0f, 6.0f, 0.0f, -0.4363323f, 0.0f, 0.0f));
        var LegpanelR5=root.addOrReplaceChild("LegpanelR5",CubeListBuilder.create().texOffs(0,47).mirror(false).addBox(-3.0f, 2.5f, -2.5f, 2, 3, 1),PartPose.offsetAndRotation(-2.0f, 6.0f, 0.0f, -0.4363323f, 0.0f, 0.0f));
        var LegpanelR6=root.addOrReplaceChild("LegpanelR6",CubeListBuilder.create().texOffs(6,43).mirror(false).addBox(-3.0f, 4.5f, -1.5f, 2, 3, 1),PartPose.offsetAndRotation(-2.0f, 6.0f, 0.0f, -0.4363323f, 0.0f, 0.0f));
        var BackpanelR1=root.addOrReplaceChild("BackpanelR1",CubeListBuilder.create().texOffs(0,18).mirror(false).addBox(-3.0f, 0.5f, 2.5f, 5, 3, 1),PartPose.offsetAndRotation(-2.0f, 6.0f, 0.0f, 0.4363323f, 0.0f, 0.0f));
        var BackpanelR2=root.addOrReplaceChild("BackpanelR2",CubeListBuilder.create().texOffs(0,18).mirror(false).addBox(-3.0f, 2.5f, 1.5f, 5, 3, 1),PartPose.offsetAndRotation(-2.0f, 6.0f, 0.0f, 0.4363323f, 0.0f, 0.0f));
        var BackpanelR3=root.addOrReplaceChild("BackpanelR3",CubeListBuilder.create().texOffs(0,18).mirror(false).addBox(-3.0f, 4.5f, 0.5f, 5, 3, 1),PartPose.offsetAndRotation(-2.0f, 6.0f, 0.0f, 0.4363323f, 0.0f, 0.0f));
        var BackpanelL3=root.addOrReplaceChild("BackpanelL3",CubeListBuilder.create().texOffs(0,18).mirror(false).addBox(-2.0f, 4.5f, 0.5f, 5, 3, 1),PartPose.offsetAndRotation(2.0f, 6.0f, 0.0f, 0.4363323f, 0.0f, 0.0f));
        var LegpanelL4=root.addOrReplaceChild("LegpanelL4",CubeListBuilder.create().texOffs(0,43).mirror(false).addBox(1.0f, 0.5f, -3.5f, 2, 3, 1),PartPose.offsetAndRotation(2.0f, 6.0f, 0.0f, -0.4363323f, 0.0f, 0.0f));
        var LegpanelL5=root.addOrReplaceChild("LegpanelL5",CubeListBuilder.create().texOffs(0,47).mirror(false).addBox(1.0f, 2.5f, -2.5f, 2, 3, 1),PartPose.offsetAndRotation(2.0f, 6.0f, 0.0f, -0.4363323f, 0.0f, 0.0f));
        var LegpanelL6=root.addOrReplaceChild("LegpanelL6",CubeListBuilder.create().texOffs(6,43).mirror(false).addBox(1.0f, 4.5f, -1.5f, 2, 3, 1),PartPose.offsetAndRotation(2.0f, 6.0f, 0.0f, -0.4363323f, 0.0f, 0.0f));
        var BackpanelL1=root.addOrReplaceChild("BackpanelL1",CubeListBuilder.create().texOffs(0,18).mirror(false).addBox(-2.0f, 0.5f, 2.5f, 5, 3, 1),PartPose.offsetAndRotation(2.0f, 6.0f, 0.0f, 0.4363323f, 0.0f, 0.0f));
        var BackpanelL2=root.addOrReplaceChild("BackpanelL2",CubeListBuilder.create().texOffs(0,18).mirror(false).addBox(-2.0f, 2.5f, 1.5f, 5, 3, 1),PartPose.offsetAndRotation(2.0f, 6.0f, 0.0f, 0.4363323f, 0.0f, 0.0f));
        var SidepanelL1=root.addOrReplaceChild("SidepanelL1",CubeListBuilder.create().texOffs(0,22).mirror(false).addBox(1.5f, 0.5f, -2.5f, 1, 4, 5),PartPose.offsetAndRotation(2.0f, 6.0f, 0.0f, 0.0f, 0.0f, -0.4363323f));
        var SidepanelR1=root.addOrReplaceChild("SidepanelR1",CubeListBuilder.create().texOffs(0,22).mirror(false).addBox(-2.5f, 0.5f, -2.5f, 1, 4, 5),PartPose.offsetAndRotation(-2.0f, 6.0f, 0.0f, 0.0f, 0.0f, 0.4363323f));
        var SidepanelR2=root.addOrReplaceChild("SidepanelR2",CubeListBuilder.create().texOffs(0,54).mirror(false).addBox(0.0f, 0.0f, -0.5f, 1, 5, 5),PartPose.offsetAndRotation(-4.5f, 9.5f, -2.0f, 0.0f, 0.0f, 0.122173f));
        var SidepanelR3=SidepanelR2.addOrReplaceChild("SidepanelR3",CubeListBuilder.create().texOffs(0,35).mirror(false).addBox(0.0f, 0.0f, -0.5f, 1, 3, 5),PartPose.offsetAndRotation(0.0f, 5.0f, 0.0f, 0.0f, 0.0f, 0.296706f));
        var SidepanelR4=SidepanelR3.addOrReplaceChild("SidepanelR4",CubeListBuilder.create().texOffs(24,35).mirror(false).addBox(0.0f, 0.0f, -0.5f, 1, 3, 5),PartPose.offsetAndRotation(0.0f, 3.0f, 0.0f, 0.0f, 0.0f, 0.5235988f));
        var SidepanelL2=root.addOrReplaceChild("SidepanelL2",CubeListBuilder.create().texOffs(0,54).mirror(false).addBox(0.0f, 0.0f, -0.5f, 1, 5, 5),PartPose.offsetAndRotation(4.5f, 9.5f, -2.0f, 0.0f, 0.0f, -0.122173f));
        var SidepanelL3=SidepanelL2.addOrReplaceChild("SidepanelL3",CubeListBuilder.create().texOffs(0,35).mirror(false).addBox(0.0f, 0.0f, -0.5f, 1, 3, 5),PartPose.offsetAndRotation(0.0f, 5.0f, 0.0f, 0.0f, 0.0f, -0.296706f));
        var SidepanelL4=SidepanelL3.addOrReplaceChild("SidepanelL4",CubeListBuilder.create().texOffs(24,35).mirror(false).addBox(0.0f, 0.0f, -0.5f, 1, 3, 5),PartPose.offsetAndRotation(0.0f, 3.0f, 0.0f, 0.0f, 0.0f, -0.5235988f));
        var LegpanelC1=root.addOrReplaceChild("LegpanelC1",CubeListBuilder.create().texOffs(16,45).mirror(false).addBox(-3.0f, 0.0f, -0.5f, 6, 8, 1),PartPose.offsetAndRotation(0.0f, 5.5f, -3.0f, 0.0f, 0.0f, 0.0f));
        var LegpanelC2=LegpanelC1.addOrReplaceChild("LegpanelC2",CubeListBuilder.create().texOffs(16,54).mirror(false).addBox(-3.0f, 0.0f, -0.5f, 6, 4, 1),PartPose.offsetAndRotation(0.0f, 8.0f, 0.0f, 0.0f, 0.0f, 0.0f));
        var LegpanelC3=LegpanelC2.addOrReplaceChild("LegpanelC3",CubeListBuilder.create().texOffs(32,59).mirror(false).addBox(-3.0f, 0.0f, -0.5f, 6, 4, 1),PartPose.offsetAndRotation(0.0f, 4.0f, 0.0f, 0.0f, 0.0f, 0.0f));
        var Cloak1=root.addOrReplaceChild("Cloak1",CubeListBuilder.create().texOffs(106,0).mirror(false).addBox(0.0f, 0.0f, -0.5f, 10, 18, 1),PartPose.offsetAndRotation(-5.0f, -6.0f, 4.0f, 0.0f, 0.0f, 0.0f));
        var Cloak2=Cloak1.addOrReplaceChild("Cloak2",CubeListBuilder.create().texOffs(106,19).mirror(false).addBox(0.0f, 0.0f, -0.5f, 10, 4, 1),PartPose.offsetAndRotation(0.0f, 18.0f, 0.0f, 0.0f, 0.0f, 0.0f));
        var Cloak3=Cloak2.addOrReplaceChild("Cloak3",CubeListBuilder.create().texOffs(106,24).mirror(false).addBox(0.0f, 0.0f, -0.5f, 10, 4, 1),PartPose.offsetAndRotation(0.0f, 4.0f, 0.0f, 0.0f, 0.0f, 0.0f));
        root.addOrReplaceChild("HoodEye",CubeListBuilder.create().texOffs(0,0).mirror(false).addBox(-4,-8,-4,8,8,8),PartPose.offset(0,-6,0));
        return LayerDefinition.create(mesh,128,64);
    }
    private GuardianMesh() {}
}
