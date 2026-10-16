package thaumcraft.blocks;

import java.util.Random;
import net.minecraft.core.Direction;

/** Original face winding, far UVs and deterministic near-layer parameters. */
public final class EldritchVoidGeometry {
    private static final float[][][] FACES={
        {{0,0,1,1,1},{0,0,0,1,0},{1,0,0,0,0},{1,0,1,0,1}},
        {{0,1,0,0,0},{0,1,1,0,1},{1,1,1,1,1},{1,1,0,1,0}},
        {{0,0,0,0,1},{0,1,0,0,0},{1,1,0,1,0},{1,0,0,1,1}},
        {{0,1,1,1,0},{0,0,1,1,1},{1,0,1,0,1},{1,1,1,0,0}},
        {{0,0,0,1,1},{0,0,1,0,1},{0,1,1,0,0},{0,1,0,1,0}},
        {{1,1,0,0,0},{1,1,1,1,0},{1,0,1,1,1},{1,0,0,0,1}}
    };
    public static float[] vertex(Direction face,int index){var vertex=FACES[face.get3DDataValue()][index].clone();vertex[3]=index<2?1:0;vertex[4]=index==0||index==3?1:0;return vertex;}
    public static boolean near(double squaredDistance){return squaredDistance<512;}
    public static float scale(int layer){return layer==0?.125f:layer==1?.5f:.0625f;}
    public static float rotation(int layer){return (layer*layer*4321+layer*9)*2f;}
    public static float[] color(int layer){var random=new Random(31100L);float r=0,g=0,b=0;for(int i=0;i<=layer;i++){float weight=1f/(17-i);r=(random.nextFloat()*.5f+.1f)*weight;g=(random.nextFloat()*.5f+.4f)*weight;b=(random.nextFloat()*.5f+.5f)*weight;}return layer==0?new float[]{.1f,.1f,.1f}:new float[]{r,g,b};}
    private EldritchVoidGeometry(){}
}
