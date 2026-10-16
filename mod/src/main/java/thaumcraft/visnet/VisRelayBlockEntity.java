package thaumcraft.visnet;

import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.api.visnet.*;
import thaumcraft.registry.R3Content;

public final class VisRelayBlockEntity extends TileVisNode {
    private byte attunement = -1;
    public VisRelayBlockEntity(BlockPos pos, BlockState state) { super(R3Content.VIS_RELAY_ENTITY.get(), pos, state); }
    @Override public int getRange() { return 8; }
    @Override public boolean isSource() { return false; }
    @Override public byte getAttunement() { return attunement; }
    /** Shard metadata order differs from the order of the wand HUD's primals. */
    public static thaumcraft.api.aspects.Aspect attunedAspect(int shard){return switch(shard){case 0->thaumcraft.api.aspects.Aspects.AIR;case 1->thaumcraft.api.aspects.Aspects.FIRE;case 2->thaumcraft.api.aspects.Aspects.WATER;case 3->thaumcraft.api.aspects.Aspects.EARTH;case 4->thaumcraft.api.aspects.Aspects.ORDER;case 5->thaumcraft.api.aspects.Aspects.ENTROPY;default->null;};}
    public net.minecraft.network.chat.Component attunementName(){var aspect=attunedAspect(attunement);return aspect==null?net.minecraft.network.chat.Component.translatable("thaumcraft.vis_relay.unattuned"):net.minecraft.network.chat.Component.literal(aspect.getName());}
    public void nextAttunement() { attunement++; if (attunement > 5) attunement = -1; changedAndSync(); if (level != null) VisNetHandler.invalidate(level); }
    public void attune(int shard) { if(shard<0||shard>6)return;attunement=(byte)(shard==6||shard==attunement?-1:shard); if(level!=null){VisNetHandler.invalidate(level);VisNetHandler.refresh(level);}changedAndSync(); }
    public void serverTick() {
        if(level==null)return;VisNetHandler.refresh(level);
        if(getBlockState().is(R3Content.WORKBENCH_CHARGER.get())&&level.hasChunkAt(worldPosition.below())
            &&level.getBlockEntity(worldPosition.below()) instanceof thaumcraft.blocks.TableBlockEntity table
            &&table.getBlockState().is(thaumcraft.registry.R2Content.ARCANE_WORKBENCH.get())) {
            var wand=table.getItem(10);
            if(wand.getItem() instanceof thaumcraft.items.CastingWandItem)for(var aspect:thaumcraft.api.aspects.Aspects.PRIMALS){int room=Math.min(5,thaumcraft.items.WandVis.capacity(wand)-thaumcraft.items.WandVis.get(wand,aspect));if(room>0){int amount=consumeVis(aspect,room);if(amount>0){thaumcraft.items.WandVis.set(wand,aspect,thaumcraft.items.WandVis.get(wand,aspect)+amount);table.setChanged();}}}
        }
    }
    @Override protected void saveAdditional(CompoundTag tag, HolderLookup.Provider lookup) { super.saveAdditional(tag,lookup); tag.putByte("Attunement",attunement); }
    @Override protected void loadAdditional(CompoundTag tag, HolderLookup.Provider lookup) { super.loadAdditional(tag,lookup); attunement = tag.contains("Attunement") ? tag.getByte("Attunement") : -1; if (attunement < -1 || attunement > 5) attunement = -1; }
}
