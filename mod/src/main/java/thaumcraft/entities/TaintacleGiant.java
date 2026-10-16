package thaumcraft.entities;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.*;
import net.minecraft.server.level.*;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import thaumcraft.registry.R3Equipment;

/** Giant inherits the rooted tentacle, including its tainted/eldritch terrain restrictions. */
public final class TaintacleGiant extends Taintacle {
    private static final EntityDataAccessor<Integer> ANGER=SynchedEntityData.defineId(TaintacleGiant.class,EntityDataSerializers.INT);
    private final ServerBossEvent bossEvent;
    public TaintacleGiant(EntityType<? extends TaintacleGiant> type,Level level){super(type,level);xpReward=20;bossEvent=new ServerBossEvent(getDisplayName(),BossEvent.BossBarColor.PURPLE,BossEvent.BossBarOverlay.PROGRESS);}
    public static AttributeSupplier.Builder attributes(){return Taintacle.attributes(false).add(Attributes.MAX_HEALTH,125).add(Attributes.ATTACK_DAMAGE,9);}
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder){super.defineSynchedData(builder);builder.define(ANGER,0);}
    public int getAnger(){return entityData.get(ANGER);}
    public void setAnger(int anger){entityData.set(ANGER,(int)(short)anger);}
    @Override public SpawnGroupData finalizeSpawn(ServerLevelAccessor world,net.minecraft.world.DifficultyInstance difficulty,MobSpawnType reason,SpawnGroupData group){ChampionSystem.makeChampion(this,true);return group;}
    @Override public void tick(){super.tick();if(getAnger()>0)setAnger(getAnger()-1);if(level().isClientSide){if(random.nextInt(15)==0&&getAnger()>0){double dx=random.nextGaussian()*.02,dy=random.nextGaussian()*.02,dz=random.nextGaussian()*.02;level().addParticle(ParticleTypes.ANGRY_VILLAGER,getX()+random.nextFloat()*getBbWidth()-getBbWidth()/2,getBoundingBox().minY+getBbHeight()+random.nextFloat()*.5,getZ()+random.nextFloat()*getBbWidth()-getBbWidth()/2,dx,dy,dz);}}else{if(tickCount%30==0)heal(1);bossEvent.setName(getDisplayName());bossEvent.setProgress(getHealth()/getMaxHealth());}}
    @Override public boolean checkSpawnRules(LevelAccessor level,MobSpawnType reason){return false;}
    @Override public boolean removeWhenFarAway(double distance){return false;}
    @Override protected int decreaseAirSupply(int air){return air;}
    @Override public boolean hurt(DamageSource source,float amount){if(!level().isClientSide&&amount>35){if(getAnger()==0){addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE,200,(int)(amount/15)));addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST,200,(int)(amount/40)));addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED,200,(int)(amount/40)));setAnger(200);if(source.getEntity() instanceof Player player)player.sendSystemMessage(getDisplayName().copy().append(" ").append(Component.translatable("tc.boss.enrage")));}amount=35;}return super.hurt(source,amount);}
    @Override protected void dropCustomDeathLoot(ServerLevel world,DamageSource source,boolean hit){if(world.getEntitiesOfClass(TaintacleGiant.class,new net.minecraft.world.phys.AABB(getX(),getY(),getZ(),getX(),getY(),getZ()).inflate(48),e->e!=this).isEmpty()){var pearl=R5Entities.SPECIAL_ITEM.get().create(world);pearl.setItem(new ItemStack(R3Equipment.PRIMAL_PEARL.get()));pearl.setPos(getX(),getY()+getBbHeight()/2f,getZ());pearl.setPickUpDelay(10);pearl.setDeltaMovement(0,(double).1f,0);world.addFreshEntity(pearl);}}
    @Override public void startSeenByPlayer(ServerPlayer player){super.startSeenByPlayer(player);bossEvent.addPlayer(player);}
    @Override public void stopSeenByPlayer(ServerPlayer player){super.stopSeenByPlayer(player);bossEvent.removePlayer(player);}
}
