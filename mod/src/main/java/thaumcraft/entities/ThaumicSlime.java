package thaumcraft.entities;

import java.util.Comparator;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import thaumcraft.registry.R2Content;

/** TC4 slime mass (health), merging, splitting and ranged loss of one mass unit. */
public final class ThaumicSlime extends Monster implements TaintedMob {
    private static final EntityDataAccessor<Integer> SIZE=SynchedEntityData.defineId(ThaumicSlime.class,EntityDataSerializers.INT);
    private int jumpDelay=10,spitCounter=100,launched=10;
    public float oldSquish,squish;private float squishTarget;private boolean previouslyGrounded;
    private boolean splitDone;
    public ThaumicSlime(EntityType<? extends ThaumicSlime> type,Level level) { super(type,level);jumpDelay=random.nextInt(20)+10;size(1<<random.nextInt(3)); }
    public static AttributeSupplier.Builder attributes() { return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH,4).add(Attributes.ATTACK_DAMAGE,1).add(Attributes.MOVEMENT_SPEED,0.1); }
    @Override protected void registerGoals() {}
    @Override public void tick(){oldSquish=squish;squish+=(squishTarget-squish)*0.5f;super.tick();if(onGround()&&!previouslyGrounded)squishTarget=-0.5f;else if(!onGround()&&previouslyGrounded)squishTarget=1;previouslyGrounded=onGround();squishTarget*=0.6f;}
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) { super.defineSynchedData(builder);builder.define(SIZE,1); }
    public int size() { return entityData.get(SIZE); }
    public void size(int value) { entityData.set(SIZE,Math.clamp(value,1,100));getAttribute(Attributes.MAX_HEALTH).setBaseValue(size());setHealth(getMaxHealth());xpReward=(int)Math.sqrt(size());refreshDimensions(); }
    @Override public EntityDimensions getDefaultDimensions(Pose pose) { float side=0.25f*(float)Math.sqrt(size())+0.25f;return EntityDimensions.scalable(side,side); }
    @Override public void onSyncedDataUpdated(EntityDataAccessor<?> key) { super.onSyncedDataUpdated(key);if(SIZE.equals(key)) refreshDimensions(); }
    @Override protected void customServerAiStep() {
        super.customServerAiStep();if(launched>0) launched--;var player=level().getNearestPlayer(this,16);
        if(player!=null&&(player.isCreative()||player.isSpectator())) player=null;
        if(player!=null) {
            getLookControl().setLookAt(player,10,20);setYRot((float)(Math.atan2(player.getZ()-getZ(),player.getX()-getX())*180/Math.PI)-90);
            if(spitCounter>0) spitCounter--;if(distanceTo(player)>4&&spitCounter<=0&&size()>3) {
                spitCounter=101;var projectile=R2Entities.THAUMIC_SLIME.get().create(level());projectile.size(1);
                double dx=player.getX()-getX(),dz=player.getZ()-getZ(),horizontal=Math.sqrt(dx*dx+dz*dz),y=getY()+getBbHeight()/2;
                projectile.moveTo(getX()+dx/horizontal,y,getZ()+dz/horizontal,0,0);
                var aim=new Vec3(dx,player.getY()+player.getBbHeight()/3-y+horizontal*0.2,dz).normalize();
                projectile.setDeltaMovement(aim.add(random.nextGaussian()*0.0075,random.nextGaussian()*0.0075,random.nextGaussian()*0.0075).scale(1.5));level().addFreshEntity(projectile);size(size()-1);
            }
        } else {
            var other=level().getEntitiesOfClass(ThaumicSlime.class,getBoundingBox().inflate(16,8,16),s->s!=this&&s.isAlive()&&s.tickCount>100&&s.size()<100).stream().min(Comparator.comparingDouble(this::distanceToSqr)).orElse(null);
            if(other!=null) {getLookControl().setLookAt(other,10,20);setYRot((float)(Math.atan2(other.getZ()-getZ(),other.getX()-getX())*180/Math.PI)-90);if(distanceTo(other)<getBbWidth()+other.getBbWidth()) {other.size(Math.min(100,other.size()+size()));discard();return;} }
        }
        if(onGround()&&jumpDelay--<=0) {jumpDelay=random.nextInt(16)+8;if(player!=null) jumpDelay/=3;getJumpControl().jump();zza=(float)Math.sqrt(size());xxa=1-random.nextFloat()*2;}
        else if(onGround()) {zza=0;xxa=0;}
    }
    @Override public void playerTouch(Player player) {
        int radius=(int)Math.max(1,Math.sqrt(size()));if(launched>0&&radius==2) radius=3;
        if(!level().isClientSide&&isAlive()&&hasLineOfSight(player)&&distanceToSqr(player)<0.64*radius*radius) player.hurt(damageSources().mobAttack(this),size());
    }
    @Override public void die(DamageSource source) {
        super.die(source);
        if(!level().isClientSide&&dead&&!splitDone) {splitDone=true;int count=(int)Math.sqrt(size());if(count>1) for(int i=0;i<count;i++) {
            var small=R2Entities.THAUMIC_SLIME.get().create(level());small.size(1);small.moveTo(getX()+((i%2)-0.5)*count/4,getY()+0.5,getZ()+((i/2)-0.5)*count/4,random.nextFloat()*360,0);level().addFreshEntity(small);
        }}
    }
    @Override protected void dropCustomDeathLoot(ServerLevel level,DamageSource source,boolean recentlyHit) { if(size()<3&&random.nextInt(3)==0) spawnAtLocation(R2Content.TAINT_TENDRIL.get()); }
    @Override public void addAdditionalSaveData(CompoundTag tag) {super.addAdditionalSaveData(tag);tag.putInt("Size",size());tag.putInt("SpitCounter",spitCounter);}
    @Override public void readAdditionalSaveData(CompoundTag tag) {super.readAdditionalSaveData(tag);float health=getHealth();size(tag.getInt("Size"));setHealth(Math.min(health,getMaxHealth()));spitCounter=tag.getInt("SpitCounter");}
}
