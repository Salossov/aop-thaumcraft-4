package thaumcraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import thaumcraft.entities.TravelingTrunk;
import thaumcraft.production.HungryChestBlockEntity;

public final class TrunkRenderers {
    private static final ResourceLocation CHEST=ResourceLocation.parse("thaumcraft:textures/models/chesthungry.png"),TRUNK=ResourceLocation.parse("thaumcraft:textures/models/trunk.png"),ANGRY=ResourceLocation.parse("thaumcraft:textures/models/trunkangry.png");
    private static ModelPart chest(boolean traveling){var mesh=new MeshDefinition();var root=mesh.getRoot();root.addOrReplaceChild("lid",CubeListBuilder.create().texOffs(0,0).addBox(0,-5,-14,14,5,14),PartPose.offset(1,traveling?7:6,15));root.addOrReplaceChild("lock",CubeListBuilder.create().texOffs(0,0).addBox(-1,-2,-15,2,4,1),PartPose.offset(8,traveling?7:6,15));root.addOrReplaceChild("body",CubeListBuilder.create().texOffs(0,19).addBox(0,0,0,14,10,14),PartPose.offset(1,6,1));return LayerDefinition.create(mesh,64,64).bakeRoot();}
    private static void draw(ModelPart model,float lid,ResourceLocation texture,PoseStack pose,MultiBufferSource buffers,int light,int overlay){float amount=1-lid;float rotation=-(1-amount*amount*amount)*(float)Math.PI/2;model.getChild("lid").xRot=rotation;model.getChild("lock").xRot=rotation;model.render(pose,buffers.getBuffer(RenderType.entityCutoutNoCull(texture)),light,overlay);}
    public static final class Chest implements BlockEntityRenderer<HungryChestBlockEntity>{private final ModelPart model=chest(false);public Chest(BlockEntityRendererProvider.Context context){}@Override public void render(HungryChestBlockEntity tile,float partial,PoseStack pose,MultiBufferSource buffers,int light,int overlay){pose.pushPose();pose.translate(.5,1,.5);pose.mulPose(Axis.YP.rotationDegrees(-tile.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING).toYRot()));pose.scale(1,-1,-1);pose.translate(-.5,0,-.5);draw(model,net.minecraft.util.Mth.lerp(partial,tile.oldLid,tile.lid),CHEST,pose,buffers,light,overlay);pose.popPose();}}
    public static final class Trunk extends EntityRenderer<TravelingTrunk>{private final ModelPart model=chest(true);public Trunk(EntityRendererProvider.Context context){super(context);shadowRadius=.4f;}@Override public ResourceLocation getTextureLocation(TravelingTrunk trunk){return trunk.anger()>0?ANGRY:TRUNK;}@Override public void render(TravelingTrunk trunk,float yaw,float partial,PoseStack pose,MultiBufferSource buffers,int light){pose.pushPose();pose.mulPose(Axis.YP.rotationDegrees(180-yaw));float bounce=net.minecraft.util.Mth.lerp(partial,trunk.oldSquish,trunk.squish),width=1/(1+bounce/2)/1.4f,scale=2/(trunk.upgrade()==1?1.33f:1.5f);pose.scale(width*scale,.5f/width*scale,width*scale);pose.translate(-.5,1,.5);pose.scale(1,-1,-1);draw(model,net.minecraft.util.Mth.lerp(partial,trunk.oldLid,trunk.lid),getTextureLocation(trunk),pose,buffers,light,OverlayTexture.NO_OVERLAY);pose.popPose();super.render(trunk,yaw,partial,pose,buffers,light);}}
    public static final class ItemRenderer extends BlockEntityWithoutLevelRenderer{private final ModelPart chest=chest(false),trunk=chest(true);public ItemRenderer(){super(Minecraft.getInstance().getBlockEntityRenderDispatcher(),Minecraft.getInstance().getEntityModels());}@Override public void renderByItem(ItemStack stack,ItemDisplayContext context,PoseStack pose,MultiBufferSource buffers,int light,int overlay){pose.pushPose();pose.translate(0,1,1);pose.scale(1,-1,-1);boolean traveling=stack.is(thaumcraft.registry.R3Equipment.TRAVELING_TRUNK.get());draw(traveling?trunk:chest,0,traveling?TRUNK:CHEST,pose,buffers,light,overlay);pose.popPose();}}
    public static final class Extension implements net.neoforged.neoforge.client.extensions.common.IClientItemExtensions{private ItemRenderer renderer;@Override public BlockEntityWithoutLevelRenderer getCustomRenderer(){if(renderer==null)renderer=new ItemRenderer();return renderer;}}
    private TrunkRenderers(){}
}
