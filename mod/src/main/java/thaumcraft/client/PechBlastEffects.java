package thaumcraft.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import thaumcraft.entities.PechBlast;

/** TC4's bodyless projectile, using the same source wisps and sparkles as the other foci. */
public final class PechBlastEffects {
    public static void install(){PechBlast.clientTrail=PechBlastEffects::trail;PechBlast.clientImpact=PechBlastEffects::impact;}
    private static Vec3 jitter(net.minecraft.util.RandomSource random,float amount){return new Vec3((random.nextFloat()-random.nextFloat())*amount,(random.nextFloat()-random.nextFloat())*amount,(random.nextFloat()-random.nextFloat())*amount);}
    private static void trail(PechBlast blast){
        var level=Minecraft.getInstance().level;if(level==null)return;var random=level.random;
        for(int i=0;i<3;i++){
            VishroomEffects.typed(blast.position().add(jitter(random,.2f)),.3f,3,true,true,.02f);
            VishroomEffects.typed(new Vec3((blast.getX()+blast.xo)/2,(blast.getY()+blast.yo)/2,(blast.getZ()+blast.zo)/2).add(jitter(random,.2f)),.3f,2,true,true,.02f);
            var delta=jitter(random,.1f);var at=new Vec3((float)((float)blast.getX()+(float)delta.x),(float)((float)blast.getY()+(float)delta.y),(float)((float)blast.getZ()+(float)delta.z));
            FollowingItemEffects.sparkle(at,1.5f,5,0);
        }
    }
    private static void impact(PechBlast blast){
        var level=Minecraft.getInstance().level;if(level==null)return;
        for(int i=0;i<9;i++)for(int type:new int[]{3,2,0}){
            var delta=jitter(level.random,.3f);VishroomEffects.travel(blast.position().add(delta),blast.position().add(delta.scale(8)),.3f,type,true,.02f);
        }
    }
    private PechBlastEffects(){}
}
