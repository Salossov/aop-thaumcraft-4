package thaumcraft.production;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.*;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.*;

/** TC4's 3/16-high plate has collision for creatures but lets loose items through while open. */
public final class ItemGrateBlock extends Block implements EntityBlock {
    public static final BooleanProperty OPEN=BlockStateProperties.OPEN;
    private static final VoxelShape PLATE=box(0,13,0,16,16,16);
    public ItemGrateBlock(){super(Properties.of().strength(3,10).sound(SoundType.METAL).noOcclusion());registerDefaultState(stateDefinition.any().setValue(OPEN,true));}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder){builder.add(OPEN);}
    @Override protected VoxelShape getShape(BlockState state,BlockGetter level,BlockPos pos,CollisionContext context){return PLATE;}
    @Override protected VoxelShape getCollisionShape(BlockState state,BlockGetter level,BlockPos pos,CollisionContext context){
        if(state.getValue(OPEN)&&(!(context instanceof EntityCollisionContext entity)||entity.getEntity()==null))return Shapes.empty();
        if(context instanceof EntityCollisionContext entity){
            if(state.getValue(OPEN)&&entity.getEntity() instanceof ItemEntity)return Shapes.empty();
            if(entity.getEntity() instanceof thaumcraft.entities.GrateItem item&&item.legacyBlockPosition().equals(pos))return Shapes.empty();
        }
        return PLATE;
    }
    @Override public BlockEntity newBlockEntity(BlockPos pos,BlockState state){return new ItemGrateBlockEntity(pos,state);}
    @Override protected InteractionResult useWithoutItem(BlockState state,Level level,BlockPos pos,Player player,BlockHitResult hit){if(!level.isClientSide){boolean open=!state.getValue(OPEN);level.setBlockAndUpdate(pos,state.setValue(OPEN,open));level.invalidateCapabilities(pos);level.playSound(null,pos,open?SoundEvents.WOODEN_TRAPDOOR_OPEN:SoundEvents.WOODEN_TRAPDOOR_CLOSE,SoundSource.BLOCKS,1,1);}return InteractionResult.sidedSuccess(level.isClientSide);}
}
