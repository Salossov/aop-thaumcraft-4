package thaumcraft.items;

import net.minecraft.world.item.*;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.entity.LivingEntity;

public final class FoodNuggetItem extends Item {
    public FoodNuggetItem(){super(new Properties().food(new FoodProperties.Builder().nutrition(1).saturationModifier(.3f).build()));}
    @Override public int getUseDuration(ItemStack stack,LivingEntity user){return 10;}
}
