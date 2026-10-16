package thaumcraft.production;

import net.minecraft.core.BlockPos;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.*;
import thaumcraft.items.CastingWandItem;
import thaumcraft.registry.R3Content;

public final class InfusionBlock extends Block implements EntityBlock {
    public final boolean matrix;
    public InfusionBlock(boolean matrix){super(BlockBehaviour.Properties.of().strength(3,15).sound(SoundType.STONE).noOcclusion());this.matrix=matrix;}
    @Override public BlockEntity newBlockEntity(BlockPos p,BlockState s){return matrix?new InfusionMatrixBlockEntity(p,s):new PedestalBlockEntity(p,s);}
    @Override protected RenderShape getRenderShape(BlockState state){return matrix?RenderShape.ENTITYBLOCK_ANIMATED:RenderShape.MODEL;}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return matrix?box(2,2,2,14,14,14):Shapes.or(box(0,0,0,16,4,16),box(4,4,4,12,12,12),box(2,12,2,14,16,14));}
    @Override protected ItemInteractionResult useItemOn(ItemStack stack,BlockState state,Level level,BlockPos pos,Player player,InteractionHand hand,BlockHitResult hit){
        if(matrix){if(stack.getItem() instanceof CastingWandItem){if(!level.isClientSide&&player instanceof net.minecraft.server.level.ServerPlayer server)((InfusionMatrixBlockEntity)level.getBlockEntity(pos)).wandClick(server,stack);return ItemInteractionResult.sidedSuccess(level.isClientSide);}return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;}
        if(level.getBlockEntity(pos) instanceof PedestalBlockEntity pedestal){if(!pedestal.isEmpty())return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;if(!stack.isEmpty()){if(!level.isClientSide){pedestal.setItem(0,stack.copyWithCount(1));if(!player.isCreative())stack.shrink(1);}return ItemInteractionResult.sidedSuccess(level.isClientSide);}}
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }
    @Override protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos p,Player player,BlockHitResult hit){
        if(!matrix&&l.getBlockEntity(p) instanceof PedestalBlockEntity tile&&!tile.isEmpty()){if(!l.isClientSide){var stack=tile.removeItemNoUpdate(0);tile.changed();if(!player.getInventory().add(stack))player.drop(stack,false);}return InteractionResult.sidedSuccess(l.isClientSide);}return InteractionResult.PASS;
    }
    @Override protected void onRemove(BlockState s,Level l,BlockPos p,BlockState next,boolean moving){if(s.getBlock()!=next.getBlock()&&l.getBlockEntity(p) instanceof PedestalBlockEntity tile)Containers.dropContents(l,p,tile);super.onRemove(s,l,p,next,moving);}
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level,BlockState s,BlockEntityType<T> type){return matrix&&type==R3Content.INFUSION_ENTITY.get()?(l,p,state,tile)->{var matrixTile=(InfusionMatrixBlockEntity)tile;if(l.isClientSide)matrixTile.clientTick();else matrixTile.serverTick();}:null;}
}
