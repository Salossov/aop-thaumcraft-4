package thaumcraft.production;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.BlockHitResult;
import thaumcraft.registry.R3Content;
public final class ThaumatoriumBlock extends Block implements EntityBlock {
 public static final EnumProperty<DoubleBlockHalf> HALF=BlockStateProperties.DOUBLE_BLOCK_HALF;
 public static final DirectionProperty FACING=BlockStateProperties.FACING;
 public ThaumatoriumBlock(){super(Properties.of().strength(3,15).noOcclusion().sound(SoundType.METAL));registerDefaultState(stateDefinition.any().setValue(HALF,DoubleBlockHalf.LOWER).setValue(FACING,Direction.NORTH));}
 @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(HALF,FACING);}
 public static ThaumatoriumBlockEntity main(Level l,BlockPos p,BlockState s){var lower=s.getValue(HALF)==DoubleBlockHalf.UPPER?p.below():p;return l.hasChunkAt(lower)&&l.getBlockEntity(lower) instanceof ThaumatoriumBlockEntity tile?tile:null;}
 public static boolean valid(Level l,BlockPos lower){return l.getBlockState(lower).is(R3Content.THAUMATORIUM.get())&&l.getBlockState(lower.above()).is(R3Content.THAUMATORIUM.get())&&l.getBlockState(lower.above()).getValue(HALF)==DoubleBlockHalf.UPPER&&l.getBlockState(lower.below()).is(R3Content.CRUCIBLE.get());}
 @Override public BlockEntity newBlockEntity(BlockPos p,BlockState s){return s.getValue(HALF)==DoubleBlockHalf.LOWER?new ThaumatoriumBlockEntity(p,s):null;}
 @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level l,BlockState s,BlockEntityType<T> t){return !l.isClientSide&&t==R3Content.THAUMATORIUM_ENTITY.get()?(level,p,state,tile)->((ThaumatoriumBlockEntity)tile).serverTick():null;}
 private InteractionResult open(Level l,BlockPos p,BlockState s,Player player){var tile=main(l,p,s);if(tile==null)return InteractionResult.PASS;if(player instanceof ServerPlayer server)server.openMenu(tile);return InteractionResult.sidedSuccess(l.isClientSide);}
 @Override protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos p,Player player,BlockHitResult hit){return open(l,p,s,player);}
 @Override protected ItemInteractionResult useItemOn(ItemStack stack,BlockState s,Level l,BlockPos p,Player player,InteractionHand hand,BlockHitResult hit){return open(l,p,s,player).consumesAction()?ItemInteractionResult.sidedSuccess(l.isClientSide):ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;}
 @Override protected BlockState updateShape(BlockState s,Direction d,BlockState adjacent,LevelAccessor l,BlockPos p,BlockPos q){boolean upper=s.getValue(HALF)==DoubleBlockHalf.UPPER;if(d==(upper?Direction.DOWN:Direction.UP)&&(!adjacent.is(this)||adjacent.getValue(HALF)==s.getValue(HALF)))return R3Content.ALCHEMICAL_CONSTRUCT.get().defaultBlockState();if(!upper&&d==Direction.DOWN&&!adjacent.is(R3Content.CRUCIBLE.get()))return R3Content.ALCHEMICAL_CONSTRUCT.get().defaultBlockState();return super.updateShape(s,d,adjacent,l,p,q);}
 @Override protected void onRemove(BlockState s,Level l,BlockPos p,BlockState n,boolean moving){if(s.getBlock()!=n.getBlock()&&l.getBlockEntity(p) instanceof ThaumatoriumBlockEntity tile)Containers.dropContents(l,p,tile);super.onRemove(s,l,p,n,moving);}
}
