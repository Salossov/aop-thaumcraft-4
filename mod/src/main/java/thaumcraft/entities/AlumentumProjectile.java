package thaumcraft.entities;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import thaumcraft.registry.R3Content;
public final class AlumentumProjectile extends ThrowableItemProjectile {
 public static java.util.function.Consumer<AlumentumProjectile> clientTrail=entity->{};
 public AlumentumProjectile(EntityType<? extends AlumentumProjectile> type,Level level){super(type,level);}
 public void launch(net.minecraft.world.entity.LivingEntity owner){LegacyThrowableLaunch.owner(this,owner,.75f,0);}
 @Override public void shoot(double x,double y,double z,float speed,float scatter){LegacyThrowableLaunch.shoot(this,random,x,y,z,speed,scatter);}
 @Override protected Item getDefaultItem(){return R3Content.ALUMENTUM.get();}
 @Override protected void onHit(HitResult hit){super.onHit(hit);if(!level().isClientSide){level().explode(null,getX(),getY(),getZ(),1.66f,Level.ExplosionInteraction.MOB);discard();}}
 @Override public void tick(){super.tick();if(level().isClientSide)clientTrail.accept(this);}
 @Override public float getLightLevelDependentMagicValue(){return .1f;}
}
