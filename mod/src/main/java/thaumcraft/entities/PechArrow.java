package thaumcraft.entities;

import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.*;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.Level;

/** Vanilla arrow body with TC4's explicit punch impulse, without double applying modern bow effects. */
public final class PechArrow extends Arrow {
    private int punch;
    public PechArrow(EntityType<? extends PechArrow> type,Level level){super(type,level);}
    public PechArrow(Level level,LivingEntity owner,int punch){super(level,owner,new ItemStack(Items.ARROW),null);this.punch=punch;}
    @Override protected void doKnockback(LivingEntity target,DamageSource source){var velocity=getDeltaMovement();double horizontal=Math.sqrt(velocity.x*velocity.x+velocity.z*velocity.z);if(punch>0&&horizontal>0)target.push(velocity.x*punch*.6/horizontal,.1,velocity.z*punch*.6/horizontal);}
}
