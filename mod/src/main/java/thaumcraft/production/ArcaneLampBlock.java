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
import thaumcraft.items.CastingWandItem;
import thaumcraft.registry.R3Content;

public final class ArcaneLampBlock extends Block implements EntityBlock {
    public enum Kind{LIGHT,GROWTH,FERTILITY}
    public static final DirectionProperty FACING=BlockStateProperties.FACING;
    public static final BooleanProperty LIT=BlockStateProperties.LIT;
    public final Kind kind;
    public ArcaneLampBlock(Kind kind){super(BlockBehaviour.Properties.of().strength(1,10).sound(SoundType.METAL).noOcclusion().lightLevel(state->kind==Kind.LIGHT||state.getValue(LIT)?15:8));this.kind=kind;registerDefaultState(stateDefinition.any().setValue(FACING,Direction.DOWN).setValue(LIT,kind==Kind.LIGHT));}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder){builder.add(FACING,LIT);}
    @Override public BlockState getStateForPlacement(BlockPlaceContext context){return defaultBlockState().setValue(FACING,context.getClickedFace().getOpposite());}
    @Override protected VoxelShape getShape(BlockState state,BlockGetter level,BlockPos pos,CollisionContext context){return box(4,2,4,12,14,12);}
    @Override public BlockEntity newBlockEntity(BlockPos pos,BlockState state){return new ArcaneLampBlockEntity(pos,state);}
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level,BlockState state,BlockEntityType<T> type){return !level.isClientSide&&type==R3Content.LAMP_ENTITY.get()?(world,pos,block,tile)->((ArcaneLampBlockEntity)tile).serverTick():null;}
    @Override protected ItemInteractionResult useItemOn(ItemStack stack,BlockState state,Level level,BlockPos pos,Player player,InteractionHand hand,BlockHitResult hit){if(!(stack.getItem() instanceof CastingWandItem))return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;if(!level.isClientSide)level.setBlock(pos,state.setValue(FACING,Direction.from3DDataValue((state.getValue(FACING).get3DDataValue()+1)%6)),3);return ItemInteractionResult.sidedSuccess(level.isClientSide);}
    @Override protected void onRemove(BlockState state,Level level,BlockPos pos,BlockState next,boolean moving){if(state.getBlock()!=next.getBlock()&&kind==Kind.LIGHT&&!level.isClientSide)for(var light:BlockPos.betweenClosed(pos.offset(-15,-15,-15),pos.offset(15,15,15))){if(level.hasChunkAt(light)&&level.getBlockEntity(light) instanceof LampLightBlockEntity tile&&pos.equals(tile.source()))level.removeBlock(light,false);}super.onRemove(state,level,pos,next,moving);}
}
