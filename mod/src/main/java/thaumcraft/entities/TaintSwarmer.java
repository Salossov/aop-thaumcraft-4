package thaumcraft.entities;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import thaumcraft.registry.R2Content;

public final class TaintSwarmer extends TaintSpore {
    private int spawnCounter=500;
    public TaintSwarmer(EntityType<? extends TaintSwarmer> type,Level level) { super(type,level);size(10); }
    @Override public EntityDimensions getDefaultDimensions(Pose pose) { return EntityDimensions.scalable(1,1); }
    @Override protected void sporeTick() {
        if(spawnCounter>0) spawnCounter--;if(spawnCounter<=0&&level().getNearestPlayer(this,16)!=null) {spawnCounter=500;swarmBurst();}
        if(hurtTime==1) swarmBurst();
    }
    private void swarmBurst() { if(!isAlive()) return;var swarm=R2Entities.TAINT_SWARM.get().create(level());swarm.moveTo(getX(),getY()+0.5,getZ(),random.nextFloat()*360,0);level().addFreshEntity(swarm);level().broadcastEntityEvent(this,(byte)6); }
    @Override public void playerTouch(Player player) {}
    @Override protected boolean onStalk() { return false; }
    @Override protected void dropCustomDeathLoot(ServerLevel level,DamageSource source,boolean recentlyHit) { for(int i=0;i<2;i++) spawnAtLocation(random.nextBoolean()?R2Content.TAINT_TENDRIL.get():R2Content.TAINT_GOO.get()); }
    @Override public void addAdditionalSaveData(CompoundTag tag) { super.addAdditionalSaveData(tag);tag.putInt("SpawnCounter",spawnCounter); }
    @Override public void readAdditionalSaveData(CompoundTag tag) { super.readAdditionalSaveData(tag);spawnCounter=tag.getInt("SpawnCounter"); }
}
