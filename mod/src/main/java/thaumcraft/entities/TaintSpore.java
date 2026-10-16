package thaumcraft.entities;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import thaumcraft.blocks.TaintFibresBlock;
import thaumcraft.registry.R2Content;
import thaumcraft.world.WorldBiomes;

public class TaintSpore extends Monster implements TaintedMob {
    private static final EntityDataAccessor<Integer> SIZE=SynchedEntityData.defineId(TaintSpore.class,EntityDataSerializers.INT);
    private int growth;
    public float displaySize=0.1f;
    public TaintSpore(EntityType<? extends TaintSpore> type,Level level) { super(type,level);xpReward=2; }
    public static AttributeSupplier.Builder attributes(boolean swarmer) { return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH,swarmer?75:1).add(Attributes.ATTACK_DAMAGE,1); }
    @Override protected void registerGoals() {}
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) { super.defineSynchedData(builder);builder.define(SIZE,2); }
    public int size() { return entityData.get(SIZE); }
    public void size(int value) { entityData.set(SIZE,Math.clamp(value,1,10));xpReward=size();refreshDimensions(); }
    @Override public EntityDimensions getDefaultDimensions(Pose pose) { float size=Math.max(0.15f*size(),0.5f);return EntityDimensions.scalable(size,size); }
    @Override public void onSyncedDataUpdated(EntityDataAccessor<?> key) { super.onSyncedDataUpdated(key);if(SIZE.equals(key)) refreshDimensions(); }
    @Override public boolean isPushable() { return false; }
    protected boolean onStalk() { var state=level().getBlockState(blockPosition().below());return state.is(R2Content.TAINT_SPORE_STALK)&&state.getValue(TaintFibresBlock.OCCUPIED); }
    @Override public void move(MoverType type,Vec3 motion) { if(!onStalk()) super.move(type,new Vec3(0,Math.min(0,motion.y),0)); }
    @Override public void tick() {
        super.tick();if(level().isClientSide) {displaySize=Math.min(size(),displaySize+0.02f);return;}
        if(tickCount%20==0&&!WorldBiomes.is((ServerLevel)level(),blockPosition(),WorldBiomes.TAINT)) hurt(damageSources().fellOutOfWorld(),1);
        sporeTick();
    }
    protected void sporeTick() {
        if(size()<10&&growth++==1200) { size(size()+1);growth=0; }
        if(isAlive()&&(!onStalk()||hurtTime>0)) burst();
    }
    @Override public void playerTouch(Player player) { if(!level().isClientSide) burst(); }
    protected void burst() {
        if(level().isClientSide||isRemoved()) return;int count=size()/3+random.nextInt(size()/2+1);
        for(int i=0;i<count;i++) { var spider=R2Entities.TAINT_SPIDER.get().create(level());spider.moveTo(getX()+random.nextFloat()-random.nextFloat(),getY()+random.nextFloat(),getZ()+random.nextFloat()-random.nextFloat(),random.nextFloat()*360,0);level().addFreshEntity(spider); }
        if(onStalk()) {var pos=blockPosition().below();level().setBlock(pos,level().getBlockState(pos).setValue(TaintFibresBlock.OCCUPIED,false),3);}discard();
    }
    @Override protected void dropCustomDeathLoot(ServerLevel level,DamageSource source,boolean recentlyHit) { spawnAtLocation(random.nextBoolean()?R2Content.TAINT_TENDRIL.get():R2Content.TAINT_GOO.get()); }
    @Override public void addAdditionalSaveData(CompoundTag tag) { super.addAdditionalSaveData(tag);tag.putInt("Size",size());tag.putInt("Growth",growth); }
    @Override public void readAdditionalSaveData(CompoundTag tag) { super.readAdditionalSaveData(tag);size(tag.getInt("Size"));growth=tag.getInt("Growth"); }
}
