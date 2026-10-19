package thaumcraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import thaumcraft.blocks.AuraNodeBlockEntity;
import thaumcraft.registry.R2Content;

/** A picked node retains the same animated layers and colours as its saved block entity. */
final class AuraNodeItemRenderer extends BlockEntityWithoutLevelRenderer {
    AuraNodeItemRenderer(){super(Minecraft.getInstance().getBlockEntityRenderDispatcher(),Minecraft.getInstance().getEntityModels());}
    @Override public void renderByItem(ItemStack stack,ItemDisplayContext context,PoseStack pose,MultiBufferSource buffers,int light,int overlay){
        var mc=Minecraft.getInstance();if(mc.level==null)return;
        var node=new AuraNodeBlockEntity(mc.player==null?BlockPos.ZERO:mc.player.blockPosition(),R2Content.AURA_NODE.get().defaultBlockState());
        var tag=stack.getOrDefault(DataComponents.BLOCK_ENTITY_DATA,CustomData.EMPTY).copyTag();if(tag.contains("nodeType"))node.loadCustomOnly(tag,mc.level.registryAccess());
        float radius=.5f;for(var amount:node.state.current.values())radius=Math.max(radius,.2f+.75f*amount/50f);
        float scale=.42f/radius;
        pose.pushPose();pose.translate(.5,.5,.5);pose.scale(scale,scale,scale);pose.translate(-.5,-.5,-.5);
        AuraNodeRenderer.renderAura(node.getBlockPos(),node.state.current,node.state.type,node.state.modifier,true,pose,buffers);pose.popPose();
    }
    static final class Extension implements net.neoforged.neoforge.client.extensions.common.IClientItemExtensions {
        private AuraNodeItemRenderer renderer;
        @Override public BlockEntityWithoutLevelRenderer getCustomRenderer(){if(renderer==null)renderer=new AuraNodeItemRenderer();return renderer;}
    }
}
