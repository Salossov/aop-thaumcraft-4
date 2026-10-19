package thaumcraft.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.*;
import thaumcraft.production.CrucibleBlockEntity;

/** Original TileCrucible.drawEffects and FXBubble, using the original particle-sheet cells. */
public final class CrucibleEffects {
    public static void tick(CrucibleBlockEntity tile){
        var mc=Minecraft.getInstance();if(mc.level==null||mc.getCameraEntity()==null)return;var p=tile.getBlockPos();var r=mc.level.random;double x=p.getX(),y=p.getY(),z=p.getZ();float h=tile.fluidHeight();
        if(tile.heat>150){add(x+.2+r.nextFloat()*.6,y+h,z+.2+r.nextFloat()*.6,.5f,.5f,.7f,1);if(tile.store.amount()>100)for(int i=0;i<2;i++){add(x,y+1,z+r.nextFloat(),.5f,.5f,.7f,2);add(x+1,y+1,z+r.nextFloat(),.5f,.5f,.7f,2);add(x+r.nextFloat(),y+1,z,.5f,.5f,.7f,2);add(x+r.nextFloat(),y+1,z+1,.5f,.5f,.7f,2);}}
        var aspects=tile.getAspects().getAspects();if(r.nextInt(6)==0&&aspects.length>0){int color=aspects[r.nextInt(aspects.length)].getColor();add(x+(5+r.nextInt(22))/32d+.015625,y+.05+h,z+(5+r.nextInt(22))/32d+.015625,(color>>16&255)/255f,(color>>8&255)/255f,(color&255)/255f,0);}
    }
    private static void add(double x,double y,double z,float red,float green,float blue,int kind){var mc=Minecraft.getInstance();if(mc.getCameraEntity().distanceToSqr(x,y,z)>(Minecraft.useFancyGraphics()?2500:625))return;mc.particleEngine.add(new Bubble(x,y,z,red,green,blue,kind));}
    private static final class Bubble extends TextureSheetParticle {
        private int frame=16;private final double acceleration;
        Bubble(double x,double y,double z,float red,float green,float blue,int kind){super(Minecraft.getInstance().level,x,y,z);rCol=red;gCol=green;bCol=blue;hasPhysics=false;quadSize=.1f*(random.nextFloat()*.3f+.2f);xd=(random.nextDouble()*2-1)*.02;yd=random.nextDouble()*.02;zd=(random.nextDouble()*2-1)*.02;lifetime=(int)(3+8/(random.nextDouble()*.8+.2));acceleration=kind==0?.002:kind==1?-.001:-.005;if(kind>0){quadSize*=.75f;lifetime=kind==1?4+random.nextInt(3):12+random.nextInt(12);xd/=5;yd/=10;zd/=5;}}
        @Override public ParticleRenderType getRenderType(){return LegacyParticles.TRAIL;}
        @Override protected int getLightColor(float partial){return 0xf000f0;}
        @Override protected float getU0(){return frame%16/16f;}
        @Override protected float getU1(){return getU0()+.0624375f;}
        @Override protected float getV0(){return frame/16/16f;}
        @Override protected float getV1(){return getV0()+.0624375f;}
        @Override public void tick(){xo=x;yo=y;zo=z;yd+=acceleration;if(acceleration>0){xd+=(random.nextFloat()-random.nextFloat())*.01f;zd+=(random.nextFloat()-random.nextFloat())*.01f;}setPos(x+xd,y+yd,z+zd);xd*=.85f;yd*=.85f;zd*=.85f;if(age++>=lifetime)remove();else if(lifetime-age<=2)frame++;}
    }
    private CrucibleEffects(){}
}
