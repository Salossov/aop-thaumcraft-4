package thaumcraft.entities;

import java.util.Comparator;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import thaumcraft.registry.*;
import thaumcraft.world.WorldBiomes;

public class Taintacle extends Monster implements TaintedMob {
    private static final EntityDataAccessor<Boolean> AGITATED=SynchedEntityData.defineId(Taintacle.class,EntityDataSerializers.BOOLEAN);
    private int attackDelay,lifetime=200;
    public float flailIntensity=1;
    public Taintacle(EntityType<? extends Taintacle> type,Level level) {super(type,level);xpReward=small()?0:10;}
    public boolean small() {return getType()==R2Entities.TAINTACLE_SMALL.get();}
    public static AttributeSupplier.Builder attributes(boolean small) {return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH,small?8:50).add(Attributes.ATTACK_DAMAGE,small?2:7);}
    @Override protected void registerGoals() {}
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder){super.defineSynchedData(builder);builder.define(AGITATED,false);}
    public boolean agitated(){return entityData.get(AGITATED);}
    @Override public void move(MoverType type,Vec3 motion) {super.move(type,new Vec3(0,Math.min(0,motion.y),0));}
    @Override public float getPickRadius() {return 0.25f;}
    @Override public boolean isPushable() {return true;}
    @Override public boolean canBeCollidedWith() {return true;}
    @Override public void tick() {
        super.tick();
        if(level().isClientSide) {boolean agitated=tickCount>getBbHeight()*10&&(hurtTime>0||attackDelay>0||agitated());flailIntensity=Math.max(1,Math.min(3,flailIntensity+(agitated?0.2f:-0.2f)));if(attackDelay>0)--attackDelay;return;}
        if(small()&&lifetime--<=0) hurt(damageSources().starve(),10);
        if(tickCount%20==0&&!WorldBiomes.effective(level(),blockPosition()).is(WorldBiomes.TAINT)) hurt(damageSources().fellOutOfWorld(),1);
    }
    @Override protected void customServerAiStep() {
        super.customServerAiStep();if(attackDelay>0)--attackDelay;
        var target=getTarget();double height=getBbHeight();
        entityData.set(AGITATED,target!=null&&target.isAlive()&&distanceTo(target)<height);
        if(target==null) {
            target=level().getEntitiesOfClass(LivingEntity.class,getBoundingBox().inflate(height*6,height*3,height*6),e->e!=this&&e.isAlive()&&!(e instanceof TaintedMob)&&!(e instanceof Player p&&(p.isCreative()||p.isSpectator())))
                .stream().min(Comparator.comparingDouble(this::distanceToSqr)).orElse(null);setTarget(target);
        } else if(!target.isAlive()||distanceToSqr(target)>=height*7*height*7) {setTarget(null);return;}
        if(target==null)return;getLookControl().setLookAt(target,5,0);
        if(attackDelay==0&&hasLineOfSight(target)) {
            if(distanceTo(target)<=height&&target.getBoundingBox().maxY>getBoundingBox().minY&&target.getBoundingBox().minY<getBoundingBox().maxY) {
                attackDelay=20;doHurtTarget(target);playSound(R2Sounds.TENTACLE.get(),getSoundVolume(),getVoicePitch());level().broadcastEntityEvent(this,(byte)4);
            } else if(target.onGround()&&!small()) spawnTentacle(target);
        }
    }
    public boolean spawnTentacle(Entity target) {
        if(level().isClientSide||small())return false;var pos=target.blockPosition();
        boolean taint=WorldBiomes.effective(level(),pos).is(WorldBiomes.TAINT)&&(taintBlock(pos)||taintBlock(pos.below()));
        boolean eldritch=WorldBiomes.effective(level(),pos).unwrapKey().map(k->k.location().toString().equals("thaumcraft:eldritch")).orElse(false);
        if(!taint&&!eldritch||!level().hasChunkAt(pos))return false;
        attackDelay=40+random.nextInt(20);var tiny=R2Entities.TAINTACLE_SMALL.get().create(level());if(tiny==null)return false;
        tiny.moveTo(target.getX()+random.nextFloat()-random.nextFloat(),target.getY(),target.getZ()+random.nextFloat()-random.nextFloat(),0,0);level().addFreshEntity(tiny);playSound(R2Sounds.TENTACLE.get(),getSoundVolume(),getVoicePitch());return true;
    }
    private boolean taintBlock(net.minecraft.core.BlockPos pos) {var block=level().getBlockState(pos).getBlock();return block instanceof thaumcraft.blocks.TaintFibresBlock||block instanceof thaumcraft.blocks.TaintSolidBlock;}
    @Override public boolean doHurtTarget(Entity target) {
        float damage=(float)getAttributeValue(Attributes.ATTACK_DAMAGE);var strength=getEffect(MobEffects.DAMAGE_BOOST);var weakness=getEffect(MobEffects.WEAKNESS);
        if(strength!=null)damage+=3<<strength.getAmplifier();if(weakness!=null)damage-=2<<weakness.getAmplifier();
        var key=net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.DAMAGE_TYPE,net.minecraft.resources.ResourceLocation.parse("thaumcraft:tentacle"));
        return target.hurt(new DamageSource(level().registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.DAMAGE_TYPE).getHolderOrThrow(key),this),Math.max(0,damage));
    }
    @Override public boolean hurt(DamageSource source,float damage) {if(!level().isClientSide&&!small()&&source.getEntity()!=null&&distanceTo(source.getEntity())>16)spawnTentacle(source.getEntity());return super.hurt(source,damage);}
    @Override public void handleEntityEvent(byte event){if(event==4)attackDelay=20;else super.handleEntityEvent(event);}
    @Override protected SoundEvent getAmbientSound() {return R2Sounds.ROOTS.get();}
    @Override protected SoundEvent getHurtSound(DamageSource source) {return R2Sounds.TENTACLE.get();}
    @Override protected SoundEvent getDeathSound() {return R2Sounds.TENTACLE.get();}
    @Override protected float getSoundVolume() {return getBbHeight()/8;}
    @Override public float getVoicePitch() {return 1.3f-getBbHeight()/10;}
    @Override public int getAmbientSoundInterval() {return 200;}
    @Override protected void dropCustomDeathLoot(ServerLevel level,DamageSource source,boolean recentlyHit) {if(!small())spawnAtLocation(random.nextBoolean()?R2Content.TAINT_TENDRIL.get():R2Content.TAINT_GOO.get());}
    @Override public void addAdditionalSaveData(CompoundTag tag) {super.addAdditionalSaveData(tag);if(small())tag.putInt("Lifetime",lifetime);}
    @Override public void readAdditionalSaveData(CompoundTag tag) {super.readAdditionalSaveData(tag);if(tag.contains("Lifetime"))lifetime=tag.getInt("Lifetime");}
}
