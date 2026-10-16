package thaumcraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import thaumcraft.blocks.*;

/** Original two rendered ModelManaPod cuboids, stage color and player-tick pulse. */
public final class ManaPodRenderer implements BlockEntityRenderer<ManaPodBlockEntity> {
    private static final ResourceLocation INNER=ResourceLocation.parse("thaumcraft:textures/models/manapod_0.png"),OUTER=ResourceLocation.parse("thaumcraft:textures/models/manapod_2.png");
    private final ModelPart inner,outer;
    public ManaPodRenderer(BlockEntityRendererProvider.Context context){var mesh=new MeshDefinition();var root=mesh.getRoot();root.addOrReplaceChild("pod0",CubeListBuilder.create().texOffs(0,0).addBox(-2,0,-2,4,5,4),PartPose.ZERO);root.addOrReplaceChild("pod2",CubeListBuilder.create().texOffs(0,0).addBox(-3.5f,0,-3.5f,7,9,7),PartPose.ZERO);var parts=LayerDefinition.create(mesh,32,32).bakeRoot();inner=parts.getChild("pod0");outer=parts.getChild("pod2");}
    public static float pulse(int ticks,int identityHash){return Mth.sin((float)(ticks+identityHash%100)/8f)*.1f+.9f;}
    public static int color(int age,thaumcraft.api.aspects.Aspect aspect){
        float r=.14509805f,g=.6156863f,b=.45882353f;
        if(aspect!=null){float ar=((aspect.getColor()>>16)&255)/255f,ag=((aspect.getColor()>>8)&255)/255f,ab=(aspect.getColor()&255)/255f;if(age==7){r=ar;g=ag;b=ab;}else {float m=age-2;r=(r+ar*m)/(m+1);g=(g+ag*m)/(m+1);b=(b+ab*m)/(m+1);}}
        return ((int)(.9f*255)<<24)|((int)(r*255)<<16)|((int)(g*255)<<8)|(int)(b*255);
    }
    @Override public void render(ManaPodBlockEntity tile,float partial,PoseStack poses,MultiBufferSource buffers,int light,int overlay){
        int age=tile.getLevel()==null?5:tile.getBlockState().getValue(ManaPodBlock.AGE);if(age<=1)return;
        poses.pushPose();poses.translate(.5,.75,.5);poses.mulPose(Axis.XP.rotationDegrees(180));
        if(age>2){var player=Minecraft.getInstance().player;float scale=pulse(player==null?0:player.tickCount,tile.hashCode());light=age*10+(int)(150f*scale);poses.pushPose();poses.translate(0,.1,0);float s=(float)(.125*(double)age*(double)scale);poses.scale(s,s,s);inner.render(poses,buffers.getBuffer(RenderType.entityTranslucent(INNER)),light,overlay,0xffffffff);poses.popPose();}
        float scale=(float)(.15*(double)age);poses.scale(scale,scale,scale);outer.render(poses,buffers.getBuffer(RenderType.entityTranslucent(OUTER)),light,overlay,color(age,tile.aspect));poses.popPose();
    }
}
