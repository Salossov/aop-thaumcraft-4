package thaumcraft.crafting;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.*;

/** TC4 selected the first registered ingot for cluster smelting and nugget recombination. */
public final class TagOutput {
    public static ItemStack first(HolderLookup.Provider lookup,ResourceLocation tag,int count){return lookup.lookupOrThrow(Registries.ITEM).get(TagKey.create(Registries.ITEM,tag)).flatMap(values->values.stream().findFirst()).map(holder->new ItemStack(holder,count)).orElse(ItemStack.EMPTY);}
    public static boolean present(HolderLookup.Provider lookup,String tag){return !first(lookup,ResourceLocation.parse(tag),1).isEmpty();}
    private TagOutput(){}
}
