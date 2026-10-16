package thaumcraft.entities;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.*;
import net.minecraft.world.entity.animal.Ocelot;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import thaumcraft.blocks.TaintFibresBlock;
import thaumcraft.effects.TaintEffects;
import thaumcraft.registry.R2Content;
import thaumcraft.world.WorldBiomes;

public final class TaintCreeper extends Monster implements TaintedMob {
    private static final EntityDataAccessor<Integer> SWELL=SynchedEntityData.defineId(TaintCreeper.class,EntityDataSerializers.INT);
    private int fuse,lastFuse;
    public TaintCreeper(EntityType<? extends TaintCreeper> type,Level level) {super(type,level);}
    public static AttributeSupplier.Builder attributes() {return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH,30).add(Attributes.ATTACK_DAMAGE,2).add(Attributes.MOVEMENT_SPEED,0.25);}
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) {super.defineSynchedData(builder);builder.define(SWELL,-1);}
    @Override protected void registerGoals() {
        goalSelector.addGoal(1,new FloatGoal(this));goalSelector.addGoal(3,new AvoidEntityGoal<>(this,Ocelot.class,6,1,1.2));goalSelector.addGoal(4,new MeleeAttackGoal(this,1,false));
        goalSelector.addGoal(5,new WaterAvoidingRandomStrollGoal(this,1));goalSelector.addGoal(6,new LookAtPlayerGoal(this,Player.class,8));goalSelector.addGoal(6,new RandomLookAroundGoal(this));
        targetSelector.addGoal(1,new NearestAttackableTargetGoal<>(this,Player.class,true));targetSelector.addGoal(2,new HurtByTargetGoal(this));
    }
    @Override public boolean doHurtTarget(Entity target) {return true;}
    public float flash(float partial) {return (lastFuse+(fuse-lastFuse)*partial)/28f;}
    @Override public void tick() {
        if(isAlive()) {
            lastFuse=fuse;if(!level().isClientSide) {var target=getTarget();entityData.set(SWELL,target!=null&&hasLineOfSight(target)&&distanceToSqr(target)<(fuse>0?49:9)?1:-1);}
            int swell=entityData.get(SWELL);if(swell>0&&fuse==0) playSound(SoundEvents.CREEPER_PRIMED,1,0.5f);fuse=Math.max(0,Math.min(30,fuse+swell));
            if(fuse>=30&&!level().isClientSide) explode();
        }super.tick();
    }
    private void explode() {
        var level=(ServerLevel)level();level.explode(this,getX(),getY()+getBbHeight()/2,getZ(),1.5f,Level.ExplosionInteraction.NONE);
        for(var living:level.getEntitiesOfClass(LivingEntity.class,getBoundingBox().inflate(6))) if(!(living instanceof TaintedMob)&&!living.isInvertedHealAndHarm()) living.addEffect(new MobEffectInstance(TaintEffects.FLUX_TAINT,100));
        for(int i=0;i<10;i++) {
            var pos=blockPosition().offset((int)((random.nextFloat()-random.nextFloat())*5),0,(int)((random.nextFloat()-random.nextFloat())*5));
            if(random.nextBoolean()&&!WorldBiomes.is(level,pos,WorldBiomes.TAINT)) {WorldBiomes.change(level,pos.getX(),pos.getZ(),WorldBiomes.TAINT);if(level.getBlockState(pos.below()).isSolidRender(level,pos.below())&&level.getBlockState(pos).canBeReplaced()) TaintFibresBlock.spread(level,pos);}
        }discard();
    }
    @Override public boolean causeFallDamage(float distance,float multiplier,DamageSource source) {fuse=Math.min(25,fuse+(int)(distance*1.5));return super.causeFallDamage(distance,multiplier,source);}
    @Override protected void dropCustomDeathLoot(ServerLevel level,DamageSource source,boolean recentlyHit) {spawnAtLocation(random.nextBoolean()?R2Content.TAINT_TENDRIL.get():R2Content.TAINT_GOO.get());}
    @Override public void addAdditionalSaveData(CompoundTag tag) {super.addAdditionalSaveData(tag);tag.putInt("ThaumcraftFuse",fuse);}
    @Override public void readAdditionalSaveData(CompoundTag tag) {super.readAdditionalSaveData(tag);fuse=Math.clamp(tag.getInt("ThaumcraftFuse"),0,30);}
}
