package thaumcraft.client;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.BreakingItemParticle;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import thaumcraft.entities.FallingTaint;

final class FallingTaintEffects {
    static void install(){FallingTaint.clientLanding=FallingTaintEffects::landing;}
    private static void landing(FallingTaint entity){
        var level=Minecraft.getInstance().level;if(level==null)return;var random=level.random;
        for(int i=0;i<10;i++){
            float angle=random.nextFloat()*(float)Math.PI*2,spread=.5f+.5f*random.nextFloat();
            float x=net.minecraft.util.Mth.sin(angle)*spread,z=net.minecraft.util.Mth.cos(angle)*spread;
            var particle=new Debris(level,entity.getX()+x,entity.centreY(),entity.getZ()+z);
            particle.setLifetime((int)(66f/(random.nextFloat()*.9f+.1f)));Minecraft.getInstance().particleEngine.add(particle);
        }
    }
    private static final class Debris extends BreakingItemParticle {
        Debris(ClientLevel level,double x,double y,double z){super(level,x,y,z,new ItemStack(Items.SLIME_BALL));setColor(.1f,0,.1f);setAlpha(.4f);}
        @Override public float getQuadSize(float partial){return super.getQuadSize(partial)*(1-age/(float)lifetime);}
        @Override public void render(VertexConsumer vertices,Camera camera,float partial){alpha=.4f*(1-age/(float)lifetime);super.render(vertices,camera,partial);}
    }
    private FallingTaintEffects(){}
}
