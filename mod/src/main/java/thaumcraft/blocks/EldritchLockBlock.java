package thaumcraft.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

/** Original lock stone and its persisted facing; encounter activation lives in its tile. */
public final class EldritchLockBlock extends BaseEntityBlock {
    public static final IntegerProperty FACING=IntegerProperty.create("facing",0,6);
    public EldritchLockBlock(){super(Properties.of().strength(-1,Float.MAX_VALUE).noOcclusion().noLootTable());registerDefaultState(stateDefinition.any().setValue(FACING,0));}
    @Override protected MapCodec<? extends BaseEntityBlock> codec(){return simpleCodec(p->new EldritchLockBlock());}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder){builder.add(FACING);}
    @Override protected RenderShape getRenderShape(BlockState state){return RenderShape.MODEL;}
    @Override public BlockEntity newBlockEntity(BlockPos pos,BlockState state){return new EldritchLockBlockEntity(pos,state);}
    @Override public <T extends BlockEntity> net.minecraft.world.level.block.entity.BlockEntityTicker<T> getTicker(net.minecraft.world.level.Level level,BlockState state,net.minecraft.world.level.block.entity.BlockEntityType<T> type){return createTickerHelper(type,thaumcraft.registry.R5Content.LOCK_ENTITY.get(),EldritchLockBlockEntity::tick);}
    @Override protected net.minecraft.world.ItemInteractionResult useItemOn(net.minecraft.world.item.ItemStack stack,BlockState state,net.minecraft.world.level.Level level,BlockPos pos,net.minecraft.world.entity.player.Player player,net.minecraft.world.InteractionHand hand,net.minecraft.world.phys.BlockHitResult hit){
        if(!stack.is(thaumcraft.registry.R5Content.RUNIC_KEY.get())||!(level.getBlockEntity(pos) instanceof EldritchLockBlockEntity lock)||lock.count>=0)return net.minecraft.world.ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        if(!level.isClientSide){if(!level.mayInteract(player,pos))return net.minecraft.world.ItemInteractionResult.FAIL;lock.activate();stack.shrink(1);}
        return net.minecraft.world.ItemInteractionResult.sidedSuccess(level.isClientSide);
    }
}
