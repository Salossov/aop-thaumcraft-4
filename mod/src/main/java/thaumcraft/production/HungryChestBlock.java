package thaumcraft.production;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.*;

public final class HungryChestBlock extends BaseEntityBlock {
    public static final MapCodec<HungryChestBlock> CODEC=simpleCodec(properties->new HungryChestBlock());
    public HungryChestBlock(){super(Properties.of().strength(2.5f).sound(SoundType.WOOD).noOcclusion());registerDefaultState(stateDefinition.any().setValue(BlockStateProperties.HORIZONTAL_FACING,Direction.NORTH));}
    @Override protected MapCodec<? extends BaseEntityBlock> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder){builder.add(BlockStateProperties.HORIZONTAL_FACING);}
    @Override public BlockState getStateForPlacement(BlockPlaceContext context){return defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING,context.getHorizontalDirection().getOpposite());}
    @Override protected RenderShape getRenderShape(BlockState state){return RenderShape.ENTITYBLOCK_ANIMATED;}
    @Override protected VoxelShape getShape(BlockState state,BlockGetter level,BlockPos pos,CollisionContext context){return box(1,0,1,15,14,15);}
    @Override public BlockEntity newBlockEntity(BlockPos pos,BlockState state){return new HungryChestBlockEntity(pos,state);}
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level,BlockState state,BlockEntityType<T> type){return createTickerHelper(type,thaumcraft.registry.R3Content.HUNGRY_CHEST_ENTITY.get(),HungryChestBlockEntity::tick);}
    @Override protected InteractionResult useWithoutItem(BlockState state,Level level,BlockPos pos,Player player,BlockHitResult hit){if(!level.isClientSide&&level.getBlockEntity(pos) instanceof HungryChestBlockEntity chest)player.openMenu(chest);return InteractionResult.sidedSuccess(level.isClientSide);}
    @Override protected void entityInside(BlockState state,Level level,BlockPos pos,Entity entity){if(!level.isClientSide&&entity instanceof ItemEntity item&&item.isAlive()&&level.getBlockEntity(pos) instanceof HungryChestBlockEntity chest)chest.eat(item);}
    @Override protected boolean hasAnalogOutputSignal(BlockState state){return true;}
    @Override protected int getAnalogOutputSignal(BlockState state,Level level,BlockPos pos){return level.getBlockEntity(pos) instanceof HungryChestBlockEntity chest?net.minecraft.world.inventory.AbstractContainerMenu.getRedstoneSignalFromContainer(chest.items):0;}
    @Override protected void onRemove(BlockState state,Level level,BlockPos pos,BlockState next,boolean moving){if(!state.is(next.getBlock())&&level.getBlockEntity(pos) instanceof HungryChestBlockEntity chest){Containers.dropContents(level,pos,chest.items);level.updateNeighbourForOutputSignal(pos,this);}super.onRemove(state,level,pos,next,moving);}
}
