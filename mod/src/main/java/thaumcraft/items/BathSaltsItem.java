package thaumcraft.items;

import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;

public final class BathSaltsItem extends Item {
    public BathSaltsItem(){super(new Properties());}
    @Override public int getEntityLifespan(ItemStack stack,Level level){return 200;}
}
