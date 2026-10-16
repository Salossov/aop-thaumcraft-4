package thaumcraft.production;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.shapes.*;
import thaumcraft.items.WandVis;

/** Each shell block records its original position; no second inventory is created. */
public final class InfernalFurnaceBlock extends BaseEntityBlock {
    public static final IntegerProperty PART=IntegerProperty.create("part",0,26);
    public static final DirectionProperty FACING=BlockStateProperties.HORIZONTAL_FACING;
    private static boolean replacing;
    public InfernalFurnaceBlock(){super(Properties.of().strength(3.5f).noOcclusion().lightLevel(state->state.getValue(PART)==13?15:0));registerDefaultState(stateDefinition.any().setValue(PART,13).setValue(FACING,Direction.NORTH));}
    @Override protected MapCodec<? extends BaseEntityBlock> codec(){return simpleCodec(properties->new InfernalFurnaceBlock());}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder){builder.add(PART,FACING);}
    @Override protected RenderShape getRenderShape(BlockState state){return RenderShape.MODEL;}
    @Override public BlockEntity newBlockEntity(BlockPos pos,BlockState state){return new InfernalFurnaceBlockEntity(pos,state);}
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level,BlockState state,BlockEntityType<T> type){return createTickerHelper(type,thaumcraft.registry.R3Content.INFERNAL_ENTITY.get(),InfernalFurnaceBlockEntity::tick);}
    @Override protected VoxelShape getShape(BlockState state,BlockGetter level,BlockPos pos,CollisionContext context){return Shapes.block();}
    @Override protected VoxelShape getCollisionShape(BlockState state,BlockGetter level,BlockPos pos,CollisionContext context){int part=state.getValue(PART);if(part==13)return box(0,0,0,16,4,16);var offset=offset(part);var facing=state.getValue(FACING);if(offset.getY()!=0||offset.getX()!=facing.getStepX()||offset.getZ()!=facing.getStepZ())return Shapes.block();return switch(facing){case NORTH->box(0,0,0,16,16,8);case SOUTH->box(0,0,8,16,16,16);case WEST->box(0,0,0,8,16,16);default->box(8,0,0,16,16,16);};}
    public static BlockPos offset(int part){return new BlockPos(part%3-1,part/9-1,part/3%3-1);}
    public static BlockPos core(BlockPos pos,BlockState state){return pos.subtract(offset(state.getValue(PART)));}
    public static BlockState original(int part,Direction output){var offset=offset(part);if(part==22)return Blocks.AIR.defaultBlockState();if(part==13)return Blocks.LAVA.defaultBlockState();if(offset.getY()==0&&offset.getX()==output.getStepX()&&offset.getZ()==output.getStepZ())return Blocks.IRON_BARS.defaultBlockState();return Math.abs(offset.getX())==1&&Math.abs(offset.getZ())==1?Blocks.NETHER_BRICKS.defaultBlockState():Blocks.OBSIDIAN.defaultBlockState();}
    public static Direction fits(Level level,BlockPos core){Direction fence=null;for(int part=0;part<27;part++){var offset=offset(part);var pos=core.offset(offset);if(!level.hasChunkAt(pos))return null;var state=level.getBlockState(pos);if(part==22){if(!state.isAir())return null;continue;}if(part==13){if(!state.is(Blocks.LAVA))return null;continue;}if(offset.getY()==0&&(offset.getX()==0||offset.getZ()==0)&&state.is(Blocks.IRON_BARS)){if(fence!=null)return null;fence=offset.getX()!=0?(offset.getX()<0?Direction.WEST:Direction.EAST):(offset.getZ()<0?Direction.NORTH:Direction.SOUTH);continue;}if(!state.is(original(part,Direction.UP).getBlock()))return null;}return fence;}
    public static boolean activate(ServerPlayer player,ItemStack wand,BlockPos clicked){var level=player.serverLevel();for(var candidate:BlockPos.betweenClosed(clicked.offset(-1,-1,-1),clicked.offset(1,1,1))){var center=candidate.immutable();var direction=fits(level,center);if(direction==null)continue;if(!thaumcraft.knowledge.KnowledgeService.get(player).completedResearch().contains("INFERNALFURNACE")){player.displayClientMessage(net.minecraft.network.chat.Component.translatable("tc.infernal.research_required"),true);return false;}for(int part=0;part<27;part++)if(!level.mayInteract(player,center.offset(offset(part))))return false;if(!WandVis.consume(wand,java.util.Map.of("ignis",50,"terra",50),WandVis.discount(player),true)){player.displayClientMessage(net.minecraft.network.chat.Component.translatable("tc.infernal.vis_required"),true);return false;}replacing=true;try{for(int part=0;part<27;part++)if(part!=22)level.setBlock(center.offset(offset(part)),thaumcraft.registry.R3Content.INFERNAL_FURNACE.get().defaultBlockState().setValue(PART,part).setValue(FACING,direction),2);for(int part=0;part<27;part++)if(part!=22){var pos=center.offset(offset(part));level.sendBlockUpdated(pos,level.getBlockState(pos),level.getBlockState(pos),3);}}finally{replacing=false;}level.playSound(null,center,thaumcraft.registry.R2Sounds.CRAFT_FINISH.get(),net.minecraft.sounds.SoundSource.BLOCKS,1,1);return true;}return false;}
    public static void restore(Level level,BlockPos center,BlockPos skip,Direction direction){if(replacing||level.isClientSide)return;replacing=true;try{for(int part=0;part<27;part++){var pos=center.offset(offset(part));if(pos.equals(skip)||!level.hasChunkAt(pos)||!level.getBlockState(pos).is(thaumcraft.registry.R3Content.INFERNAL_FURNACE.get()))continue;if(level.getBlockEntity(pos) instanceof InfernalFurnaceBlockEntity tile&&part==13)net.minecraft.world.Containers.dropContents(level,pos,tile.items);level.setBlockAndUpdate(pos,original(part,direction));}}finally{replacing=false;}}
    @Override protected void onRemove(BlockState state,Level level,BlockPos pos,BlockState next,boolean moving){if(!state.is(next.getBlock())&&!replacing&&!level.isClientSide){if(state.getValue(PART)==13&&level.getBlockEntity(pos) instanceof InfernalFurnaceBlockEntity tile)net.minecraft.world.Containers.dropContents(level,pos,tile.items);restore(level,core(pos,state),pos,state.getValue(FACING));}super.onRemove(state,level,pos,next,moving);}
    @Override protected void entityInside(BlockState state,Level level,BlockPos pos,Entity entity){if(state.getValue(PART)!=13)return;var center=pos.getCenter();var motion=entity.getDeltaMovement();entity.setDeltaMovement(motion.add(entity.getX()<center.x-.2?.0001:entity.getX()>center.x+.2?-.0001:0,0,entity.getZ()<center.z-.2?.0001:entity.getZ()>center.z+.2?-.0001:0));if(!level.isClientSide&&entity instanceof ItemEntity item){entity.setDeltaMovement(entity.getDeltaMovement().x,.025,entity.getDeltaMovement().z);if(item.onGround()&&level.getBlockEntity(pos) instanceof InfernalFurnaceBlockEntity tile)tile.eat(item);}else if(!level.isClientSide&&entity instanceof LivingEntity living&&!living.fireImmune()){living.hurt(level.damageSources().inFire(),3);living.igniteForSeconds(10);}}
}
