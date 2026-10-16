package thaumcraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.*;
import thaumcraft.visnet.*;

/** Inventory, hand and dropped forms share the actual placed device OBJ renderer. */
final class VisDeviceItemRenderer extends BlockEntityWithoutLevelRenderer {
    private final VisDeviceRenderers.Relay relay=new VisDeviceRenderers.Relay(null);
    private final VisDeviceRenderers.Stabilizer stabilizer=new VisDeviceRenderers.Stabilizer(null);
    private final VisDeviceRenderers.Converter converter=new VisDeviceRenderers.Converter(null);
    VisDeviceItemRenderer(){super(Minecraft.getInstance().getBlockEntityRenderDispatcher(),Minecraft.getInstance().getEntityModels());}
    @Override public void renderByItem(ItemStack stack,ItemDisplayContext context,PoseStack pose,MultiBufferSource buffers,int light,int overlay){
        if(!(stack.getItem() instanceof BlockItem item)||!(item.getBlock() instanceof VisDeviceBlock block))return;var state=block.defaultBlockState().setValue(VisDeviceBlock.FACING,Direction.UP);
        switch(block.kind){case RELAY,CHARGER->relay.render(new VisRelayBlockEntity(BlockPos.ZERO,state),0,pose,buffers,light,overlay);case STABILIZER,ADVANCED_STABILIZER->stabilizer.render(new NodeStabilizerBlockEntity(BlockPos.ZERO,state),0,pose,buffers,light,overlay);case CONVERTER->converter.render(new NodeConverterBlockEntity(BlockPos.ZERO,state),0,pose,buffers,light,overlay);default->{}}
    }
    static final class Extension implements net.neoforged.neoforge.client.extensions.common.IClientItemExtensions {
        private VisDeviceItemRenderer renderer;
        @Override public BlockEntityWithoutLevelRenderer getCustomRenderer(){if(renderer==null)renderer=new VisDeviceItemRenderer();return renderer;}
    }
}
