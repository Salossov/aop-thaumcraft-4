package thaumcraft.blocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
public final class GolemFetterBlock extends Block {
    public static final BooleanProperty POWERED=BlockStateProperties.POWERED;
    public GolemFetterBlock(){super(Properties.of().strength(2,10).sound(SoundType.STONE));registerDefaultState(stateDefinition.any().setValue(POWERED,false));}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder){builder.add(POWERED);}
    @Override protected void neighborChanged(BlockState state,Level level,BlockPos pos,Block block,BlockPos source,boolean moving){if(!level.isClientSide&&state.getValue(POWERED)!=level.hasNeighborSignal(pos))level.setBlock(pos,state.setValue(POWERED,level.hasNeighborSignal(pos)),3);}
    @Override protected void onPlace(BlockState state,Level level,BlockPos pos,BlockState previous,boolean moving){if(previous.getBlock()!=this)neighborChanged(state,level,pos,this,pos,false);}
}
