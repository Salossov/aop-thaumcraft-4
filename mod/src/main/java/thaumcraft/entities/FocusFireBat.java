package thaumcraft.entities;

import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.*;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class FocusFireBat extends Bat {
    private static final EntityDataAccessor<Integer> FLAGS=SynchedEntityData.defineId(FocusFireBat.class,EntityDataSerializers.INT);
    private UUID targetId,ownerId;private int potency,attackDelay;
    public FocusFireBat(EntityType<? extends FocusFireBat> type,Level level){super(type,level);setResting(false);}
    public static AttributeSupplier.Builder attributes(){return Bat.createAttributes().add(Attributes.MAX_HEALTH,5).add(Attributes.ATTACK_DAMAGE,2);}
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder){super.defineSynchedData(builder);builder.define(FLAGS,0);}
    public void summon(Player owner,LivingEntity target,int potency,boolean devil,boolean explosive,boolean vampire){this.ownerId=owner.getUUID();this.targetId=target.getUUID();this.potency=potency;entityData.set(FLAGS,(devil?1:0)|(explosive?2:0)|(vampire?4:0));getAttribute(Attributes.MAX_HEALTH).setBaseValue(devil?15:5);setHealth(getMaxHealth());getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue((devil?3:2)+potency);}
    public boolean devil(){return (entityData.get(FLAGS)&1)!=0;}public boolean explosive(){return (entityData.get(FLAGS)&2)!=0;}public boolean vampire(){return (entityData.get(FLAGS)&4)!=0;}
    @Override protected void customServerAiStep(){
        if(!(level() instanceof net.minecraft.server.level.ServerLevel level))return;var target=targetId==null?null:level.getEntity(targetId);var owner=ownerId==null?null:level.getEntity(ownerId);
        if(!(target instanceof LivingEntity living)||!FocusProjectile.allowed(owner,target)||target instanceof Player player&&player.getAbilities().invulnerable){hurt(damageSources().fellOutOfWorld(),2);return;}
        var delta=living.position().add(0,living.getEyeHeight()*.66,0).subtract(position());var velocity=getDeltaMovement();velocity=velocity.add((Math.signum(delta.x)*.5-velocity.x)*.1,(Math.signum(delta.y)*.7-velocity.y)*.1,(Math.signum(delta.z)*.5-velocity.z)*.1);setDeltaMovement(velocity);setYRot((float)(Math.atan2(velocity.z,velocity.x)*180/Math.PI)-90);yBodyRot=getYRot();setResting(false);
        if(isInWater())hurt(damageSources().drown(),1);if(attackDelay>0)attackDelay--;
        if(attackDelay==0&&distanceTo(target)<Math.max(2.5,target.getBbWidth()*1.1)&&getBoundingBox().intersects(target.getBoundingBox().inflate(0,1,0))){attackDelay=20;
            if(vampire()){if(owner instanceof LivingEntity caster&&!caster.hasEffect(MobEffects.REGENERATION))caster.addEffect(new MobEffectInstance(MobEffects.REGENERATION,26,1));heal(1);}
            if((explosive()||random.nextInt(10)==0)&&!devil()){target.invulnerableTime=0;level.explode(this,getX(),getY(),getZ(),1.5f+(explosive()?potency*.33f:0),Level.ExplosionInteraction.NONE);discard();}
            else if(vampire()||random.nextBoolean()){Vec3 movement=target.getDeltaMovement();target.hurt(owner instanceof Player caster?damageSources().playerAttack(caster):damageSources().mobAttack(this),(float)getAttributeValue(Attributes.ATTACK_DAMAGE));target.setDeltaMovement(movement);}
            else target.igniteForSeconds(4);playSound(net.minecraft.sounds.SoundEvents.BAT_HURT,.5f,.9f+random.nextFloat()*.2f);
        }
    }
    @Override public void tick(){super.tick();if(level().isClientSide&&!vampire()){level().addParticle(net.minecraft.core.particles.ParticleTypes.FLAME,getX(),getY()+getBbHeight()/2,getZ(),0,0,0);level().addParticle(net.minecraft.core.particles.ParticleTypes.SMOKE,getX(),getY()+getBbHeight()/2,getZ(),0,0,0);}}
    @Override public boolean hurt(DamageSource source,float amount){return !source.is(DamageTypeTags.IS_FIRE)&&!source.is(DamageTypeTags.IS_EXPLOSION)&&super.hurt(source,amount);}
    @Override public boolean fireImmune(){return true;}
    @Override public void addAdditionalSaveData(CompoundTag tag){super.addAdditionalSaveData(tag);if(ownerId!=null)tag.putUUID("Owner",ownerId);if(targetId!=null)tag.putUUID("Target",targetId);tag.putInt("Flags",entityData.get(FLAGS));tag.putInt("Potency",potency);tag.putInt("AttackDelay",attackDelay);}
    @Override public void readAdditionalSaveData(CompoundTag tag){super.readAdditionalSaveData(tag);ownerId=tag.hasUUID("Owner")?tag.getUUID("Owner"):null;targetId=tag.hasUUID("Target")?tag.getUUID("Target"):null;entityData.set(FLAGS,tag.getInt("Flags"));potency=tag.getInt("Potency");attackDelay=tag.getInt("AttackDelay");setResting(false);}
}
