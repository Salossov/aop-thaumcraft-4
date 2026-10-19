package thaumcraft.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(NearestAttackableTargetGoal.class)
public interface NearestTargetClassAccessor {
    @Accessor("targetType") Class<? extends Entity> thaumcraft$targetClass();
    @Mutable @Accessor("targetType") void thaumcraft$targetClass(Class<? extends Entity> type);
}
