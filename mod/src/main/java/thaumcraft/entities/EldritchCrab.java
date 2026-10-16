package thaumcraft.entities;

import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.*;
import net.minecraft.sounds.*;
import net.minecraft.tags.TagKey;
import net.minecraft.world.*;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.*;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.*;
import net.minecraft.world.level.*;
import net.minecraft.world.phys.Vec3;
import thaumcraft.registry.*;

public final class EldritchCrab extends Monster {
    private static final EntityDataAccessor<Byte> FLAGS=SynchedEntityData.defineId(EldritchCrab.class,EntityDataSerializers.BYTE);
    private static final TagKey<EntityType<?>> CULTISTS=TagKey.create(Registries.ENTITY_TYPE,ResourceLocation.parse("thaumcraft:cultists"));
    private int rideAttackTime;
    public EldritchCrab(EntityType<? extends EldritchCrab> type,Level level){super(type,level);xpReward=6;setCanPickUpLoot(false);if(getNavigation() instanceof GroundPathNavigation navigation)navigation.setCanOpenDoors(true);setPathfindingMalus(net.minecraft.world.level.pathfinder.PathType.WATER,8);}
    // Original constructor initializes attributes while Flags=0; setting the spawn helmet does not change speed.
    public static AttributeSupplier.Builder attributes(){return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH,20).add(Attributes.ATTACK_DAMAGE,4).add(Attributes.MOVEMENT_SPEED,.3);}
    @Override protected void defineSynchedData(SynchedEntityData.Builder data){super.defineSynchedData(data);data.define(FLAGS,(byte)0);}
    @Override protected void registerGoals(){goalSelector.addGoal(0,new FloatGoal(this));goalSelector.addGoal(2,new LeapAtTargetGoal(this,.63f));goalSelector.addGoal(3,new MeleeAttackGoal(this,1,false));goalSelector.addGoal(7,new RandomStrollGoal(this,.8));goalSelector.addGoal(8,new RandomLookAroundGoal(this));targetSelector.addGoal(1,new HurtByTargetGoal(this).setAlertOthers());targetSelector.addGoal(2,new NearestAttackableTargetGoal<>(this,Player.class,true));targetSelector.addGoal(3,new NearestAttackableTargetGoal<>(this,LivingEntity.class,0,true,false,e->e.getType().is(CULTISTS)));}
    public boolean hasHelm(){return (entityData.get(FLAGS)&1)!=0;}
    public void setHelm(boolean helmet){byte flags=entityData.get(FLAGS);entityData.set(FLAGS,(byte)(helmet?flags|1:flags&~1));}
    @Override public int getArmorValue(){return hasHelm()?5:0;}
    @Override public boolean canPickUpLoot(){return false;}
    @Override public boolean canBeAffected(MobEffectInstance effect){return !effect.is(MobEffects.POISON)&&super.canBeAffected(effect);}
    @Override public boolean isAlliedTo(Entity other){return other instanceof EldritchCrab;}
    @Override public Vec3 getVehicleAttachmentPoint(Entity vehicle){return new Vec3(0,isPassenger()?-.5:0,0);}
    @Override public SpawnGroupData finalizeSpawn(ServerLevelAccessor level,DifficultyInstance difficulty,MobSpawnType reason,SpawnGroupData data){setHelm(level.getDifficulty()==Difficulty.HARD||random.nextFloat()<.33f);if(data==null){var group=new Spider.SpiderEffectsGroupData();if(level.getDifficulty()==Difficulty.HARD&&level.getRandom().nextFloat()<.1f*difficulty.getSpecialMultiplier())group.setRandomEffect(level.getRandom());data=group;}if(data instanceof Spider.SpiderEffectsGroupData group&&group.effect!=null)addEffect(new MobEffectInstance(group.effect,Integer.MAX_VALUE));return super.finalizeSpawn(level,difficulty,reason,data);}
    @Override public void tick(){super.tick();if(tickCount<20)fallDistance=0;if(rideAttackTime>0)--rideAttackTime;var target=getTarget();if(!isPassenger()&&target!=null&&target.getPassengers().isEmpty()&&!onGround()&&!hasHelm()&&target.isAlive()&&getY()-target.getY()>=target.getBbHeight()/2f&&distanceToSqr(target)<4)startRiding(target,true);if(!level().isClientSide&&isPassenger()&&rideAttackTime<=0){rideAttackTime=10+random.nextInt(10);var vehicle=getVehicle();doHurtTarget(vehicle);if(isPassenger()&&random.nextFloat()<.2f){setPos(LegacyRiderPosition.choose(this,getVehicle()));}}}
    @Override public boolean hurt(DamageSource source,float amount){boolean hit=super.hurt(source,amount);if(hasHelm()&&getHealth()/getMaxHealth()<=.5f){setHelm(false);if(!level().isClientSide)spawnAtLocation(new ItemStack(R5Content.CULTIST_ARMOR.get("cultist_plate_chestplate").get()));getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(.3);}return hit;}
    @Override public boolean doHurtTarget(Entity target){boolean hit=super.doHurtTarget(target);if(hit)playSound(R2Sounds.CRAB_CLAW.get(),1,.9f+level().random.nextFloat()*.2f);return hit;}
    @Override protected void dropCustomDeathLoot(ServerLevel level,DamageSource source,boolean recentlyHit){super.dropCustomDeathLoot(level,source,recentlyHit);int looting=source.getEntity() instanceof LivingEntity killer?EnchantmentHelper.getEnchantmentLevel(level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.LOOTING),killer):0;if(recentlyHit&&(random.nextInt(3)==0||random.nextInt(1+looting)>0))spawnAtLocation(Items.ENDER_PEARL);}
    @Override public int getAmbientSoundInterval(){return 160;}
    @Override protected SoundEvent getAmbientSound(){return R2Sounds.CRAB_IDLE.get();}
    @Override protected SoundEvent getHurtSound(DamageSource source){return SoundEvents.HOSTILE_HURT;}
    @Override protected SoundEvent getDeathSound(){return R2Sounds.CRAB_DEATH.get();}
    @Override protected void playStepSound(net.minecraft.core.BlockPos pos,net.minecraft.world.level.block.state.BlockState state){playSound(SoundEvents.SPIDER_STEP,.15f,1);}
    @Override public void addAdditionalSaveData(CompoundTag tag){super.addAdditionalSaveData(tag);tag.putByte("Flags",entityData.get(FLAGS));}
    @Override public void readAdditionalSaveData(CompoundTag tag){super.readAdditionalSaveData(tag);entityData.set(FLAGS,tag.getByte("Flags"));if(!hasHelm())getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(.3);}
}
