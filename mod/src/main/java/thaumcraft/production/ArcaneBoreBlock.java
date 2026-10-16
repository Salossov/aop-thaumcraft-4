package thaumcraft.production;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.*;

public final class ArcaneBoreBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING=BlockStateProperties.FACING;
    public static final BooleanProperty HANGING=BooleanProperty.create("hanging");
    public final boolean base;
    public ArcaneBoreBlock(boolean base){super(Properties.of().strength(2.5f).sound(SoundType.WOOD).noOcclusion());this.base=base;registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(HANGING,false));}
    @Override protected MapCodec<? extends BaseEntityBlock> codec(){return simpleCodec(properties->new ArcaneBoreBlock(base));}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder){builder.add(FACING,HANGING);}
    @Override public BlockState getStateForPlacement(BlockPlaceContext context){return defaultBlockState().setValue(FACING,context.getHorizontalDirection().getOpposite()).setValue(HANGING,!base&&context.getClickedFace()==Direction.DOWN);}
    @Override protected RenderShape getRenderShape(BlockState state){return RenderShape.ENTITYBLOCK_ANIMATED;}
    @Override protected VoxelShape getShape(BlockState state,BlockGetter level,BlockPos pos,CollisionContext context){return base?box(1,0,1,15,16,15):box(1,0,1,15,16,15);}
    @Override public BlockEntity newBlockEntity(BlockPos pos,BlockState state){return new ArcaneBoreBlockEntity(pos,state);}
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level,BlockState state,BlockEntityType<T> type){return createTickerHelper(type,thaumcraft.registry.R3Content.BORE_ENTITY.get(),ArcaneBoreBlockEntity::tick);}
    @Override protected ItemInteractionResult useItemOn(ItemStack stack,BlockState state,Level level,BlockPos pos,Player player,InteractionHand hand,BlockHitResult hit){if(stack.getItem() instanceof thaumcraft.items.CastingWandItem){if(!level.isClientSide){level.setBlockAndUpdate(pos,state.setValue(FACING,Direction.getNearest(player.getLookAngle().x,player.getLookAngle().y,player.getLookAngle().z).getOpposite()));if(level.getBlockEntity(pos) instanceof ArcaneBoreBlockEntity bore)bore.resetAim();}return ItemInteractionResult.sidedSuccess(level.isClientSide);}return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;}
    @Override protected InteractionResult useWithoutItem(BlockState state,Level level,BlockPos pos,Player player,BlockHitResult hit){if(base)return InteractionResult.PASS;if(!level.isClientSide&&level.getBlockEntity(pos) instanceof ArcaneBoreBlockEntity bore)player.openMenu(bore,buffer->buffer.writeBlockPos(pos));return InteractionResult.sidedSuccess(level.isClientSide);}
    @Override protected void onRemove(BlockState state,Level level,BlockPos pos,BlockState next,boolean moving){if(!state.is(next.getBlock())&&level.getBlockEntity(pos) instanceof ArcaneBoreBlockEntity bore)Containers.dropContents(level,pos,bore.items);super.onRemove(state,level,pos,next,moving);}
}
