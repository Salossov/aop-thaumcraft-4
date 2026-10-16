package thaumcraft.mixin;

import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ItemEntity.class)
public interface ItemAgeAccessor {
    @Accessor("age") void thaumcraft$setAge(int age);
    @Accessor("pickupDelay") int thaumcraft$getPickupDelay();
}
