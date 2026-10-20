package thaumcraft.addon;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntPredicate;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.BlockSnapshot;
import net.neoforged.neoforge.event.EventHooks;
import thaumcraft.api.world.BlockPlacementApi;
import thaumcraft.mixin.BlockItemPlacementAccessor;

/** The scope affects only the matching native block-item placement call. */
public final class SelectedBlockPlacement implements BlockPlacementApi.Access {
    private static final class Scope {
        final BlockItem item;final BlockPlaceContext context;final BlockPlacementApi.Plan plan;boolean applied;
        Scope(BlockItem item,BlockPlaceContext context,BlockPlacementApi.Plan plan){this.item=item;this.context=context;this.plan=plan;}
    }
    private record Prepared(BlockItem item,BlockPlaceContext context,BlockPlacementApi.Plan plan){}
    private static final ThreadLocal<Scope> ACTIVE=new ThreadLocal<>();

    public static BlockState selectedState(BlockItem item,BlockPlaceContext context,Supplier<BlockState> original){
        var scope=ACTIVE.get();
        if(scope==null||scope.item!=item||scope.context.getItemInHand()!=context.getItemInHand()
                ||scope.context.getLevel()!=context.getLevel()||!scope.context.getClickedPos().equals(context.getClickedPos()))return original.get();
        scope.applied=true;
        var state=scope.plan.state();
        return ((BlockItemPlacementAccessor)item).thaumcraft$canPlace(context,state)?state:null;
    }

    @Override @Nullable public BlockPlacementApi.Plan plan(Player player,InteractionHand hand,BlockPos pos,Direction face,BlockState selected){
        var prepared=prepare(player,hand,pos,face,selected);return prepared==null?null:prepared.plan();
    }

    @Nullable private static Prepared prepare(Player player,InteractionHand hand,BlockPos pos,Direction face,BlockState selected){
        var level=player.level();
        if(selected.isAir()||selected.liquid()||!(selected.getBlock().asItem() instanceof BlockItem item)
                ||!loaded(level,pos)||!level.isInWorldBounds(pos)||!level.getWorldBorder().isWithinBounds(pos)
                ||!item.getBlock().isEnabled(level.enabledFeatures()))return null;
        var stack=new ItemStack(item);
        // Selection never carries a source block entity or arbitrary state-component data.
        stack.remove(DataComponents.BLOCK_ENTITY_DATA);stack.remove(DataComponents.BLOCK_STATE);
        var context=new SelectedContext(player,hand,stack,pos,face,selected);
        var effective=item.updatePlacementContext(context);
        if(effective==null||!effective.getClickedPos().equals(pos)||!effective.canPlace())return null;
        if(!player.mayUseItemAt(pos,face,stack)||!level.mayInteract(player,pos))return null;

        boolean wallVariant=item instanceof StandingAndWallBlockItem&&selected.getBlock()!=item.getBlock();
        // Paired standing/wall variants share a real block item. Other shared-item
        // states (e.g. filled cauldrons) use that item's native empty/default block.
        BlockState nativeState=wallVariant?selected.getBlock().getStateForPlacement(effective)
                :((BlockItemPlacementAccessor)item).thaumcraft$placementState(effective);
        if(nativeState==null)return null;
        BlockState state=orientation(nativeState,selected);
        int cost=1;
        if(state.is(selected.getBlock())){
            if(state.getBlock() instanceof SlabBlock){state=state.setValue(SlabBlock.TYPE,selected.getValue(SlabBlock.TYPE));cost=state.getValue(SlabBlock.TYPE)==SlabType.DOUBLE?2:1;if(cost==2)state=state.setValue(SlabBlock.WATERLOGGED,false);}
            else if(state.getBlock() instanceof SnowLayerBlock){cost=selected.getValue(SnowLayerBlock.LAYERS);state=state.setValue(SnowLayerBlock.LAYERS,cost);}
            else if(state.getBlock() instanceof CandleBlock){cost=selected.getValue(CandleBlock.CANDLES);state=state.setValue(CandleBlock.CANDLES,cost);}
            else if(state.getBlock() instanceof SeaPickleBlock){cost=selected.getValue(SeaPickleBlock.PICKLES);state=state.setValue(SeaPickleBlock.PICKLES,cost);}
            else if(state.getBlock() instanceof TurtleEggBlock){cost=selected.getValue(TurtleEggBlock.EGGS);state=state.setValue(TurtleEggBlock.EGGS,cost);}
            else if(state.getBlock() instanceof PinkPetalsBlock){cost=selected.getValue(PinkPetalsBlock.AMOUNT);state=state.setValue(PinkPetalsBlock.AMOUNT,cost);}
        }
        if(!((BlockItemPlacementAccessor)item).thaumcraft$canPlace(effective,state))return null;
        var parts=new ArrayList<BlockPlacementApi.Part>();parts.add(new BlockPlacementApi.Part(pos,state));
        if(state.getBlock() instanceof DoorBlock)parts.add(new BlockPlacementApi.Part(pos.above(),state.setValue(DoorBlock.HALF,DoubleBlockHalf.UPPER)));
        else if(state.getBlock() instanceof DoublePlantBlock)parts.add(new BlockPlacementApi.Part(pos.above(),state.setValue(DoublePlantBlock.HALF,DoubleBlockHalf.UPPER)));
        else if(state.getBlock() instanceof BedBlock)parts.add(new BlockPlacementApi.Part(pos.relative(state.getValue(BedBlock.FACING)),state.setValue(BedBlock.PART,BedPart.HEAD)));
        for(var part:parts)if(!loaded(level,part.pos())||!level.isInWorldBounds(part.pos())||!level.getWorldBorder().isWithinBounds(part.pos())
                ||!level.mayInteract(player,part.pos())||!player.mayUseItemAt(part.pos(),face,stack))return null;
        return new Prepared(item,context,new BlockPlacementApi.Plan(state,cost,parts));
    }

