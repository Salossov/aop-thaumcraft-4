package thaumcraft.addon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.function.BooleanSupplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.GameMasterBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.BlockSnapshot;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.level.BlockEvent;
import thaumcraft.api.world.BlockReplacementApi;

/** Native onRemove and its drops remain provisional until protection and payment succeed. */
public final class ProtectedBlockReplacement implements BlockReplacementApi.Access {
    private static final Set<ServerLevel> ACTIVE=Collections.newSetFromMap(new IdentityHashMap<>());

    @Override public boolean replace(ServerPlayer player,ItemStack tool,BlockPos pos,Direction face,
                                     BlockState expected,BlockState replacement,BooleanSupplier payment){
        var level=player.serverLevel();
        if(!level.getServer().isSameThread())throw new IllegalStateException("Server thread required");
        if(!ACTIVE.add(level))return false;
        try{
            if(expected==replacement||!allowed(player,tool,pos,face)||level.getBlockState(pos)!=expected
                    ||!replacement.getBlock().isEnabled(level.enabledFeatures())
                    ||!mastersAllowed(player,expected)||!mastersAllowed(player,replacement))return false;
            var originalEntity=level.getBlockEntity(pos);var toolSnapshot=tool.copy();
            if(NeoForge.EVENT_BUS.post(new BlockEvent.BreakEvent(level,pos,expected,player)).isCanceled())return false;
            if(!allowed(player,tool,pos,face)||level.getBlockState(pos)!=expected||level.getBlockEntity(pos)!=originalEntity
                    ||!ItemStack.matches(tool,toolSnapshot))return false;
            return mutate(player,tool,pos,face,expected,replacement,toolSnapshot,payment);
        }finally{ACTIVE.remove(level);}
    }
    private static boolean mastersAllowed(ServerPlayer player,BlockState state){return !(state.getBlock() instanceof GameMasterBlock)||player.canUseGameMasterBlocks();}
    private static boolean allowed(ServerPlayer player,ItemStack tool,BlockPos pos,Direction face){
        var level=player.serverLevel();
        if(tool.isEmpty()||(player.getMainHandItem()!=tool&&player.getOffhandItem()!=tool)||player.isRemoved()||!player.isAlive()
                ||player.gameMode.getGameModeForPlayer()==GameType.SPECTATOR||!level.isInWorldBounds(pos)
                ||!level.getWorldBorder().isWithinBounds(pos))return false;
        // No synchronous request, including in permission/support preflight.
        for(int x=(pos.getX()-2)>>4;x<=(pos.getX()+2)>>4;x++)for(int z=(pos.getZ()-2)>>4;z<=(pos.getZ()+2)>>4;z++)
            if(level.getChunkSource().getChunkNow(x,z)==null)return false;
        return level.mayInteract(player,pos)&&player.mayUseItemAt(pos,face,tool);
    }
    private static boolean mutate(ServerPlayer player,ItemStack tool,BlockPos pos,Direction face,BlockState expected,
                                   BlockState replacement,ItemStack toolSnapshot,BooleanSupplier payment){
        var level=player.serverLevel();boolean outerCapture=level.captureBlockSnapshots;
        var outerSnapshots=new ArrayList<>(level.capturedBlockSnapshots);
        // Keep original inventory NBT even if a callback consumes its live slots.
        var initial=BlockSnapshot.create(level.dimension(),level,pos,Block.UPDATE_ALL);
        List<BlockSnapshot> changes=List.of();boolean committed=false;
        try(var effects=ReplacementEffects.open(level)){
            level.capturedBlockSnapshots.clear();level.captureBlockSnapshots=true;
            try{
                if(!level.setBlock(pos,replacement,Block.UPDATE_ALL)||level.getBlockState(pos)!=replacement)return false;
                changes=snapshots(initial,level.capturedBlockSnapshots);level.captureBlockSnapshots=false;
                if(!replacement.canSurvive(level,pos))return false;
                boolean canceled=changes.size()==1?EventHooks.onBlockPlace(player,changes.getFirst(),face):EventHooks.onMultiBlockPlace(player,changes,face);
                if(canceled||!allowed(player,tool,pos,face)||level.getBlockState(pos)!=replacement
                        ||!ItemStack.matches(tool,toolSnapshot)||!replacement.canSurvive(level,pos)||!payment.getAsBoolean())return false;
                committed=true;
                try{
                    for(var snapshot:changes){
                        var current=level.getBlockState(snapshot.getPos());
                        var chunk=level.getChunkSource().getChunkNow(snapshot.getPos().getX()>>4,snapshot.getPos().getZ()>>4);
                        if(chunk==null)throw new IllegalStateException("Prepared replacement chunk disappeared");
                        current.onPlace(level,snapshot.getPos(),snapshot.getState(),false);
                        level.markAndNotifyBlock(snapshot.getPos(),chunk,snapshot.getState(),current,snapshot.getFlags(),512);
                    }
                }finally{
                    // A later notification failure must not silently eat the
                    // inventory/XP of an already committed transformation.
                    effects.publish();
                }
                return true;
            }finally{
                level.captureBlockSnapshots=false;
                try{if(!committed)rollback(level,snapshots(initial,level.capturedBlockSnapshots));}
                finally{level.capturedBlockSnapshots.clear();level.capturedBlockSnapshots.addAll(outerSnapshots);level.captureBlockSnapshots=outerCapture;}
            }
        }
    }
    private static List<BlockSnapshot> snapshots(BlockSnapshot initial,List<BlockSnapshot> captured){
        var first=new LinkedHashMap<BlockPos,BlockSnapshot>();first.put(initial.getPos(),initial);
        for(var snapshot:captured)first.putIfAbsent(snapshot.getPos(),snapshot);
        return List.copyOf(first.values());
    }
    private static void rollback(ServerLevel level,List<BlockSnapshot> snapshots){
        boolean restoring=level.restoringBlockSnapshots;level.restoringBlockSnapshots=true;RuntimeException problem=null;
        try{
            for(int index=snapshots.size()-1;index>=0;index--){
                var snapshot=snapshots.get(index);var pos=snapshot.getPos();
                try{
                    if(level.getBlockState(pos)!=snapshot.getState())snapshot.restore((snapshot.getFlags()&~Block.UPDATE_NEIGHBORS)|Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE);
                    else if(snapshot.getTag()!=null){
                        if(level.getBlockEntity(pos)==null&&snapshot.getState().getBlock() instanceof EntityBlock factory){
                            var entity=factory.newBlockEntity(pos,snapshot.getState());
                            if(entity!=null)level.setBlockEntity(entity);
                        }
                        snapshot.restoreBlockEntity(level,pos);
                    }
                    if(level.getBlockState(pos)!=snapshot.getState()||snapshot.getTag()!=null&&level.getBlockEntity(pos)==null)
                        throw new IllegalStateException("Cannot restore protected replacement at "+pos);
                }catch(RuntimeException failure){if(problem==null)problem=failure;else problem.addSuppressed(failure);}
            }
        }finally{level.restoringBlockSnapshots=restoring;}
        for(var snapshot:snapshots){var pos=snapshot.getPos();var state=level.getBlockState(pos);level.sendBlockUpdated(pos,state,state,Block.UPDATE_ALL);level.updateNeighborsAt(pos,state.getBlock());}
        if(problem!=null)throw problem;
    }
}
