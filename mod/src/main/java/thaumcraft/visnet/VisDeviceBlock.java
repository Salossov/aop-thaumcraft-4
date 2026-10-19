package thaumcraft.visnet;

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

public final class VisDeviceBlock extends Block implements EntityBlock {
    public enum Kind { RELAY, CHARGER, CONVERTER, STABILIZER, ADVANCED_STABILIZER, ENERGIZED, WAND_PEDESTAL, RESONATOR }
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public final Kind kind;
    public VisDeviceBlock(Kind kind) {
        super(Properties.of().strength(kind==Kind.ENERGIZED?-1:3,kind==Kind.ENERGIZED?3600000:15).noOcclusion().sound(SoundType.STONE).lightLevel(state -> kind==Kind.ENERGIZED?7:0));
        this.kind=kind; registerDefaultState(stateDefinition.any().setValue(FACING,Direction.UP));
    }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder) { builder.add(FACING); }
    @Override public BlockState getStateForPlacement(BlockPlaceContext context) { return defaultBlockState().setValue(FACING,kind==Kind.RELAY?context.getClickedFace():kind==Kind.CHARGER?Direction.DOWN:Direction.UP); }
    @Override public BlockEntity newBlockEntity(BlockPos pos,BlockState state) { return switch(kind) { case RELAY,CHARGER -> new VisRelayBlockEntity(pos,state);case CONVERTER -> new NodeConverterBlockEntity(pos,state);case STABILIZER,ADVANCED_STABILIZER -> new NodeStabilizerBlockEntity(pos,state);case ENERGIZED -> new EnergizedNodeBlockEntity(pos,state);case WAND_PEDESTAL -> new WandPedestalBlockEntity(pos,state);default -> null; }; }
    @Override protected RenderShape getRenderShape(BlockState state) { return switch(kind){case ENERGIZED,RELAY,CHARGER,CONVERTER,STABILIZER,ADVANCED_STABILIZER->RenderShape.ENTITYBLOCK_ANIMATED;default->RenderShape.MODEL;}; }
    @Override protected VoxelShape getShape(BlockState state,BlockGetter level,BlockPos pos,CollisionContext context) { return switch(kind) {case ENERGIZED -> box(4.8,4.8,4.8,11.2,11.2,11.2); case RELAY,CHARGER -> box(4,4,4,12,12,12);case CONVERTER->box(0,8,0,16,16,16);case STABILIZER,ADVANCED_STABILIZER -> box(0,0,0,16,8,16);default -> Shapes.or(box(0,0,0,16,3,16),box(5,3,5,11,13,11),box(2,13,2,14,16,14));}; }
    @Override protected VoxelShape getCollisionShape(BlockState state,BlockGetter level,BlockPos pos,CollisionContext context){return kind==Kind.ENERGIZED?Shapes.empty():super.getCollisionShape(state,level,pos,context);}
    @Override protected ItemInteractionResult useItemOn(ItemStack stack,BlockState state,Level level,BlockPos pos,Player player,InteractionHand hand,BlockHitResult hit) {
        if (!player.isShiftKeyDown() && level.getBlockEntity(pos) instanceof VisRelayBlockEntity relay) {
            int shard=stack.is(R3Content.BALANCED_SHARD.get())?6:-1;
            for(int i=0;i<thaumcraft.registry.R2Content.SHARDS.size();i++)if(stack.is(thaumcraft.registry.R2Content.SHARDS.get(i).get()))shard=i;
            if(shard>=0){if(!level.isClientSide){relay.attune(shard);player.displayClientMessage(net.minecraft.network.chat.Component.translatable("thaumcraft.vis_relay.configured",relay.attunementName()).append(" (" ).append(net.minecraft.network.chat.Component.translatable(relay.getRootSource()==null?"thaumcraft.vis_relay.disconnected":"thaumcraft.vis_relay.connected")).append(")"),true);level.playSound(null,pos,thaumcraft.registry.R2Sounds.CRYSTAL.get(),net.minecraft.sounds.SoundSource.BLOCKS,.2f,1);}return ItemInteractionResult.sidedSuccess(level.isClientSide);}
        }
        if (level.getBlockEntity(pos) instanceof WandPedestalBlockEntity pedestal && pedestal.isEmpty() && WandPedestalBlockEntity.accepts(stack)) { if(!level.isClientSide){pedestal.setItem(0,stack.copyWithCount(1));if(!player.isCreative())stack.shrink(1);}return ItemInteractionResult.sidedSuccess(level.isClientSide); }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }
    @Override protected InteractionResult useWithoutItem(BlockState state,Level level,BlockPos pos,Player player,BlockHitResult hit) {
        if (level.getBlockEntity(pos) instanceof WandPedestalBlockEntity pedestal && !pedestal.isEmpty()) { if(!level.isClientSide){var stack=pedestal.removeItem(0,1);if(!player.getInventory().add(stack))player.drop(stack,false);}return InteractionResult.sidedSuccess(level.isClientSide); }return InteractionResult.PASS;
    }
    @Override protected void onRemove(BlockState state,Level level,BlockPos pos,BlockState next,boolean moving) {
        if (state.getBlock()!=next.getBlock()) {
            if(level.getBlockEntity(pos) instanceof WandPedestalBlockEntity pedestal)Containers.dropContents(level,pos,pedestal);
            if(kind==Kind.CONVERTER && level.getBlockEntity(pos.below()) instanceof EnergizedNodeBlockEntity)NodeConverterBlockEntity.explodeNode(level,pos.below());
        }
        super.onRemove(state,level,pos,next,moving);
    }
    public static int lock(Level level,BlockPos node) {
        var pos=node.below(); if(!level.hasChunkAt(pos)||level.hasNeighborSignal(pos))return 0;
        var state=level.getBlockState(pos);
        if(state.is(R3Content.NODE_STABILIZER.get()))return 1;
        return state.is(R3Content.ADVANCED_NODE_STABILIZER.get())?2:0;
    }
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level,BlockState state,BlockEntityType<T> type) {
        if(level.isClientSide&&kind==Kind.CONVERTER&&type==R3Content.NODE_CONVERTER_ENTITY.get())return (l,p,s,t)->((NodeConverterBlockEntity)t).clientTick();
        if(level.isClientSide)return (kind==Kind.STABILIZER||kind==Kind.ADVANCED_STABILIZER)&&type==R3Content.NODE_STABILIZER_ENTITY.get()?(l,p,s,t)->((NodeStabilizerBlockEntity)t).clientTick():null;
        if((kind==Kind.RELAY||kind==Kind.CHARGER) && type==R3Content.VIS_RELAY_ENTITY.get())return (l,p,s,t)->((VisRelayBlockEntity)t).serverTick();
        if(kind==Kind.ENERGIZED && type==R3Content.ENERGIZED_NODE_ENTITY.get())return (l,p,s,t)->((EnergizedNodeBlockEntity)t).serverTick();
        if(kind==Kind.CONVERTER && type==R3Content.NODE_CONVERTER_ENTITY.get())return (l,p,s,t)->((NodeConverterBlockEntity)t).serverTick();
        if(kind==Kind.WAND_PEDESTAL && type==R3Content.WAND_PEDESTAL_ENTITY.get())return (l,p,s,t)->((WandPedestalBlockEntity)t).serverTick();
        return null;
    }
}
