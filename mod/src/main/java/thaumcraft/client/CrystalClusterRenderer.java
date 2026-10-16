package thaumcraft.client;

import java.util.Random;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import thaumcraft.api.aspects.Aspects;
import thaumcraft.visnet.*;

public final class CrystalClusterRenderer implements BlockEntityRenderer<CrystalClusterBlockEntity> {
    private final ModelPart crystal;
    public CrystalClusterRenderer(BlockEntityRendererProvider.Context context){var mesh=new MeshDefinition();mesh.getRoot().addOrReplaceChild("crystal",CubeListBuilder.create().texOffs(0,0).mirror().addBox(-16,-16,0,16,16,16),PartPose.offsetAndRotation(0,32,0,.7071f,0,.7071f));crystal=LayerDefinition.create(mesh,64,32).bakeRoot().getChild("crystal");}
    @Override public void render(CrystalClusterBlockEntity tile,float partial,PoseStack pose,MultiBufferSource buffers,int light,int overlay){
        var block=(CrystalClusterBlock)tile.getBlockState().getBlock();int index=block.aspectIndex();var pos=tile.getBlockPos();var random=new Random(index+pos.getX()+pos.getY()*pos.getZ());var facing=tile.getBlockState().getValue(CrystalClusterBlock.FACING);
        renderCluster(index,block.color(),facing,random,pose,buffers,overlay);
    }
    void renderCluster(int index,int tint,Direction facing,Random random,PoseStack pose,MultiBufferSource buffers,int overlay){
        draw(facing,(random.nextFloat()-random.nextFloat())*5,(random.nextFloat()-random.nextFloat())*5,random,tint,1.1f,pose,buffers,overlay);
        for(int i=1;i<6;i++){int color=index==6?(switch(i){case 1->Aspects.AIR;case 2->Aspects.FIRE;case 3->Aspects.WATER;case 4->Aspects.EARTH;default->Aspects.ORDER;}).getColor():tint;int yaw=random.nextInt(36)+72*i,pitch=15+random.nextInt(15);draw(facing,yaw,pitch,random,color,.8f,pose,buffers,overlay);}
    }
    private void draw(Direction facing,float yaw,float pitch,Random random,int color,float size,PoseStack pose,MultiBufferSource buffers,int overlay){
        // Preserve the original random calls used by each crystal's size and tilt.
        random.nextInt(10);random.nextFloat();pose.pushPose();
        switch(facing){case DOWN->{pose.translate(.5,1.3,.5);pose.mulPose(Axis.XP.rotationDegrees(180));}case UP->pose.translate(.5,-.3,.5);case NORTH->{pose.translate(.5,.5,1.3);pose.mulPose(Axis.XN.rotationDegrees(90));}case SOUTH->{pose.translate(.5,.5,-.3);pose.mulPose(Axis.XP.rotationDegrees(90));}case WEST->{pose.translate(1.3,.5,.5);pose.mulPose(Axis.ZP.rotationDegrees(90));}case EAST->{pose.translate(-.3,.5,.5);pose.mulPose(Axis.ZN.rotationDegrees(90));}}
        pose.mulPose(Axis.YP.rotationDegrees(yaw));pose.mulPose(Axis.XP.rotationDegrees(pitch));pose.scale((.15f+random.nextFloat()*.075f)*size,(.5f+random.nextFloat()*.1f)*size,(.15f+random.nextFloat()*.05f)*size);
        crystal.render(pose,buffers.getBuffer(RenderType.entityTranslucent(ResourceLocation.parse("thaumcraft:textures/models/crystal.png"))),LightTexture.FULL_BRIGHT,overlay,0xff000000|color);pose.popPose();
    }
}
