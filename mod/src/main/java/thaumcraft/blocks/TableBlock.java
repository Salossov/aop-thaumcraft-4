package thaumcraft.blocks;

import net.minecraft.core.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.*;
import thaumcraft.registry.R2Content;

public final class TableBlock extends Block implements EntityBlock {
    public enum Kind { TABLE,RESEARCH,ARCANE }
    public static final DirectionProperty FACING=BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty SLAVE=BooleanProperty.create("slave");
    public final Kind kind;
    public TableBlock(Kind kind) { super(Properties.of().strength(2.5f).sound(SoundType.WOOD).noOcclusion());this.kind=kind;registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(SLAVE,false)); }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder) { builder.add(FACING,SLAVE); }
    @Override public BlockState getStateForPlacement(BlockPlaceContext context) { return defaultBlockState().setValue(FACING,context.getHorizontalDirection()); }
    @Override public BlockEntity newBlockEntity(BlockPos pos,BlockState state) { return new TableBlockEntity(pos,state); }
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level,BlockState state,BlockEntityType<T> type) {
        return !level.isClientSide&&kind==Kind.RESEARCH&&type==R2Content.TABLE_ENTITY.get()?(world,pos,block,tile)->TableBlockEntity.tick(world,pos,block,(TableBlockEntity)tile):null;
    }
    @Override protected VoxelShape getShape(BlockState state,BlockGetter level,BlockPos pos,CollisionContext context) { return box(0,0,0,16,kind==Kind.RESEARCH?16:14,16); }
    @Override protected InteractionResult useWithoutItem(BlockState state,Level level,BlockPos pos,Player player,BlockHitResult hit) {
        if(kind==Kind.TABLE||player.isShiftKeyDown()) return InteractionResult.PASS;
        var master=state.getValue(SLAVE)?pos.relative(state.getValue(FACING)):pos;
        if(player instanceof ServerPlayer server&&level.getBlockEntity(master) instanceof TableBlockEntity tile) server.openMenu(tile);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
    @Override protected void onRemove(BlockState state,Level level,BlockPos pos,BlockState next,boolean moving) {
        if(!state.is(next.getBlock())) { if(level.getBlockEntity(pos) instanceof TableBlockEntity tile) Containers.dropContents(level,pos,tile);super.onRemove(state,level,pos,next,moving); }
    }
    @Override protected void neighborChanged(BlockState state,Level level,BlockPos pos,Block block,BlockPos neighbour,boolean moving) {
        if(kind==Kind.RESEARCH&&!level.isClientSide) {
            var partner=level.getBlockState(pos.relative(state.getValue(FACING)));
            if(!partner.is(this)||partner.getValue(SLAVE)==state.getValue(SLAVE)||partner.getValue(FACING)!=state.getValue(FACING).getOpposite()) level.setBlock(pos,R2Content.TABLE.get().defaultBlockState(),3);
        }
    }
}
