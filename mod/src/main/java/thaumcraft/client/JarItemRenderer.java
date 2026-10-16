package thaumcraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import thaumcraft.api.aspects.*;
import thaumcraft.production.TransportBlockEntity;

/** Filled creative/picked jars render the same liquid and label as placed jars. */
final class JarItemRenderer extends BlockEntityWithoutLevelRenderer {
    private final EssentiaTransportRenderer contents=new EssentiaTransportRenderer(null);
    JarItemRenderer(){super(Minecraft.getInstance().getBlockEntityRenderDispatcher(),Minecraft.getInstance().getEntityModels());}
    @Override public void renderByItem(ItemStack stack,ItemDisplayContext context,PoseStack pose,MultiBufferSource buffers,int light,int overlay){
        if(!(stack.getItem() instanceof BlockItem item))return;var state=item.getBlock().defaultBlockState();
        var tag=stack.getOrDefault(DataComponents.BLOCK_ENTITY_DATA,CustomData.EMPTY).copyTag();var stored=tag.getCompound("Essentia");var aspects=new AspectList();for(var key:stored.getAllKeys()){var aspect=Aspect.getAspect(key);if(aspect!=null&&stored.getInt(key)>0)aspects.add(aspect,Math.min(64,stored.getInt(key)));}
        var tile=new TransportBlockEntity(BlockPos.ZERO,state);tile.store.setAspects(aspects);tile.store.filter(Aspect.getAspect(tag.getString("AspectFilter")));contents.render(tile,0,pose,buffers,light,overlay);
        // ItemJarFilledRenderer draws the contents before the depth-writing glass.
        // Flushing this layer prevents a shared entity buffer from postponing the liquid.
        if(buffers instanceof MultiBufferSource.BufferSource source)source.endBatch(RenderType.entitySolid(net.minecraft.world.inventory.InventoryMenu.BLOCK_ATLAS));
        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(state,pose,buffers,light,overlay);
    }
    static final class Extension implements net.neoforged.neoforge.client.extensions.common.IClientItemExtensions {
        private JarItemRenderer renderer;
        @Override public BlockEntityWithoutLevelRenderer getCustomRenderer(){if(renderer==null)renderer=new JarItemRenderer();return renderer;}
    }
}
