package thaumcraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import thaumcraft.blocks.JarredNodeBlockEntity;

public final class JarredNodeRenderer implements BlockEntityRenderer<JarredNodeBlockEntity> {
    public JarredNodeRenderer(BlockEntityRendererProvider.Context context){}
    @Override public void render(JarredNodeBlockEntity jar,float partial,PoseStack pose,MultiBufferSource buffers,int light,int overlay){pose.pushPose();pose.translate(0,-.1,0);AuraNodeRenderer.renderAura(jar.getBlockPos(),jar.state.current,jar.state.type,jar.state.modifier,false,pose,buffers);pose.popPose();}
    public static final class Extension implements net.neoforged.neoforge.client.extensions.common.IClientItemExtensions {
        private BlockEntityWithoutLevelRenderer renderer;
        @Override public BlockEntityWithoutLevelRenderer getCustomRenderer(){
            if(renderer==null)renderer=new BlockEntityWithoutLevelRenderer(Minecraft.getInstance().getBlockEntityRenderDispatcher(),Minecraft.getInstance().getEntityModels()){
                @Override public void renderByItem(ItemStack stack,ItemDisplayContext context,PoseStack pose,MultiBufferSource buffers,int light,int overlay){
                    var mc=Minecraft.getInstance();if(mc.level==null)return;var state=thaumcraft.registry.R3Content.NODE_JAR.get().defaultBlockState();
                    var jar=new JarredNodeBlockEntity(mc.player==null?BlockPos.ZERO:mc.player.blockPosition(),state);jar.loadCustomOnly(stack.getOrDefault(DataComponents.BLOCK_ENTITY_DATA,CustomData.EMPTY).copyTag(),mc.level.registryAccess());
                    mc.getBlockRenderer().renderSingleBlock(state,pose,buffers,light,overlay);
                    // Finish the glass first; otherwise its later item batch covers the node in GUI slots.
                    if(buffers instanceof MultiBufferSource.BufferSource source)source.endBatch();
                    pose.pushPose();pose.translate(0,-.1,0);AuraNodeRenderer.renderAura(jar.getBlockPos(),jar.state.current,jar.state.type,jar.state.modifier,true,pose,buffers);pose.popPose();
                }
            };return renderer;
        }
    }
}
