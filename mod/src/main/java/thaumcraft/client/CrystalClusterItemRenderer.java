package thaumcraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.core.Direction;
import net.minecraft.world.item.*;
import thaumcraft.visnet.CrystalClusterBlock;

/** Inventory, hand and dropped stacks share the placed cluster's six crystals. */
final class CrystalClusterItemRenderer extends BlockEntityWithoutLevelRenderer {
    private final CrystalClusterRenderer cluster=new CrystalClusterRenderer(null);
    CrystalClusterItemRenderer(){super(Minecraft.getInstance().getBlockEntityRenderDispatcher(),Minecraft.getInstance().getEntityModels());}
    @Override public void renderByItem(ItemStack stack,ItemDisplayContext context,PoseStack pose,MultiBufferSource buffers,int light,int overlay){
        if(stack.getItem() instanceof BlockItem item&&item.getBlock() instanceof CrystalClusterBlock block)
            cluster.renderCluster(block.aspectIndex(),block.color(),Direction.UP,new Random(block.aspectIndex()),pose,buffers,overlay);
    }
    static final class Extension implements net.neoforged.neoforge.client.extensions.common.IClientItemExtensions {
        private CrystalClusterItemRenderer renderer;
        @Override public BlockEntityWithoutLevelRenderer getCustomRenderer(){if(renderer==null)renderer=new CrystalClusterItemRenderer();return renderer;}
    }
}
