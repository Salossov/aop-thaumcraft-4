package thaumcraft.production;
import net.minecraft.core.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.shapes.*;
import thaumcraft.registry.R3Content;
/** Formed columns retain the original stone/brick drops. */
public final class InfusionPillarBlock extends Block {
    public static final EnumProperty<DoubleBlockHalf> HALF=BlockStateProperties.DOUBLE_BLOCK_HALF;
    public static final IntegerProperty ORIENTATION=IntegerProperty.create("orientation",2,5);
    public InfusionPillarBlock(){super(BlockBehaviour.Properties.of().strength(3,15).noOcclusion());registerDefaultState(stateDefinition.any().setValue(HALF,DoubleBlockHalf.LOWER).setValue(ORIENTATION,2));}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(HALF,ORIENTATION);}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return Shapes.or(box(2,0,2,14,3,14),box(4,3,4,12,14,12),box(2,14,2,14,16,14));}
    @Override protected BlockState updateShape(BlockState s,Direction d,BlockState adjacent,LevelAccessor l,BlockPos p,BlockPos q){boolean upper=s.getValue(HALF)==DoubleBlockHalf.UPPER;if(d==(upper?Direction.DOWN:Direction.UP)&&!adjacent.is(this))return (upper?R3Content.ARCANE_STONE:R3Content.ARCANE_STONE_BRICKS).get().defaultBlockState();return super.updateShape(s,d,adjacent,l,p,q);}
}
