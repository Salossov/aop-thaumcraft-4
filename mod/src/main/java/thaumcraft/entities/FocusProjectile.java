package thaumcraft.entities;

import net.minecraft.core.*;
import net.minecraft.core.particles.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.*;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.damagesource.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.neoforged.neoforge.entity.IEntityWithComplexSpawn;
import thaumcraft.api.aspects.Aspects;
import thaumcraft.registry.R3Equipment;
import thaumcraft.world.*;

public final class FocusProjectile extends ThrowableItemProjectile implements IEntityWithComplexSpawn {
    public enum Kind { EMBER,FROST,FIREBALL,PRIMAL,SHOCK }
    private static final EntityDataAccessor<Integer> KIND=SynchedEntityData.defineId(FocusProjectile.class,EntityDataSerializers.INT),FLAGS=SynchedEntityData.defineId(FocusProjectile.class,EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> DAMAGE=SynchedEntityData.defineId(FocusProjectile.class,EntityDataSerializers.FLOAT);
    public int duration=20,upgrade,bounces=3,area=4;
    public double bounce=.5;
    private int primalCount;
    public record Cracks(Vec3 position,net.minecraft.world.level.block.state.BlockState state,java.util.List<Vec3> velocities,boolean frost){}
    public static java.util.function.Consumer<Cracks> clientCracks=effect->{};
    public static java.util.function.Consumer<FocusProjectile> clientTick=entity->{},clientImpact=entity->{};
    private static final ResourceKey<DamageType> EMBER_DAMAGE=ResourceKey.create(Registries.DAMAGE_TYPE,ResourceLocation.fromNamespaceAndPath("thaumcraft","ember"));
    public FocusProjectile(EntityType<? extends FocusProjectile> type,Level level){super(type,level);if(type==R3Entities.PRIMAL_ORB.get())configure(Kind.PRIMAL,2,0);}
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder){super.defineSynchedData(builder);builder.define(KIND,0);builder.define(FLAGS,0);builder.define(DAMAGE,1f);}
    public FocusProjectile configure(Kind kind,float damage,int flags){entityData.set(KIND,kind.ordinal());entityData.set(DAMAGE,damage);entityData.set(FLAGS,flags);setItem(new net.minecraft.world.item.ItemStack(R3Equipment.FOCI.get(switch(kind){case EMBER,FIREBALL->"fire";case FROST->"frost";case PRIMAL->"primal";case SHOCK->"shock";}).get()));return this;}
    public Kind kind(){return Kind.values()[Math.floorMod(entityData.get(KIND),Kind.values().length)];}
    public float damage(){return entityData.get(DAMAGE);}
    public boolean flag(int bit){return (entityData.get(FLAGS)&bit)!=0;}
    @Override protected Item getDefaultItem(){return R3Equipment.FOCI.get("fire").get();}
    /** EntityThrowable used Gaussian dispersion without inheriting the caster's movement. */
    @Override public void shoot(double x,double y,double z,float speed,float scatter){
        LegacyThrowableLaunch.shoot(this,random,x,y,z,speed,scatter);
    }
    public void launch(LivingEntity caster,float speed,float scatter){
        LegacyThrowableLaunch.owner(this,caster,speed,0);
        if(kind()==Kind.EMBER||kind()==Kind.FROST){var first=getDeltaMovement();shoot(first.x,first.y,first.z,speed,scatter);}
        if(kind()==Kind.EMBER)setPos(position().add(getDeltaMovement()));
    }
    public net.minecraft.util.RandomSource effectRandom(){return random;}
    @Override protected double getDefaultGravity(){return switch(kind()){case EMBER->0;case PRIMAL->(double).001f;case FIREBALL->(double).01f;case FROST->flag(1)?(double).015f:(double).05f;case SHOCK->(double).05f;};}
    @Override public EntityDimensions getDimensions(Pose pose){float size=entityData!=null&&kind()==Kind.FROST?.15f+damage()*.15f:0;return size>0?EntityDimensions.scalable(size,size):super.getDimensions(pose);}
    @Override public void onSyncedDataUpdated(EntityDataAccessor<?> key){super.onSyncedDataUpdated(key);if(key.equals(KIND)||key.equals(DAMAGE))refreshDimensions();}
    private boolean visible(Vec3 target){return level().clip(new ClipContext(position(),target,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,this)).getType()==HitResult.Type.MISS;}
    private boolean insidePortal(){return level().getBlockState(BlockPos.containing(getX(),getEyeY(),getZ())).is(BlockTags.PORTALS);}
    private DamageSource fireDamage(boolean explosive){return new DamageSource(level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(explosive?DamageTypes.FIREBALL:EMBER_DAMAGE),this,getOwner()==null&&explosive?this:getOwner());}
    @Override public boolean hurt(DamageSource source,float amount){
        if((kind()!=Kind.FIREBALL&&kind()!=Kind.SHOCK)||isInvulnerableTo(source))return false;
        markHurt();var attacker=source.getEntity();if(attacker==null)return false;setDeltaMovement(attacker.getLookAngle().scale(.9));
        if(kind()==Kind.SHOCK)playSound(thaumcraft.registry.R2Sounds.ZAP.get(),1,1+(random.nextFloat()-random.nextFloat())*.2f);return true;
    }
    @Override public void writeSpawnData(RegistryFriendlyByteBuf buffer){buffer.writeInt(duration);buffer.writeInt(upgrade);buffer.writeInt(bounces);buffer.writeInt(area);buffer.writeDouble(bounce);var velocity=getDeltaMovement();buffer.writeDouble(velocity.x);buffer.writeDouble(velocity.y);buffer.writeDouble(velocity.z);}
    @Override public void readSpawnData(RegistryFriendlyByteBuf buffer){duration=buffer.readInt();upgrade=buffer.readInt();bounces=buffer.readInt();area=buffer.readInt();bounce=buffer.readDouble();setDeltaMovement(buffer.readDouble(),buffer.readDouble(),buffer.readDouble());}
    public static boolean allowed(Entity owner,Entity target){return target!=owner&&target.isAlive()&&(!(owner instanceof Player player)||!(target instanceof Player other)||player.canHarmPlayer(other));}
    @Override public void tick(){
        if(kind()==Kind.EMBER){if(tickCount>duration)discard();setDeltaMovement(getDeltaMovement().scale(duration<=20?.95:.975));if(onGround())setDeltaMovement(getDeltaMovement().scale(.66));}
        if(kind()==Kind.PRIMAL){
            ++primalCount;if(insidePortal())onHit(new EntityHitResult(this));
            if(level().isClientSide)clientTick.accept(this);
            if(tickCount>20){
                var velocity=getDeltaMovement();
                if(flag(1)){
                    LivingEntity nearest=null;double closest=Double.MAX_VALUE;var centre=position();
                    for(var entity:level().getEntitiesOfClass(LivingEntity.class,new AABB(centre,centre).inflate(16),e->e!=getOwner()&&e.isAlive())){
                        double distance=distanceToSqr(entity);if(distance<closest){closest=distance;nearest=entity;}
                    }
                    if(nearest!=null){
                        var delta=nearest.position().add(0,nearest.getBbHeight()*.9,0).subtract(position()).scale(.2/Math.max(1e-12,closest));velocity=velocity.add(delta);
                        velocity=new Vec3(net.minecraft.util.Mth.clamp((float)velocity.x,-.2f,.2f),net.minecraft.util.Mth.clamp((float)velocity.y,-.2f,.2f),net.minecraft.util.Mth.clamp((float)velocity.z,-.2f,.2f));
                    }
                }else{
                    var jitter=new java.util.Random(getId()+primalCount);velocity=velocity.add((double)((jitter.nextFloat()-jitter.nextFloat())*.01f),(double)((jitter.nextFloat()-jitter.nextFloat())*.01f),(double)((jitter.nextFloat()-jitter.nextFloat())*.01f));
                }
                setDeltaMovement(velocity);
            }
        }
        super.tick();
        if(kind()==Kind.FROST){var velocity=getDeltaMovement();float yaw=(float)(Math.atan2(velocity.x,velocity.z)*180/Math.PI),pitch=(float)(Math.atan2(velocity.y,(float)Math.sqrt(velocity.x*velocity.x+velocity.z*velocity.z))*180/Math.PI);setYRot(yRotO+net.minecraft.util.Mth.wrapDegrees(yaw-yRotO)*.2f);setXRot(xRotO+net.minecraft.util.Mth.wrapDegrees(pitch-xRotO)*.2f);}
        if((kind()==Kind.PRIMAL&&tickCount>5000)||((kind()==Kind.FIREBALL||kind()==Kind.SHOCK)&&tickCount>500))discard();
        if(level().isClientSide&&kind()!=Kind.PRIMAL)clientTick.accept(this);
    }
    @Override protected void onHit(HitResult hit){
        if(kind()==Kind.FROST){frostHit(hit);return;}
        if(level().isClientSide){if(kind()==Kind.PRIMAL||kind()==Kind.SHOCK)clientImpact.accept(this);if(kind()!=Kind.PRIMAL)discard();return;}
        var level=(ServerLevel)level();var owner=getOwner();
        if(kind()==Kind.EMBER){
            if(hit instanceof EntityHitResult target){if(!target.getEntity().fireImmune()&&target.getEntity().hurt(fireDamage(false),damage()))target.getEntity().igniteForSeconds(3+upgrade);}
            else if(hit instanceof BlockHitResult block&&random.nextFloat()<.025f*upgrade&&owner instanceof net.minecraft.server.level.ServerPlayer player){var pos=block.getBlockPos().relative(block.getDirection());if(level.isEmptyBlock(pos)&&thaumcraft.blocks.FocusActions.canChange(player,pos))level.setBlockAndUpdate(pos,BaseFireBlock.getState(level,pos));}
        }else if(kind()==Kind.FIREBALL){
            if(hit instanceof EntityHitResult target)target.getEntity().hurt(fireDamage(true),damage()*1.5f);
            level.explode(null,getX(),getY(),getZ(),damage(),flag(1),Level.ExplosionInteraction.NONE);
        }else if(kind()==Kind.SHOCK){
            var centre=position();
            for(var target:level.getEntities(this,new AABB(centre,centre).inflate(area)))if(visible(target.position()))target.hurt(damageSources().indirectMagic(this,owner),damage());
            for(int attempt=0;attempt<20;attempt++){
                int x=getBlockX()+random.nextInt(area)-random.nextInt(area),z=getBlockZ()+random.nextInt(area)-random.nextInt(area);var base=new BlockPos(x,getBlockY()+area,z);
                while(base.getY()>getBlockY()-area&&level.isEmptyBlock(base))base=base.below();var pos=base.above();
                if(level.hasChunkAt(pos)&&!level.isEmptyBlock(base)&&level.isEmptyBlock(pos)&&!level.getBlockState(pos).is(thaumcraft.registry.R3Content.SHOCK_FIELD.get())&&visible(Vec3.atCenterOf(pos))
                    &&owner instanceof net.minecraft.server.level.ServerPlayer player&&thaumcraft.blocks.FocusActions.canChange(player,pos))level.setBlockAndUpdate(pos,thaumcraft.registry.R3Content.SHOCK_FIELD.get().defaultBlockState());
            }
            playSound(thaumcraft.registry.R2Sounds.SHOCK.get(),1,1+(random.nextFloat()-random.nextFloat())*.2f);
        }else if(kind()==Kind.PRIMAL){
            boolean portalBlock=hit instanceof BlockHitResult&&insidePortal();level.explode(null,getX(),getY(),getZ(),portalBlock?4:2,Level.ExplosionInteraction.TNT);
            if(!flag(1)&&random.nextInt(100)<=(portalBlock?10:1)){
                if(random.nextBoolean()){
                    for(int i=0;i<10;i++){
                        int x=(int)getX()+(int)(random.nextFloat()-random.nextFloat()*6),z=(int)getZ()+(int)(random.nextFloat()-random.nextFloat()*6);
                        if(!random.nextBoolean()||!level.hasChunk(x>>4,z>>4)||WorldBiomes.is(level,new BlockPos(x,getBlockY(),z),WorldBiomes.TAINT))continue;
                        WorldBiomes.change(level,x,z,WorldBiomes.TAINT);var pos=level.getHeightmapPos(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,new BlockPos(x,0,z));
                        if(!level.isEmptyBlock(pos.below()))level.setBlockAndUpdate(pos,thaumcraft.registry.R2Content.TAINT_FIBRES.get().defaultBlockState());
                    }
                }else{
                    // MovingObjectPosition(Entity) leaves its legacy block coordinates at zero.
                    var pos=hit instanceof BlockHitResult block?block.getBlockPos():BlockPos.ZERO;boolean loaded=true;
                    for(var sample:BlockPos.betweenClosed(pos.offset(-5,-5,-5),pos.offset(5,5,5)))if(!level.hasChunkAt(sample)){loaded=false;break;}
                    if(loaded)NodeFactory.createSmall(level,pos,random);
                }
            }
        }
        discard();
    }
    private void frostHit(HitResult hit){
        var velocity=getDeltaMovement();
        if(hit instanceof BlockHitResult block){
            var axis=block.getDirection().getAxis();velocity=velocity.multiply(axis==Direction.Axis.X?-1:1,axis==Direction.Axis.Y?-.9:1,axis==Direction.Axis.Z?-1:1);
            var state=level().getBlockState(block.getBlockPos());playSound(state.getSoundType(level(),block.getBlockPos(),this).getBreakSound(),.3f,1.2f/(random.nextFloat()*.2f+.9f));cracks(state,damage(),false);
        }else if(hit instanceof EntityHitResult entity){
            var delta=blockPosition().subtract(entity.getEntity().blockPosition());velocity=velocity.multiply(delta.getX()!=0?-1:1,delta.getY()!=0?-.9:1,delta.getZ()!=0?-1:1).scale(.66);cracks(Blocks.ICE.defaultBlockState(),damage(),true);
        }
        velocity=velocity.scale(bounce);setDeltaMovement(velocity);double speed=(float)Math.sqrt(velocity.lengthSqr());
        if(speed>0)setPos(position().subtract(velocity.scale((double).05f/speed)));hasImpulse=true;
        if(!level().isClientSide&&hit instanceof EntityHitResult entity){
            var target=entity.getEntity();var before=target.getDeltaMovement();target.hurt(damageSources().thrown(this,getOwner()),damage());
            if(target instanceof LivingEntity living&&upgrade>0)living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,200,upgrade-1));
            if(flag(1)){target.invulnerableTime=0;discard();iceBreak();target.setDeltaMovement(before.add(target.getDeltaMovement().subtract(before).scale(.1)));}
        }
        if(bounces--<=0){discard();iceBreak();cracks(Blocks.ICE.defaultBlockState(),8*damage(),true);}
    }
    private void iceBreak(){playSound(Blocks.ICE.defaultBlockState().getSoundType().getBreakSound(),.3f,1.2f/(random.nextFloat()*.2f+.9f));}
    private void cracks(net.minecraft.world.level.block.state.BlockState state,float count,boolean frost){
        var velocities=new java.util.ArrayList<Vec3>();
        for(int i=0;i<count;i++)velocities.add(new Vec3(4*((double)random.nextFloat()-.5),.5,((double)random.nextFloat()-.5)*4));
        if(level().isClientSide)clientCracks.accept(new Cracks(position(),state,java.util.List.copyOf(velocities),frost));
    }
    @Override public void addAdditionalSaveData(CompoundTag tag){super.addAdditionalSaveData(tag);tag.putInt("Kind",kind().ordinal());tag.putInt("Flags",entityData.get(FLAGS));tag.putFloat("Damage",damage());tag.putInt("Duration",duration);tag.putInt("Upgrade",upgrade);tag.putInt("Bounces",bounces);tag.putDouble("Bounce",bounce);tag.putInt("Area",area);}
    @Override public void readAdditionalSaveData(CompoundTag tag){super.readAdditionalSaveData(tag);configure(Kind.values()[Math.floorMod(tag.getInt("Kind"),Kind.values().length)],tag.getFloat("Damage"),tag.getInt("Flags"));duration=tag.getInt("Duration");upgrade=tag.getInt("Upgrade");bounces=tag.getInt("Bounces");bounce=tag.getDouble("Bounce");area=Math.max(1,Math.min(16,tag.getInt("Area")));}
}
