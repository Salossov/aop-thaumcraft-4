package thaumcraft.entities;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.items.*;

public final class AspectOrb extends Entity {
    private static final EntityDataAccessor<String> ASPECT=SynchedEntityData.defineId(AspectOrb.class,EntityDataSerializers.STRING);
    private int age,amount=1,health=5;
    private Player closest;
    public AspectOrb(EntityType<? extends AspectOrb> type,Level level) { super(type,level); }
    public AspectOrb(Level level,Vec3 pos,Aspect aspect,int amount) { this(R2Entities.ASPECT_ORB.get(),level);setPos(pos);entityData.set(ASPECT,aspect.getTag());this.amount=amount;setDeltaMovement((random.nextDouble()*0.2-0.1)*2,random.nextDouble()*0.2*2,(random.nextDouble()*0.2-0.1)*2); }
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) { builder.define(ASPECT,"aer"); }
    public Aspect aspect() { return Aspect.getAspect(entityData.get(ASPECT)); }
    public int age(){return age;}
    private int wandSlot(Player player) {
        if(aspect()==null||!aspect().isPrimal()) return -1;for(int i=0;i<9;i++) {var stack=player.getInventory().getItem(i);if(stack.getItem() instanceof CastingWandItem&&WandVis.get(stack,aspect())+amount*100<=WandVis.CAPACITY) return i;}return -1;
    }
    @Override public void tick() {
        super.tick();var motion=getDeltaMovement().add(0,-0.03,0);
        if(tickCount%5==0&&closest==null) closest=level().getEntitiesOfClass(Player.class,getBoundingBox().inflate(8),p->wandSlot(p)>=0).stream().min(java.util.Comparator.comparingDouble(this::distanceToSqr)).orElse(null);
        if(closest!=null&&closest.isAlive()) { var direction=closest.getEyePosition().subtract(position()).scale(1.0/8);double length=direction.length(),factor=1-length;if(factor>0&&length>1e-8) motion=motion.add(direction.scale(factor*factor*0.1/length)); }
        setDeltaMovement(motion);move(MoverType.SELF,motion);var floor=getBlockPosBelowThatAffectsMyMovement();float friction=onGround()?level().getBlockState(floor).getFriction(level(),floor,this)*0.98f:0.98f;
        motion=getDeltaMovement().multiply(friction,0.98,friction);if(onGround()) motion=motion.multiply(1,-0.9,1);setDeltaMovement(motion);
        if(++age>=150) discard();
    }
    @Override public void playerTouch(Player player) {
        if(level().isClientSide||player.takeXpDelay>0) return;int slot=wandSlot(player);if(slot>=0) {WandVis.add(player.getInventory().getItem(slot),aspect(),amount);player.takeXpDelay=2;player.getInventory().setChanged();discard();}
    }
    @Override public boolean hurt(net.minecraft.world.damagesource.DamageSource source,float damage) { health=(int)(health-damage);if(health<=0) discard();return false; }
    @Override protected void addAdditionalSaveData(CompoundTag tag) { tag.putString("Aspect",entityData.get(ASPECT));tag.putInt("Value",amount);tag.putInt("Age",age);tag.putInt("Health",health); }
    @Override protected void readAdditionalSaveData(CompoundTag tag) { entityData.set(ASPECT,tag.getString("Aspect"));amount=tag.getInt("Value");age=tag.getInt("Age");health=tag.getInt("Health"); }
}
