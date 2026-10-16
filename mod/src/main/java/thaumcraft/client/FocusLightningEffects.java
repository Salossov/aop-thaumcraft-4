package thaumcraft.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import thaumcraft.network.FocusLightningPayload;

public final class FocusLightningEffects {
    public static void accept(FocusLightningPayload packet){
        var mc=Minecraft.getInstance();if(mc.level==null)return;var source=mc.level.getEntity(packet.source());var target=mc.level.getEntity(packet.target());if(source==null)return;
        var from=source.position().add(0,source.getBbHeight()/2f,0);var to=packet.endpoint();
        if(packet.fromWand()){
            float yaw=source.getYRot()/180f*3.141593f;double y=source==mc.player?source.getEyeY()-.06:source.getBoundingBox().minY+source.getBbHeight()/2f+.25;
            from=new Vec3(source.getX()-net.minecraft.util.Mth.cos(yaw)*.06f,y,source.getZ()-net.minecraft.util.Mth.sin(yaw)*.06f).add(source.getViewVector(1).scale(.3));
            if(packet.blockSparkles())sparks(packet.endpoint(),.3f);
            if(target!=null){to=target.getBoundingBox().getCenter();sparks(to,.6f);}
        }else{
            if(target==null)return;to=target.getBoundingBox().getCenter();
        }
        LegacyLightningParticle.shock(from,to);
    }
    private static void sparks(Vec3 position,float spread){
        var r=Minecraft.getInstance().level.random;
        for(int i=0;i<5;i++){
            var p=new Vec3((float)position.x+(r.nextFloat()-r.nextFloat())*spread,(float)position.y+(r.nextFloat()-r.nextFloat())*spread,(float)position.z+(r.nextFloat()-r.nextFloat())*spread);
            float scale=2+r.nextFloat(),gravity=.05f+r.nextFloat()*.05f;FollowingItemEffects.sparkle(p,scale,2,gravity);
        }
    }
    private FocusLightningEffects(){}
}
