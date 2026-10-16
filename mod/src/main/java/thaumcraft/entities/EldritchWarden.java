package thaumcraft.entities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.*;
import net.minecraft.sounds.*;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.*;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.*;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.*;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityTeleportEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import thaumcraft.knowledge.KnowledgeService;
import thaumcraft.network.*;
import thaumcraft.registry.*;

/** TC4 Warden: absorption shield, a single field frenzy and alternating orb/sonic attacks. */
public final class EldritchWarden extends ThaumcraftBoss implements RangedAttackMob,thaumcraft.api.entities.IEldritchMob {
    private static final EntityDataAccessor<Integer> TITLE=SynchedEntityData.defineId(EldritchWarden.class,EntityDataSerializers.INT);
    private static final TagKey<EntityType<?>> CULTISTS=TagKey.create(Registries.ENTITY_TYPE,ResourceLocation.parse("thaumcraft:cultists"));
    private static final String[] TITLES={"Aphoom-Zhah","Basatan","Chaugnar Faugn","Mnomquah","Nyogtha","Oorn","Shaikorth","Rhan-Tegoth","Rhogog","Shudde M'ell","Vulthoom","Yag-Kosha","Yibb-Tstll","Zathog","Zushakon"};
    private boolean fieldFrenzy,lastBlast;
    private int fieldFrenzyCounter;
    public float armLiftL,armLiftR;
    public static java.util.function.Consumer<EldritchWarden> clientEffects=warden->{};
    public EldritchWarden(EntityType<? extends EldritchWarden> type,Level level){super(type,level);if(getNavigation() instanceof GroundPathNavigation ground)ground.setCanOpenDoors(true);}
    public static AttributeSupplier.Builder attributes(){return bossAttributes().add(Attributes.MAX_HEALTH,200).add(Attributes.ATTACK_DAMAGE,10).add(Attributes.MOVEMENT_SPEED,.33).add(Attributes.ARMOR,4).add(Attributes.MAX_ABSORPTION,200);}
    @Override protected void defineSynchedData(SynchedEntityData.Builder data){super.defineSynchedData(data);data.define(TITLE,0);}
    public int getTitleIndex(){return entityData.get(TITLE);}
    public String getTitle(){return TITLES[Math.floorMod(getTitleIndex(),TITLES.length)];}
    @Override public void generateChampionName(int modifier){setCustomName(net.minecraft.network.chat.Component.translatable("entity.thaumcraft.eldritch_warden.champion",getTitle(),net.minecraft.network.chat.Component.translatable("champion.mod."+ChampionSystem.NAMES[modifier])));}
    public void setTitle(int title){entityData.set(TITLE,title);}
    public int getFieldFrenzyCounter(){return fieldFrenzyCounter;}
    private boolean legacyAir(BlockPos pos){var state=level().getBlockState(pos);return state.isAir()||state.is(R5Content.ELDRITCH_FIELD.get());}
    @Override protected void registerGoals(){
        goalSelector.addGoal(0,new FloatGoal(this));
        goalSelector.addGoal(2,new RangedAttackGoal(this,1,20,40,24){@Override public boolean canUse(){if(!super.canUse())return false;var target=getTarget();if(target==null)return false;if(!target.isAlive()){setTarget(null);return false;}return distanceToSqr(target.getX(),target.getBoundingBox().minY,target.getZ())>=9;}});
        goalSelector.addGoal(3,new MeleeAttackGoal(this,1.1,false){
            private int attackTicks;
            @Override public void tick(){if(attackTicks>0)--attackTicks;super.tick();}
            @Override protected boolean canPerformAttack(LivingEntity target){float width=getBbWidth();return attackTicks<=0&&distanceToSqr(target.getX(),target.getBoundingBox().minY,target.getZ())<=width*2f*width*2f+target.getBbWidth();}
            @Override protected void checkAndPerformAttack(LivingEntity target){if(canPerformAttack(target)){attackTicks=10;if(!getMainHandItem().isEmpty())swing(net.minecraft.world.InteractionHand.MAIN_HAND);doHurtTarget(target);}}
        });
        goalSelector.addGoal(5,new MoveTowardsRestrictionGoal(this,.8));goalSelector.addGoal(7,new RandomStrollGoal(this,1));goalSelector.addGoal(8,new LookAtPlayerGoal(this,Player.class,8));goalSelector.addGoal(8,new RandomLookAroundGoal(this));
        targetSelector.addGoal(1,new HurtByTargetGoal(this));targetSelector.addGoal(2,new NearestAttackableTargetGoal<>(this,Player.class,true));targetSelector.addGoal(3,new NearestAttackableTargetGoal<>(this,LivingEntity.class,10,true,false,e->e.getType().is(CULTISTS)));
    }
    @Override public boolean isEffectiveAi(){return fieldFrenzyCounter==0&&super.isEffectiveAi();}
    @Override public boolean isInvulnerableTo(DamageSource source){return fieldFrenzyCounter>0||super.isInvulnerableTo(source);}
    @Override public boolean hurt(DamageSource source,float amount){if(isInvulnerableTo(source)||source.is(DamageTypes.DROWN)||source.is(DamageTypes.WITHER))return false;boolean hurt=super.hurt(source,amount);if(!level().isClientSide&&hurt&&!fieldFrenzy&&getAbsorptionAmount()<=0){fieldFrenzy=true;fieldFrenzyCounter=150;}return hurt;}
    @Override public SpawnGroupData finalizeSpawn(ServerLevelAccessor world,net.minecraft.world.DifficultyInstance difficulty,MobSpawnType reason,SpawnGroupData group){spawnTimer=150;setTitle(random.nextInt(TITLES.length));setAbsorptionAmount((float)(getAbsorptionAmount()+getAttribute(Attributes.MAX_HEALTH).getBaseValue()*.66));return super.finalizeSpawn(world,difficulty,reason,group);}
    @Override public void tick(){
        if(getSpawnTimer()==150)level().broadcastEntityEvent(this,(byte)18);super.tick();
        if(level().isClientSide){if(armLiftL>0)armLiftL-=.05f;if(armLiftR>0)armLiftR-=.05f;clientEffects.accept(this);}
        else if(invulnerableTime<=0&&tickCount%25==0&&getAbsorptionAmount()<(int)(getAttribute(Attributes.MAX_HEALTH).getBaseValue()*.66))setAbsorptionAmount(getAbsorptionAmount()+1);
    }
    @Override public void aiStep(){super.aiStep();for(int l=0;l<4;l++){var p=BlockPos.containing(getX()+(l%2*2-1)*.25f,getY(),getZ()+(l/2%2*2-1)*.25f);if(legacyAir(p))level().setBlock(p,R5Content.ELDRITCH_FIELD.get().defaultBlockState(),3);}if(!level().isClientSide&&fieldFrenzyCounter>0){if(fieldFrenzyCounter==150)teleportHome();performFieldFrenzy();}}
    private void performFieldFrenzy(){
        if(fieldFrenzyCounter<121&&fieldFrenzyCounter%10==0){
            level().broadcastEntityEvent(this,(byte)17);double radius=(150-fieldFrenzyCounter)/8.0;int step=1+fieldFrenzyCounter/8;var center=blockPosition();
            for(int q=0;q<180/step;q++){double radians=Math.toRadians(q*2*step);var p=center.offset((int)(radius*Math.cos(radians)),0,(int)(radius*Math.sin(radians)));if(!legacyAir(p)||!level().getBlockState(p.below()).isSolidRender(level(),p.below()))continue;
                level().setBlock(p,R5Content.ELDRITCH_FIELD.get().defaultBlockState(),3);level().scheduleTick(p,R5Content.ELDRITCH_FIELD.get(),250+random.nextInt(150));
                net.minecraft.network.protocol.common.custom.CustomPacketPayload packet=random.nextFloat()<.3f?new BlockArcPayload(p,getId()):new BlockSparklePayload(p,0x800080);PacketDistributor.sendToPlayersNear((ServerLevel)level(),null,p.getX(),p.getY(),p.getZ(),32,packet);
            }playSound(R2Sounds.ZAP.get(),1,.9f+random.nextFloat()*.1f);
        }--fieldFrenzyCounter;
    }
    protected void teleportHome(){
        var home=getRestrictCenter();var event=new EntityTeleportEvent.EnderEntity(this,home.getX(),home.getY(),home.getZ());NeoForge.EVENT_BUS.post(event);if(event.isCanceled())return;
        Vec3 prior=position();var target=event.getTarget();int x=Mth.floor(target.x),y=Mth.floor(target.y),z=Mth.floor(target.z);boolean success=false;
        if(level().hasChunkAt(new BlockPos(x,y,z))){int tries=20;boolean ground=false;while(!ground&&tries>0){var p=new BlockPos(x,y,z);if(level().getBlockState(p.below()).blocksMotion()&&!level().getBlockState(p).blocksMotion())ground=true;else{x=Mth.floor(target.x)+random.nextInt(8)-random.nextInt(8);z=Mth.floor(target.z)+random.nextInt(8)-random.nextInt(8);--tries;}}if(ground){setPos(x+.5,y+.1,z+.5);success=level().noCollision(this,getBoundingBox());}}
        if(!success){setPos(prior);return;}
        for(int l=0;l<128;l++){double fraction=l/127.0;float vx=(random.nextFloat()-.5f)*.2f,vy=(random.nextFloat()-.5f)*.2f,vz=(random.nextFloat()-.5f)*.2f;double px=prior.x+(getX()-prior.x)*fraction+(random.nextDouble()-.5)*getBbWidth()*2,py=prior.y+(getY()-prior.y)*fraction+random.nextDouble()*getBbHeight(),pz=prior.z+(getZ()-prior.z)*fraction+(random.nextDouble()-.5)*getBbWidth()*2;((ServerLevel)level()).sendParticles(ParticleTypes.PORTAL,px,py,pz,0,vx,vy,vz,1);}
        level().playSound(null,prior.x,prior.y,prior.z,SoundEvents.ENDERMAN_TELEPORT,getSoundSource(),1,1);playSound(SoundEvents.ENDERMAN_TELEPORT,1,1);
    }
    @Override public void performRangedAttack(LivingEntity target,float strength){if(level().isClientSide)return;if(random.nextFloat()>.2f)shootOrb(target);else if(hasLineOfSight(target))sonicAttack(target);}
    public void shootOrb(LivingEntity target){if(level().isClientSide)return;var orb=new EldritchOrb(level(),this);lastBlast=!lastBlast;level().broadcastEntityEvent(this,lastBlast?(byte)16:(byte)15);float angle=(getYRot()+(lastBlast?90:180))%360/180*(float)Math.PI;orb.setPos(orb.getX()-Mth.cos(angle)*.5f,orb.getY()-.13,orb.getZ()-Mth.sin(angle)*.5f);orb.shoot(target.getX()+target.getDeltaMovement().x-getX(),target.getY()-getY()-target.getBbHeight()/2f,target.getZ()+target.getDeltaMovement().z-getZ(),1,2);playSound(R2Sounds.GUARDIAN_ATTACK.get(),2,1+random.nextFloat()*.1f);level().addFreshEntity(orb);}
    public void sonicAttack(LivingEntity target){if(level().isClientSide)return;PacketDistributor.sendToPlayersNear((ServerLevel)level(),null,getX(),getY(),getZ(),32,new SonicPayload(getId()));target.push(-Mth.sin(getYRot()*(float)Math.PI/180)*1.5f,.1,Mth.cos(getYRot()*(float)Math.PI/180)*1.5f);target.addEffect(new MobEffectInstance(MobEffects.WITHER,400));target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS,400));if(target instanceof ServerPlayer player){int amount=3+level().random.nextInt(3);KnowledgeService.mutate(player,k->{k.setWarp(k.permanentWarp(),k.stickyWarp(),k.temporaryWarp()+amount,k.warpCounter());return true;});}playSound(R2Sounds.GUARDIAN_SCREECH.get(),4,1+random.nextFloat()*.1f);}
    @Override public void handleEntityEvent(byte event){if(event==15)armLiftL=.5f;else if(event==16)armLiftR=.5f;else if(event==17){armLiftL=.9f;armLiftR=.9f;}else if(event==18)spawnTimer=150;else super.handleEntityEvent(event);}
    @Override public boolean canAttackType(EntityType<?> type){return type!=R2Entities.ELDRITCH_GUARDIAN.get()&&super.canAttackType(type);}
    @Override public void addAdditionalSaveData(CompoundTag tag){super.addAdditionalSaveData(tag);tag.putByte("title",(byte)getTitleIndex());}
    @Override public void readAdditionalSaveData(CompoundTag tag){super.readAdditionalSaveData(tag);setTitle(tag.getByte("title"));}
    @Override protected SoundEvent getAmbientSound(){return R2Sounds.GUARDIAN_IDLE.get();}
    @Override protected SoundEvent getDeathSound(){return R2Sounds.GUARDIAN_DEATH.get();}
    @Override public int getAmbientSoundInterval(){return 500;}
}
