package thaumcraft.client;

import net.minecraft.world.entity.player.Player;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.Camera;
import com.mojang.blaze3d.vertex.VertexConsumer;

/** Five smoke streams wind around the wielder while the Sword of the Zephyr is held. */
public final class ElementalToolEffects {
    public static void gather(net.minecraft.world.entity.item.ItemEntity item){var r=item.level().random;Minecraft.getInstance().particleEngine.add(new GatherBubble(item.getX()+(r.nextFloat()-r.nextFloat())*.125,item.getY()+(r.nextFloat()-r.nextFloat())*.125,item.getZ()+(r.nextFloat()-r.nextFloat())*.125));}
    public static void wind(Player player){
        var level=player.level();double bottom=player.onGround()?player.getY():player.getY()-2;
        for(int i=0;i<5;i++){
            Minecraft.getInstance().particleEngine.add(new WindSpiral(player.getX(),player.getY()+player.getBbHeight()/2,player.getZ(),level.random.nextInt(360),(int)Math.floor(bottom)));
        }
        if(player.onGround()){double angle=level.random.nextDouble()*Math.PI*2;level.addParticle(ParticleTypes.SMOKE,player.getX(),player.getY()+.1,player.getZ(),-Math.sin(angle)/5,0,Math.cos(angle)/5);}
    }
    private static final class WindSpiral extends TextureSheetParticle {
        private final double centreX,centreY,centreZ;private final int start,minimumY;
        WindSpiral(double x,double y,double z,int start,int minimumY){super(Minecraft.getInstance().level,x,y,z);centreX=x;centreY=y;centreZ=z;this.start=start;this.minimumY=minimumY;hasPhysics=false;lifetime=20+random.nextInt(10);quadSize=.15f;setColor(.8667f,.8667f,.8667f);}
        @Override public ParticleRenderType getRenderType(){return LegacyParticles.TRAIL;}
        @Override protected float getU0(){return (1+(int)(age/(float)lifetime*4))/16f;}
        @Override protected float getU1(){return getU0()+.0624375f;}
        @Override protected float getV0(){return 0;}
        @Override protected float getV1(){return .0624375f;}
        @Override public void tick(){alpha=.66f*(lifetime-age)/(float)lifetime;if(age++>=lifetime)remove();}
        @Override public void render(VertexConsumer vertices,Camera camera,float partial){
            double progress=(age+partial)/lifetime,heading=Math.toRadians(start+720*progress),vertical=Math.toRadians(90-180*progress);
            xo=x=centreX-Math.sin(heading)*Math.cos(vertical)*1.5;yo=y=Math.max(centreY-Math.sin(vertical)*1.5,minimumY+.1);zo=z=centreZ+Math.cos(heading)*Math.cos(vertical)*1.5;super.render(vertices,camera,partial);
        }
    }
    private static final class GatherBubble extends TextureSheetParticle {
        private int frame=16;
        GatherBubble(double x,double y,double z){super(Minecraft.getInstance().level,x,y,z);hasPhysics=false;quadSize=.1f*(random.nextFloat()*.3f+.2f);lifetime=(int)(2+8/(random.nextDouble()*.8+.2));xd=(random.nextDouble()*2-1)*.02;yd=random.nextDouble()*.02;zd=(random.nextDouble()*2-1)*.02;setColor(.33f,.33f,1);}
        @Override public ParticleRenderType getRenderType(){return LegacyParticles.TRAIL;}
        @Override protected float getU0(){return frame%16/16f;}@Override protected float getU1(){return getU0()+.0624375f;}
        @Override protected float getV0(){return frame/16/16f;}@Override protected float getV1(){return getV0()+.0624375f;}
        @Override public void tick(){xo=x;yo=y;zo=z;yd+=.002;xd+=(random.nextFloat()-random.nextFloat())*.01;zd+=(random.nextFloat()-random.nextFloat())*.01;x+=xd;y+=yd;z+=zd;xd*=.85;yd*=.85;zd*=.85;if(age++>=lifetime)remove();else if(lifetime-age<=2)frame++;}
    }
    private ElementalToolEffects(){}
}
