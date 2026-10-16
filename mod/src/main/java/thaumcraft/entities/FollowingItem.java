package thaumcraft.entities;

import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import thaumcraft.mixin.ItemAgeAccessor;

/** Original following drops; the temporary target deliberately does not survive a save/load. */
public final class FollowingItem extends SpecialItemEntity {
    private static final EntityDataAccessor<Integer> TARGET=SynchedEntityData.defineId(FollowingItem.class,EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> STYLE=SynchedEntityData.defineId(FollowingItem.class,EntityDataSerializers.INT);
    private static final EntityDataAccessor<Long> X=SynchedEntityData.defineId(FollowingItem.class,EntityDataSerializers.LONG);
    private static final EntityDataAccessor<Long> Y=SynchedEntityData.defineId(FollowingItem.class,EntityDataSerializers.LONG);
    private static final EntityDataAccessor<Long> Z=SynchedEntityData.defineId(FollowingItem.class,EntityDataSerializers.LONG);
    public record Trail(Vec3 position,int style){}
    public static Consumer<Trail> clientTrail=trail->{};

    public FollowingItem(EntityType<? extends FollowingItem> type,Level level){super(type,level);setPickUpDelay(20);}
    public FollowingItem(Level level,ItemEntity drop,Entity target,int style){
        this(R5Entities.FOLLOWING_ITEM.get(),level);setPos(drop.position());setItem(drop.getItem().copy());
        setYRot(random.nextFloat()*360);setDeltaMovement(drop.getDeltaMovement());entityData.set(STYLE,style);
        entityData.set(TARGET,target.getId());setDestination(target.position().add(0,target.getBbHeight()/2,0));noPhysics=true;
    }
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder){
        super.defineSynchedData(builder);builder.define(TARGET,-1);builder.define(STYLE,3);builder.define(X,0L);builder.define(Y,0L);builder.define(Z,0L);
    }
    private Vec3 destination(){return new Vec3(Double.longBitsToDouble(entityData.get(X)),Double.longBitsToDouble(entityData.get(Y)),Double.longBitsToDouble(entityData.get(Z)));}
    private void setDestination(Vec3 point){entityData.set(X,Double.doubleToRawLongBits(point.x));entityData.set(Y,Double.doubleToRawLongBits(point.y));entityData.set(Z,Double.doubleToRawLongBits(point.z));}
    public boolean isFollowing(){return !destination().equals(Vec3.ZERO);}
    @Override public void tick(){
        var target=level().getEntity(entityData.get(TARGET));
        if(target!=null)setDestination(target.position().add(0,target.getBbHeight()/2,0));
        if(isFollowing()){
            var point=destination();float dx=(float)(point.x-getX()),dy=(float)(point.y-getY()-getBbHeight()/2),dz=(float)(point.z-getZ());
            int delay=((ItemAgeAccessor)(ItemEntity)this).thaumcraft$getPickupDelay();if(delay>1)setPickUpDelay(--delay);
            double distance=Mth.sqrt(dx*dx+dy*dy+dz*dz);
            if(distance>.5){
                // The legacy pickup timer is decremented again by ItemEntity.tick. Guard its zero
                // endpoint so a delayed arrival cannot inject infinite velocity into the modern server.
                double divisor=distance*Math.max(1,delay);setDeltaMovement(dx/divisor,dy/divisor,dz/divisor);
            }else{setDeltaMovement(getDeltaMovement().scale((double).1f));setDestination(Vec3.ZERO);entityData.set(TARGET,-1);noPhysics=false;}
            if(level().isClientSide)clientTrail.accept(new Trail(new Vec3((float)xo+(random.nextFloat()-random.nextFloat())*.125f,
                (float)yo+getBbHeight()+(random.nextFloat()-random.nextFloat())*.125f,
                (float)zo+(random.nextFloat()-random.nextFloat())*.125f),entityData.get(STYLE)));
        }else setDeltaMovement(getDeltaMovement().add(0,-(double).04f,0));
        super.tick();
    }
    public static void followFreshDrops(ServerLevel level,BlockPos block,Entity target,int style){
        var centre=Vec3.atCenterOf(block);var box=new AABB(centre,centre).inflate(2);
        for(var drop:level.getEntitiesOfClass(ItemEntity.class,box)){
            if(drop.isRemoved()||drop.tickCount!=0||drop instanceof FollowingItem||drop.getItem().isEmpty())continue;
            if(level.addFreshEntity(new FollowingItem(level,drop,target,style)))drop.discard();
        }
    }
    @Override public void addAdditionalSaveData(CompoundTag tag){super.addAdditionalSaveData(tag);tag.putShort("type",(short)(int)entityData.get(STYLE));}
    @Override public void readAdditionalSaveData(CompoundTag tag){super.readAdditionalSaveData(tag);entityData.set(STYLE,(int)tag.getShort("type"));entityData.set(TARGET,-1);setDestination(Vec3.ZERO);noPhysics=false;}
}
