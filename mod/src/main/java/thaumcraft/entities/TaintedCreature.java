package thaumcraft.entities;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.*;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.*;
import net.minecraft.world.level.Level;
import thaumcraft.registry.R2Content;

/** Original TC4 tainted animals are hostile mobs, including the villager. */
public final class TaintedCreature extends Monster implements TaintedMob {
    public enum Kind { PIG,COW,CHICKEN,SHEEP,VILLAGER }
    public TaintedCreature(EntityType<? extends TaintedCreature> type,Level level) { super(type,level); }
    public Kind kind() {
        if(getType()==R2Entities.TAINT_COW.get()) return Kind.COW;if(getType()==R2Entities.TAINT_CHICKEN.get()) return Kind.CHICKEN;
        if(getType()==R2Entities.TAINT_SHEEP.get()) return Kind.SHEEP;if(getType()==R2Entities.TAINT_VILLAGER.get()) return Kind.VILLAGER;return Kind.PIG;
    }
    public static AttributeSupplier.Builder attributes(Kind kind) {
        double health=switch(kind) {case PIG,SHEEP->20;case COW->40;case CHICKEN->8;case VILLAGER->30;};
        double attack=switch(kind) {case PIG,VILLAGER->4;case COW->6;default->3;};
        double speed=switch(kind) {case PIG->0.275;case COW->0.27;case CHICKEN->0.4;case SHEEP->0.25;case VILLAGER->0.3;};
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH,health).add(Attributes.ATTACK_DAMAGE,attack).add(Attributes.MOVEMENT_SPEED,speed);
    }
    @Override protected void registerGoals() {
        goalSelector.addGoal(0,new FloatGoal(this));goalSelector.addGoal(2,new MeleeAttackGoal(this,1,false));
        goalSelector.addGoal(5,new WaterAvoidingRandomStrollGoal(this,1));goalSelector.addGoal(6,new LookAtPlayerGoal(this,Player.class,8));goalSelector.addGoal(6,new RandomLookAroundGoal(this));
        targetSelector.addGoal(0,new HurtByTargetGoal(this));targetSelector.addGoal(2,new NearestAttackableTargetGoal<>(this,Player.class,true));
        if(kind()==Kind.SHEEP) goalSelector.addGoal(4,new EatBlockGoal(this));
        else if(kind()==Kind.VILLAGER) {
            if(getNavigation() instanceof GroundPathNavigation navigation) {navigation.setCanOpenDoors(true);navigation.setCanPassDoors(true);}
            goalSelector.addGoal(4,new OpenDoorGoal(this,true));goalSelector.addGoal(6,new MoveThroughVillageGoal(this,1,false,32,()->true));
        } else {
            targetSelector.addGoal(3,new NearestAttackableTargetGoal<>(this,Villager.class,true));
            targetSelector.addGoal(4,new NearestAttackableTargetGoal<>(this,Animal.class,true));
            if(kind()==Kind.CHICKEN) goalSelector.addGoal(3,new LeapAtTargetGoal(this,0.3f));
        }
    }
    @Override public int getArmorValue() { return Math.min(20,super.getArmorValue()+switch(kind()) {case PIG,CHICKEN->2;case SHEEP->1;default->0;}); }
    @Override public void aiStep() {super.aiStep();if(kind()==Kind.CHICKEN&&!onGround()&&getDeltaMovement().y<0) setDeltaMovement(getDeltaMovement().multiply(1,0.9,1));}
    @Override public boolean causeFallDamage(float distance,float multiplier,DamageSource source) {return kind()!=Kind.CHICKEN&&super.causeFallDamage(distance,multiplier,source);}
    @Override protected void dropCustomDeathLoot(ServerLevel level,DamageSource source,boolean recentlyHit) {
        boolean drops=switch(kind()) {case COW,CHICKEN->true;case PIG,SHEEP->random.nextInt(3)==0;case VILLAGER->random.nextBoolean();};
        if(drops) spawnAtLocation((kind()==Kind.CHICKEN?random.nextInt(4)==0:random.nextBoolean())?R2Content.TAINT_TENDRIL.get():R2Content.TAINT_GOO.get());
        if(kind()==Kind.VILLAGER) {
            int looting=source.getEntity() instanceof LivingEntity attacker?EnchantmentHelper.getEnchantmentLevel(level.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT).getOrThrow(Enchantments.LOOTING),attacker):0;
            if(random.nextInt(13)<1+looting) spawnAtLocation(R2Content.COIN.get());
        }
    }
}
