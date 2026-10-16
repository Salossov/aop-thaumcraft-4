package thaumcraft.entities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import thaumcraft.registry.*;

/** Shared original crimson cultist drops, home and cross-class retaliation. */
public abstract class Cultist extends Monster {
    private static final TagKey<EntityType<?>> FAMILY=TagKey.create(Registries.ENTITY_TYPE,ResourceLocation.parse("thaumcraft:cultists"));
    protected Cultist(EntityType<? extends Cultist> type,Level level){super(type,level);xpReward=10;if(getNavigation() instanceof GroundPathNavigation ground){ground.setCanOpenDoors(true);ground.setCanPassDoors(true);}}
    public static AttributeSupplier.Builder attributes(){return Monster.createMonsterAttributes().add(Attributes.FOLLOW_RANGE,32).add(Attributes.MOVEMENT_SPEED,.3).add(Attributes.ATTACK_DAMAGE,4);}
    @Override public boolean canPickUpLoot(){return false;}
    @Override public boolean isAlliedTo(Entity entity){return entity instanceof Cultist||entity.getType().is(FAMILY)||super.isAlliedTo(entity);}
    @Override protected void dropCustomDeathLoot(ServerLevel level,DamageSource source,boolean recentlyHit){super.dropCustomDeathLoot(level,source,recentlyHit);int looting=source.getEntity() instanceof LivingEntity killer?EnchantmentHelper.getEnchantmentLevel(level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.LOOTING),killer):0;int roll=random.nextInt(10);if(roll==0)spawnAtLocation(new ItemStack(R3Equipment.KNOWLEDGE_FRAGMENT.get()),1.5f);else if(roll<=1)spawnAtLocation(new ItemStack(R5Content.LOOT_BAGS.get(1).get()),1.5f);else if(roll<=3+looting)spawnAtLocation(new ItemStack(R5Content.LOOT_BAGS.get(0).get()),1.5f);if(recentlyHit&&random.nextFloat()<.025f+looting*.01f)spawnAtLocation(new ItemStack(R3Equipment.CRIMSON_RITES.get()),1);}
    @Override public void addAdditionalSaveData(CompoundTag tag){super.addAdditionalSaveData(tag);if(hasRestriction()&&getRestrictRadius()>0){tag.putInt("HomeD",(int)getRestrictRadius());tag.putInt("HomeX",getRestrictCenter().getX());tag.putInt("HomeY",getRestrictCenter().getY());tag.putInt("HomeZ",getRestrictCenter().getZ());}}
    @Override public void readAdditionalSaveData(CompoundTag tag){super.readAdditionalSaveData(tag);if(tag.contains("HomeD"))restrictTo(new BlockPos(tag.getInt("HomeX"),tag.getInt("HomeY"),tag.getInt("HomeZ")),tag.getInt("HomeD"));}
    /** Original attack cadence and reach; modern vanilla changed both. */
    static class Melee extends net.minecraft.world.entity.ai.goal.Goal {
        private final net.minecraft.world.entity.PathfinderMob owner;private final double speed;private int attackTicks,delay,penalty;private double lastX,lastY,lastZ;private net.minecraft.world.level.pathfinder.Path path;
        Melee(net.minecraft.world.entity.PathfinderMob owner,double speed){this.owner=owner;this.speed=speed;setFlags(java.util.EnumSet.of(Flag.MOVE,Flag.LOOK));}
        @Override public boolean canUse(){var target=owner.getTarget();if(target==null||!target.isAlive())return false;if(--delay<=0){path=owner.getNavigation().createPath(target,0);delay=4+owner.getRandom().nextInt(7);return path!=null;}return true;}
        @Override public boolean canContinueToUse(){var target=owner.getTarget();return target!=null&&target.isAlive()&&!owner.getNavigation().isDone();}
        @Override public void start(){owner.getNavigation().moveTo(path,speed);delay=0;}
        @Override public void stop(){owner.getNavigation().stop();}
        @Override public void tick(){var target=owner.getTarget();if(target==null)return;owner.getLookControl().setLookAt(target,30,30);double distance=owner.distanceToSqr(target.getX(),target.getBoundingBox().minY,target.getZ()),reach=owner.getBbWidth()*2f*owner.getBbWidth()*2f+target.getBbWidth();--delay;if(attackTicks>0)--attackTicks;if(owner.getSensing().hasLineOfSight(target)&&delay<=0&&(lastX==0&&lastY==0&&lastZ==0||target.distanceToSqr(lastX,lastY,lastZ)>=1||owner.getRandom().nextFloat()<.05f)){lastX=target.getX();lastY=target.getBoundingBox().minY;lastZ=target.getZ();delay=penalty+4+owner.getRandom().nextInt(7);var current=owner.getNavigation().getPath();var end=current==null?null:current.getEndNode();penalty=end!=null&&target.distanceToSqr(end.x,end.y,end.z)<1?0:penalty+10;if(distance>1024)delay+=10;else if(distance>256)delay+=5;if(!owner.getNavigation().moveTo(target,speed))delay+=15;}if(distance<=reach&&attackTicks<=0){attackTicks=10;if(!owner.getMainHandItem().isEmpty())owner.swing(net.minecraft.world.InteractionHand.MAIN_HAND);owner.doHurtTarget(target);}}
    }
    static final class Retaliate extends HurtByTargetGoal {
        private final Cultist cultist;
        Retaliate(Cultist cultist){super(cultist);this.cultist=cultist;}
        @Override public void start(){super.start();var attacker=cultist.getLastHurtByMob();if(attacker==null)return;double range=cultist.getAttributeValue(Attributes.FOLLOW_RANGE);var bounds=new AABB(cultist.getX(),cultist.getY(),cultist.getZ(),cultist.getX()+1,cultist.getY()+1,cultist.getZ()+1).inflate(range,10,range);for(var ally:cultist.level().getEntitiesOfClass(Cultist.class,bounds)){if(ally==cultist||ally.getTarget()!=null||ally.isAlliedTo(attacker))continue;if(ally instanceof CultistCleric cleric&&cleric.isRitualist()){if(cultist.level().random.nextInt(3)!=0)continue;cleric.setRitualist(false);}ally.setTarget(attacker);}}
    }
}
