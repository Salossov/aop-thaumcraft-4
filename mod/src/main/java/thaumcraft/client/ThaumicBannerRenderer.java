package thaumcraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.minecraft.world.phys.AABB;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.blocks.*;
import thaumcraft.items.ThaumicBannerItem;

public final class ThaumicBannerRenderer implements BlockEntityRenderer<ThaumicBannerBlockEntity> {
    private final ThaumicBannerModel model=new ThaumicBannerModel(ThaumicBannerModel.layer().bakeRoot());
    public ThaumicBannerRenderer(BlockEntityRendererProvider.Context c){}
    @Override public void render(ThaumicBannerBlockEntity tile,float partial,PoseStack pose,MultiBufferSource buffers,int light,int overlay){var p=tile.getBlockPos();var s=tile.getBlockState();draw(model,tile.color,tile.aspect,s.getValue(ThaumicBannerBlock.ROTATION),s.getValue(ThaumicBannerBlock.WALL),p.getX()*7+p.getY()*9+p.getZ()*13+tile.getLevel().getGameTime()+partial,true,pose,buffers,light,overlay);}
    @Override public AABB getRenderBoundingBox(ThaumicBannerBlockEntity tile){return new AABB(tile.getBlockPos()).expandTowards(0,1,0);}
    private static void draw(ThaumicBannerModel model,int color,Aspect aspect,int rotation,boolean wall,float time,boolean world,PoseStack pose,MultiBufferSource buffers,int light,int overlay){
        var texture=ResourceLocation.parse("thaumcraft:textures/models/banner_"+(color<0&&aspect==null?"cultist":"blank")+".png");var vertices=buffers.getBuffer(RenderType.entityCutoutNoCull(texture));
        pose.pushPose();pose.translate(.5,1.5,.5);pose.mulPose(Axis.XP.rotationDegrees(180));if(world)pose.mulPose(Axis.YP.rotationDegrees(180+rotation*22.5f));
        if(!wall)model.pole.render(pose,vertices,light,overlay);else pose.translate(0,0,-.4125);model.beam.render(pose,vertices,light,overlay);
        int tint=color<0?0xffffffff:0xff000000|ThaumicBannerItem.RGB[color];model.tabs.render(pose,vertices,light,overlay,tint);float wave=(.005f+.005f*(float)Math.cos(time*Math.PI*.02f))*(float)Math.PI;model.cloth.xRot=wave;model.cloth.render(pose,vertices,light,overlay,tint);
        if(aspect!=null){pose.pushPose();pose.translate(0,0,.05001);pose.scale(.0375f,.0375f,.0375f);pose.mulPose(Axis.YP.rotationDegrees(180));pose.mulPose(Axis.XP.rotation(-wave*2));var icon=buffers.getBuffer(RenderType.entityTranslucent(ResourceLocation.parse(aspect.texture())));var matrix=pose.last();int rgb=aspect.getColor();float[][] corners={{-8,4,0,0},{-8,20,0,1},{8,20,1,1},{8,4,1,0}};for(var v:corners)icon.addVertex(matrix.pose(),v[0],v[1],0).setColor((rgb>>16)&255,(rgb>>8)&255,rgb&255,191).setUv(v[2],v[3]).setOverlay(overlay).setLight(light).setNormal(matrix,0,0,1);pose.popPose();}
        pose.popPose();
    }
    private static final class ItemRenderer extends BlockEntityWithoutLevelRenderer {
        private final ThaumicBannerModel model=new ThaumicBannerModel(ThaumicBannerModel.layer().bakeRoot());
        ItemRenderer(){super(Minecraft.getInstance().getBlockEntityRenderDispatcher(),Minecraft.getInstance().getEntityModels());}
        @Override public void renderByItem(ItemStack stack,ItemDisplayContext context,PoseStack pose,MultiBufferSource buffers,int light,int overlay){pose.pushPose();pose.translate(0,-.5,0);draw(model,ThaumicBannerItem.color(stack),ThaumicBannerItem.aspect(stack),0,false,0,false,pose,buffers,light,overlay);pose.popPose();}
    }
    public static final class Extension implements net.neoforged.neoforge.client.extensions.common.IClientItemExtensions {
        private ItemRenderer renderer;
        @Override public BlockEntityWithoutLevelRenderer getCustomRenderer(){if(renderer==null)renderer=new ItemRenderer();return renderer;}
    }
}
