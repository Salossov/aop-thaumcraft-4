package thaumcraft.entities;

import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.*;
import net.minecraft.sounds.*;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import thaumcraft.blocks.*;
import thaumcraft.network.BlockArcPayload;
import thaumcraft.registry.*;

/** Stationary invasion portal. Stage persists; countdown and pulse deliberately restart on reload. */
public final class CultistPortal extends Monster {
    private int stage,stagecounter=200;
    public int pulse;
    private final ServerBossEvent bossEvent;
    public CultistPortal(EntityType<? extends CultistPortal> type,Level level){super(type,level);xpReward=30;bossEvent=new ServerBossEvent(getDisplayName(),BossEvent.BossBarColor.PURPLE,BossEvent.BossBarOverlay.PROGRESS);}
    public static AttributeSupplier.Builder attributes(){return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH,500).add(Attributes.ATTACK_DAMAGE,0).add(Attributes.KNOCKBACK_RESISTANCE,1).add(Attributes.ARMOR,5);}
    @Override protected void registerGoals(){}
    @Override public void move(MoverType type,Vec3 motion){}
    @Override public float getPickRadius(){return 0;}
    @Override public boolean canBeCollidedWith(){return true;}
    @Override public boolean isPushable(){return false;}
    @Override public boolean isEffectiveAi(){return false;}
    @Override public boolean shouldRenderAtSqrDistance(double distance){return distance<4096;}
    @Override public boolean canBeAffected(MobEffectInstance effect){return false;}
    @Override public boolean causeFallDamage(float distance,float multiplier,DamageSource source){return false;}
    public int getStage(){return stage;}
    public int getStageCounter(){return stagecounter;}
    @Override public void tick(){super.tick();if(!level().isClientSide){if(stagecounter>0){--stagecounter;if(stagecounter==160&&stage==0)createBanners();if(stagecounter>20&&stagecounter<150&&stage==0&&stagecounter%13==0)createCrate();}else if(level().getNearestPlayer(this,48)!=null){level().broadcastEntityEvent(this,(byte)16);switch(stage){case 0,1,2,3,4->{stagecounter=15+random.nextInt(10-stage)-stage;spawnMinions();}case 12->{stagecounter=50+getTiming()*2+random.nextInt(50);spawnBoss();}default->{int t=getTiming();stagecounter=t+random.nextInt(5+t/3);spawnMinions();}}++stage;}else stagecounter=30+random.nextInt(30);if(stage<12)heal(1);bossEvent.setProgress(getHealth()/getMaxHealth());}if(pulse>0)--pulse;}
    private void createBanners(){level().broadcastEntityEvent(this,(byte)16);for(var direction:new Direction[]{Direction.NORTH,Direction.SOUTH,Direction.WEST,Direction.EAST}){var pos=new BlockPos((int)getX()-direction.getStepX()*6,(int)getY(),(int)getZ()+direction.getStepZ()*6);int facing=switch(direction){case NORTH->8;case SOUTH->0;case WEST->12;default->4;};level().setBlock(pos,R4Content.BANNER.get().defaultBlockState().setValue(ThaumicBannerBlock.ROTATION,facing),3);if(level().getBlockEntity(pos) instanceof ThaumicBannerBlockEntity tile){tile.color=-1;tile.sync();arc(pos);}}}
    private void createCrate(){int x=(int)getX()+random.nextInt(5)-random.nextInt(5),z=(int)getZ()+random.nextInt(5)-random.nextInt(5);var pos=new BlockPos(x,(int)getY(),z);if(x!=(int)getX()&&z!=(int)getZ()&&level().isEmptyBlock(pos)){level().broadcastEntityEvent(this,(byte)16);float r=level().random.nextFloat();int rarity=r<.05f?2:r<.2f?1:0;level().setBlock(pos,R5Content.LOOT_CRATE.get().defaultBlockState().setValue(AncientLootBlock.RARITY,rarity),3);arc(pos);}}
    private void arc(BlockPos pos){if(level() instanceof ServerLevel server)PacketDistributor.sendToPlayersNear(server,null,getX(),getY(),getZ(),32,new BlockArcPayload(pos,getId()));playSound(R2Sounds.WAND_FAIL.get(),1,1);}
    public int getTiming(){return level().getEntitiesOfClass(Cultist.class,new net.minecraft.world.phys.AABB(getX(),getY(),getZ(),getX(),getY(),getZ()).inflate(32)).size()*20;}
    public void spawnMinions(){if(!(level() instanceof ServerLevel server))return;Cultist cultist=random.nextFloat()>.33?R5Entities.CULTIST_KNIGHT.get().create(server):R5Entities.CULTIST_CLERIC.get().create(server);if(cultist==null)return;prepare(cultist,server);server.addFreshEntity(cultist);cultist.playSound(R2Sounds.WAND_FAIL.get(),1,1);if(stage>12)hurt(damageSources().fellOutOfWorld(),5+random.nextInt(5));}
    public void spawnBoss(){if(!(level() instanceof ServerLevel server))return;var cultist=R5Entities.CULTIST_LEADER.get().create(server);if(cultist==null)return;prepare(cultist,server);server.addFreshEntity(cultist);cultist.playSound(R2Sounds.WAND_FAIL.get(),1,1);}
    private void prepare(Mob cultist,ServerLevel server){cultist.setPos(getX()+random.nextFloat()-random.nextFloat(),getY()+.25,getZ()+random.nextFloat()-random.nextFloat());cultist.finalizeSpawn(server,server.getCurrentDifficultyAt(cultist.blockPosition()),MobSpawnType.MOB_SUMMONED,null);cultist.restrictTo(new BlockPos((int)getX(),(int)getY(),(int)getZ()),32);server.broadcastEntityEvent(cultist,(byte)20);}
    @Override public void playerTouch(Player player){if(distanceToSqr(player)<3&&player.hurt(damageSources().indirectMagic(this,this),8))playSound(R2Sounds.ZAP.get(),1,(random.nextFloat()-random.nextFloat())*.1f+1);}
    @Override protected float getSoundVolume(){return .75f;}
    @Override public int getAmbientSoundInterval(){return 540;}
    @Override protected SoundEvent getAmbientSound(){return R2Sounds.MONOLITH.get();}
    @Override protected SoundEvent getHurtSound(DamageSource source){return R2Sounds.ZAP.get();}
    @Override protected SoundEvent getDeathSound(){return R2Sounds.SHOCK.get();}
    @Override public void die(DamageSource source){if(!level().isClientSide)level().explode(this,getX(),getY(),getZ(),2,false,Level.ExplosionInteraction.NONE);super.die(source);}
    @Override protected void dropCustomDeathLoot(ServerLevel world,DamageSource source,boolean hit){var pearl=R5Entities.SPECIAL_ITEM.get().create(world);pearl.setItem(new ItemStack(R3Equipment.PRIMAL_PEARL.get()));pearl.setPos(getX(),getY()+getBbHeight()/2f,getZ());pearl.setPickUpDelay(10);pearl.setDeltaMovement(0,(double).1f,0);world.addFreshEntity(pearl);}
    @Override public void handleEntityEvent(byte event){if(event==16)pulse=10;else super.handleEntityEvent(event);}
    @Override public void addAdditionalSaveData(CompoundTag tag){super.addAdditionalSaveData(tag);tag.putInt("stage",stage);}
    @Override public void readAdditionalSaveData(CompoundTag tag){super.readAdditionalSaveData(tag);stage=tag.getInt("stage");}
    @Override public void startSeenByPlayer(ServerPlayer player){super.startSeenByPlayer(player);bossEvent.addPlayer(player);}
    @Override public void stopSeenByPlayer(ServerPlayer player){super.stopSeenByPlayer(player);bossEvent.removePlayer(player);}
}
