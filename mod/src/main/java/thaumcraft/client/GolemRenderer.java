package thaumcraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import thaumcraft.entities.ThaumicGolem;
import thaumcraft.items.GolemCoreItem;
import thaumcraft.registry.R3Equipment;

public final class GolemRenderer extends MobRenderer<ThaumicGolem,GolemModel> {
    public GolemRenderer(EntityRendererProvider.Context context){super(context,new GolemModel(context.bakeLayer(GolemModel.LAYER)),.25f);var accessories=new GolemAccessories(context.bakeLayer(GolemAccessories.LAYER));addLayer(new RenderLayer<>(this){@Override public void render(PoseStack pose,MultiBufferSource buffers,int light,ThaumicGolem golem,float swing,float amount,float partial,float age,float yaw,float pitch){accessories.render(golem,getParentModel(),pose,buffers,light,LivingEntityRenderer.getOverlayCoords(golem,0));
        if(golem.getHealth()<golem.getMaxHealth())getParentModel().renderToBuffer(pose,buffers.getBuffer(RenderType.entityTranslucent(ResearchGraphics.texture("models/golem_damage"))),light,LivingEntityRenderer.getOverlayCoords(golem,0),((int)(255*(1-golem.getHealth()/golem.getMaxHealth()))<<24)|0xffffff);
        if(golem.core()>=0){pose.pushPose();pose.translate(0,.8725,golem.decoration().contains("P")?-.18:-.15);pose.mulPose(Axis.YP.rotationDegrees(180));pose.scale(.175f,.175f,.175f);Minecraft.getInstance().getItemRenderer().renderStatic(new ItemStack(R3Equipment.GOLEM_CORES.get(GolemCoreItem.Kind.values()[golem.core()]).get()),ItemDisplayContext.FIXED,light,OverlayTexture.NO_OVERLAY,pose,buffers,golem.level(),golem.getId());pose.popPose();}
        GolemHeldRenderer.render(golem,pose,buffers,light);
    }});}
    @Override public ResourceLocation getTextureLocation(ThaumicGolem golem){return ResearchGraphics.texture("models/golem_"+golem.material().name().toLowerCase(java.util.Locale.ROOT));}
}
