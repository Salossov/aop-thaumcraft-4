package thaumcraft.client;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/** Float operations from FXBeamWand, independent of a client or OpenGL context. */
public final class WandBeamShape {
    public record Aim(float length,float yaw,float pitch){}
    public record Frame(float size,float opacity,float rotation,float scroll){}
    public record Ribbon(double nearLeft,double nearRight,double farLeft,double farRight,double length,double v0,double v1){}
    public static Vec3 look(float yaw,float pitch){
        float cos=Mth.cos(-yaw*((float)Math.PI/180)-(float)Math.PI),sin=Mth.sin(-yaw*((float)Math.PI/180)-(float)Math.PI);
        float cp=-Mth.cos(-pitch*((float)Math.PI/180)),sp=Mth.sin(-pitch*((float)Math.PI/180));
        // Legacy Vec3 canonicalizes negative zero in its constructor.
        return Vec3.ZERO.add((double)(sin*cp),(double)sp,(double)(cos*cp));
    }
    public static Aim aim(Vec3 source,Vec3 target){
        float xd=(float)(source.x-target.x),yd=(float)(source.y-target.y),zd=(float)(source.z-target.z);
        return new Aim(Mth.sqrt(xd*xd+yd*yd+zd*zd),(float)(Math.atan2(xd,zd)*180/Math.PI),
                (float)(Math.atan2(yd,(double)Mth.sqrt(xd*xd+zd*zd))*180/Math.PI));
    }
    public static float unwrap(float previous,float current){
        while(current-previous< -180)previous-=360;
        while(current-previous>=180)previous+=360;
        return previous;
    }
    public static Frame frame(int age,int lifetime,float previousSize,float partial,int viewerTicks,long worldTime,boolean reverse){
        float size=Math.min((float)age/4,1);size=(float)((double)previousSize+(double)(size-previousSize)*(double)partial);
        float opacity=.4f;if(lifetime-age<=4)opacity=.4f-(float)(4-(lifetime-age))*.1f;
        float clock=viewerTicks+partial;if(reverse)clock*= -1;
        float scroll=-clock*.2f-(float)Mth.floor(-clock*.1f);
        return new Frame(size,opacity,(float)(worldTime%72*5)+5f*partial,scroll);
    }
    public static Ribbon ribbon(float length,Frame frame,float endWidth,int index){
        double left=-.15*(double)frame.size(),right=.15*(double)frame.size();
        double v0=-1f+frame.scroll()+(float)index/3f;
        return new Ribbon(left,right,left*(double)endWidth,right*(double)endWidth,(double)(length*frame.size()),v0,(double)(length*frame.size())+v0);
    }
    public static Vec3 origin(Vec3 previous,Vec3 current,float previousYaw,float yaw,Vec3 look,Vec3 camera,float partial){
        double prex=previous.x-(double)(Mth.cos(previousYaw/180f*3.141593f)*.066f)+look.x*.3;
        double prey=previous.y-.06+look.y*.3;
        double prez=previous.z-(double)(Mth.sin(previousYaw/180f*3.141593f)*.04f)+look.z*.3;
        double px=current.x-(double)(Mth.cos(yaw/180f*3.141593f)*.066f)+look.x*.3;
        double py=current.y-.06+look.y*.3;
        double pz=current.z-(double)(Mth.sin(yaw/180f*3.141593f)*.04f)+look.z*.3;
        return new Vec3((float)(prex+(px-prex)*(double)partial-camera.x),(float)(prey+(py-prey)*(double)partial-camera.y),
                (float)(prez+(pz-prez)*(double)partial-camera.z));
    }
    private WandBeamShape(){}
}
