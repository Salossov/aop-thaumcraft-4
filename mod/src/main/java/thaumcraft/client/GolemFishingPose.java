package thaumcraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import thaumcraft.entities.ThaumicGolem;

/** The rod and the line share the same tip, including the animated right arm. */
final class GolemFishingPose {
    static void rod(PoseStack pose,float armPitch){
        pose.mulPose(Axis.XP.rotationDegrees(5+90*armPitch/(float)Math.PI));
        pose.translate(-.26875,1.25,-.53);
        pose.mulPose(Axis.YP.rotationDegrees(-90));pose.mulPose(Axis.ZP.rotationDegrees(-30));
        pose.scale(.66f,-.66f,.66f);pose.translate(.5,.5,0);
    }
    static Vec3 tip(ThaumicGolem golem,float partial){
        float age=golem.tickCount+partial,amount=golem.walkAnimation.speed(partial),swing=golem.walkAnimation.position(partial);
        float pitch=golem.action()>0?-2+1.5f*wave(3-Math.abs(Math.max(0,golem.action()-(age-golem.tickCount))-3),5):!golem.displayCarried().isEmpty()?-1:(-.2f+1.5f*wave(swing,13))*amount;
        var pose=new PoseStack();rod(pose,pitch);var p=new org.joml.Vector3f(.375f,.375f,0).mulPosition(pose.last().pose());
        // LivingEntityRenderer flips model X/Y and places its origin 1.501 above the feet.
        var offset=new Vec3(-p.x,1.501-p.y,p.z).yRot((180-Mth.rotLerp(partial,golem.yBodyRotO,golem.yBodyRot))*Mth.DEG_TO_RAD);
        return golem.getPosition(partial).add(offset);
    }
    private static float wave(float phase,float period){return (Math.abs(phase%period-period*.5f)-period*.25f)/(period*.25f);}
    private GolemFishingPose(){}
}
