package thaumcraft.entities;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.*;
import net.minecraft.server.level.*;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.*;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.Vec3;

/** A hopping, owner-bound inventory; the six upgrades retain their TC4 indices. */
public final class TravelingTrunk extends PathfinderMob implements MenuProvider {
    private static final EntityDataAccessor<Integer> UPGRADE=SynchedEntityData.defineId(TravelingTrunk.class,EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> STAY=SynchedEntityData.defineId(TravelingTrunk.class,EntityDataSerializers.BOOLEAN),OPEN=SynchedEntityData.defineId(TravelingTrunk.class,EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> ANGER=SynchedEntityData.defineId(TravelingTrunk.class,EntityDataSerializers.INT);
    public final SimpleContainer inventory=new SimpleContainer(36);
    private java.util.UUID owner;
    private int viewers,jumpDelay,lastEatingTick=-1;
    public float lid,oldLid,squish,oldSquish;
    public TravelingTrunk(EntityType<? extends TravelingTrunk> type,Level level){super(type,level);setPersistenceRequired();}
    public static AttributeSupplier.Builder attributes(){return Mob.createMobAttributes().add(Attributes.MAX_HEALTH,75).add(Attributes.MOVEMENT_SPEED,.3).add(Attributes.ATTACK_DAMAGE,4).add(Attributes.FOLLOW_RANGE,32);}
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder){super.defineSynchedData(builder);builder.define(UPGRADE,-1);builder.define(STAY,false);builder.define(OPEN,false);builder.define(ANGER,0);}
    @Override protected void registerGoals(){goalSelector.addGoal(0,new FloatGoal(this));goalSelector.addGoal(1,new MeleeAttackGoal(this,1.2,true){@Override public boolean canUse(){return upgrade()==2&&super.canUse();}});goalSelector.addGoal(2,new Goal(){ {setFlags(java.util.EnumSet.of(Flag.MOVE,Flag.LOOK));} @Override public boolean canUse(){var owner=owner();return viewers==0&&!stay()&&owner!=null&&owner.level()==level()&&distanceToSqr(owner)>25&&getTarget()==null;}@Override public void tick(){var owner=owner();if(owner==null)return;getLookControl().setLookAt(owner,10,20);getNavigation().moveTo(owner,upgrade()==0?1.33:1);}});}
    public Player owner(){return owner==null?null:level() instanceof ServerLevel server?server.getServer().getPlayerList().getPlayer(owner):level().getPlayerByUUID(owner);}
    public void owner(Player player){owner=player.getUUID();}
    public int upgrade(){return entityData.get(UPGRADE);}
    public int rows(){return upgrade()==1?4:3;}
    public boolean stay(){return entityData.get(STAY);}
    public void toggleStay(){entityData.set(STAY,!stay());getNavigation().stop();}
    public int anger(){return entityData.get(ANGER);}
    public boolean allowed(Player player){return upgrade()!=3||owner==null||owner.equals(player.getUUID());}
    public void configure(ItemStack stack,Player player){owner(player);var tag=stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();entityData.set(UPGRADE,tag.contains("upgrade")?Math.max(-1,Math.min(5,tag.getByte("upgrade"))):-1);thaumcraft.production.ContainerNbt.load(inventory,tag.getList("inventory",Tag.TAG_COMPOUND),registryAccess());if(stack.has(DataComponents.CUSTOM_NAME))setCustomName(stack.getHoverName());}
    public void open(){viewers++;entityData.set(OPEN,true);playSound(SoundEvents.CHEST_OPEN,.5f,.9f+random.nextFloat()*.1f);}
    public void close(){viewers=Math.max(0,viewers-1);entityData.set(OPEN,viewers>0);playSound(SoundEvents.CHEST_CLOSE,.5f,.9f+random.nextFloat()*.1f);}
    @Override public Component getDisplayName(){return getName();}
    @Override public AbstractContainerMenu createMenu(int id,Inventory inventory,Player player){return allowed(player)?new thaumcraft.menu.TrunkMenu(id,inventory,this):null;}
    @Override protected InteractionResult mobInteract(Player player,InteractionHand hand){var stack=player.getItemInHand(hand);if(!allowed(player))return InteractionResult.FAIL;if(stack.getItem() instanceof thaumcraft.items.GolemUpgradeItem item&&upgrade()<0){if(!level().isClientSide){entityData.set(UPGRADE,item.kind);stack.consume(1,player);}return InteractionResult.sidedSuccess(level().isClientSide);}var food=stack.get(DataComponents.FOOD);if(food!=null&&getHealth()<getMaxHealth()){if(!level().isClientSide){heal(food.nutrition());stack.consume(1,player);chew(true);playSound(SoundEvents.GENERIC_EAT);}return InteractionResult.sidedSuccess(level().isClientSide);}if(!level().isClientSide)player.openMenu(this,buffer->buffer.writeVarInt(getId()));return InteractionResult.sidedSuccess(level().isClientSide);}
    @Override public void tick(){super.tick();oldLid=lid;oldSquish=squish;squish*=.8f;if(!onGround()&&getDeltaMovement().y<0&&!isInWater())lid+=.015f;if((onGround()||isInWater())&&!entityData.get(OPEN))lid=Math.max(0,lid-.1f);if(entityData.get(OPEN))lid+=.035f;lid=Math.min(lid,entityData.get(OPEN)?.5f:.2f);
        if(level().isClientSide)return;if(anger()>0)entityData.set(ANGER,anger()-1);if(tickCount%(upgrade()==3?1:50)==0)heal(1);
        var player=owner();if(player!=null&&!stay()&&player.level()!=level()&&level() instanceof ServerLevel){changeDimension(new DimensionTransition((ServerLevel)player.level(),player.position().add(1,0,0),Vec3.ZERO,player.getYRot(),0,DimensionTransition.DO_NOTHING));return;}
        if(viewers==0&&player!=null&&player.level()==level()&&!stay()&&distanceToSqr(player)>400&&tickCount%20==0){for(int x=-2;x<=2;x++)for(int z=-2;z<=2;z++){if(Math.abs(x)<2&&Math.abs(z)<2)continue;var pos=player.blockPosition().offset(x,0,z);if(level().hasChunkAt(pos)&&level().getBlockState(pos).getCollisionShape(level(),pos).isEmpty()&&level().getBlockState(pos.above()).getCollisionShape(level(),pos.above()).isEmpty()&&(level().getBlockState(pos.below()).isSolidRender(level(),pos.below())||level().getFluidState(pos.below()).isSource())){teleportTo(pos.getX()+.5,pos.getY(),pos.getZ()+.5);getNavigation().stop();x=3;break;}}}
        if(upgrade()==2&&player!=null&&player.getLastHurtByMob()!=null&&player.getLastHurtByMob().isAlive()&&player.tickCount-player.getLastHurtByMobTimestamp()<100){setTarget(player.getLastHurtByMob());entityData.set(ANGER,600);}if(anger()==0)setTarget(null);
        if(viewers==0&&!getNavigation().isDone()&&(onGround()||isInWater())&&--jumpDelay<=0){jumpDelay=(random.nextInt(10)+5)/3;getJumpControl().jump();squish=1;}
        if(upgrade()==0&&!onGround()&&getDeltaMovement().y<0)setDeltaMovement(getDeltaMovement().add(0,.015,0));
        if(upgrade()==5&&viewers==0)for(var item:level().getEntitiesOfClass(ItemEntity.class,getBoundingBox().inflate(3),ItemEntity::isAlive)){if(distanceToSqr(item)>.5){item.setDeltaMovement(item.getDeltaMovement().add(position().add(0,.25,0).subtract(item.position()).normalize().scale(.06)));}else{var rest=item.getItem().copy();rest=net.neoforged.neoforge.items.ItemHandlerHelper.insertItemStacked(new net.neoforged.neoforge.items.wrapper.RangedWrapper(new net.neoforged.neoforge.items.wrapper.InvWrapper(inventory),0,rows()*9),rest,false);if(rest.getCount()!=item.getItem().getCount()){if(rest.isEmpty())item.discard();else item.setItem(rest);chew(false);playSound(SoundEvents.GENERIC_EAT,.25f,1);}}}
    }
    private void chew(boolean fed){if(lastEatingTick==tickCount)return;lastEatingTick=tickCount;level().broadcastEntityEvent(this,(byte)(fed?18:17));}
    @Override public void handleEntityEvent(byte event){if(event==17||event==18){oldLid=lid;lid=.15f;if(event==18)for(int i=0;i<7;i++)level().addParticle(net.minecraft.core.particles.ParticleTypes.HEART,getRandomX(1),getRandomY()+.5,getRandomZ(1),0,.02,0);}else super.handleEntityEvent(event);}
    @Override public boolean hurt(DamageSource source,float damage){return upgrade()!=3&&!source.is(net.minecraft.tags.DamageTypeTags.IS_FALL)&&super.hurt(source,damage);}
    @Override protected void dropCustomDeathLoot(ServerLevel level,DamageSource source,boolean recentlyHit){super.dropCustomDeathLoot(level,source,recentlyHit);dropInventory();}
    private void dropInventory(){for(int i=0;i<inventory.getContainerSize();i++){var stack=inventory.removeItemNoUpdate(i);if(!stack.isEmpty())spawnAtLocation(stack);}}
    public void dismantle(Player player){if(level().isClientSide||!allowed(player))return;var stack=new ItemStack(thaumcraft.registry.R3Equipment.TRAVELING_TRUNK.get());var tag=new CompoundTag();if(upgrade()>=0){if(player.isShiftKeyDown()){if(random.nextBoolean())spawnAtLocation(new ItemStack(thaumcraft.registry.R3Equipment.GOLEM_UPGRADES.get(upgrade()).get()));}else tag.putByte("upgrade",(byte)upgrade());}if(upgrade()==4&&!player.isShiftKeyDown())tag.put("inventory",thaumcraft.production.ContainerNbt.save(inventory,registryAccess()));else dropInventory();stack.set(DataComponents.CUSTOM_DATA,CustomData.of(tag));if(hasCustomName()&&!player.isShiftKeyDown())stack.set(DataComponents.CUSTOM_NAME,getCustomName());spawnAtLocation(stack);playSound(thaumcraft.registry.R2Sounds.ZAP.get(),.5f,1);discard();}
    @Override public void addAdditionalSaveData(CompoundTag tag){super.addAdditionalSaveData(tag);if(owner!=null)tag.putUUID("Owner",owner);tag.putByte("Upgrade",(byte)upgrade());tag.putBoolean("Stay",stay());tag.putInt("Anger",anger());tag.put("Inventory",thaumcraft.production.ContainerNbt.save(inventory,registryAccess()));}
    @Override public void readAdditionalSaveData(CompoundTag tag){super.readAdditionalSaveData(tag);owner=tag.hasUUID("Owner")?tag.getUUID("Owner"):null;entityData.set(UPGRADE,tag.contains("Upgrade")?Math.max(-1,Math.min(5,tag.getByte("Upgrade"))):-1);entityData.set(STAY,tag.getBoolean("Stay"));entityData.set(ANGER,tag.getInt("Anger"));thaumcraft.production.ContainerNbt.load(inventory,tag.getList("Inventory",Tag.TAG_COMPOUND),registryAccess());}
}
