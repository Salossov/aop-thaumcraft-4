package thaumcraft.items;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Original six hunger points, 0.8 saturation multiplier and 66% regeneration. */
public final class TripleMeatTreatItem extends Item {
    public TripleMeatTreatItem(){super(new Properties().food(new FoodProperties.Builder().nutrition(6).saturationModifier(.8f).alwaysEdible().build()));}
    @Override public ItemStack finishUsingItem(ItemStack stack,Level level,LivingEntity user){
        var result=super.finishUsingItem(stack,level,user);
        if(!level.isClientSide&&level.random.nextFloat()<.66f)user.addEffect(new MobEffectInstance(MobEffects.REGENERATION,100,0));
        return result;
    }
}
