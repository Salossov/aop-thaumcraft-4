package thaumcraft.entities;

import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.*;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.*;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.*;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.*;
import net.minecraft.world.level.*;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.items.WispEssenceItem;
import thaumcraft.knowledge.KnowledgeService;
import thaumcraft.registry.*;

public final class EldritchGuardian extends Monster implements RangedAttackMob,thaumcraft.api.entities.IEldritchMob {
    private static final TagKey<DamageType> MAGIC=TagKey.create(Registries.DAMAGE_TYPE,ResourceLocation.parse("thaumcraft:magic"));
    private static final TagKey<EntityType<?>> CULTISTS=TagKey.create(Registries.ENTITY_TYPE,ResourceLocation.parse("thaumcraft:cultists"));
    public float armLiftL,armLiftR;
    private boolean lastBlast;
    public EldritchGuardian(EntityType<? extends EldritchGuardian> type,Level level) {super(type,level);xpReward=20;if(getNavigation() instanceof GroundPathNavigation navigation)navigation.setCanOpenDoors(true);}
    public static AttributeSupplier.Builder attributes() {return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH,50).add(Attributes.MAX_ABSORPTION,50).add(Attributes.FOLLOW_RANGE,40).add(Attributes.MOVEMENT_SPEED,0.28).add(Attributes.ATTACK_DAMAGE,7).add(Attributes.ARMOR,4);}
    @Override protected void registerGoals() {
        goalSelector.addGoal(0,new FloatGoal(this));
        goalSelector.addGoal(2,new RangedAttackGoal(this,1,20,40,24) {
            @Override public boolean canUse() {return super.canUse()&&getTarget()!=null&&distanceToSqr(getTarget())>=64;}
            @Override public boolean canContinueToUse() {return getTarget()!=null&&getTarget().isAlive()&&distanceToSqr(getTarget())>=64&&super.canContinueToUse();}
        });
        goalSelector.addGoal(3,new MeleeAttackGoal(this,1,false));goalSelector.addGoal(5,new MoveTowardsRestrictionGoal(this,0.8));goalSelector.addGoal(7,new WaterAvoidingRandomStrollGoal(this,1));goalSelector.addGoal(8,new LookAtPlayerGoal(this,Player.class,8));goalSelector.addGoal(8,new RandomLookAroundGoal(this));
        targetSelector.addGoal(1,new HurtByTargetGoal(this));targetSelector.addGoal(2,new NearestAttackableTargetGoal<>(this,Player.class,true));
        targetSelector.addGoal(3,new NearestAttackableTargetGoal<>(this,LivingEntity.class,10,true,false,e->e.getType().is(CULTISTS)));
    }
    @Override public boolean hurt(DamageSource source,float damage) {return super.hurt(source,source.is(MAGIC)?damage/2:damage);}
    @Override protected int decreaseAirSupply(int supply) {return supply;}
    private boolean outer() {return level().dimension().location().equals(ResourceLocation.parse("thaumcraft:outer_lands"));}
    @Override public void tick() {
        super.tick();armLiftL=Math.max(0,armLiftL-0.05f);armLiftR=Math.max(0,armLiftR-0.05f);
        if(level().isClientSide)return;
        if(outer()&&invulnerableTime<=0&&tickCount%25==0&&getAbsorptionAmount()<getMaxHealth()/2)setAbsorptionAmount(Math.min(getMaxHealth()/2,getAbsorptionAmount()+1));
        if(!outer()&&tickCount%100==0&&level().getDifficulty()!=Difficulty.EASY) {
            double range=level().getDifficulty()==Difficulty.HARD?576:256;
            for(var player:((ServerLevel)level()).players())if(player.isAlive()&&distanceToSqr(player)<range)net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(player,new thaumcraft.network.GuardianMistPayload());
        }
    }
    @Override public SpawnGroupData finalizeSpawn(ServerLevelAccessor level,net.minecraft.world.DifficultyInstance difficulty,MobSpawnType reason,SpawnGroupData data) {var result=super.finalizeSpawn(level,difficulty,reason,data);if(outer())setAbsorptionAmount(getMaxHealth()/2);return result;}
    @Override public boolean doHurtTarget(Entity target) {boolean hit=super.doHurtTarget(target);int difficulty=level().getDifficulty().getId();if(hit&&getMainHandItem().isEmpty()&&isOnFire()&&random.nextFloat()<difficulty*0.3f)target.igniteForSeconds(2*difficulty);return hit;}
    @Override public void performRangedAttack(LivingEntity target,float strength) {if(random.nextFloat()>0.1f)shootOrb(target);else if(hasLineOfSight(target))sonicAttack(target);}
    public void shootOrb(LivingEntity target) {
        if(level().isClientSide)return;var orb=new EldritchOrb(level(),this);lastBlast=!lastBlast;level().broadcastEntityEvent(this,lastBlast?(byte)16:(byte)15);
        double angle=Math.toRadians((getYRot()+(lastBlast?90:180))%360);orb.setPos(orb.getX()-Math.cos(angle)*0.5,orb.getY()-0.057777777*getBbHeight(),orb.getZ()-Math.sin(angle)*0.5);
        orb.shoot(target.getX()+target.getDeltaMovement().x-getX(),target.getY()-getY()-target.getBbHeight()/2,target.getZ()+target.getDeltaMovement().z-getZ(),1,2);
        playSound(R2Sounds.GUARDIAN_ATTACK.get(),2,1+random.nextFloat()*0.1f);level().addFreshEntity(orb);
    }
    public void sonicAttack(LivingEntity target) {
        if(level().isClientSide)return;level().broadcastEntityEvent(this,(byte)17);target.addEffect(new MobEffectInstance(MobEffects.WITHER,400));
        if(target instanceof ServerPlayer player) {int amount=1+random.nextInt(3);KnowledgeService.mutate(player,k->{k.setWarp(k.permanentWarp(),k.stickyWarp(),k.temporaryWarp()+amount,k.warpCounter());return true;});}
        playSound(R2Sounds.GUARDIAN_SCREECH.get(),3,1+random.nextFloat()*0.1f);
    }
    @Override public void handleEntityEvent(byte event) {if(event==15)armLiftL=0.5f;else if(event==16)armLiftR=0.5f;else if(event==17){armLiftL=0.9f;armLiftR=0.9f;}else super.handleEntityEvent(event);}
    @Override protected SoundEvent getAmbientSound() {return R2Sounds.GUARDIAN_IDLE.get();}
    @Override protected SoundEvent getDeathSound() {return R2Sounds.GUARDIAN_DEATH.get();}
    @Override public int getAmbientSoundInterval() {return 500;}
    @Override protected float getSoundVolume() {return 1.5f;}
    @Override protected void dropCustomDeathLoot(ServerLevel level,DamageSource source,boolean recentlyHit) {
        if(random.nextBoolean())spawnAtLocation(WispEssenceItem.of(Aspect.getAspect("exanimis")));if(random.nextBoolean())spawnAtLocation(WispEssenceItem.of(Aspect.getAspect("alienis")));
        int looting=source.getEntity() instanceof LivingEntity killer?EnchantmentHelper.getEnchantmentLevel(level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.LOOTING),killer):0;
        if(recentlyHit&&random.nextFloat()<0.025f+looting*0.01f)spawnAtLocation(R2Content.ELDRITCH_EYE.get());
    }
    @Override public boolean removeWhenFarAway(double distance) {return !hasRestriction();}
    @Override public boolean isAlliedTo(Entity other) {return other instanceof thaumcraft.api.entities.IEldritchMob||super.isAlliedTo(other);}
    @Override public void addAdditionalSaveData(CompoundTag tag) {super.addAdditionalSaveData(tag);if(hasRestriction()){tag.putInt("HomeD",(int)getRestrictRadius());tag.putInt("HomeX",getRestrictCenter().getX());tag.putInt("HomeY",getRestrictCenter().getY());tag.putInt("HomeZ",getRestrictCenter().getZ());}}
    @Override public void readAdditionalSaveData(CompoundTag tag) {super.readAdditionalSaveData(tag);if(tag.contains("HomeD"))restrictTo(new net.minecraft.core.BlockPos(tag.getInt("HomeX"),tag.getInt("HomeY"),tag.getInt("HomeZ")),tag.getInt("HomeD"));}
}
