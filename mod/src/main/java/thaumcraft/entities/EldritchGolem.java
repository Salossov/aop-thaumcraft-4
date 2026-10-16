package thaumcraft.entities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.*;
import net.minecraft.sounds.*;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.*;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.blocks.AncientLootBlock;
import thaumcraft.registry.R2Sounds;

/** Original construct phases, ten-tick melee gate and150-tick headless projectile recharge. */
public final class EldritchGolem extends ThaumcraftBoss implements RangedAttackMob,thaumcraft.api.entities.IEldritchMob {
    private static final EntityDataAccessor<Boolean> HEADLESS=SynchedEntityData.defineId(EldritchGolem.class,EntityDataSerializers.BOOLEAN);
    private int beamCharge,attackTimer;
    private boolean chargingBeam;
    public int arcing,arcX,arcY,arcZ;
    public static java.util.function.Consumer<EldritchGolem> clientEffects=golem->{};
    public EldritchGolem(EntityType<? extends EldritchGolem> type,Level level){super(type,level);}
    public static AttributeSupplier.Builder attributes(){return bossAttributes().add(Attributes.MOVEMENT_SPEED,.3).add(Attributes.ATTACK_DAMAGE,10).add(Attributes.MAX_HEALTH,250).add(Attributes.ARMOR,6);}
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder){super.defineSynchedData(builder);builder.define(HEADLESS,false);}
    @Override protected void registerGoals(){goalSelector.addGoal(0,new FloatGoal(this));goalSelector.addGoal(3,new MeleeAttackGoal(this,1.1,false){
        private int attackTicks;
        @Override public void tick(){if(attackTicks>0)--attackTicks;super.tick();}
        @Override protected boolean canPerformAttack(LivingEntity target){float width=getBbWidth();double reach=width*2f*width*2f+target.getBbWidth();return attackTicks<=0&&distanceToSqr(target.getX(),target.getBoundingBox().minY,target.getZ())<=reach;}
        @Override protected void checkAndPerformAttack(LivingEntity target){if(canPerformAttack(target)){attackTicks=10;if(!getMainHandItem().isEmpty())swing(net.minecraft.world.InteractionHand.MAIN_HAND);doHurtTarget(target);}}
    });goalSelector.addGoal(6,new MoveTowardsRestrictionGoal(this,.8));goalSelector.addGoal(7,new RandomStrollGoal(this,.8));goalSelector.addGoal(8,new LookAtPlayerGoal(this,Player.class,8));goalSelector.addGoal(8,new RandomLookAroundGoal(this));targetSelector.addGoal(1,new HurtByTargetGoal(this).setAlertOthers());targetSelector.addGoal(2,new NearestAttackableTargetGoal<>(this,Player.class,true));}
    public boolean isHeadless(){return entityData.get(HEADLESS);}
    public void setHeadless(boolean value){entityData.set(HEADLESS,value);refreshDimensions();}
    @Override public void onSyncedDataUpdated(EntityDataAccessor<?> key){super.onSyncedDataUpdated(key);if(HEADLESS.equals(key))refreshDimensions();}
    private void makeHeadless(){goalSelector.addGoal(2,new RangedAttackGoal(this,1,5,5,24){@Override public boolean canUse(){if(!super.canUse())return false;var target=getTarget();if(target==null)return false;if(!target.isAlive()){setTarget(null);return false;}return distanceToSqr(target)>=9;}});}
    @Override public EntityDimensions getDefaultDimensions(Pose pose){return super.getDefaultDimensions(pose).withEyeHeight(isHeadless()?3.33f:3f);}
    @Override public SpawnGroupData finalizeSpawn(ServerLevelAccessor world,net.minecraft.world.DifficultyInstance difficulty,MobSpawnType reason,SpawnGroupData group){spawnTimer=100;return super.finalizeSpawn(world,difficulty,reason,group);}
    @Override public boolean hurt(DamageSource source,float damage){
        if(!level().isClientSide&&damage>getHealth()&&!isHeadless()){setHeadless(true);spawnTimer=100;float angle=getYRot()%360/180*(float)Math.PI;level().explode(this,getX()+Mth.cos(angle)*.75f,getY()+getEyeHeight(),getZ()+Mth.sin(angle)*.75f,2,false,Level.ExplosionInteraction.MOB);makeHeadless();return false;}
        return super.hurt(source,damage);
    }
    @Override public boolean doHurtTarget(Entity target){if(attackTimer>0)return false;attackTimer=10;level().broadcastEntityEvent(this,(byte)4);boolean hit=target.hurt(damageSources().mobAttack(this),(float)getAttributeValue(Attributes.ATTACK_DAMAGE)*.75f);if(hit){target.setDeltaMovement(target.getDeltaMovement().add(0,.2000000059604645,0));if(isHeadless())target.push(-Mth.sin(getYRot()*(float)Math.PI/180)*1.5f,.1,Mth.cos(getYRot()*(float)Math.PI/180)*1.5f);}return hit;}
    @Override public void performRangedAttack(LivingEntity target,float power){if(!level().isClientSide&&hasLineOfSight(target)&&!chargingBeam&&beamCharge>0){beamCharge-=15+random.nextInt(5);getLookControl().setLookAt(target.getX(),target.getBoundingBox().minY+target.getBbHeight()/2f,target.getZ(),30,30);var look=getViewVector(1);var orb=new GolemOrb(level(),this,target,false);orb.setPos(orb.getX()+look.x,orb.getY(),orb.getZ()+look.z);orb.shoot(target.getX()+target.getDeltaMovement().x-getX(),target.getY()-getY()-target.getBbHeight()/2f,target.getZ()+target.getDeltaMovement().z-getZ(),.66f,5);playSound(R2Sounds.GUARDIAN_ATTACK.get(),1,1+random.nextFloat()*.1f);level().addFreshEntity(orb);}}
    @Override public void tick(){if(getSpawnTimer()==150)level().broadcastEntityEvent(this,(byte)18);if(getSpawnTimer()>0)heal(2);super.tick();if(level().isClientSide){if(isHeadless()){setXRot(0);clientEffects.accept(this);}return;}if(isHeadless()&&beamCharge<=0)chargingBeam=true;if(isHeadless()&&chargingBeam){beamCharge++;level().broadcastEntityEvent(this,(byte)19);if(beamCharge==150)chargingBeam=false;}}
    @Override public void aiStep(){super.aiStep();if(attackTimer>0)--attackTimer;
        var velocity=getDeltaMovement();if(velocity.x*velocity.x+velocity.z*velocity.z>2.500000277905201E-7&&random.nextInt(5)==0){var pos=BlockPos.containing(getX(),getY()-(double).2f,getZ());var state=level().getBlockState(pos);if(!state.isAir())level().addParticle(new BlockParticleOption(ParticleTypes.BLOCK,state),getX()+(random.nextFloat()-.5)*getBbWidth(),getBoundingBox().minY+.1,getZ()+(random.nextFloat()-.5)*getBbWidth(),4*(random.nextFloat()-.5),.5,(random.nextFloat()-.5)*4);if(!level().isClientSide&&state.getBlock() instanceof AncientLootBlock)level().destroyBlock(pos,true,this);}
        if(!level().isClientSide){var pos=BlockPos.containing(getX()+velocity.x,getBoundingBox().minY,getZ()+velocity.z);float hardness=level().getBlockState(pos).getDestroySpeed(level(),pos);if(hardness>=0&&hardness<=.15f)level().destroyBlock(pos,true,this);}
    }
    @Override public void handleEntityEvent(byte event){if(event==4){attackTimer=10;playSound(SoundEvents.IRON_GOLEM_ATTACK,1,1);}else if(event==18)spawnTimer=150;else if(event==19){if(arcing==0){float radius=2+random.nextFloat()*2;double radians=Math.toRadians(random.nextInt(360));int x=Mth.floor(getX()+radius*Math.cos(radians)),y=Mth.floor(getY()),z=Mth.floor(getZ()+radius*Math.sin(radians));int attempts=0;while(attempts<5&&level().isEmptyBlock(new BlockPos(x,y,z))){attempts++;y--;}if(level().isEmptyBlock(new BlockPos(x,y+1,z))&&!level().isEmptyBlock(new BlockPos(x,y,z))){arcX=x;arcY=y;arcZ=z;arcing=8+random.nextInt(5);playSound(R2Sounds.JACOBS.get(),.8f,1+(random.nextFloat()-random.nextFloat())*.05f);}}}else super.handleEntityEvent(event);}
    public int getAttackTimer(){return attackTimer;}
    public int getBeamCharge(){return beamCharge;}
    public boolean isChargingBeam(){return chargingBeam;}
    @Override protected SoundEvent getHurtSound(DamageSource source){return SoundEvents.IRON_GOLEM_HURT;}
    @Override protected SoundEvent getDeathSound(){return SoundEvents.IRON_GOLEM_DEATH;}
    @Override protected void playStepSound(BlockPos pos,BlockState state){playSound(SoundEvents.IRON_GOLEM_STEP,1,1);}
    @Override public void addAdditionalSaveData(CompoundTag tag){super.addAdditionalSaveData(tag);tag.putBoolean("headless",isHeadless());}
    @Override public void readAdditionalSaveData(CompoundTag tag){super.readAdditionalSaveData(tag);setHeadless(tag.getBoolean("headless"));if(isHeadless())makeHeadless();}
}
