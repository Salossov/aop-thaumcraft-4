package thaumcraft.entities;

import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

public final class EldritchOrb extends ThrowableProjectile {
    public static java.util.function.Consumer<EldritchOrb> clientImpact=orb->{};
    public EldritchOrb(EntityType<? extends EldritchOrb> type,Level level) {super(type,level);}
    public EldritchOrb(Level level,LivingEntity owner) {super(R2Entities.ELDRITCH_ORB.get(),level);LegacyThrowableLaunch.owner(this,owner,1.5f,0);}
    @Override public void shoot(double x,double y,double z,float speed,float scatter){LegacyThrowableLaunch.shoot(this,random,x,y,z,speed,scatter);}
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) {}
    @Override protected double getDefaultGravity() {return 0;}
    @Override public float getPickRadius() {return 0.1f;}
    @Override public void tick() {super.tick();if(tickCount>100)discard();}
    @Override protected void onHit(HitResult hit) {super.onHit(hit);if(!level().isClientSide&&getOwner() instanceof LivingEntity owner)impact(owner);}
    public void impact(LivingEntity owner) {
        if(level().isClientSide||tickCount>=100)return;
        for(var entity:level().getEntitiesOfClass(LivingEntity.class,getBoundingBox().inflate(2),e->e!=owner&&!e.isInvertedHealAndHarm())) {
            entity.hurt(damageSources().indirectMagic(this,owner),(float)owner.getAttributeValue(Attributes.ATTACK_DAMAGE)*0.666f);
            entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,160));
        }
        playSound(SoundEvents.FIRE_EXTINGUISH,0.5f,2.6f+(random.nextFloat()-random.nextFloat())*0.8f);
        level().broadcastEntityEvent(this,(byte)16);tickCount=100;
    }
    @Override public void handleEntityEvent(byte event) {
        if(event==16)clientImpact.accept(this);
        else super.handleEntityEvent(event);
    }
}
