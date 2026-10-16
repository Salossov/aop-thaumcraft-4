package thaumcraft.entities;

import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.*;
import net.minecraft.world.level.Level;

public record GolemMarker(ResourceKey<Level> dimension,BlockPos pos,Direction face,int color) {
    public GolemMarker{pos=pos.immutable();color=Math.max(-1,Math.min(15,color));}
    public CompoundTag save(){var tag=new CompoundTag();tag.putString("Dimension",dimension.location().toString());tag.putLong("Position",pos.asLong());tag.putByte("Face",(byte)face.get3DDataValue());tag.putByte("Color",(byte)color);return tag;}
    public static GolemMarker load(CompoundTag tag){var id=ResourceLocation.tryParse(tag.getString("Dimension"));return id==null?null:new GolemMarker(ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION,id),BlockPos.of(tag.getLong("Position")),Direction.from3DDataValue(tag.getByte("Face")),tag.getByte("Color"));}
}
