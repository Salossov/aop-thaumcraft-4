package thaumcraft.entities;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.*;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.phys.Vec3;
import thaumcraft.registry.*;

/** TC4 common boss runtime. Spawn countdown and aggro remain transient, as in the original. */
public abstract class ThaumcraftBoss extends Monster {
    private static final EntityDataAccessor<Integer> ANGER=SynchedEntityData.defineId(ThaumcraftBoss.class,EntityDataSerializers.INT);
    private static final ResourceLocation[] HP=new ResourceLocation[5],DAMAGE=new ResourceLocation[5];
    static{for(int i=0;i<5;i++){HP[i]=ResourceLocation.parse("thaumcraft:boss_health_"+i);DAMAGE[i]=ResourceLocation.parse("thaumcraft:boss_damage_"+i);}}
    protected final Map<Integer,Integer> aggro=new HashMap<>();
    protected int spawnTimer;
    private final ServerBossEvent bossEvent;
    protected ThaumcraftBoss(EntityType<? extends ThaumcraftBoss> type,Level level){super(type,level);xpReward=50;bossEvent=new ServerBossEvent(getDisplayName(),BossEvent.BossBarColor.PURPLE,BossEvent.BossBarOverlay.PROGRESS);}
    public static AttributeSupplier.Builder bossAttributes(){return Monster.createMonsterAttributes().add(Attributes.KNOCKBACK_RESISTANCE,.95).add(Attributes.FOLLOW_RANGE,40);}
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder){super.defineSynchedData(builder);builder.define(ANGER,0);}
    public int getAnger(){return entityData.get(ANGER);}
    public void setAnger(int value){entityData.set(ANGER,(int)(short)value);}
    public int getSpawnTimer(){return spawnTimer;}
    public void generateChampionName(int modifier){setCustomName(net.minecraft.network.chat.Component.translatable("entity.thaumcraft."+getType().builtInRegistryHolder().key().location().getPath()+".champion",net.minecraft.network.chat.Component.translatable("champion.mod."+ChampionSystem.NAMES[modifier])));}
    @Override public boolean isEffectiveAi(){return spawnTimer==0&&super.isEffectiveAi();}
    @Override public boolean isInvulnerableTo(DamageSource source){return spawnTimer>0||super.isInvulnerableTo(source);}
    @Override public boolean isPushable(){return spawnTimer==0&&!isInvulnerable()&&super.isPushable();}
    @Override protected int decreaseAirSupply(int air){return air;}
    @Override public void makeStuckInBlock(net.minecraft.world.level.block.state.BlockState state,Vec3 multiplier){if(!state.is(net.minecraft.world.level.block.Blocks.COBWEB))super.makeStuckInBlock(state,multiplier);}
    @Override public boolean canPickUpLoot(){return false;}
    @Override public boolean removeWhenFarAway(double distance){return false;}
    @Override public boolean isAlliedTo(Entity other){return other instanceof thaumcraft.api.entities.IEldritchMob;}
    @Override public SpawnGroupData finalizeSpawn(ServerLevelAccessor world,net.minecraft.world.DifficultyInstance difficulty,MobSpawnType reason,SpawnGroupData group){restrictTo(new BlockPos((int)getX(),(int)getY(),(int)getZ()),24);return group;}
    @Override public void tick(){
        super.tick();if(spawnTimer>0)--spawnTimer;if(getAnger()>0)setAnger(getAnger()-1);
        if(level().isClientSide){if(random.nextInt(15)==0&&getAnger()>0)level().addParticle(ParticleTypes.ANGRY_VILLAGER,getX()+random.nextFloat()*getBbWidth()-getBbWidth()/2,getBoundingBox().minY+getBbHeight()+random.nextFloat()*.5,getZ()+random.nextFloat()*getBbWidth()-getBbWidth()/2,random.nextGaussian()*.02,random.nextGaussian()*.02,random.nextGaussian()*.02);return;}
        if(tickCount%30==0)heal(1);
        if(getTarget()!=null&&!getTarget().isAlive())setTarget(null);
        if(getTarget()!=null&&tickCount%20==0)updateAggro();
        bossEvent.setName(getDisplayName());bossEvent.setProgress(getHealth()/getMaxHealth());
        if(tickCount%2==0&&!bossEvent.getPlayers().isEmpty())for(var player:bossEvent.getPlayers())sendStatus(player,false);
    }
    /** Choice2 A: retain TC4 thresholds, but resolve each candidate and compare actual aggro scores. */
    private void updateAggro(){
        var current=getTarget();int ad=aggro.getOrDefault(current.getId(),0),best=ad,players=0;LivingEntity next=current;var expired=new ArrayList<Integer>();
        for(var entry:aggro.entrySet()){
            var entity=level().getEntity(entry.getKey());
            if(!(entity instanceof LivingEntity living)||!living.isAlive()||distanceToSqr(living)>16384){expired.add(entry.getKey());continue;}
            if(living instanceof Player)players++;
            int score=entry.getValue();
            if(score>ad+25&&score>ad*1.1&&(score>best||score==best&&living.getId()<next.getId())){next=living;best=score;}
        }
        expired.forEach(aggro::remove);if(next!=current)setTarget(next);updateParticipantModifiers(players);
    }
    protected void updateParticipantModifiers(int players){
        float old=getMaxHealth();var health=getAttribute(Attributes.MAX_HEALTH);var damage=getAttribute(Attributes.ATTACK_DAMAGE);
        for(int i=0;i<5;i++){health.removeModifier(HP[i]);damage.removeModifier(DAMAGE[i]);}
        for(int i=0;i<Math.min(5,players-1);i++){health.addTransientModifier(new AttributeModifier(HP[i],50,AttributeModifier.Operation.ADD_VALUE));damage.addTransientModifier(new AttributeModifier(DAMAGE[i],.5,AttributeModifier.Operation.ADD_VALUE));}
        setHealth((float)(getHealth()*(double)getMaxHealth()/old));
    }
    @Override public boolean hurt(DamageSource source,float amount){
        if(!level().isClientSide){if(source.getEntity() instanceof LivingEntity attacker)aggro.merge(attacker.getId(),(int)amount,Integer::sum);
            if(amount>35){if(getAnger()==0){addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE,200,(int)(amount/15)));addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST,200,(int)(amount/40)));addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED,200,(int)(amount/40)));setAnger(200);if(source.getEntity() instanceof Player player)player.sendSystemMessage(getDisplayName().copy().append(" ").append(Component.translatable("tc.boss.enrage")));}amount=35;}
        }return super.hurt(source,amount);
    }
    private void sendStatus(ServerPlayer player,boolean removed){float maximum=this instanceof EldritchWarden?(float)(getAttribute(Attributes.MAX_HEALTH).getBaseValue()*.66):0;if(ChampionSystem.modifier(this)==5)maximum+=(float)(getAttribute(Attributes.MAX_HEALTH).getBaseValue()/2);maximum=Math.max(maximum,getAbsorptionAmount());net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(player,new thaumcraft.network.BossStatusPayload(bossEvent.getId(),getHealth(),getMaxHealth(),getAbsorptionAmount(),maximum,getAnger(),removed));}
    @Override public void startSeenByPlayer(ServerPlayer player){super.startSeenByPlayer(player);bossEvent.addPlayer(player);sendStatus(player,false);}
    @Override public void stopSeenByPlayer(ServerPlayer player){super.stopSeenByPlayer(player);bossEvent.removePlayer(player);sendStatus(player,true);}
    @Override public void addAdditionalSaveData(CompoundTag tag){super.addAdditionalSaveData(tag);if(hasRestriction()){tag.putInt("HomeD",(int)getRestrictRadius());tag.putInt("HomeX",getRestrictCenter().getX());tag.putInt("HomeY",getRestrictCenter().getY());tag.putInt("HomeZ",getRestrictCenter().getZ());}}
    @Override public void readAdditionalSaveData(CompoundTag tag){super.readAdditionalSaveData(tag);if(tag.contains("HomeD"))restrictTo(new BlockPos(tag.getInt("HomeX"),tag.getInt("HomeY"),tag.getInt("HomeZ")),tag.getInt("HomeD"));}
    @Override protected void dropCustomDeathLoot(ServerLevel world,DamageSource source,boolean recentlyHit){
        var pearl=R5Entities.SPECIAL_ITEM.get().create(world);pearl.setItem(new ItemStack(R3Equipment.PRIMAL_PEARL.get()));pearl.setPos(getX(),getY()+getBbHeight()/2f,getZ());pearl.setPickUpDelay(10);pearl.setDeltaMovement(0,(double).1f,0);world.addFreshEntity(pearl);spawnAtLocation(new ItemStack(R5Content.LOOT_BAGS.get(2).get()),1.5f);
    }
}
