package thaumcraft.entities;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import thaumcraft.registry.R2Content;
import thaumcraft.world.WorldBiomes;

public final class TaintSwarm extends Monster implements TaintedMob {
    private BlockPos flightTarget;
    private int attackDelay;
    public TaintSwarm(EntityType<? extends TaintSwarm> type,Level level) { super(type,level);setNoGravity(true); }
    public static AttributeSupplier.Builder attributes() { return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH,30).add(Attributes.ATTACK_DAMAGE,2); }
    @Override protected void registerGoals() {}
    @Override public boolean isPushable() { return false; }
    @Override public boolean causeFallDamage(float distance,float multiplier,DamageSource source) { return false; }
    @Override protected void customServerAiStep() {
        super.customServerAiStep();var level=(ServerLevel)level();var player=level.getNearestPlayer(this,12);if(player!=null&&(player.isCreative()||player.isSpectator())) player=null;setTarget(player);
        double x,y,z;boolean pursuing=player!=null;
        if(pursuing) { x=player.getX();y=player.getEyeY();z=player.getZ(); }
        else {
            if(flightTarget!=null&&(!level.isEmptyBlock(flightTarget)||flightTarget.getY()<level.getMinBuildHeight()+1||flightTarget.getY()>level.getHeight(Heightmap.Types.WORLD_SURFACE,flightTarget.getX(),flightTarget.getZ())+8||!WorldBiomes.is(level,flightTarget,WorldBiomes.TAINT))) flightTarget=null;
            if(flightTarget==null||random.nextInt(30)==0||flightTarget.distSqr(blockPosition())<4) flightTarget=blockPosition().offset(random.nextInt(7)-random.nextInt(7),random.nextInt(6)-2,random.nextInt(7)-random.nextInt(7));
            x=flightTarget.getX()+0.5;y=flightTarget.getY()+0.1;z=flightTarget.getZ()+0.5;
        }
        var motion=getDeltaMovement();double rate=pursuing?0.025:0.015;
        setDeltaMovement(motion.add((Math.signum(x-getX())*0.5-motion.x)*rate,(Math.signum(y-getY())*0.7-motion.y)*0.1,(Math.signum(z-getZ())*0.5-motion.z)*rate));
        setYRot((float)(Math.atan2(getDeltaMovement().z,getDeltaMovement().x)*180/Math.PI)-90);
        if(attackDelay>0) attackDelay--;if(player!=null&&attackDelay==0&&distanceTo(player)<3&&player.getBoundingBox().intersects(getBoundingBox())) {
            attackDelay=10+random.nextInt(5);var original=player.getDeltaMovement();
            if(doHurtTarget(player)) player.addEffect(new MobEffectInstance(MobEffects.POISON,100));player.setDeltaMovement(original);player.hurtMarked=false;
        }
    }
    @Override public void tick() { super.tick();setDeltaMovement(getDeltaMovement().multiply(1,0.6,1)); }
    @Override protected void dropCustomDeathLoot(ServerLevel level,DamageSource source,boolean recentlyHit) { if(random.nextBoolean()) spawnAtLocation(R2Content.TAINT_TENDRIL.get()); }
}
