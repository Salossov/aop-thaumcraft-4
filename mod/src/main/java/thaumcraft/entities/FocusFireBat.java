package thaumcraft.entities;

import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** TC4's wild fire bat and the focus's separately marked summoned bat. */
public final class FocusFireBat extends Bat {
    // Keep the port's existing three upgrade bits; bit 3 explicitly records the mode.
    private static final int SUMMONED=8;
    private static final EntityDataAccessor<Integer> FLAGS=SynchedEntityData.defineId(FocusFireBat.class,EntityDataSerializers.INT);
    private UUID targetId,ownerId;
    private int potency,attackDelay;
    private BlockPos flightTarget;

    public FocusFireBat(EntityType<? extends FocusFireBat> type,Level level){super(type,level);xpReward=5;}
    public static AttributeSupplier.Builder attributes(){return Bat.createAttributes().add(Attributes.MAX_HEALTH,5).add(Attributes.ATTACK_DAMAGE,1);}
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder){super.defineSynchedData(builder);builder.define(FLAGS,0);}
    public void summon(Player owner,LivingEntity target,int potency,boolean devil,boolean explosive,boolean vampire){
        ownerId=owner.getUUID();targetId=target.getUUID();this.potency=potency;
        entityData.set(FLAGS,SUMMONED|(devil?1:0)|(explosive?2:0)|(vampire?4:0));
        applyModeAttributes();setHealth(getMaxHealth());setResting(false);
    }
    public boolean summoned(){return (entityData.get(FLAGS)&SUMMONED)!=0;}
    public boolean devil(){return (entityData.get(FLAGS)&1)!=0;}
    public boolean explosive(){return (entityData.get(FLAGS)&2)!=0;}
    public boolean vampire(){return (entityData.get(FLAGS)&4)!=0;}
    private void applyModeAttributes(){
        getAttribute(Attributes.MAX_HEALTH).setBaseValue(devil()?15:5);
        getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(summoned()?(devil()?3:2)+potency:1);
    }
    private boolean solidAbove(){
        var above=blockPosition().above();
        return level().getChunkSource().getChunkNow(above.getX()>>4,above.getZ()>>4)!=null&&level().getBlockState(above).isRedstoneConductor(level(),above);
    }
    private void wake(){setResting(false);level().levelEvent(null,1025,blockPosition(),0);}
    private LivingEntity wildTarget(ServerLevel level){
        var target=getTarget();
        if(target!=null&&target.isAlive()&&(!(target instanceof Player player)||!player.getAbilities().invulnerable&&!player.isSpectator()))return target;
        // The old closest-vulnerable-player query had no line-of-sight requirement.
        Player nearest=null;double closest=Double.MAX_VALUE;
        for(var player:level.players()){
            if(!player.isAlive()||player.isSpectator()||player.getAbilities().invulnerable)continue;
            double range=12;
            if(player.isShiftKeyDown())range*=.8;
            if(player.isInvisible())range*=.7*Math.max(.1,player.getArmorCoverPercentage());
            double distance=distanceToSqr(player);
            if(distance<range*range&&distance<closest){nearest=player;closest=distance;}
        }
        setTarget(nearest);return nearest;
    }
    private void steer(Vec3 point){
        var delta=point.subtract(position());var velocity=getDeltaMovement();
        velocity=velocity.add((Math.signum(delta.x)*.5-velocity.x)*.1,(Math.signum(delta.y)*.7-velocity.y)*.1,(Math.signum(delta.z)*.5-velocity.z)*.1);
        setDeltaMovement(velocity);
        float yaw=(float)(Mth.atan2(velocity.z,velocity.x)*180/Math.PI)-90;
        setYRot(getYRot()+Mth.wrapDegrees(yaw-getYRot()));yBodyRot=getYRot();zza=.5f;
    }
    private void wander(){
        if(flightTarget!=null){
            boolean loaded=level().getChunkSource().getChunkNow(flightTarget.getX()>>4,flightTarget.getZ()>>4)!=null;
            if(!loaded||!level().isEmptyBlock(flightTarget)||flightTarget.getY()<1)flightTarget=null;
        }
        if(flightTarget==null||random.nextInt(30)==0||flightTarget.distToCenterSqr(position())<4){
            flightTarget=new BlockPos((int)getX()+random.nextInt(7)-random.nextInt(7),(int)getY()+random.nextInt(6)-2,(int)getZ()+random.nextInt(7)-random.nextInt(7));
        }
        steer(new Vec3(flightTarget.getX()+.5,flightTarget.getY()+.1,flightTarget.getZ()+.5));
        if(random.nextInt(100)==0&&solidAbove())setResting(true);
    }
    @Override protected void customServerAiStep(){
        if(!(level() instanceof ServerLevel level))return;
        var owner=ownerId==null?null:level.getEntity(ownerId);
        LivingEntity target;
        if(summoned()){
            var candidate=targetId==null?null:level.getEntity(targetId);
            target=candidate instanceof LivingEntity living&&FocusProjectile.allowed(owner,living)&&(!(living instanceof Player player)||!player.getAbilities().invulnerable&&!player.isSpectator())?living:null;
        }else target=wildTarget(level);
        if(attackDelay>0)attackDelay--;
        if(isResting()){
            if(!solidAbove())wake();
            else{
                if(random.nextInt(200)==0)yHeadRot=random.nextInt(360);
                if(level.getNearestPlayer(this,4)!=null)wake();
            }
        }else if(target==null){
            if(summoned())hurt(damageSources().fellOutOfWorld(),2);
            wander();
        }else steer(target.position().add(0,target.getEyeHeight()*.66,0));
        if(target!=null&&attackDelay==0&&distanceTo(target)<Math.max(2.5,target.getBbWidth()*1.1)&&target.getBoundingBox().maxY>getBoundingBox().minY&&target.getBoundingBox().minY<getBoundingBox().maxY)attack(level,owner,target);
    }
    private void attack(ServerLevel level,Entity owner,LivingEntity target){
        attackDelay=20;
        if(summoned())((thaumcraft.mixin.LivingEntityRecentHitAccessor)target).thaumcraft$setRecentlyHit(100);
        if(vampire()){
            if(owner instanceof LivingEntity caster&&!caster.hasEffect(MobEffects.REGENERATION))caster.addEffect(new MobEffectInstance(MobEffects.REGENERATION,26,1));
            heal(1);
        }
        if((explosive()||level.random.nextInt(10)==0)&&!devil()){
            target.invulnerableTime=0;
            level.explode(this,getX(),getY(),getZ(),1.5f+(explosive()?potency*.33f:0),Level.ExplosionInteraction.NONE);discard();
        }else if(vampire()||level.random.nextBoolean()){
            Vec3 movement=target.getDeltaMovement();
            target.hurt(owner instanceof Player caster?damageSources().playerAttack(caster):damageSources().mobAttack(this),(float)getAttributeValue(Attributes.ATTACK_DAMAGE));
            target.hurtMarked=false;target.setDeltaMovement(movement);
        }else target.igniteForSeconds(summoned()?4:2);
        playSound(net.minecraft.sounds.SoundEvents.BAT_HURT,.5f,.9f+level.random.nextFloat()*.2f);
    }
    @Override public void aiStep(){if(isInWater())hurt(damageSources().drown(),1);super.aiStep();}
    @Override public void tick(){
        super.tick();
        if(level().isClientSide&&!vampire()){
            level().addParticle(net.minecraft.core.particles.ParticleTypes.FLAME,getX(),getY()+getBbHeight()/2,getZ(),0,0,0);
            level().addParticle(net.minecraft.core.particles.ParticleTypes.SMOKE,getX(),getY()+getBbHeight()/2,getZ(),0,0,0);
        }
    }
    @Override public boolean hurt(DamageSource source,float amount){return !source.is(DamageTypeTags.IS_FIRE)&&!source.is(DamageTypeTags.IS_EXPLOSION)&&super.hurt(source,amount);}
    @Override public boolean fireImmune(){return true;}
    @Override protected void dropFromLootTable(DamageSource source,boolean recentlyHit){}
    @Override protected void dropCustomDeathLoot(ServerLevel level,DamageSource source,boolean recentlyHit){
        if(summoned())return;
        int looting=source.getEntity() instanceof LivingEntity killer?EnchantmentHelper.getEnchantmentLevel(level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.LOOTING),killer):0;
        int count=random.nextInt(3)+(looting>0?random.nextInt(looting+1):0);
        for(int i=0;i<count;i++)spawnAtLocation(Items.GUNPOWDER);
    }
    @Override public void addAdditionalSaveData(CompoundTag tag){
        super.addAdditionalSaveData(tag);
        if(ownerId!=null)tag.putUUID("Owner",ownerId);if(targetId!=null)tag.putUUID("Target",targetId);
        tag.putInt("Flags",entityData.get(FLAGS));tag.putBoolean("Summoned",summoned());tag.putInt("Potency",potency);tag.putInt("AttackDelay",attackDelay);
    }
    @Override public void readAdditionalSaveData(CompoundTag tag){
        super.readAdditionalSaveData(tag);
        ownerId=tag.hasUUID("Owner")?tag.getUUID("Owner"):null;targetId=tag.hasUUID("Target")?tag.getUUID("Target"):null;
        int flags=tag.getInt("Flags");potency=tag.getInt("Potency");
        if(!tag.contains("Flags")&&tag.contains("BatFlags")){
            // TC4 packed hanging/summoned/explosive/devil/vampire into BatFlags.
            int old=tag.getByte("BatFlags");flags=((old&2)!=0?SUMMONED:0)|((old&4)!=0?2:0)|((old&8)!=0?1:0)|((old&16)!=0?4:0);potency=tag.getByte("damBonus");
        }else if(tag.contains("Summoned"))flags=tag.getBoolean("Summoned")?flags|SUMMONED:flags&~SUMMONED;
        else if(ownerId!=null||targetId!=null)flags|=SUMMONED;
        entityData.set(FLAGS,flags);attackDelay=tag.getInt("AttackDelay");applyModeAttributes();
    }
}
