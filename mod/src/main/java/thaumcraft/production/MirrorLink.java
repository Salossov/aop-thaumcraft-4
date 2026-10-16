package thaumcraft.production;

import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

public record MirrorLink(ResourceKey<Level> dimension,BlockPos pos) {
    public MirrorLink{pos=pos.immutable();}
    public CompoundTag save(){var tag=new CompoundTag();tag.putString("Dimension",dimension.location().toString());tag.putLong("Position",pos.asLong());return tag;}
    public static MirrorLink load(CompoundTag tag){var id=ResourceLocation.tryParse(tag.getString("Dimension"));return id==null||!tag.contains("Position")?null:new MirrorLink(ResourceKey.create(Registries.DIMENSION,id),BlockPos.of(tag.getLong("Position")));}
    public static MirrorLink from(ItemStack stack){return load(stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getCompound("MirrorLink"));}
    public void write(ItemStack stack){CustomData.update(DataComponents.CUSTOM_DATA,stack,tag->tag.put("MirrorLink",save()));}
    public MirrorBlockEntity target(MinecraftServer server,boolean load){var level=server.getLevel(dimension);if(level==null||level.isOutsideBuildHeight(pos)||!level.getWorldBorder().isWithinBounds(pos)||!load&&!level.hasChunkAt(pos))return null;return level.getBlockEntity(pos) instanceof MirrorBlockEntity mirror?mirror:null;}
}
