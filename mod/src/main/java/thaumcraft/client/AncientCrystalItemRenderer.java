package thaumcraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.core.Direction;
import net.minecraft.world.item.*;

final class AncientCrystalItemRenderer extends BlockEntityWithoutLevelRenderer {
    AncientCrystalItemRenderer(){super(Minecraft.getInstance().getBlockEntityRenderDispatcher(),Minecraft.getInstance().getEntityModels());}
    @Override public void renderByItem(ItemStack stack,ItemDisplayContext context,PoseStack pose,MultiBufferSource buffers,int light,int overlay){OuterRoomRenderers.drawCrystal(Direction.UP,0,pose,buffers,light,overlay);}
    static final class Extension implements net.neoforged.neoforge.client.extensions.common.IClientItemExtensions {
        private AncientCrystalItemRenderer renderer;
        @Override public BlockEntityWithoutLevelRenderer getCustomRenderer(){if(renderer==null)renderer=new AncientCrystalItemRenderer();return renderer;}
    }
}
