package thaumcraft.entities;

import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.effect.*;

/** Original poison/slow/blindness projectile; Pech are excluded from its two-block burst. */
public final class PechBlast extends ThrowableProjectile {
    private int strength,duration;private boolean nightshade,burst;
    public static java.util.function.Consumer<PechBlast> clientTrail=blast->{},clientImpact=blast->{};
    public PechBlast(EntityType<? extends PechBlast> type,Level level){super(type,level);}
    public PechBlast(Level level,LivingEntity owner,int strength,int duration,boolean nightshade){super(R5Entities.PECH_BLAST.get(),level);LegacyThrowableLaunch.owner(this,owner,1.5f,0);this.strength=strength;this.duration=duration;this.nightshade=nightshade;}
    @Override public void shoot(double x,double y,double z,float speed,float scatter){LegacyThrowableLaunch.shoot(this,random,x,y,z,speed,scatter);}
    @Override protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder){}
    @Override protected double getDefaultGravity(){return (double).025f;}
    @Override public float getPickRadius(){return .1f;}
    @Override public void tick(){if(level().isClientSide)clientTrail.accept(this);super.tick();if(tickCount>500)discard();}
    @Override protected void onHit(HitResult hit){if(level().isClientSide){impact();return;}for(var target:level().getEntitiesOfClass(LivingEntity.class,getBoundingBox().inflate(2),e->e!=getOwner()&&!(e instanceof Pech))){target.hurt(damageSources().thrown(this,getOwner()),strength+2);int ticks=100+duration*40;if(nightshade){target.addEffect(new MobEffectInstance(MobEffects.POISON,ticks,strength));target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,ticks,strength+1));target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS,ticks,strength));}else switch(random.nextInt(3)){case 0->target.addEffect(new MobEffectInstance(MobEffects.POISON,ticks,strength));case 1->target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,ticks,strength+1));case 2->target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS,ticks,strength));}}level().broadcastEntityEvent(this,(byte)16);discard();}
    private void impact(){if(!burst){burst=true;clientImpact.accept(this);}}
    @Override public void handleEntityEvent(byte event){if(event==16)impact();else super.handleEntityEvent(event);}
}
