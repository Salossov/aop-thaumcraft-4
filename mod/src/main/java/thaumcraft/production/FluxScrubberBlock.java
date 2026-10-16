package thaumcraft.production;

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
import thaumcraft.registry.R4Content;

/** The output port is on the mounting face, matching the original six directions. */
public final class FluxScrubberBlock extends Block implements EntityBlock {
    public static final DirectionProperty FACING=BlockStateProperties.FACING;
    public FluxScrubberBlock(){super(Properties.of().strength(3,10).sound(SoundType.STONE).noOcclusion());registerDefaultState(stateDefinition.any().setValue(FACING,Direction.DOWN));}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder){builder.add(FACING);}
    @Override public BlockState getStateForPlacement(BlockPlaceContext context){return defaultBlockState().setValue(FACING,context.getClickedFace().getOpposite());}
    @Override protected RenderShape getRenderShape(BlockState state){return RenderShape.INVISIBLE;}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return Shapes.block();}
    @Override protected BlockState rotate(BlockState s,Rotation r){return s.setValue(FACING,r.rotate(s.getValue(FACING)));}
    @Override protected BlockState mirror(BlockState s,Mirror m){return rotate(s,m.getRotation(s.getValue(FACING)));}
    @Override public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new FluxScrubberBlockEntity(p,s);}
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level l,BlockState s,BlockEntityType<T> type){return type==R4Content.SCRUBBER_ENTITY.get()?(world,p,state,tile)->((FluxScrubberBlockEntity)tile).tick():null;}
    @Override protected ItemInteractionResult useItemOn(ItemStack stack,BlockState state,Level level,BlockPos pos,Player player,InteractionHand hand,BlockHitResult hit){
        if(!(stack.getItem() instanceof thaumcraft.items.CastingWandItem))return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        if(!level.isClientSide)level.setBlockAndUpdate(pos,state.setValue(FACING,Direction.from3DDataValue((state.getValue(FACING).get3DDataValue()+1)%6)));
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }
}
