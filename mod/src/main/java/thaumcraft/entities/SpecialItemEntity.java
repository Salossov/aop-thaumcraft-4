package thaumcraft.entities;

import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;

/** TC4 floating special loot; unlike PermanentItemEntity its ordinary lifespan expires. */
public class SpecialItemEntity extends ItemEntity {
    public SpecialItemEntity(EntityType<? extends SpecialItemEntity> type,Level level){super(type,level);}
    @Override protected double getDefaultGravity(){return (double).04f;}
    @Override public void tick(){var v=getDeltaMovement();setDeltaMovement(v.x,(v.y>0?v.y*(double).9f:v.y)+(double).04f,v.z);super.tick();}
    @Override public boolean hurt(DamageSource source,float amount){return !source.is(DamageTypeTags.IS_EXPLOSION)&&super.hurt(source,amount);}
}
