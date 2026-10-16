package thaumcraft.entities;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.*;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.*;
import net.minecraft.world.*;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.phys.Vec3;
import thaumcraft.registry.*;

public final class CultistCleric extends Cultist implements net.minecraft.world.entity.monster.RangedAttackMob {
    private static final EntityDataAccessor<Byte> FLAGS=SynchedEntityData.defineId(CultistCleric.class,EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<BlockPos> HOME=SynchedEntityData.defineId(CultistCleric.class,EntityDataSerializers.BLOCK_POS);
    public CultistCleric(EntityType<? extends CultistCleric> type,Level level){super(type,level);}
    public static AttributeSupplier.Builder attributes(){return Cultist.attributes().add(Attributes.MAX_HEALTH,30);}
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder){super.defineSynchedData(builder);builder.define(FLAGS,(byte)0);builder.define(HOME,BlockPos.ZERO);}
    public boolean isRitualist(){return (entityData.get(FLAGS)&1)!=0;}
    public void setRitualist(boolean value){entityData.set(FLAGS,(byte)(value?entityData.get(FLAGS)|1:entityData.get(FLAGS)&~1));}
    public BlockPos ritualHome(){return entityData.get(HOME);}
    @Override public void restrictTo(BlockPos pos,int radius){super.restrictTo(pos,radius);if(entityData!=null)entityData.set(HOME,pos.immutable());}
    @Override protected void registerGoals(){goalSelector.addGoal(0,new FloatGoal(this));goalSelector.addGoal(1,new Goal(){ {setFlags(java.util.EnumSet.of(Flag.MOVE,Flag.LOOK,Flag.JUMP));}public boolean canUse(){return isRitualist()&&hasRestriction();}public boolean canContinueToUse(){return canUse();}public void tick(){if(tickCount%40==0&&(getRestrictCenter().distSqr(BlockPos.containing(getX(),getY(),getZ()))>16||!(level().getBlockState(getRestrictCenter()).getBlock() instanceof thaumcraft.production.EldritchAltarBlock||level().getBlockState(getRestrictCenter()).is(R5Content.OBELISK.get())||level().getBlockState(getRestrictCenter()).is(R5Content.ELDRITCH_CAP.get()))))setRitualist(false);}});goalSelector.addGoal(2,new RangedAttackGoal(this,1,20,40,24){@Override public boolean canUse(){return super.canUse()&&getTarget()!=null&&distanceToSqr(getTarget().getX(),getTarget().getBoundingBox().minY,getTarget().getZ())>=4;}});goalSelector.addGoal(3,new Melee(this,1));goalSelector.addGoal(5,new OpenDoorGoal(this,true));goalSelector.addGoal(6,new MoveTowardsRestrictionGoal(this,.8));goalSelector.addGoal(7,new RandomStrollGoal(this,.8));goalSelector.addGoal(8,new LookAtPlayerGoal(this,Player.class,8));goalSelector.addGoal(8,new RandomLookAroundGoal(this));targetSelector.addGoal(1,new Retaliate(this));targetSelector.addGoal(2,new NearestAttackableTargetGoal<>(this,Player.class,true));}
    @Override protected void populateDefaultEquipmentSlots(RandomSource rng,DifficultyInstance difficulty){for(var entry:java.util.Map.of(EquipmentSlot.HEAD,"cultist_robe_helmet",EquipmentSlot.CHEST,"cultist_robe_chestplate",EquipmentSlot.LEGS,"cultist_robe_leggings").entrySet())setItemSlot(entry.getKey(),new ItemStack(R5Content.CULTIST_ARMOR.get(entry.getValue()).get()));if(rng.nextFloat()<(level().getDifficulty()==Difficulty.HARD?.3f:.1f))setItemSlot(EquipmentSlot.FEET,new ItemStack(R5Content.CULTIST_ARMOR.get("cultist_boots").get()));}
    @Override public SpawnGroupData finalizeSpawn(ServerLevelAccessor level,DifficultyInstance difficulty,MobSpawnType reason,SpawnGroupData data){var result=super.finalizeSpawn(level,difficulty,reason,data);populateDefaultEquipmentSlots(random,difficulty);return result;}
    @Override public void performRangedAttack(LivingEntity target,float strength){if(level().isClientSide)return;double dx=target.getX()-getX(),dy=target.getBoundingBox().minY+target.getBbHeight()/2f-(getY()+getBbHeight()/2f),dz=target.getZ()-getZ();swing(InteractionHand.MAIN_HAND);if(random.nextFloat()>.66f){var orb=new GolemOrb(level(),this,target,true);orb.setPos(orb.getX()+orb.getDeltaMovement().x/2,orb.getY(),orb.getZ()+orb.getDeltaMovement().z/2);orb.shoot(dx,dy+2,dz,.66f,3);playSound(R2Sounds.GUARDIAN_ATTACK.get(),1,1+random.nextFloat()*.1f);level().addFreshEntity(orb);}else{float spread=Mth.sqrt(strength)*.5f;level().levelEvent(null,1018,BlockPos.containing((int)getX(),(int)getY(),(int)getZ()),0);for(int i=0;i<3;i++){var fireball=new SmallFireball(level(),this,new Vec3(dx+random.nextGaussian()*spread,dy,dz+random.nextGaussian()*spread));fireball.setPos(fireball.getX(),getY()+getBbHeight()/2f+.5,fireball.getZ());level().addFreshEntity(fireball);}}}
    @Override public boolean removeWhenFarAway(double distance){return !isRitualist();}
    @Override public boolean hurt(DamageSource source,float damage){if(isInvulnerableTo(source))return false;setRitualist(false);return super.hurt(source,damage);}
    @Override public void tick(){super.tick();if(level().isClientSide&&isRitualist()){var pos=ritualHome();double dx=pos.getX()+.5-getX(),dy=pos.getY()+1.5-(getY()+getEyeHeight()),dz=pos.getZ()+.5-getZ();setXRot(rotate(getXRot(),(float)-(Math.atan2(dy,Math.sqrt(dx*dx+dz*dz))*180/Math.PI),10));setYHeadRot(rotate(getYHeadRot(),(float)(Math.atan2(dz,dx)*180/Math.PI)-90,getMaxHeadYRot()));}}
    private static float rotate(float current,float target,float maximum){return current+Mth.clamp(Mth.wrapDegrees(target-current),-maximum,maximum);}
    @Override protected SoundEvent getAmbientSound(){return R2Sounds.CHANT.get();}
    @Override public int getAmbientSoundInterval(){return 500;}
    @Override public void addAdditionalSaveData(CompoundTag tag){super.addAdditionalSaveData(tag);tag.putByte("Flags",entityData.get(FLAGS));}
    @Override public void readAdditionalSaveData(CompoundTag tag){super.readAdditionalSaveData(tag);entityData.set(FLAGS,tag.getByte("Flags"));}
}
