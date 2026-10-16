package thaumcraft.entities;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.*;
import net.minecraft.sounds.*;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.ai.goal.target.*;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.phys.AABB;
import thaumcraft.registry.*;

/** The original armored host: no vanilla death drops or villager conversion. */
public final class InhabitedZombie extends Zombie {
    private static final TagKey<EntityType<?>> CULTISTS=TagKey.create(Registries.ENTITY_TYPE,ResourceLocation.parse("thaumcraft:cultists"));
    public InhabitedZombie(EntityType<? extends InhabitedZombie> type,Level level){super(type,level);}
    public static AttributeSupplier.Builder attributes(){return Zombie.createAttributes().add(Attributes.MAX_HEALTH,30).add(Attributes.ATTACK_DAMAGE,5).add(Attributes.SPAWN_REINFORCEMENTS_CHANCE,0);}
    @Override protected void registerGoals(){super.registerGoals();targetSelector.addGoal(1,new HurtByTargetGoal(this).setAlertOthers());targetSelector.addGoal(3,new NearestAttackableTargetGoal<>(this,LivingEntity.class,0,true,false,e->e.getType().is(CULTISTS)));}
    @Override public SpawnGroupData finalizeSpawn(ServerLevelAccessor level,DifficultyInstance difficulty,MobSpawnType reason,SpawnGroupData data){float chance=level.getDifficulty()==Difficulty.HARD?.9f:.6f;setItemSlot(EquipmentSlot.HEAD,armor("helmet"));if(random.nextFloat()<=chance)setItemSlot(EquipmentSlot.CHEST,armor("chestplate"));if(random.nextFloat()<=chance)setItemSlot(EquipmentSlot.LEGS,armor("leggings"));return data;}
    private static ItemStack armor(String slot){return new ItemStack(R5Content.CULTIST_ARMOR.get("cultist_plate_"+slot).get());}
    @Override public boolean killedEntity(ServerLevel level,LivingEntity victim){return false;}
    @Override public void die(DamageSource source){} // TC4 defers the entire transition to its first death tick.
    @Override protected SoundEvent getDeathSound(){return null;}
    @Override protected SoundEvent getAmbientSound(){return R2Sounds.CRAB_IDLE.get();}
    @Override protected SoundEvent getHurtSound(DamageSource source){return SoundEvents.GENERIC_HURT;}
    @Override protected void tickDeath(){
        if(level() instanceof ServerLevel server){var crab=R5Entities.CRAB.get().create(server);if(crab!=null){crab.moveTo(getX(),getY()+getEyeHeight(),getZ(),getYRot(),getXRot());crab.setHelm(true);server.addFreshEntity(crab);}dropExperience(lastHurtByPlayer);}
        for(int i=0;i<20;i++){double vx=random.nextGaussian()*.02,vy=random.nextGaussian()*.02,vz=random.nextGaussian()*.02;level().addParticle(ParticleTypes.POOF,getX()+random.nextFloat()*getBbWidth()*2-getBbWidth(),getY()+random.nextFloat()*getBbHeight(),getZ()+random.nextFloat()*getBbWidth()*2-getBbWidth(),vx,vy,vz);}
        discard();
    }
    @Override public boolean checkSpawnRules(LevelAccessor level,MobSpawnType reason){return level.getEntitiesOfClass(InhabitedZombie.class,new AABB(getX(),getY(),getZ(),getX()+1,getY()+1,getZ()+1).inflate(32,16,32)).isEmpty()&&super.checkSpawnRules(level,reason);}
}
