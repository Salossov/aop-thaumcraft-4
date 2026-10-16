package thaumcraft.entities;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.level.Level;

public final class MindSpider extends Spider {
    private static final EntityDataAccessor<Boolean> HARMLESS=SynchedEntityData.defineId(MindSpider.class,EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<java.util.Optional<java.util.UUID>> VIEWER=SynchedEntityData.defineId(MindSpider.class,EntityDataSerializers.OPTIONAL_UUID);
    public MindSpider(EntityType<? extends MindSpider> type,Level level){super(type,level);xpReward=1;}
    public static AttributeSupplier.Builder attributes(){return Spider.createAttributes().add(Attributes.MAX_HEALTH,1).add(Attributes.ATTACK_DAMAGE,1);}
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder){super.defineSynchedData(builder);builder.define(HARMLESS,false);builder.define(VIEWER,java.util.Optional.empty());}
    public boolean harmless(){return entityData.get(HARMLESS);}
    public java.util.UUID viewer(){return entityData.get(VIEWER).orElse(null);}
    public void illusion(net.minecraft.world.entity.player.Player player){entityData.set(HARMLESS,true);entityData.set(VIEWER,java.util.Optional.of(player.getUUID()));}
    @Override public boolean doHurtTarget(Entity target){return !harmless()&&super.doHurtTarget(target);}
    @Override protected int getBaseExperienceReward(){return harmless()?0:super.getBaseExperienceReward();}
    @Override protected void dropCustomDeathLoot(ServerLevel level,DamageSource source,boolean recentlyHit){}
    @Override protected void dropFromLootTable(DamageSource source,boolean recentlyHit){}
    @Override public void tick(){super.tick();if(!level().isClientSide&&harmless()&&tickCount>1200)discard();}
    @Override public void addAdditionalSaveData(CompoundTag tag){super.addAdditionalSaveData(tag);tag.putBoolean("Harmless",harmless());if(viewer()!=null)tag.putUUID("Viewer",viewer());tag.putInt("Lifetime",tickCount);}
    @Override public void readAdditionalSaveData(CompoundTag tag){super.readAdditionalSaveData(tag);entityData.set(HARMLESS,tag.getBoolean("Harmless"));entityData.set(VIEWER,tag.hasUUID("Viewer")?java.util.Optional.of(tag.getUUID("Viewer")):java.util.Optional.empty());tickCount=Math.max(0,tag.getInt("Lifetime"));}
}
