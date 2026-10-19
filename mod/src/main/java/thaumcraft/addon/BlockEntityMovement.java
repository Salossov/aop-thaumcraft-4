package thaumcraft.addon;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import thaumcraft.api.world.BlockEntityMovementApi;
import thaumcraft.mixin.BlockEntityPositionAccessor;

/** Native chunk/ticker/listener bookkeeping; never reconstructs the moving object. */
public final class BlockEntityMovement implements BlockEntityMovementApi.Access {
    private static void serverThread(ServerLevel level){if(!level.getServer().isSameThread())throw new IllegalStateException("Block entity movement requires the server thread");}
    private static LevelChunk chunk(ServerLevel level,BlockPos pos){return level.isOutsideBuildHeight(pos)?null:level.getChunkSource().getChunkNow(pos.getX()>>4,pos.getZ()>>4);}
    @Override public boolean loaded(ServerLevel level,BlockPos pos){serverThread(level);return chunk(level,pos)!=null;}
    @Override public boolean detach(ServerLevel level,BlockEntity entity){
        serverThread(level);if(entity.getLevel()!=level)return false;var position=entity.getBlockPos();var chunk=chunk(level,position);
        if(chunk==null||chunk.getBlockEntities().get(position)!=entity)return false;
        chunk.removeBlockEntity(position);chunk.setUnsaved(true);level.invalidateCapabilities(position);
        return chunk.getBlockEntities().get(position)!=entity;
    }
    @Override public boolean relocate(ServerLevel level,BlockEntity entity,BlockPos destination){
        serverThread(level);if(entity.getLevel()!=level)return false;
        var source=entity.getBlockPos();var from=chunk(level,source);var to=chunk(level,destination);
        if(from==null||to==null)return false;
        var state=to.getBlockState(destination);if(!state.hasBlockEntity()||!entity.isValidBlockState(state))return false;
        var registered=from.getBlockEntities().get(source);
        if(registered!=entity&&!entity.isRemoved())return false;
        if(registered==entity&&!detach(level,entity))return false;
        var placeholder=to.getBlockEntities().get(destination);if(placeholder!=null)to.removeBlockEntity(destination);
        ((BlockEntityPositionAccessor)entity).thaumcraft$setPosition(destination.immutable());
        entity.setBlockState(state);entity.setLevel(level);
        to.addAndRegisterBlockEntity(entity);to.setUnsaved(true);entity.setChanged();
        level.invalidateCapabilities(source);level.invalidateCapabilities(destination);
        entity.requestModelDataUpdate();level.sendBlockUpdated(destination,state,state,3);
        return to.getBlockEntities().get(destination)==entity&&!entity.isRemoved();
    }
}
