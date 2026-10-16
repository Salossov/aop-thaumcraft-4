package thaumcraft.entities;

import net.minecraft.network.syncher.*;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import net.minecraft.util.Mth;
import thaumcraft.registry.R2Sounds;

/** Original homing orb: inverse squared-distance acceleration, float-clamped speed, no gravity. */
public final class GolemOrb extends ThrowableProjectile {
    private static final EntityDataAccessor<Integer> TARGET=SynchedEntityData.defineId(GolemOrb.class,EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> RED=SynchedEntityData.defineId(GolemOrb.class,EntityDataSerializers.BOOLEAN);
    public static java.util.function.Consumer<GolemOrb> clientImpact=orb->{};
    private boolean burstPlayed;
    public GolemOrb(EntityType<? extends GolemOrb> type,Level level){super(type,level);}
    public GolemOrb(Level level,LivingEntity owner,LivingEntity target,boolean red){super(R5Entities.GOLEM_ORB.get(),level);LegacyThrowableLaunch.owner(this,owner,1.5f,0);entityData.set(TARGET,target==null?-1:target.getId());entityData.set(RED,red);}
    @Override public void shoot(double x,double y,double z,float speed,float scatter){LegacyThrowableLaunch.shoot(this,random,x,y,z,speed,scatter);}
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder){builder.define(TARGET,-1);builder.define(RED,false);}
    public boolean red(){return entityData.get(RED);}
    public LivingEntity homingTarget(){var entity=level().getEntity(entityData.get(TARGET));return entity instanceof LivingEntity living?living:null;}
    @Override protected double getDefaultGravity(){return 0;}
    @Override public float getPickRadius(){return .1f;}
    @Override public void tick(){super.tick();if(tickCount>(red()?240:160)){discard();return;}if(isRemoved())return;var target=homingTarget();if(target!=null){double distance=distanceToSqr(target);if(distance==0)return;var velocity=getDeltaMovement();double dx=(target.getX()-getX())/distance*.2,dy=(target.getBoundingBox().minY+target.getBbHeight()*.6-getY())/distance*.2,dz=(target.getZ()-getZ())/distance*.2;setDeltaMovement(Mth.clamp((float)(velocity.x+dx),-.25f,.25f),Mth.clamp((float)(velocity.y+dy),-.25f,.25f),Mth.clamp((float)(velocity.z+dz),-.25f,.25f));}}
    @Override protected void onHit(HitResult hit){
        if(!level().isClientSide){if(hit instanceof EntityHitResult entity&&getOwner() instanceof LivingEntity owner)entity.getEntity().hurt(damageSources().indirectMagic(this,owner),(float)owner.getAttributeValue(Attributes.ATTACK_DAMAGE)*(red()?1f:.6f));playSound(R2Sounds.SHOCK.get(),1,1+(random.nextFloat()-random.nextFloat())*.2f);level().broadcastEntityEvent(this,(byte)16);discard();}
        else{impact();discard();}
    }
    private void impact(){if(!burstPlayed){burstPlayed=true;clientImpact.accept(this);}}
    @Override public void handleEntityEvent(byte event){if(event==16)impact();else super.handleEntityEvent(event);}
    @Override public boolean hurt(DamageSource source,float damage){if(isInvulnerableTo(source))return false;markHurt();if(source.getEntity()!=null){setDeltaMovement(source.getEntity().getLookAngle().scale(.9));playSound(R2Sounds.ZAP.get(),1,1+(random.nextFloat()-random.nextFloat())*.2f);return true;}return false;}
}
