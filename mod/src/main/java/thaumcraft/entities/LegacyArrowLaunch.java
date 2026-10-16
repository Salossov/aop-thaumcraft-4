package thaumcraft.entities;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.Vec3;

/** Owned 1.7.10 EntityArrow arithmetic; its scatter also consumes a sign draw per axis. */
public final class LegacyArrowLaunch {
    public static void shoot(Projectile projectile,double x,double y,double z,float speed,float scatter){
        float length=(float)Math.sqrt(x*x+y*y+z*z);
        if(length==0||!Float.isFinite(length)){projectile.setDeltaMovement(Vec3.ZERO);return;}
        var random=projectile.getRandom();
        x=(x/length+random.nextGaussian()*(random.nextBoolean()?-1:1)*(double).0075f*scatter)*speed;
        y=(y/length+random.nextGaussian()*(random.nextBoolean()?-1:1)*(double).0075f*scatter)*speed;
        z=(z/length+random.nextGaussian()*(random.nextBoolean()?-1:1)*(double).0075f*scatter)*speed;
        projectile.setDeltaMovement(x,y,z);projectile.hasImpulse=true;
        float horizontal=(float)Math.sqrt(x*x+z*z);
        projectile.setYRot((float)(Math.atan2(x,z)*180/(double)(float)Math.PI));
        projectile.setXRot((float)(Math.atan2(y,horizontal)*180/(double)(float)Math.PI));
        projectile.yRotO=projectile.getYRot();projectile.xRotO=projectile.getXRot();
    }
    public static void owner(Projectile projectile,LivingEntity owner,float speed,float yawOffset){
        float yaw=(owner.getYRot()+yawOffset)/180f*(float)Math.PI,pitch=owner.getXRot()/180f*(float)Math.PI;
        shoot(projectile,(double)(-Mth.sin(yaw)*Mth.cos(pitch)),(double)(-Mth.sin(pitch)),(double)(Mth.cos(yaw)*Mth.cos(pitch)),speed,1);
    }
    private LegacyArrowLaunch(){}
}
