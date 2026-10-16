package thaumcraft.effects;

import net.minecraft.world.phys.Vec3;

/** Shared TC4 homing path and arrival timing for the server's infusion phase boundary. */
public final class EssentiaFlow {
    public static final int SOURCE_TICKS=12;
    public static Vec3 initialVelocity(float phase){return new Vec3(Math.sin(phase/4f)*.015,.1+Math.sin(phase/3f)*.01,Math.sin(phase/2f)*.015);}
    public static Vec3 nextVelocity(Vec3 position,Vec3 target,Vec3 velocity){
        var delta=target.subtract(position);double distance=delta.length();if(distance<.12)return Vec3.ZERO;
        double acceleration=.01/Math.min(1,distance);var v=velocity.scale(.985).add(delta.scale(acceleration/distance)).add(0,.002,0);
        return new Vec3(Math.clamp(v.x,-.05,.05),Math.clamp(v.y,-.05,.05),Math.clamp(v.z,-.05,.05));
    }
    public static int flightTicks(Vec3 from,Vec3 target,float phase){
        var position=from;var velocity=initialVelocity(phase);
        for(int tick=1;tick<=1000;tick++){position=position.add(velocity);if(position.distanceTo(target)<.12)return tick;velocity=nextVelocity(position,target,velocity);}
        return 1000;
    }
    public static int settlingTicks(Vec3 from,Vec3 target,long startTick){
        int lastArrival=0;
        // Include the full emission tail and a small packet/tick scheduling allowance.
        for(int tick=0;tick<SOURCE_TICKS+4;tick++)for(int n=0;n<4;n++)lastArrival=Math.max(lastArrival,tick+flightTicks(from,target,(float)(startTick+tick)+n*.25f));
        return lastArrival+4;
    }
    private EssentiaFlow(){}
}
