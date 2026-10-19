package thaumcraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.inventory.InventoryMenu;
import thaumcraft.entities.ThaumicGolem;

final class GolemHeldRenderer {
    private static JarItemRenderer jarRenderer;
    static void render(ThaumicGolem golem,GolemModel model,PoseStack pose,MultiBufferSource buffers,int light){
        var mc=Minecraft.getInstance();var held=golem.displayCarried();
        if(golem.core()==thaumcraft.items.GolemCoreItem.Kind.FISHING.ordinal())fishingRod(golem,model,pose,buffers,light);
        if(golem.core()==5){
            // Original RenderGolemBase: a real bucket mesh, present even when empty.
            pose.pushPose();pose.scale(.4f,.4f,.4f);pose.translate(0,3,-1.1);pose.mulPose(Axis.XP.rotationDegrees(90));OriginalObjMesh.get("bucket").render("Bucket",pose,buffers,ResearchGraphics.texture("models/bucket"),light,OverlayTexture.NO_OVERLAY,0xffffffff,false);
            var fluid=net.neoforged.neoforge.fluids.FluidStack.parseOptional(golem.registryAccess(),held.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getCompound("Fluid"));
            if(!fluid.isEmpty()){
                var extension=net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions.of(fluid.getFluid());var sprite=mc.getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(extension.getStillTexture(fluid));int colour=extension.getTintColor(fluid);float fill=fluid.getAmount()/(float)Math.max(fluid.getAmount(),golem.fluidLimit());pose.translate(0,0,.2+.8*fill);pose.scale(.8f,.8f,.8f);var out=buffers.getBuffer(RenderType.entityTranslucent(InventoryMenu.BLOCK_ATLAS));
                for(int side=0;side<2;side++)for(int n=0;n<4;n++){int i=side==0?n:3-n;out.addVertex(pose.last(),i<2?-.5f:.5f,i==0||i==3?-.5f:.5f,0).setColor(colour).setUv(i<2?sprite.getU0():sprite.getU1(),i==0||i==3?sprite.getV0():sprite.getV1()).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose.last(),0,0,side==0?1:-1);}
            }pose.popPose();return;
        }
        if(held.isEmpty()||golem.action()>0)return;pose.pushPose();
        if(golem.core()==6){
            pose.scale(.4f,.4f,.4f);pose.translate(0,2.5,-1);float scale=.5f+Math.min(64,golem.carryLimit())/128f;pose.scale(scale,scale,scale);pose.translate(-.5,0,-.8);pose.mulPose(Axis.XP.rotationDegrees(180));pose.mulPose(Axis.YP.rotationDegrees(180));pose.mulPose(Axis.ZP.rotationDegrees(25));pose.mulPose(Axis.YP.rotationDegrees(-50));
            if(jarRenderer==null)jarRenderer=new JarItemRenderer();jarRenderer.renderByItem(held,ItemDisplayContext.NONE,pose,buffers,light,OverlayTexture.NO_OVERLAY);pose.popPose();return;
        }
        pose.translate(0,1,-.5);pose.mulPose(Axis.XP.rotationDegrees(180));pose.scale(.4f,.4f,.4f);
        // NONE avoids multiplying the original held-object scale by the modern ground transform.
        mc.getItemRenderer().renderStatic(held,ItemDisplayContext.NONE,light,OverlayTexture.NO_OVERLAY,pose,buffers,golem.level(),golem.getId());pose.popPose();
    }
    private static void fishingRod(ThaumicGolem golem,GolemModel model,PoseStack pose,MultiBufferSource buffers,int light){
        pose.pushPose();GolemFishingPose.rod(pose,model.rightArm.xRot);
        Minecraft.getInstance().getItemRenderer().renderStatic(new ItemStack(Items.FISHING_ROD),ItemDisplayContext.NONE,light,OverlayTexture.NO_OVERLAY,pose,buffers,golem.level(),golem.getId());pose.popPose();
    }
    private GolemHeldRenderer(){}
}
