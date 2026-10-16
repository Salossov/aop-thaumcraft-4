package thaumcraft.entities;

import net.minecraft.core.registries.Registries;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.*;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.neoforged.neoforge.entity.IEntityWithComplexSpawn;
import net.minecraft.resources.*;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;

/** Original first ordinary-arrow pass, followed by TC4's independent elemental pass. */
public final class PrimalArrow extends AbstractArrow implements IEntityWithComplexSpawn {
    private static final EntityDataAccessor<Integer> ELEMENT=SynchedEntityData.defineId(PrimalArrow.class,EntityDataSerializers.INT);
    private boolean legacyGround;
    private net.minecraft.core.BlockPos legacyBlock=new net.minecraft.core.BlockPos(-1,-1,-1);
    private BlockState legacyState=Blocks.AIR.defaultBlockState();
    private int legacyGroundTicks,legacyAirTicks,firstKnockback,shootingEntityId;
    public PrimalArrow(EntityType<? extends PrimalArrow> type,Level l){super(type,l);pickup=Pickup.DISALLOWED;}
    public PrimalArrow(Level l,LivingEntity owner,ItemStack ammo,int element){super(R3Entities.PRIMAL_ARROW.get(),owner,l,ammo.copyWithCount(1),null);element(element);setBaseDamage(2.1*(element==3?1.5:element>=4?.8:1));pickup=Pickup.DISALLOWED;shootingEntityId=owner.getId();var v=owner.getViewVector(1);float yaw=owner.getYRot()/180f*(float)Math.PI;setPos(owner.getX()-(double)(Mth.cos(yaw)*.16f)+v.x,owner.getEyeY()-.10000000014901161+v.y,owner.getZ()-(double)(Mth.sin(yaw)*.16f)+v.z);}
    @Override protected void defineSynchedData(SynchedEntityData.Builder b){super.defineSynchedData(b);b.define(ELEMENT,0);}
    public int element(){return entityData.get(ELEMENT);}
    public void element(int value){if(value<0||value>5)throw new IllegalArgumentException("Unknown primal arrow type "+value);entityData.set(ELEMENT,value);}
    public void knockback(int n){firstKnockback=n;}
    public double tc4Damage(){return 2.1*(element()==3?1.5:element()>=4?.8:1);}
    public int embeddedTicks(){return legacyGroundTicks;}
    @Override public boolean shouldRenderAtSqrDistance(double distance){double size=getBoundingBox().getSize();if(Double.isNaN(size))size=1;size*=64*10*Entity.getViewScale();return distance<size*size;}
    @Override protected ItemStack getDefaultPickupItem(){return new ItemStack(Items.ARROW);}
    @Override public void playerTouch(Player p){}
    @Override public void shoot(double x,double y,double z,float speed,float scatter){LegacyArrowLaunch.shoot(this,x,y,z,speed,scatter);legacyAirTicks=0;}
    @Override protected double getDefaultGravity(){return (double).05f;}
    @Override protected float getWaterInertia(){return .8f;}
    @Override protected void doKnockback(LivingEntity target,net.minecraft.world.damagesource.DamageSource source){if(firstKnockback>0){var motion=getDeltaMovement();float d=(float)Math.sqrt(motion.x*motion.x+motion.z*motion.z);if(d>0)target.push(motion.x*firstKnockback*(double).6f/d,.1,motion.z*firstKnockback*(double).6f/d);}}
    @Override public void tick(){
        if(legacyGround){var s=level().hasChunkAt(legacyBlock)?level().getBlockState(legacyBlock):legacyState;if(s.equals(legacyState)){if(++legacyGroundTicks==100)discard();}else{legacyGround=false;setDeltaMovement(getDeltaMovement().multiply((double)(random.nextFloat()*.2f),(double)(random.nextFloat()*.2f),(double)(random.nextFloat()*.2f)));legacyGroundTicks=legacyAirTicks=0;}baseTick();return;}
        super.tick();
        if(xRotO==0&&yRotO==0){var motion=getDeltaMovement();float horizontal=(float)Math.sqrt(motion.x*motion.x+motion.z*motion.z);setYRot((float)(Math.atan2(motion.x,motion.z)*180/Math.PI));setXRot((float)(Math.atan2(motion.y,horizontal)*180/Math.PI));yRotO=getYRot();xRotO=getXRot();}
        if(level().hasChunkAt(legacyBlock)){var s=level().getBlockState(legacyBlock);if(!s.isAir()&&s.getCollisionShape(level(),legacyBlock).toAabbs().stream().anyMatch(box->box.move(legacyBlock).contains(position())))legacyGround=true;}
        if(shakeTime>0)shakeTime--;
        legacyAirTicks++;var from=position();var to=from.add(getDeltaMovement());HitResult hit=level().clip(new ClipContext(from,to,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,this));var end=hit.getType()==HitResult.Type.MISS?to:hit.getLocation();Entity target=null;double closest=0;
        for(var e:level().getEntities(this,getBoundingBox().expandTowards(getDeltaMovement()).inflate(1))){if(!e.isPickable()||e.getId()==shootingEntityId&&legacyAirTicks<5)continue;var intercept=e.getBoundingBox().inflate((double).3f).clip(from,end);if(intercept.isEmpty())continue;double distance=from.distanceTo(intercept.get());if(closest==0||distance<closest){target=e;closest=distance;}}
        if(target!=null)hit=new EntityHitResult(target);if(hit instanceof EntityHitResult e&&e.getEntity() instanceof Player player&&(player.getAbilities().invulnerable||getOwner() instanceof Player owner&&!owner.canHarmPlayer(player)))hit=null;
        if(hit instanceof EntityHitResult e){if(inflictDamage(e.getEntity())){if(firstKnockback>0&&e.getEntity() instanceof LivingEntity living){var motion=getDeltaMovement();float d=(float)Math.sqrt(motion.x*motion.x+motion.z*motion.z);if(d>0)living.push(motion.x*firstKnockback*(double).6f/d,.1,motion.z*firstKnockback*(double).6f/d);}playSound(net.minecraft.sounds.SoundEvents.ARROW_HIT,1,1.2f/(random.nextFloat()*.2f+.9f));if(!(e.getEntity() instanceof net.minecraft.world.entity.monster.EnderMan))discard();}else{setDeltaMovement(getDeltaMovement().scale((double)-.1f));setYRot(getYRot()+180);yRotO+=180;legacyAirTicks=0;}}
        else if(hit instanceof BlockHitResult b&&hit.getType()!=HitResult.Type.MISS){legacyBlock=b.getBlockPos();legacyState=level().getBlockState(legacyBlock);var delta=b.getLocation().subtract(position());var motion=new Vec3((float)delta.x,(float)delta.y,(float)delta.z);setDeltaMovement(motion);float length=(float)Math.sqrt(motion.lengthSqr());if(length>0)setPos(position().subtract(motion.scale((double).05f/length)));legacyGround=true;shakeTime=7;setCritArrow(false);if(legacyState.isAir())legacyState.entityInside(level(),legacyBlock,this);playSound(net.minecraft.sounds.SoundEvents.ARROW_HIT,1,1.2f/(random.nextFloat()*.2f+.9f));}
        var motion=getDeltaMovement();if(isCritArrow())for(int i=0;i<4;i++)level().addParticle(ParticleTypes.CRIT,getX()+motion.x*i/4,getY()+motion.y*i/4,getZ()+motion.z*i/4,-motion.x,-motion.y+.2,-motion.z);setPos(position().add(motion));float friction=.99f;if(isInWater()){for(int i=0;i<4;i++)level().addParticle(ParticleTypes.BUBBLE,getX()-motion.x*.25,getY()-motion.y*.25,getZ()-motion.z*.25,motion.x,motion.y,motion.z);friction=.8f;}setDeltaMovement(motion.scale(friction).add(0,-(double).05f,0));float horizontal=(float)Math.sqrt(motion.x*motion.x+motion.z*motion.z);setYRot((float)(Math.atan2(motion.x,motion.z)*180/Math.PI));setXRot((float)(Math.atan2(motion.y,horizontal)*180/Math.PI));while(getXRot()-xRotO<-180)xRotO-=360;while(getXRot()-xRotO>=180)xRotO+=360;while(getYRot()-yRotO<-180)yRotO-=360;while(getYRot()-yRotO>=180)yRotO+=360;setXRot(xRotO+(getXRot()-xRotO)*.2f);setYRot(yRotO+(getYRot()-yRotO)*.2f);checkInsideBlocks();
    }
    public boolean inflictDamage(Entity target){int damage=Mth.ceil((double)(float)Math.sqrt(getDeltaMovement().lengthSqr())*getBaseDamage());if(isCritArrow())damage+=random.nextInt(damage/2+2);int fire=isOnFire()&&element()!=2?5:0;String type=element()==0?"airarrow":element()==1?"firearrow":element()==4?"orderarrow":null;if(element()==1)fire+=5;if(target instanceof LivingEntity living){if(element()==2)living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,200,4));else if(element()==4)living.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,200,4));else if(element()==5)living.addEffect(new MobEffectInstance(MobEffects.WITHER,100));}var owner=getOwner()==null?this:getOwner();var source=type==null?damageSources().arrow(this,owner):new net.minecraft.world.damagesource.DamageSource(registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(ResourceKey.create(Registries.DAMAGE_TYPE,ResourceLocation.fromNamespaceAndPath("thaumcraft",type))),this,owner);if(fire>0&&!(target instanceof net.minecraft.world.entity.monster.EnderMan))target.igniteForSeconds(fire);boolean accepted=target.hurt(source,damage);if(accepted&&level() instanceof net.minecraft.server.level.ServerLevel server&&target instanceof LivingEntity living){net.minecraft.world.item.enchantment.EnchantmentHelper.doPostAttackEffects(server,living,source);if(living!=getOwner()&&living instanceof Player&&getOwner() instanceof net.minecraft.server.level.ServerPlayer player)player.connection.send(new net.minecraft.network.protocol.game.ClientboundGameEventPacket(net.minecraft.network.protocol.game.ClientboundGameEventPacket.ARROW_HIT_PLAYER,0));}return accepted;}
    @Override public void writeSpawnData(RegistryFriendlyByteBuf buffer){var velocity=getDeltaMovement();buffer.writeDouble(velocity.x);buffer.writeDouble(velocity.y);buffer.writeDouble(velocity.z);buffer.writeFloat(getYRot());buffer.writeFloat(getXRot());buffer.writeByte(element());buffer.writeInt(shootingEntityId);}
    @Override public void readSpawnData(RegistryFriendlyByteBuf buffer){setDeltaMovement(buffer.readDouble(),buffer.readDouble(),buffer.readDouble());setYRot(buffer.readFloat());setXRot(buffer.readFloat());yRotO=getYRot();xRotO=getXRot();element(buffer.readByte());shootingEntityId=buffer.readInt();}
    @Override public void addAdditionalSaveData(CompoundTag t){super.addAdditionalSaveData(t);t.putByte("TC4ArrowType",(byte)element());}
    @Override public void readAdditionalSaveData(CompoundTag t){super.readAdditionalSaveData(t);element(t.contains("TC4ArrowType")?t.getByte("TC4ArrowType"):t.getByte("type"));pickup=Pickup.DISALLOWED;legacyGround=false;legacyGroundTicks=legacyAirTicks=firstKnockback=shootingEntityId=0;legacyBlock=new net.minecraft.core.BlockPos(-1,-1,-1);legacyState=Blocks.AIR.defaultBlockState();}
}
