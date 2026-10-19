package thaumcraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.world.level.block.RenderShape;
import thaumcraft.blocks.FocusMemoryBlockEntity;
import thaumcraft.registry.R3Content;

public final class FocusMemoryRenderer implements BlockEntityRenderer<FocusMemoryBlockEntity> {
    public FocusMemoryRenderer(BlockEntityRendererProvider.Context context){}
    @Override public void render(FocusMemoryBlockEntity tile,float partial,PoseStack pose,MultiBufferSource buffers,int light,int overlay){
        if(tile.getBlockState().is(R3Content.WARDED_BLOCK.get())&&tile.original().getRenderShape()==RenderShape.MODEL)Minecraft.getInstance().getBlockRenderer().renderSingleBlock(tile.original(),pose,buffers,light,overlay);
        else if(tile.getBlockState().is(R3Content.PORTABLE_HOLE.get()))PortableHoleEffects.render(tile,pose,buffers);
    }
}
