package thaumcraft.api.world;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

/** Shared data-driven classifications for removed legacy materials. Extend through block tags. */
public final class BlockMaterials {
    /** Flattened counterparts of pinned 1.7.10 Material.wood blocks, plus addon contributions. */
    public static final TagKey<Block> WOOD=TagKey.create(Registries.BLOCK,ResourceLocation.parse("thaumcraft:materials/wood"));
    private BlockMaterials(){}
}
