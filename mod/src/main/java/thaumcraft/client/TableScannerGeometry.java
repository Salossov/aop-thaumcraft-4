package thaumcraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;

/** TC4 EntityItem renderer applied the entity helper, its .5 model scale and the table's .8 scale. */
final class TableScannerGeometry {
    static void draw(PoseStack pose,MultiBufferSource buffers,int light,int overlay){
        pose.pushPose();pose.translate(.5,1.02,.5);pose.scale(.2f,.2f,.2f);
        OriginalObjMesh.get("scanner").renderAll(pose,buffers,ResourceLocation.parse("thaumcraft:textures/models/scanner.png"),light,overlay,-1,false);
        pose.popPose();
    }
    private TableScannerGeometry(){}
}
