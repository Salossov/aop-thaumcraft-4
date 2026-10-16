package thaumcraft.visnet;

import net.minecraft.core.*;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.shapes.*;
import thaumcraft.api.crafting.IInfusionStabiliser;
import thaumcraft.api.aspects.Aspects;

public final class CrystalClusterBlock extends Block implements IInfusionStabiliser,EntityBlock {
    public static final DirectionProperty FACING=BlockStateProperties.FACING;
    private final int aspect;
    public CrystalClusterBlock(int aspect){super(Properties.of().strength(.7f,1).lightLevel(s->7).noOcclusion().sound(SoundType.AMETHYST));this.aspect=aspect;registerDefaultState(stateDefinition.any().setValue(FACING,Direction.UP));}
    public int color(){return aspect<6?(switch(aspect){case 0->Aspects.AIR;case 1->Aspects.FIRE;case 2->Aspects.WATER;case 3->Aspects.EARTH;case 4->Aspects.ORDER;default->Aspects.ENTROPY;}).getColor():0xaaaaff;}
    public int aspectIndex(){return aspect;}
    @Override public net.minecraft.world.level.block.entity.BlockEntity newBlockEntity(BlockPos pos,BlockState state){return new CrystalClusterBlockEntity(pos,state);}
    @Override protected RenderShape getRenderShape(BlockState state){return RenderShape.ENTITYBLOCK_ANIMATED;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder){builder.add(FACING);}
    @Override public BlockState getStateForPlacement(BlockPlaceContext context){var state=defaultBlockState().setValue(FACING,context.getClickedFace());if(state.canSurvive(context.getLevel(),context.getClickedPos()))return state;for(var direction:context.getNearestLookingDirections()){state=state.setValue(FACING,direction.getOpposite());if(state.canSurvive(context.getLevel(),context.getClickedPos()))return state;}return null;}
    @Override protected boolean canSurvive(BlockState state,LevelReader level,BlockPos pos){var face=state.getValue(FACING);var support=pos.relative(face.getOpposite());return level.hasChunkAt(support)&&level.getBlockState(support).isFaceSturdy(level,support,face);}
    @Override protected BlockState updateShape(BlockState state,Direction direction,BlockState other,LevelAccessor level,BlockPos pos,BlockPos neighbor){return state.canSurvive(level,pos)?super.updateShape(state,direction,other,level,pos,neighbor):Blocks.AIR.defaultBlockState();}
    @Override protected VoxelShape getShape(BlockState state,BlockGetter level,BlockPos pos,CollisionContext context){return switch(state.getValue(FACING)){case UP->box(2,0,2,14,13,14);case DOWN->box(2,3,2,14,16,14);case NORTH->box(2,2,3,14,14,16);case SOUTH->box(2,2,0,14,14,13);case WEST->box(3,2,2,16,14,14);case EAST->box(0,2,2,13,14,14);};}
    @Override public boolean canStabaliseInfusion(Level level,BlockPos pos){return true;}
    @Override public void animateTick(BlockState state,Level level,BlockPos pos,RandomSource random){if(random.nextInt(17)!=0)return;int c=aspect==6?Aspects.PRIMALS.get(random.nextInt(6)).getColor():color();level.addParticle(new DustParticleOptions(new org.joml.Vector3f((c>>16&255)/255f,(c>>8&255)/255f,(c&255)/255f),.25f),pos.getX()+.3+random.nextFloat()*.4,pos.getY()+.3+random.nextFloat()*.4,pos.getZ()+.3+random.nextFloat()*.4,0,0,0);}
}
