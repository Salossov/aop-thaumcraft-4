package example.thaumcraft;
import java.util.*;
import net.minecraft.world.item.ItemStack;
import thaumcraft.api.aspects.*;
import thaumcraft.api.wands.*;
public final class ExampleFocus extends ItemFocusBasic {
 public Map<Aspect,Integer> getVisCost(ItemStack focus){return Map.of(Aspects.AIR,10);}
 public boolean isVisCostPerTick(ItemStack focus){return true;}
 /** Milliseconds, matching the original focus contract. */
 public int getActivationCooldown(ItemStack focus){return 100;}
 public int getFocusColor(ItemStack focus){return 0xdca73a;}
 public List<FocusUpgradeType> getPossibleUpgradesByRank(ItemStack focus,int rank){return List.of(FocusUpgradeType.frugal,ExampleAddon.UPGRADE);}
}
