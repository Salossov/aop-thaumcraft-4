package thaumcraft.entities;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.level.Level;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.neoforged.neoforge.entity.IEntityWithComplexSpawn;

/** Source EntityDart: an arrow with the golem launch arc, original dispersion and muzzle smoke. */
public final class Dart extends Arrow implements IEntityWithComplexSpawn {
    private boolean first=true;
    public Dart(EntityType<? extends Dart> type,Level level){super(type,level);pickup=Pickup.DISALLOWED;}
    public Dart(Level level,ThaumicGolem shooter,LivingEntity target){
        this(R3Entities.DART.get(),level);setOwner(shooter);
        double y=shooter.getY()+shooter.getEyeHeight()-(double).1f;
        double dx=target.getX()-shooter.getX(),dy=target.getY()+target.getEyeHeight()-(double).7f-y,dz=target.getZ()-shooter.getZ();
        double horizontal=(float)Math.sqrt(dx*dx+dz*dz);setPos(shooter.getX(),y,shooter.getZ());
        if(horizontal>=1e-7){
            setPos(shooter.getX()+dx/horizontal/5,y,shooter.getZ()+dz/horizontal/5);
            shoot(dx,dy+(double)((float)horizontal*.2f),dz,1.6f,7-shooter.upgrade(3)*1.75f);
        }
        setBaseDamage((float)shooter.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE)*.4f);
    }
    @Override protected double getDefaultGravity(){return (double).05f;}
    @Override protected float getWaterInertia(){return .8f;}
    @Override public void shoot(double x,double y,double z,float speed,float inaccuracy){
        LegacyArrowLaunch.shoot(this,x,y,z,speed,inaccuracy);
    }
    @Override public void writeSpawnData(RegistryFriendlyByteBuf buffer){var velocity=getDeltaMovement();buffer.writeDouble(velocity.x);buffer.writeDouble(velocity.y);buffer.writeDouble(velocity.z);buffer.writeFloat(getYRot());buffer.writeFloat(getXRot());}
    @Override public void readSpawnData(RegistryFriendlyByteBuf buffer){setDeltaMovement(buffer.readDouble(),buffer.readDouble(),buffer.readDouble());setYRot(buffer.readFloat());setXRot(buffer.readFloat());yRotO=getYRot();xRotO=getXRot();}
    @Override public void tick(){
        if(first&&level().isClientSide){
            first=false;var velocity=getDeltaMovement();
            for(int i=0;i<5;i++)level().addParticle(ParticleTypes.SMOKE,getX()-velocity.x/1.5,getY()-velocity.y/1.5,getZ()-velocity.z/1.5,
                velocity.x/9+random.nextGaussian()*.01,velocity.y/9+random.nextGaussian()*.01,velocity.z/9+random.nextGaussian()*.01);
        }
        super.tick();
    }
}
