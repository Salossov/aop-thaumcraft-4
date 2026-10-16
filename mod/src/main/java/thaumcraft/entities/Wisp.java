package thaumcraft.entities;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.*;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.items.WispEssenceItem;
import thaumcraft.registry.R2Sounds;
import thaumcraft.world.WorldBiomes;

public final class Wisp extends FlyingMob {
    private static final EntityDataAccessor<String> ASPECT=SynchedEntityData.defineId(Wisp.class,EntityDataSerializers.STRING);
    private Vec3 waypoint=Vec3.ZERO;
    private int courseDelay,aggroDelay,attackCounter;
    public Wisp(EntityType<? extends Wisp> type,Level level) { super(type,level);xpReward=5; }
    public static AttributeSupplier.Builder attributes() { return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH,22).add(Attributes.ATTACK_DAMAGE,3); }
    @Override protected void registerGoals() {}
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) { super.defineSynchedData(builder);builder.define(ASPECT,""); }
    public Aspect aspect() { return Aspect.getAspect(entityData.get(ASPECT)); }
    public void setAspect(Aspect aspect) { entityData.set(ASPECT,aspect==null?"":aspect.getTag()); }
    public void chooseAspect() {
        if(aspect()!=null) return;
        java.util.List<Aspect> options;
        if(WorldBiomes.effective(level(),blockPosition()).is(WorldBiomes.EERIE)) options=java.util.stream.Stream.of("tenebrae","exanimis","perditio","alienis","venenum","mortuus").map(Aspect::getAspect).toList();
        else {boolean primal=random.nextInt(10)!=0;options=Aspect.all().values().stream().filter(a->a.isPrimal()==primal).toList();}
        setAspect(options.get(random.nextInt(options.size())));
    }
    @Override public boolean hurt(DamageSource source,float damage) {
        boolean accepted=super.hurt(source,damage);
        if(accepted&&source.getEntity() instanceof LivingEntity attacker) { setTarget(attacker);aggroDelay=200; }
        return accepted;
    }
    @Override protected void customServerAiStep() {
        super.customServerAiStep();if(level().getDifficulty()==Difficulty.PEACEFUL) {discard();return;}chooseAspect();
        var delta=waypoint.subtract(position());double length=delta.length();
        if(length<1||length>60) waypoint=position().add((random.nextFloat()*2-1)*16,(random.nextFloat()*2-1)*16,(random.nextFloat()*2-1)*16);
        if(courseDelay--<=0) {
            courseDelay+=random.nextInt(5)+2;delta=waypoint.subtract(position());length=delta.length();
            if(length>1e-8&&traversable(delta,length)) setDeltaMovement(getDeltaMovement().add(delta.scale(0.1/length)));
            else waypoint=position();
        }
        if(getTarget()!=null&&!getTarget().isAlive()) setTarget(null);--aggroDelay;
        if(random.nextInt(1000)==0&&(getTarget()==null||aggroDelay--<=0)) {
            var player=level().getNearestPlayer(this,16);if(player!=null&&!player.isCreative()&&!player.isSpectator()) {setTarget(player);aggroDelay=50;}
        }
        var target=getTarget();
        if(target!=null&&distanceToSqr(target)<256) {
            setYRot((float)-Math.atan2(target.getX()-getX(),target.getZ()-getZ())*180/(float)Math.PI);yBodyRot=getYRot();
            if(hasLineOfSight(target)) {
                if(++attackCounter==20) {
                    playSound(R2Sounds.ZAP.get(),1,1.1f);level().broadcastEntityEvent(this,(byte)20);
                    var speed=target.getDeltaMovement();boolean moving=Math.abs(speed.x)>0.1||Math.abs(speed.y)>0.1||Math.abs(speed.z)>0.1;
                    if(random.nextFloat()<(moving?0.4f:0.66f)) target.hurt(damageSources().mobAttack(this),(float)getAttributeValue(Attributes.ATTACK_DAMAGE)+(moving?0:1));
                    attackCounter=-20+random.nextInt(20);
                }
            } else if(attackCounter>0) --attackCounter;
        } else { var velocity=getDeltaMovement();setYRot((float)-Math.atan2(velocity.x,velocity.z)*180/(float)Math.PI);yBodyRot=getYRot();if(attackCounter>0)--attackCounter; }
    }
    private boolean traversable(Vec3 delta,double length) {
        var end=BlockPos.containing(waypoint);if(!level().hasChunkAt(end)||level().getBlockState(end).isSolid())return false;
        var box=getBoundingBox();var step=delta.scale(1/length);
        for(int i=1;i<length;i++) {box=box.move(step);if(!level().noCollision(this,box))return false;}
        for(int y=0;y<11;y++) if(!level().isEmptyBlock(end.below(y)))return true;
        return false;
    }
    @Override protected SoundEvent getAmbientSound() {return R2Sounds.WISP_LIVE.get();}
    @Override protected SoundEvent getHurtSound(DamageSource source) {return SoundEvents.FIRE_EXTINGUISH;}
    @Override protected SoundEvent getDeathSound() {return R2Sounds.WISP_DEAD.get();}
    @Override protected float getSoundVolume() {return 0.25f;}
    @Override public int getMaxSpawnClusterSize() {return 2;}
    @Override protected void dropCustomDeathLoot(ServerLevel level,DamageSource source,boolean recentlyHit) {if(aspect()!=null)spawnAtLocation(WispEssenceItem.of(aspect()));}
    @Override public void addAdditionalSaveData(CompoundTag tag) {super.addAdditionalSaveData(tag);tag.putString("Type",entityData.get(ASPECT));}
    @Override public void readAdditionalSaveData(CompoundTag tag) {super.readAdditionalSaveData(tag);setAspect(Aspect.getAspect(tag.getString("Type")));}
}