    private static boolean loaded(Level level,BlockPos pos){
        for(int x=(pos.getX()-2)>>4;x<=(pos.getX()+2)>>4;x++)for(int z=(pos.getZ()-2)>>4;z<=(pos.getZ()+2)>>4;z++){
            if(level instanceof ServerLevel server){if(server.getChunkSource().getChunkNow(x,z)==null)return false;}
            else if(!level.hasChunk(x,z))return false;
        }
        return true;
    }

    private static BlockState orientation(BlockState nativeState,BlockState selected){
        if(!nativeState.is(selected.getBlock()))return nativeState;
        var result=nativeState;
        for(var property:selected.getProperties()){
            // Assembly half, connections, power, age, fluids and occupancy are
            // intentionally native. Half here is stairs/trapdoors, not DoubleBlockHalf.
            var value=selected.getValue(property);
            if(value instanceof Direction||value instanceof Direction.Axis||value instanceof Half||value instanceof AttachFace
                    ||property==BlockStateProperties.ROTATION_16)result=copy(result,selected,property);
        }
        return result;
    }
    private static <T extends Comparable<T>> BlockState copy(BlockState to,BlockState from,Property<T> property){return to.hasProperty(property)?to.setValue(property,from.getValue(property)):to;}

    @Override public boolean place(Player player,InteractionHand hand,BlockPos pos,Direction face,BlockState selected,IntPredicate debit){
        if(!(player.level() instanceof ServerLevel level))return false;
        if(!level.getServer().isSameThread())throw new IllegalStateException("Server thread required");
        var prepared=prepare(player,hand,pos,face,selected);if(prepared==null)return false;
        boolean outerCapture=level.captureBlockSnapshots;
        var outerSnapshots=new ArrayList<BlockSnapshot>(level.capturedBlockSnapshots);
        var outerScope=ACTIVE.get();
        level.capturedBlockSnapshots.clear();level.captureBlockSnapshots=true;
        List<BlockSnapshot> snapshots=List.of();boolean committed=false;
        try{
            var scope=new Scope(prepared.item(),prepared.context(),prepared.plan());ACTIVE.set(scope);
            // Native place preserves specialised items, setPlacedBy and complete
            // assemblies. Bypass the non-nestable ItemStack snapshot wrapper.
            var result=prepared.item().place(prepared.context());
            if(outerScope==null)ACTIVE.remove();else ACTIVE.set(outerScope);
            snapshots=List.copyOf(level.capturedBlockSnapshots);level.captureBlockSnapshots=false;
            if(!scope.applied||!result.consumesAction()||snapshots.isEmpty())return false;
            boolean canceled=snapshots.size()==1?EventHooks.onBlockPlace(player,snapshots.get(0),face):EventHooks.onMultiBlockPlace(player,snapshots,face);
            if(canceled||!debit.test(prepared.plan().cost()))return false;
            committed=true;
            for(var snapshot:snapshots){
                var previous=snapshot.getState();var current=level.getBlockState(snapshot.getPos());
                current.onPlace(level,snapshot.getPos(),previous,false);
                level.markAndNotifyBlock(snapshot.getPos(),level.getChunkAt(snapshot.getPos()),previous,current,snapshot.getFlags(),512);
            }
            return true;
        }finally{
            if(outerScope==null)ACTIVE.remove();else ACTIVE.set(outerScope);
            level.captureBlockSnapshots=false;
            try{
                if(!committed){
                    if(snapshots.isEmpty())snapshots=List.copyOf(level.capturedBlockSnapshots);
                    boolean restoring=level.restoringBlockSnapshots;level.restoringBlockSnapshots=true;
                    try{for(int i=snapshots.size()-1;i>=0;i--)snapshots.get(i).restore(snapshots.get(i).getFlags()|Block.UPDATE_CLIENTS);}
                    finally{level.restoringBlockSnapshots=restoring;}
                }
            }finally{level.capturedBlockSnapshots.clear();level.capturedBlockSnapshots.addAll(outerSnapshots);level.captureBlockSnapshots=outerCapture;}
        }
    }

