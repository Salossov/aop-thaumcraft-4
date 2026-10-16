package thaumcraft.entities;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.*;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import thaumcraft.registry.R2Content;

public final class BrainyZombie extends Zombie {
    private static final EntityDataAccessor<Float> ANGER=SynchedEntityData.defineId(BrainyZombie.class,EntityDataSerializers.FLOAT);
    private final boolean giant;
    public BrainyZombie(EntityType<? extends BrainyZombie> type,Level level) { super(type,level);giant=type==R2Entities.GIANT_BRAINY.get();xpReward=giant?15:5;if(giant) goalSelector.addGoal(2,new LeapAtTargetGoal(this,0.4f)); }
    public static AttributeSupplier.Builder attributes(boolean giant) { return Zombie.createAttributes().add(Attributes.MAX_HEALTH,giant?60:25).add(Attributes.ATTACK_DAMAGE,giant?7:5).add(Attributes.SPAWN_REINFORCEMENTS_CHANCE,0); }
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) { super.defineSynchedData(builder);builder.define(ANGER,1f); }
    public float anger() { return entityData.get(ANGER); }
    @Override public int getArmorValue() { return Math.min(20,super.getArmorValue()+3); }
    @Override public EntityDimensions getDefaultDimensions(Pose pose) { return giant?EntityDimensions.scalable(0.6f*(1.2f+anger()),1.8f*(1.2f+anger())):super.getDefaultDimensions(pose); }
    @Override public boolean hurt(DamageSource source,float amount) { if(giant) entityData.set(ANGER,Math.min(2f,anger()+0.1f));return super.hurt(source,amount); }
    @Override public void aiStep() {
        super.aiStep();if(giant) { if(!level().isClientSide&&anger()>1) entityData.set(ANGER,anger()-0.002f);refreshDimensions();if(!level().isClientSide) getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(7+(anger()-1)*5); }
    }
    @Override protected void dropCustomDeathLoot(net.minecraft.server.level.ServerLevel level,DamageSource source,boolean recentlyHit) {
        int looting=net.minecraft.world.item.enchantment.EnchantmentHelper.getEnchantmentLevel(level.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT).getOrThrow(net.minecraft.world.item.enchantment.Enchantments.LOOTING),source.getEntity() instanceof net.minecraft.world.entity.LivingEntity living?living: this);
        for(int i=0;i<(giant?12:3);i++) if(random.nextBoolean()) spawnAtLocation(Items.ROTTEN_FLESH,giant?2:1);
        if(random.nextInt(10)-looting<=4) spawnAtLocation(R2Content.ZOMBIE_BRAIN.get());
    }
    @Override public void addAdditionalSaveData(CompoundTag tag) { super.addAdditionalSaveData(tag);tag.putFloat("ThaumcraftAnger",anger()); }
    @Override public void readAdditionalSaveData(CompoundTag tag) { super.readAdditionalSaveData(tag);entityData.set(ANGER,Math.max(1,tag.getFloat("ThaumcraftAnger"))); }
}
