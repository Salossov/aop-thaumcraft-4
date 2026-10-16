package thaumcraft.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import java.util.function.Consumer;

/** ClientProxy.blockSparkle's parameters and world RNG order, independently of the client engine. */
public final class PavingSparkles {
    public record Sparkle(Vec3 position,double upward,float red,float green,float blue,int lifetime,int delay,float scale) {}
    private PavingSparkles(){}
    public static void emit(BlockPos pos,int color,int count,RandomSource r,Consumer<Sparkle> sink){
        float red=(color>>16&255)/255f,green=(color>>8&255)/255f,blue=(color&255)/255f;
        for(int i=0;i<count;i++){
            if(color==-9999){red=.33f+r.nextFloat()*.67f;green=.33f+r.nextFloat()*.67f;blue=.33f+r.nextFloat()*.67f;}
            var from=new Vec3((float)pos.getX()-.1f+r.nextFloat()*1.2f,(float)pos.getY()-.1f+r.nextFloat()*1.2f,(float)pos.getZ()-.1f+r.nextFloat()*1.2f);
            double upward=r.nextFloat()*.02;
            float redJitter=red-.2f+r.nextFloat()*.4f,greenJitter=green-.2f+r.nextFloat()*.4f,blueJitter=blue-.2f+r.nextFloat()*.4f;
            int life=5+r.nextInt(8),delay=r.nextInt(10);float scale=.7f+r.nextFloat()*.4f;
            sink.accept(new Sparkle(from,upward,redJitter,greenJitter,blueJitter,life,delay,scale));
        }
    }
}