    /** Supplies selected orientation to native geometry/hinge/connection calculations. */
    private static final class SelectedContext extends BlockPlaceContext {
        private final BlockPos target;
        private Direction horizontal;
        private final Direction looking;
        private final float rotation;
        SelectedContext(Player player,InteractionHand hand,ItemStack stack,BlockPos pos,Direction face,BlockState selected){
            super(player.level(),player,hand,stack,hit(pos,face,selected));target=pos.immutable();
            horizontal=player.getDirection();looking=selected.hasProperty(BlockStateProperties.FACING)?selected.getValue(BlockStateProperties.FACING).getOpposite():horizontal;
            rotation=selected.hasProperty(BlockStateProperties.ROTATION_16)?selected.getValue(BlockStateProperties.ROTATION_16)*22.5F:player.getYRot();
            if(selected.hasProperty(BlockStateProperties.HORIZONTAL_FACING)){
                var desired=selected.getValue(BlockStateProperties.HORIZONTAL_FACING);
                horizontal=desired;
                var block=selected.getBlock();
                if(!(block instanceof DoorBlock||block instanceof BedBlock||block instanceof StairBlock)){
                    var probe=block.getStateForPlacement(this);
                    if(probe!=null&&probe.hasProperty(BlockStateProperties.HORIZONTAL_FACING)&&probe.getValue(BlockStateProperties.HORIZONTAL_FACING)==desired.getOpposite())horizontal=desired.getOpposite();
                }
            }
        }
        private static BlockHitResult hit(BlockPos pos,Direction original,BlockState state){
            var face=original;
            if(state.hasProperty(BlockStateProperties.AXIS))face=Direction.fromAxisAndDirection(state.getValue(BlockStateProperties.AXIS),Direction.AxisDirection.POSITIVE);
            if(state.hasProperty(BlockStateProperties.HALF))face=state.getValue(BlockStateProperties.HALF)==Half.TOP?Direction.DOWN:Direction.UP;
            if(state.hasProperty(BlockStateProperties.ATTACH_FACE))face=switch(state.getValue(BlockStateProperties.ATTACH_FACE)){case FLOOR->Direction.UP;case CEILING->Direction.DOWN;case WALL->state.getValue(BlockStateProperties.HORIZONTAL_FACING);};
            if(state.getBlock() instanceof SlabBlock&&state.getValue(SlabBlock.TYPE)!=SlabType.DOUBLE)face=state.getValue(SlabBlock.TYPE)==SlabType.TOP?Direction.DOWN:Direction.UP;
            return new BlockHitResult(Vec3.atCenterOf(pos).add(0,face==Direction.DOWN?.25:-.25,0),face,pos,false);
        }
        @Override public BlockPos getClickedPos(){return target==null?super.getClickedPos():target;}
        @Override public Direction getHorizontalDirection(){return horizontal==null?getPlayer().getDirection():horizontal;}
        @Override public float getRotation(){return rotation;}
        @Override public Direction getNearestLookingDirection(){return looking==null?super.getNearestLookingDirection():looking;}
        @Override public Direction[] getNearestLookingDirections(){
            var first=getNearestLookingDirection();var result=new Direction[6];result[0]=first;int i=1;
            for(var direction:Direction.values())if(direction!=first)result[i++]=direction;
            return result;
        }
        @Override public boolean canPlace(){return getLevel().getBlockState(getClickedPos()).canBeReplaced(this);}
        @Override public boolean replacingClickedOnBlock(){return true;}
    }
}
