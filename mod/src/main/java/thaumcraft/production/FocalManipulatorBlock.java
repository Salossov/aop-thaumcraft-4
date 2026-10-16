package thaumcraft.production;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.*;
import thaumcraft.registry.R3Content;

public final class FocalManipulatorBlock extends Block implements EntityBlock {
    public FocalManipulatorBlock(){super(BlockBehaviour.Properties.of().strength(2.5f,10).noOcclusion().sound(SoundType.WOOD));}
    @Override public BlockEntity newBlockEntity(BlockPos pos,BlockState state){return new FocalManipulatorBlockEntity(pos,state);}
    @Override protected VoxelShape getShape(BlockState state,BlockGetter level,BlockPos pos,CollisionContext context){return Shapes.or(box(0,12,0,16,16,16),box(1,0,1,4,12,4),box(12,0,1,15,12,4),box(1,0,12,4,12,15),box(12,0,12,15,12,15));}
    @Override protected InteractionResult useWithoutItem(BlockState state,Level level,BlockPos pos,Player player,BlockHitResult hit){if(!level.isClientSide&&player instanceof net.minecraft.server.level.ServerPlayer server&&level.getBlockEntity(pos) instanceof FocalManipulatorBlockEntity tile)server.openMenu(new SimpleMenuProvider((id,inventory,p)->new thaumcraft.menu.FocalManipulatorMenu(id,inventory,tile),Component.translatable("tile.BlockStoneDevice.13.name")),buffer->buffer.writeBlockPos(pos));return InteractionResult.sidedSuccess(level.isClientSide);}
    @Override protected void onRemove(BlockState state,Level level,BlockPos pos,BlockState next,boolean moving){if(state.getBlock()!=next.getBlock()&&level.getBlockEntity(pos) instanceof FocalManipulatorBlockEntity tile)Containers.dropContents(level,pos,tile);super.onRemove(state,level,pos,next,moving);}
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level,BlockState state,BlockEntityType<T> type){return !level.isClientSide&&type==R3Content.FOCAL_ENTITY.get()?(world,pos,block,tile)->((FocalManipulatorBlockEntity)tile).serverTick():null;}
}
