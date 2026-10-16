package thaumcraft.entities;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.*;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import thaumcraft.registry.R2Content;

public final class TaintSpider extends Spider implements TaintedMob {
    public TaintSpider(EntityType<? extends TaintSpider> type,Level level) { super(type,level);xpReward=2; }
    public static AttributeSupplier.Builder attributes() { return Spider.createAttributes().add(Attributes.MAX_HEALTH,5).add(Attributes.ATTACK_DAMAGE,2).add(Attributes.FOLLOW_RANGE,12); }
    @Override protected void registerGoals() {
        goalSelector.addGoal(1,new FloatGoal(this));goalSelector.addGoal(3,new LeapAtTargetGoal(this,0.4f));
        goalSelector.addGoal(4,new MeleeAttackGoal(this,1.0,true));goalSelector.addGoal(5,new WaterAvoidingRandomStrollGoal(this,0.8));
        targetSelector.addGoal(1,new HurtByTargetGoal(this));targetSelector.addGoal(2,new NearestAttackableTargetGoal<>(this,Player.class,true));
    }
    @Override protected void dropCustomDeathLoot(ServerLevel level,DamageSource source,boolean recentlyHit) {
        if(random.nextInt(6)==0) spawnAtLocation(random.nextBoolean()?R2Content.TAINT_TENDRIL.get():R2Content.TAINT_GOO.get());
    }
}
