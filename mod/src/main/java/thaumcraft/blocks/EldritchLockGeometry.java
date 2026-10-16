package thaumcraft.blocks;

import java.util.*;
import net.minecraft.util.Mth;
import net.minecraft.core.Direction;

/** Original lock arm integer timing and camera-age pulse; independent of the rendering backend. */
public final class EldritchLockGeometry {
    public record Cube(int arm,int index,double offsetY,double width,double height){}
    public static List<Cube> cubes(int count,int cameraTicks,float partial){
        float age=(float)cameraTicks+partial;var cubes=new ArrayList<Cube>();
        for(int arm=0;arm<4;arm++)for(int index=1;index<5-(count+arm*5)/20;index++){
            float width=Mth.sin((age+(float)(index*10)+(float)(arm*20))/20f)*.1f;
            if(index==1||index==4)width=width/2f+.2f;
            cubes.add(new Cube(arm,index,.25f+.5f*(float)index,.5+(double)width,.5));
        }return List.copyOf(cubes);
    }
    public static float[] planeVertex(Direction face,int index){
        if(face.getAxis()==Direction.Axis.Y)throw new IllegalArgumentException("Original lock has no vertical void plane");
        var vertex=EldritchVoidGeometry.vertex(face,index);for(int axis=0;axis<3;axis++)vertex[axis]=axis==(face.getAxis()==Direction.Axis.X?0:2)?.5f:vertex[axis]*5f-2f;return vertex;
    }
    private EldritchLockGeometry(){}
}
