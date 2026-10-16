package thaumcraft.blocks;

import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.items.ThaumicBannerItem;
import thaumcraft.registry.R4Content;

public final class ThaumicBannerBlockEntity extends BlockEntity {
    public int color=-1;
    public Aspect aspect;
    public ThaumicBannerBlockEntity(BlockPos pos,BlockState state){super(R4Content.BANNER_ENTITY.get(),pos,state);}
    public net.minecraft.world.item.ItemStack item(){return ThaumicBannerItem.stack(color,aspect);}
    public void sync(){setChanged();if(level!=null)level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),3);}
    @Override protected void saveAdditional(CompoundTag t,HolderLookup.Provider lookup){super.saveAdditional(t,lookup);t.putByte("color",(byte)color);t.putString("aspect",aspect==null?"":aspect.getTag());}
    @Override protected void loadAdditional(CompoundTag t,HolderLookup.Provider lookup){super.loadAdditional(t,lookup);color=t.contains("color",99)?t.getByte("color"):-1;if(color<0||color>15)color=-1;aspect=Aspect.getAspect(t.getString("aspect"));}
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider lookup){return saveWithoutMetadata(lookup);}
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket(){return ClientboundBlockEntityDataPacket.create(this);}
}
