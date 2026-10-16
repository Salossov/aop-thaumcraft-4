package thaumcraft.entities;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.tags.DamageTypeTags;

/** Original floating special item with a renewable age and explosion immunity. */
public final class PermanentItemEntity extends ItemEntity {
    public PermanentItemEntity(EntityType<? extends PermanentItemEntity> type,Level level){super(type,level);}
    @Override protected double getDefaultGravity(){return (double).04f;}
    @Override public void tick(){var velocity=getDeltaMovement();setDeltaMovement(velocity.x,velocity.y>0?velocity.y*(double).9f+(double).04f:velocity.y+(double).04f,velocity.z);super.tick();if(getAge()+5>=lifespan)((thaumcraft.mixin.ItemAgeAccessor)(Object)this).thaumcraft$setAge(0);}
    @Override public boolean hurt(DamageSource source,float amount){return !source.is(DamageTypeTags.IS_EXPLOSION)&&super.hurt(source,amount);}
}
