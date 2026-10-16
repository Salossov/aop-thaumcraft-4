package thaumcraft.items;

import net.minecraft.network.chat.Component;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import thaumcraft.api.*;

public final class CrimsonSwordItem extends SwordItem implements IRepairable,IWarpingGear {
    private static final Tier TIER=new Tier(){public int getUses(){return 200;}public float getSpeed(){return 8;}public float getAttackDamageBonus(){return 3.5f;}public net.minecraft.tags.TagKey<net.minecraft.world.level.block.Block> getIncorrectBlocksForDrops(){return BlockTags.INCORRECT_FOR_NETHERITE_TOOL;}public int getEnchantmentValue(){return 20;}public Ingredient getRepairIngredient(){return Ingredient.of(thaumcraft.registry.R3Equipment.VOID_INGOT.get());}};
    public CrimsonSwordItem(){super(TIER,new Properties().rarity(Rarity.RARE).attributes(SwordItem.createAttributes(TIER,4,-2.4f)));}
    @Override public void inventoryTick(ItemStack stack,Level level,Entity entity,int slot,boolean selected){super.inventoryTick(stack,level,entity,slot,selected);if(stack.isDamaged()&&entity instanceof LivingEntity&&entity.tickCount%20==0)stack.setDamageValue(stack.getDamageValue()-1);}
    @Override public boolean hurtEnemy(ItemStack stack,LivingEntity target,LivingEntity hitter){if(!target.level().isClientSide&&(!(target instanceof Player)||!(hitter instanceof Player)||target.getServer().isPvpAllowed())){target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS,60));target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,120));}return super.hurtEnemy(stack,target,hitter);}
    @Override public int getWarp(ItemStack stack,Player player){return 2;}
    @Override public void appendHoverText(ItemStack stack,TooltipContext context,java.util.List<Component> lines,TooltipFlag flags){lines.add(Component.translatable("enchantment.special.sapgreat").withStyle(net.minecraft.ChatFormatting.GOLD));super.appendHoverText(stack,context,lines,flags);}
}
