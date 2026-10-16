package thaumcraft.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.*;
import net.minecraft.sounds.*;
import net.minecraft.world.item.*;
import net.minecraft.world.phys.Vec3;
import thaumcraft.network.BottleTaintBreakPayload;
import thaumcraft.registry.R3Equipment;

/** Original shrinking, fading tinted slime fragments plus eight bottle shards. */
public final class BottleTaintEffects {
    public static void burst(BottleTaintBreakPayload packet){
        var mc=Minecraft.getInstance();var l=mc.level;if(l==null)return;var p=packet.position();
        int count=switch(mc.options.particles().get()){case ALL->200;case DECREASED->100;case MINIMAL->0;};
        for(int i=0;i<count;i++)mc.particleEngine.add(new Fragment(l,p,packet.height()));
        for(int i=0;i<8;i++)l.addParticle(new ItemParticleOption(ParticleTypes.ITEM,new ItemStack(R3Equipment.BOTTLE_TAINT.get())),p.x,p.y,p.z,l.random.nextGaussian()*.15,l.random.nextDouble()*.2,l.random.nextGaussian()*.15);
        l.playLocalSound(p.x,p.y,p.z,SoundEvents.SPLASH_POTION_BREAK,SoundSource.NEUTRAL,1,l.random.nextFloat()*.1f+.9f,false);
    }
    private static final class Fragment extends TextureSheetParticle {
        private final float u,v,baseAlpha;
        Fragment(ClientLevel l,Vec3 p,float height){
            super(l,p.x,p.y+l.random.nextFloat()*height,p.z);
            setSprite(Minecraft.getInstance().getItemRenderer().getModel(new ItemStack(Items.SLIME_BALL),l,null,0).getParticleIcon());
            u=random.nextFloat()*3;v=random.nextFloat()*3;quadSize/=2;gravity=1;friction=.98f;
            boolean light=random.nextBoolean();setColor(light?.6f:.3f,0,.3f);baseAlpha=light?.4f:.6f;
            var d=new Vec3(random.nextDouble()*2-1,random.nextDouble()*2-1,random.nextDouble()*2-1).normalize().scale((random.nextDouble()+random.nextDouble()+1)*.15*.9640000000596046);
            xd=d.x;yd=d.y+.1;zd=d.z;lifetime=(int)(66/(l.random.nextFloat()*.9f+.1f));
        }
        @Override public float getQuadSize(float partial){alpha=baseAlpha*(1-(float)age/lifetime);return quadSize*(1-(float)age/lifetime);}
        @Override protected float getU0(){return sprite.getU(u/4);}
        @Override protected float getU1(){return sprite.getU((u+1)/4);}
        @Override protected float getV0(){return sprite.getV(v/4);}
        @Override protected float getV1(){return sprite.getV((v+1)/4);}
        @Override public ParticleRenderType getRenderType(){return ParticleRenderType.TERRAIN_SHEET;}
    }
    private BottleTaintEffects(){}
}
