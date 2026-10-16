package thaumcraft.visnet;

import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.blocks.AuraNodeBlockEntity;
import thaumcraft.fluids.FiniteFluxFluid;
import thaumcraft.registry.*;

public final class NodeConverterBlockEntity extends BlockEntity {
    private int count, status;
    private int motion;
    private float visualCount,previousVisual;
    private boolean visualInitialized;
    public NodeConverterBlockEntity(BlockPos pos, BlockState state) { super(R3Content.NODE_CONVERTER_ENTITY.get(),pos,state); }
    public int progress() { return count; }
    public boolean animating() { return motion!=0; }
    public float progress(float partial) { return level!=null&&level.isClientSide?net.minecraft.util.Mth.lerp(partial,previousVisual,visualCount):count; }
    public void clientTick(){
        if(!visualInitialized){visualCount=previousVisual=count;visualInitialized=true;}
        previousVisual=visualCount;visualCount=net.minecraft.util.Mth.clamp(visualCount+motion,0,1000);
        if(level!=null&&level.isClientSide&&visualCount>50&&motion!=0)thaumcraft.client.NodeConverterEffects.tick(this);
    }
    public int status() { return status; }
    public boolean hasStabilizer() { return level != null && VisDeviceBlock.lock(level,worldPosition.below()) > 0; }
    public void serverTick() {
        if (level == null || level.isClientSide) return;
        int oldStatus=status,oldMotion=motion;
        var below = worldPosition.below(); var tile = level.getBlockEntity(below);
        if (status == 2 && count > 50 && (!hasStabilizer() || !(tile instanceof EnergizedNodeBlockEntity))) {
            explodeNode(level,below); status = 0; count = 50; changed(); return;
        }
        boolean powered = level.hasNeighborSignal(worldPosition);
        if (tile instanceof EnergizedNodeBlockEntity) { if (status != 2) count = 1000; status = 2; }
        else status = powered && hasStabilizer() && tile instanceof AuraNodeBlockEntity && tile.getBlockState().is(R2Content.AURA_NODE.get()) ? 1 : 0;
        if (status == 1 && count >= 1000 && tile instanceof AuraNodeBlockEntity node) {
            // Copy before replacing the block: removing it invalidates the old BlockEntity.
            level.setBlockAndUpdate(below,R3Content.ENERGIZED_NODE.get().defaultBlockState());
            if (level.getBlockEntity(below) instanceof EnergizedNodeBlockEntity energy) energy.initialize(node);
            status = 2; changed(); return;
        }
        if (status == 2 && count <= 50 && tile instanceof EnergizedNodeBlockEntity energy) {
            level.setBlockAndUpdate(below,R2Content.AURA_NODE.get().defaultBlockState());
            if (level.getBlockEntity(below) instanceof AuraNodeBlockEntity node) {
                node.initialize(energy.aura(),energy.nodeType(),energy.modifier()); node.restoreIdentity(energy.id());
                node.state.current.replaceAll((tag,value) -> 0); node.changedAndSync();
            }
            // The original continues its idle countdown in this same tick.
            status = 0; count = Math.max(0,count-1); changed(); return;
        }
        int old = count;
        if (status == 0 || !powered) count = Math.max(0,count-1);
        else if (count < 1000) {
            count++;
            if (tile instanceof AuraNodeBlockEntity node) {
                var tags = node.state.current.entrySet().stream().filter(entry -> entry.getValue() > 0).map(java.util.Map.Entry::getKey).toList();
                if (!tags.isEmpty()) { var tag = tags.get(level.random.nextInt(tags.size())); node.state.current.merge(tag,-1,Integer::sum); if (count % 5 == 0) node.changedAndSync(); else node.setChanged(); }
            }
        }
        motion=Integer.compare(count,old);
        if (old != count) setChanged();
        if(oldStatus!=status||oldMotion!=motion||old!=count&&(count%20==0||count==50||count==1000))changed();
    }
    public static void explodeNode(Level level, BlockPos pos) {
        if (level.isClientSide) return;
        level.removeBlock(pos,false); level.explode(null,pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5,3,Level.ExplosionInteraction.NONE);
        for (int i=0;i<50;i++) {
            var target = pos.offset(level.random.nextInt(8)-level.random.nextInt(8),level.random.nextInt(8)-level.random.nextInt(8),level.random.nextInt(8)-level.random.nextInt(8));
            if (level.hasChunkAt(target) && level.isEmptyBlock(target)) level.setBlockAndUpdate(target,(target.getY()<pos.getY()?R2Content.FLUX_GOO:R2Content.FLUX_GAS).get().defaultBlockState().setValue(FiniteFluxFluid.QUANTA,9));
        }
    }
    private void changed() { setChanged(); if (level != null) level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),3); }
    @Override protected void saveAdditional(CompoundTag tag,HolderLookup.Provider lookup) { super.saveAdditional(tag,lookup); tag.putInt("Count",count); tag.putInt("Status",status); }
    @Override protected void loadAdditional(CompoundTag tag,HolderLookup.Provider lookup) { super.loadAdditional(tag,lookup); count=Math.clamp(tag.getInt("Count"),0,1000);status=Math.clamp(tag.getInt("Status"),0,2);motion=Math.clamp(tag.getInt("Motion"),-1,1);if(!visualInitialized||Math.abs(visualCount-count)>2){visualCount=previousVisual=count;visualInitialized=true;} }
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider lookup) { var tag=saveWithoutMetadata(lookup);tag.putInt("Motion",motion);return tag; }
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
}
