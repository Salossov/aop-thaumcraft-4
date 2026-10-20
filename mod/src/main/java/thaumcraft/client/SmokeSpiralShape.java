package thaumcraft.client;

import net.minecraft.util.Mth;
import thaumcraft.api.client.ParticleApi;

/** Source float arithmetic kept separate from the particle engine for comparison. */
final class SmokeSpiralShape {
    record Point(double x,double y,double z){}
    static Point position(ParticleApi.SmokeSpiral effect,int age,int lifetime,float partialTick){
        float r1=(float)effect.start()+720f*(((float)age+partialTick)/(float)lifetime);
        float r2=90f-180f*(((float)age+partialTick)/(float)lifetime);
        float x=-Mth.sin(r1/180f*(float)Math.PI)*Mth.cos(r2/180f*(float)Math.PI);
        float z=Mth.cos(r1/180f*(float)Math.PI)*Mth.cos(r2/180f*(float)Math.PI);
        float y=-Mth.sin(r2/180f*(float)Math.PI);
        x*=effect.radius();y*=effect.radius();z*=effect.radius();
        return new Point(effect.x()+(double)x,Math.max(effect.y()+(double)y,(double)((float)effect.minY()+.1f)),effect.z()+(double)z);
    }
    private SmokeSpiralShape(){}
}
