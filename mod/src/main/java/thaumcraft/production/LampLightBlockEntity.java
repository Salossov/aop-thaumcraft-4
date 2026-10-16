package thaumcraft.production;

import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.registry.R3Content;

public final class LampLightBlockEntity extends BlockEntity {
    private BlockPos source;private int count;
    public LampLightBlockEntity(BlockPos pos,BlockState state){super(R3Content.LAMP_LIGHT_ENTITY.get(),pos,state);}
    public BlockPos source(){return source;}
    public void source(BlockPos pos){source=pos.immutable();setChanged();}
    public void serverTick(){if(++count%100==0&&(source==null||level.hasChunkAt(source)&&!level.getBlockState(source).is(R3Content.ARCANE_LAMP.get())))level.removeBlock(worldPosition,false);}
    @Override protected void saveAdditional(CompoundTag tag,HolderLookup.Provider registries){super.saveAdditional(tag,registries);if(source!=null)tag.putLong("Source",source.asLong());}
    @Override protected void loadAdditional(CompoundTag tag,HolderLookup.Provider registries){super.loadAdditional(tag,registries);source=tag.contains("Source")?BlockPos.of(tag.getLong("Source")):null;}
}
