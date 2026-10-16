package thaumcraft.entities;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.*;
import net.minecraft.server.level.*;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.*;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import thaumcraft.registry.*;

/** Crimson Praetor: source equipment, resistance aura and the red homing orb. */
public final class CultistLeader extends ThaumcraftBoss implements RangedAttackMob {
    private static final EntityDataAccessor<Integer> TITLE=SynchedEntityData.defineId(CultistLeader.class,EntityDataSerializers.INT);
    private static final String[] TITLES={"Alberic","Anselm","Bastian","Beturian","Chabier","Chorache","Chuse","Dodorol","Ebardo","Ferrando","Fertus","Guillen","Larpe","Obano","Zelipe"};
    public CultistLeader(EntityType<? extends CultistLeader> type,Level level){super(type,level);xpReward=40;}
    public static AttributeSupplier.Builder attributes(){return bossAttributes().add(Attributes.MAX_HEALTH,125).add(Attributes.ATTACK_DAMAGE,5).add(Attributes.MOVEMENT_SPEED,.32);}
    @Override protected void defineSynchedData(SynchedEntityData.Builder data){super.defineSynchedData(data);data.define(TITLE,0);}
    public String getTitle(){return TITLES[Math.floorMod(entityData.get(TITLE),TITLES.length)];}
    @Override public void generateChampionName(int modifier){setCustomName(Component.translatable("entity.thaumcraft.cultist_leader.champion",getTitle(),Component.translatable("champion.mod."+ChampionSystem.NAMES[modifier])));}
    @Override protected void registerGoals(){
        goalSelector.addGoal(0,new FloatGoal(this));
        goalSelector.addGoal(2,new RangedAttackGoal(this,1,30,40,24){@Override public boolean canUse(){return super.canUse()&&getTarget()!=null&&distanceToSqr(getTarget().getX(),getTarget().getBoundingBox().minY,getTarget().getZ())>=256;}});
        goalSelector.addGoal(3,new MeleeAttackGoal(this,1.1,false){private int attackTicks;@Override public void tick(){if(attackTicks>0)--attackTicks;super.tick();}@Override protected boolean canPerformAttack(LivingEntity target){float width=getBbWidth();return attackTicks<=0&&distanceToSqr(target.getX(),target.getBoundingBox().minY,target.getZ())<=width*2f*width*2f+target.getBbWidth();}@Override protected void checkAndPerformAttack(LivingEntity target){if(canPerformAttack(target)){attackTicks=10;if(!getMainHandItem().isEmpty())swing(InteractionHand.MAIN_HAND);doHurtTarget(target);}}});
        goalSelector.addGoal(6,new MoveTowardsRestrictionGoal(this,.8));goalSelector.addGoal(7,new RandomStrollGoal(this,.8));goalSelector.addGoal(8,new LookAtPlayerGoal(this,Player.class,8));goalSelector.addGoal(8,new RandomLookAroundGoal(this));
        targetSelector.addGoal(1,new HurtByTargetGoal(this){@Override protected void alertOthers(){for(var cultist:level().getEntitiesOfClass(Cultist.class,getBoundingBox().inflate(getFollowDistance(),10,getFollowDistance())))if(cultist!=getLastHurtByMob()&&cultist.getTarget()==null)cultist.setTarget(getLastHurtByMob());}});targetSelector.addGoal(2,new NearestAttackableTargetGoal<>(this,Player.class,true));
    }
    @Override public boolean isAlliedTo(Entity other){return other instanceof Cultist||other instanceof CultistLeader;}
    @Override protected void customServerAiStep(){super.customServerAiStep();for(var cultist:level().getEntitiesOfClass(Cultist.class,new net.minecraft.world.phys.AABB(getX(),getY(),getZ(),getX(),getY(),getZ()).inflate(8)))if(!cultist.hasEffect(MobEffects.DAMAGE_RESISTANCE))cultist.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE,60,1));}
    @Override public SpawnGroupData finalizeSpawn(ServerLevelAccessor world,net.minecraft.world.DifficultyInstance difficulty,MobSpawnType reason,SpawnGroupData group){setItemSlot(EquipmentSlot.HEAD,new ItemStack(R5Content.CULTIST_ARMOR.get("cultist_leader_helmet").get()));setItemSlot(EquipmentSlot.CHEST,new ItemStack(R5Content.CULTIST_ARMOR.get("cultist_leader_chestplate").get()));setItemSlot(EquipmentSlot.LEGS,new ItemStack(R5Content.CULTIST_ARMOR.get("cultist_leader_leggings").get()));setItemSlot(EquipmentSlot.FEET,new ItemStack(R5Content.CULTIST_ARMOR.get("cultist_boots").get()));setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(world.getDifficulty()==Difficulty.EASY?R3Equipment.TOOLS.get("void_sword").get():R5Content.CRIMSON_SWORD.get()));populateDefaultEquipmentEnchantments(world,random,difficulty);entityData.set(TITLE,random.nextInt(TITLES.length));return super.finalizeSpawn(world,difficulty,reason,group);}
    @Override protected void populateDefaultEquipmentEnchantments(ServerLevelAccessor world,net.minecraft.util.RandomSource random,net.minecraft.world.DifficultyInstance difficulty){float local=difficulty.getSpecialMultiplier();if(!getMainHandItem().isEmpty()&&random.nextFloat()<.5f*local){net.minecraft.world.item.enchantment.EnchantmentHelper.enchantItem(random,getMainHandItem(),(int)(7+local*random.nextInt(22)),world.registryAccess(),java.util.Optional.empty());}}
    @Override public void performRangedAttack(LivingEntity target,float power){if(!hasLineOfSight(target))return;swing(InteractionHand.MAIN_HAND);getLookControl().setLookAt(target.getX(),target.getBoundingBox().minY+target.getBbHeight()/2f,target.getZ(),30,30);var orb=new GolemOrb(level(),this,target,true);var velocity=orb.getDeltaMovement();orb.setPos(orb.getX()+velocity.x/2,orb.getY(),orb.getZ()+velocity.z/2);orb.shoot(target.getX()-getX(),target.getBoundingBox().minY+target.getBbHeight()/2f-(getY()+getBbHeight()/2f)+2,target.getZ()-getZ(),.66f,3);playSound(R2Sounds.GUARDIAN_ATTACK.get(),1,1+random.nextFloat()*.1f);level().addFreshEntity(orb);}
    @Override protected void dropCustomDeathLoot(ServerLevel world,DamageSource source,boolean hit){spawnAtLocation(new ItemStack(R5Content.LOOT_BAGS.get(2).get()),1.5f);}
    @Override public void addAdditionalSaveData(CompoundTag tag){super.addAdditionalSaveData(tag);tag.putByte("title",(byte)(int)entityData.get(TITLE));}
    @Override public void readAdditionalSaveData(CompoundTag tag){super.readAdditionalSaveData(tag);entityData.set(TITLE,(int)tag.getByte("title"));}
}
