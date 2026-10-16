package thaumcraft.blocks;

import net.minecraft.core.*;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import thaumcraft.api.crafting.IInfusionStabiliser;

/** TC4 crystal metadata7: independent ancient model and non-air support rule. */
public final class AncientCrystalBlock extends Block implements EntityBlock,IInfusionStabiliser {
    public static final DirectionProperty FACING=BlockStateProperties.FACING;
    public AncientCrystalBlock(){super(Properties.of().strength(.7f,.6f).lightLevel(s->7).noOcclusion().sound(thaumcraft.registry.R5Content.CRYSTAL_SOUND));registerDefaultState(stateDefinition.any().setValue(FACING,Direction.UP));}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder){builder.add(FACING);}
    @Override public BlockState getStateForPlacement(BlockPlaceContext context){var face=context.getClickedFace();var support=context.getClickedPos().relative(face.getOpposite());return context.getLevel().getBlockState(support).isFaceSturdy(context.getLevel(),support,face)?defaultBlockState().setValue(FACING,face):null;}
    @Override public boolean canSurvive(BlockState state,LevelReader level,BlockPos pos){return !level.isEmptyBlock(pos.relative(state.getValue(FACING).getOpposite()));}
    @Override protected void neighborChanged(BlockState state,Level level,BlockPos pos,Block block,BlockPos from,boolean moving){if(!level.isClientSide&&!state.canSurvive(level,pos)){dropResources(state,level,pos,level.getBlockEntity(pos));level.removeBlock(pos,false);}}
    @Override protected RenderShape getRenderShape(BlockState state){return RenderShape.ENTITYBLOCK_ANIMATED;}
    @Override public BlockEntity newBlockEntity(BlockPos pos,BlockState state){return new AncientCrystalBlockEntity(pos,state);}
    @Override public boolean canStabaliseInfusion(Level level,BlockPos pos){return true;}
}
