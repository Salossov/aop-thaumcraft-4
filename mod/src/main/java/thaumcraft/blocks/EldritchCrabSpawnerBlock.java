package thaumcraft.blocks;

import net.minecraft.core.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.util.valueproviders.UniformInt;

public final class EldritchCrabSpawnerBlock extends DropExperienceBlock implements EntityBlock {
    public static final DirectionProperty FACING=BlockStateProperties.FACING;
    public EldritchCrabSpawnerBlock(){super(UniformInt.of(6,10),Properties.of().strength(15,30).noOcclusion().sound(SoundType.STONE).lightLevel(s->4).isValidSpawn((s,l,p,t)->false));registerDefaultState(stateDefinition.any().setValue(FACING,Direction.DOWN));}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder){builder.add(FACING);}
    @Override protected RenderShape getRenderShape(BlockState state){return RenderShape.MODEL;}
    @Override public BlockEntity newBlockEntity(BlockPos pos,BlockState state){return new EldritchCrabSpawnerBlockEntity(pos,state);}
    @Override protected boolean triggerEvent(BlockState state,Level level,BlockPos pos,int id,int value){var tile=level.getBlockEntity(pos);return tile!=null&&tile.triggerEvent(id,value);}
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level,BlockState state,BlockEntityType<T> type){return type==thaumcraft.registry.R5Content.CRAB_SPAWNER_ENTITY.get()?(l,p,s,t)->((EldritchCrabSpawnerBlockEntity)t).tick():null;}
}
