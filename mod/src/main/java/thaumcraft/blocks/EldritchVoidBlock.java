package thaumcraft.blocks;

import net.minecraft.core.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.*;

/** TC4's opaque, unselectable void with its inset collision and out-of-world damage. */
public final class EldritchVoidBlock extends Block implements EntityBlock {
    public static final BooleanProperty EXPOSED=BooleanProperty.create("exposed");
    private static final VoxelShape COLLISION=box(2,2,2,14,14,14);
    public EldritchVoidBlock(){super(Properties.of().strength(-1,3600000).sound(SoundType.WOOL).lightLevel(s->3).noLootTable().pushReaction(PushReaction.BLOCK).isValidSpawn((s,l,p,t)->false));registerDefaultState(stateDefinition.any().setValue(EXPOSED,false));}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder){builder.add(EXPOSED);}
    @Override protected VoxelShape getShape(BlockState state,BlockGetter level,BlockPos pos,CollisionContext context){return Shapes.empty();}
    @Override protected VoxelShape getCollisionShape(BlockState state,BlockGetter level,BlockPos pos,CollisionContext context){return COLLISION;}
    @Override protected VoxelShape getOcclusionShape(BlockState state,BlockGetter level,BlockPos pos){return Shapes.block();}
    @Override protected RenderShape getRenderShape(BlockState state){return RenderShape.INVISIBLE;}
    @Override public ItemStack getCloneItemStack(BlockState state,HitResult hit,LevelReader level,BlockPos pos,Player player){return ItemStack.EMPTY;}
    @Override protected void entityInside(BlockState state,Level level,BlockPos pos,Entity entity){if(!level.isClientSide&&entity.tickCount>20&&(!(entity instanceof Player player)||!player.getAbilities().instabuild))entity.hurt(level.damageSources().fellOutOfWorld(),8);}
    @Override public BlockEntity newBlockEntity(BlockPos pos,BlockState state){return state.getValue(EXPOSED)?new EldritchVoidBlockEntity(pos,state):null;}
    @Override protected void neighborChanged(BlockState state,Level level,BlockPos pos,Block block,BlockPos from,boolean moving){updateExposure(level,pos);}
    public static boolean exposed(BlockGetter level,BlockPos pos){for(var face:Direction.values()){var neighbor=pos.relative(face);if(!level.getBlockState(neighbor).isSolidRender(level,neighbor))return true;}return false;}
    public static void updateExposure(Level level,BlockPos pos){var state=level.getBlockState(pos);if(!(state.getBlock() instanceof EldritchVoidBlock block))return;boolean visible=exposed(level,pos);if(visible){if(!state.getValue(EXPOSED)){state=state.setValue(EXPOSED,true);level.setBlock(pos,state,2);}if(!(level.getBlockEntity(pos) instanceof EldritchVoidBlockEntity))level.setBlockEntity(new EldritchVoidBlockEntity(pos,state));}else{level.removeBlockEntity(pos);if(state.getValue(EXPOSED)){state=state.setValue(EXPOSED,false);level.setBlock(pos,state,2);}}}
}
