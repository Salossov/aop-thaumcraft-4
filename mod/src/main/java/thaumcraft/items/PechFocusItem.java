package thaumcraft.items;

import java.util.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import thaumcraft.api.aspects.*;
import thaumcraft.api.wands.*;
import thaumcraft.entities.PechBlast;

public final class PechFocusItem extends ItemFocusBasic {
    @Override public Map<Aspect,Integer> getVisCost(ItemStack stack){var cost=new LinkedHashMap<Aspect,Integer>();for(var aspect:isUpgradedWith(stack,FocusUpgradeType.nightshade)?Aspects.PRIMALS:List.of(Aspects.EARTH,Aspects.ENTROPY,Aspects.WATER))cost.put(aspect,10);return cost;}
    @Override public int getActivationCooldown(ItemStack stack){return 250;}
    @Override public int getFocusColor(ItemStack stack){return 0x229944;}
    @Override public List<FocusUpgradeType> getPossibleUpgradesByRank(ItemStack stack,int rank){return switch(rank){case 1,3->List.of(FocusUpgradeType.frugal,FocusUpgradeType.potency);case 2,4->List.of(FocusUpgradeType.frugal,FocusUpgradeType.potency,FocusUpgradeType.extend);case 5->List.of(FocusUpgradeType.frugal,FocusUpgradeType.potency,FocusUpgradeType.nightshade);default->List.of();};}
    public boolean cast(ServerPlayer player,ItemStack wand,ItemStack focus){if(!WandFocus.consume(wand,player,getVisCost(focus),true))return false;var blast=new PechBlast(player.level(),player,getUpgradeLevel(focus,FocusUpgradeType.potency),getUpgradeLevel(focus,FocusUpgradeType.extend),isUpgradedWith(focus,FocusUpgradeType.nightshade));player.level().addFreshEntity(blast);blast.playSound(thaumcraft.registry.R2Sounds.ICE.get(),.4f,1+player.getRandom().nextFloat()*.1f);return true;}
}
