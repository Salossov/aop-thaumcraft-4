package thaumcraft.production;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.storage.loot.*;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.shapes.*;
import thaumcraft.registry.R3Content;

public final class MirrorBlock extends Block implements EntityBlock {
    public static final DirectionProperty FACING=BlockStateProperties.FACING;
    public MirrorBlock(){super(BlockBehaviour.Properties.of().strength(1,10).sound(SoundType.GLASS).noOcclusion().noCollission());registerDefaultState(stateDefinition.any().setValue(FACING,Direction.UP));}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder){builder.add(FACING);}
    @Override public BlockState getStateForPlacement(BlockPlaceContext context){return defaultBlockState().setValue(FACING,context.getClickedFace());}
    @Override public BlockEntity newBlockEntity(BlockPos pos,BlockState state){return new MirrorBlockEntity(pos,state);}
    @Override protected RenderShape getRenderShape(BlockState state){return RenderShape.ENTITYBLOCK_ANIMATED;}
    @Override protected VoxelShape getShape(BlockState state,BlockGetter level,BlockPos pos,CollisionContext context){return switch(state.getValue(FACING)){case UP->box(0,0,0,16,1,16);case DOWN->box(0,15,0,16,16,16);case NORTH->box(0,0,15,16,16,16);case SOUTH->box(0,0,0,16,16,1);case EAST->box(0,0,0,1,16,16);case WEST->box(15,0,0,16,16,16);};}
    @Override protected boolean canSurvive(BlockState state,LevelReader level,BlockPos pos){var face=state.getValue(FACING);var support=pos.relative(face.getOpposite());return level.getBlockState(support).isFaceSturdy(level,support,face);}
    @Override protected BlockState updateShape(BlockState state,Direction direction,BlockState neighbor,LevelAccessor level,BlockPos pos,BlockPos other){return direction==state.getValue(FACING).getOpposite()&&!canSurvive(state,level,pos)?Blocks.AIR.defaultBlockState():super.updateShape(state,direction,neighbor,level,pos,other);}
    @Override public void setPlacedBy(Level level,BlockPos pos,BlockState state,LivingEntity player,ItemStack stack){if(!level.isClientSide&&level.getBlockEntity(pos) instanceof MirrorBlockEntity tile){var link=MirrorLink.from(stack);if(link!=null)tile.setLink(link);}}
    @Override protected void entityInside(BlockState state,Level level,BlockPos pos,Entity entity){if(!level.isClientSide&&entity instanceof ItemEntity item&&level.getBlockEntity(pos) instanceof MirrorBlockEntity tile)tile.transport(item);}
    @Override protected List<ItemStack> getDrops(BlockState state,LootParams.Builder context){var stack=new ItemStack(asItem());if(context.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof MirrorBlockEntity tile&&tile.link()!=null&&tile.linked())tile.link().write(stack);return List.of(stack);}
    @Override protected void onRemove(BlockState state,Level level,BlockPos pos,BlockState next,boolean moving){if(state.getBlock()!=next.getBlock()&&level.getBlockEntity(pos) instanceof MirrorBlockEntity tile){tile.invalidateLink();if(!level.isClientSide)for(var stack:tile.outputs())popResource(level,pos,stack);}super.onRemove(state,level,pos,next,moving);}
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level,BlockState state,BlockEntityType<T> type){return type==R3Content.MIRROR_ENTITY.get()?level.isClientSide?(world,pos,block,tile)->{var mirror=(MirrorBlockEntity)tile;if(mirror.pulse>0)mirror.pulse--;}:(world,pos,block,tile)->((MirrorBlockEntity)tile).serverTick():null;}
}
