package thaumcraft.api.internal;

import java.util.ArrayList;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.world.item.ItemStack;

/** TC4 extension lists, using native item stacks and native random weights. */
public class WeightedRandomLoot extends WeightedEntry.IntrusiveBase {
    public ItemStack item;
    public static final ArrayList<WeightedRandomLoot> lootBagCommon=new ArrayList<>(),lootBagUncommon=new ArrayList<>(),lootBagRare=new ArrayList<>();
    public WeightedRandomLoot(ItemStack stack,int weight){super(weight);this.item=stack;}
}
