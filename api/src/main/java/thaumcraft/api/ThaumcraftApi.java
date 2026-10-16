package thaumcraft.api;

import net.minecraft.world.item.ItemStack;
import thaumcraft.api.internal.WeightedRandomLoot;

/** Original extension entry points are added alongside their native implementations. */
public final class ThaumcraftApi {
    public static void addLootBagItem(ItemStack item,int weight,int... bagTypes){
        if(bagTypes==null||bagTypes.length==0){WeightedRandomLoot.lootBagCommon.add(new WeightedRandomLoot(item,weight));return;}
        for(int rarity:bagTypes)switch(rarity){case 0->WeightedRandomLoot.lootBagCommon.add(new WeightedRandomLoot(item,weight));case 1->WeightedRandomLoot.lootBagUncommon.add(new WeightedRandomLoot(item,weight));case 2->WeightedRandomLoot.lootBagRare.add(new WeightedRandomLoot(item,weight));default->{}}
    }
    private ThaumcraftApi(){}
}
