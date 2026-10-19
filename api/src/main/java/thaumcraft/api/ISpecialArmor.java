package thaumcraft.api;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Legacy per-piece armor absorption for players, before effects and enchantments.
 * Wearing an implementation selects the original priority/ratio rules for the entire set.
 * Slots use the original order: feet 0, legs 1, chest 2, head 3.
 * Ordinary armor in a mixed set contributes defense / 25 unless the source bypasses armor.
 * Core fortress/void robes retain their original special properties in a mixed set.
 */
public interface ISpecialArmor {
    SpecialArmorProperties getProperties(LivingEntity wearer,ItemStack stack,DamageSource source,double damage,int slot);
    /** Display contribution; do not query wearer.getArmorValue() here, since it calls this contract. */
    int getArmorDisplay(Player wearer,ItemStack stack,int slot);
    void damageArmor(LivingEntity wearer,ItemStack stack,DamageSource source,int damage,int slot);
}
