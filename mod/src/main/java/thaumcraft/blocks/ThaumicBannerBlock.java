package thaumcraft.blocks;

import java.util.List;
import net.minecraft.core.*;
import net.minecraft.sounds.*;
import net.minecraft.util.Mth;
import net.minecraft.world.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.*;
import thaumcraft.items.ThaumicBannerItem;
import thaumcraft.registry.R3Content;

public final class ThaumicBannerBlock extends Block implements EntityBlock {
    public static final IntegerProperty ROTATION=BlockStateProperties.ROTATION_16;
    public static final BooleanProperty WALL=BooleanProperty.create("wall");
    public ThaumicBannerBlock(){super(Properties.of().strength(2.5f,10).sound(SoundType.WOOD).noOcclusion().noCollission());registerDefaultState(stateDefinition.any().setValue(ROTATION,0).setValue(WALL,false));}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(ROTATION,WALL);}
    @Override public BlockState getStateForPlacement(BlockPlaceContext c){var side=c.getClickedFace();int rotation=side.getAxis()==Direction.Axis.Y?Mth.floor((c.getRotation()+180)*16/360+.5)&15:switch(side){case NORTH->8;case WEST->4;case EAST->12;default->0;};return defaultBlockState().setValue(ROTATION,rotation).setValue(WALL,side.getAxis()!=Direction.Axis.Y);}
    @Override public void setPlacedBy(Level l,BlockPos p,BlockState s,LivingEntity owner,ItemStack stack){if(l.getBlockEntity(p) instanceof ThaumicBannerBlockEntity tile){tile.color=ThaumicBannerItem.color(stack);tile.aspect=ThaumicBannerItem.aspect(stack);tile.sync();}}
    @Override public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new ThaumicBannerBlockEntity(p,s);}
    @Override protected RenderShape getRenderShape(BlockState s){return RenderShape.ENTITYBLOCK_ANIMATED;}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){if(!s.getValue(WALL))return box(5.28,0,5.28,10.56,32,10.56);return switch(s.getValue(ROTATION)){case 8->box(0,0,12,16,32,16);case 12->box(0,0,0,4,32,16);case 4->box(12,0,0,16,32,16);default->box(0,0,0,16,32,4);};}
    @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return Shapes.empty();}
    private static void decorate(Level l,BlockPos p,Player player,ItemStack stack){if(!l.isClientSide&&l.getBlockEntity(p) instanceof ThaumicBannerBlockEntity tile&&tile.color>=0){if(player.isShiftKeyDown())tile.aspect=null;else {var aspect=R3Content.aspect(stack);if(aspect!=null){tile.aspect=aspect;stack.shrink(1);}}tile.sync();l.playSound(null,p,SoundEvents.WOOL_STEP,SoundSource.BLOCKS,1,1);}}
    @Override protected ItemInteractionResult useItemOn(ItemStack stack,BlockState s,Level l,BlockPos p,Player player,InteractionHand hand,BlockHitResult hit){if(player.isShiftKeyDown()||stack.is(R3Content.ESSENTIA_PHIAL.get())||stack.is(R3Content.EMPTY_PHIAL.get())){decorate(l,p,player,stack);return ItemInteractionResult.sidedSuccess(l.isClientSide);}return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;}
    @Override protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos p,Player player,BlockHitResult hit){if(player.isShiftKeyDown())decorate(l,p,player,ItemStack.EMPTY);return InteractionResult.sidedSuccess(l.isClientSide);}
    @Override protected List<ItemStack> getDrops(BlockState s,LootParams.Builder params){return params.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof ThaumicBannerBlockEntity tile?List.of(tile.item()):List.of(ThaumicBannerItem.stack(-1,null));}
    @Override public BlockState playerWillDestroy(Level l,BlockPos p,BlockState s,Player player){if(player.isCreative()&&!l.isClientSide&&l.getBlockEntity(p) instanceof ThaumicBannerBlockEntity tile)popResource(l,p,tile.item());return super.playerWillDestroy(l,p,s,player);}
    @Override public ItemStack getCloneItemStack(BlockState s,net.minecraft.world.phys.HitResult hit,LevelReader l,BlockPos p,Player player){return l.getBlockEntity(p) instanceof ThaumicBannerBlockEntity tile?tile.item():ThaumicBannerItem.stack(-1,null);}
    @Override protected BlockState rotate(BlockState s,Rotation r){return s.setValue(ROTATION,r.rotate(s.getValue(ROTATION),16));}
    @Override protected BlockState mirror(BlockState s,Mirror m){return s.setValue(ROTATION,m.mirror(s.getValue(ROTATION),16));}
}
