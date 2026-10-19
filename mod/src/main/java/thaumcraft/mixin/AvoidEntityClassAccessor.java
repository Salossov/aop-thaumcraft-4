package thaumcraft.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AvoidEntityGoal.class)
public interface AvoidEntityClassAccessor {
    @Accessor("avoidClass") Class<? extends LivingEntity> thaumcraft$avoidClass();
    @Mutable @Accessor("avoidClass") void thaumcraft$avoidClass(Class<? extends LivingEntity> type);
}
