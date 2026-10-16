package thaumcraft.entities;

import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;

/** Owned 1.7.10 EntityThrowable launch arithmetic, including float normalisation and Gaussian scatter. */
final class LegacyThrowableLaunch {
    static void shoot(Projectile projectile,RandomSource random,double x,double y,double z,float speed,float scatter){
        double length=(float)Math.sqrt(x*x+y*y+z*z);if(length==0){projectile.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);return;}
        x=(x/length+random.nextGaussian()*(double).0075f*scatter)*speed;
        y=(y/length+random.nextGaussian()*(double).0075f*scatter)*speed;
        z=(z/length+random.nextGaussian()*(double).0075f*scatter)*speed;
        projectile.setDeltaMovement(x,y,z);double horizontal=(float)Math.sqrt(x*x+z*z);
        projectile.setYRot((float)(Math.atan2(x,z)*180/(double)(float)Math.PI));projectile.setXRot((float)(Math.atan2(y,horizontal)*180/(double)(float)Math.PI));
        projectile.yRotO=projectile.getYRot();projectile.xRotO=projectile.getXRot();
    }
    static void owner(Projectile projectile,LivingEntity owner,float speed,float pitchOffset){
        projectile.setOwner(owner);float yaw=owner.getYRot()/180f*(float)Math.PI,pitch=owner.getXRot()/180f*(float)Math.PI;
        projectile.setPos(owner.getX()-(double)(Mth.cos(yaw)*.16f),owner.getEyeY()-(double).1f,owner.getZ()-(double)(Mth.sin(yaw)*.16f));
        projectile.shoot((double)(-Mth.sin(yaw)*Mth.cos(pitch)*.4f),(double)(-Mth.sin((owner.getXRot()+pitchOffset)/180f*(float)Math.PI)*.4f),(double)(Mth.cos(yaw)*Mth.cos(pitch)*.4f),speed,1);
    }
    private LegacyThrowableLaunch(){}
}
