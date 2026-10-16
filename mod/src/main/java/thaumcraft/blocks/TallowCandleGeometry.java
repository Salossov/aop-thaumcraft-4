package thaumcraft.blocks;

import java.util.*;
import net.minecraft.core.BlockPos;

/** TC4's coordinate-seeded wax profile. Keep the int overflow before widening the seed. */
public final class TallowCandleGeometry {
    public record Box(float x0,float y0,float z0,float x1,float y1,float z1,boolean wick){}
    public static final Box BODY=new Box(.375f,0,.375f,.625f,.5f,.625f,false);
    public static final Box WICK=new Box(.475f,.5f,.475f,.525f,.625f,.525f,true);
    public static List<Box> world(BlockPos pos){
        var boxes=new ArrayList<Box>();boxes.add(BODY);
        var random=new Random(pos.getX()+pos.getY()*pos.getZ());
        int count=1+random.nextInt(5);
        for(int a=0;a<count;a++){
            boolean side=random.nextBoolean();int loc=2+random.nextInt(2);float height=.0625f*(1+random.nextInt(3));
            boxes.add((a&1)==0?new Box(.3125f+.0625f*loc,0,side?.3125f:.625f,.375f+.0625f*loc,height,side?.375f:.6875f,false):
                new Box(side?.3125f:.625f,0,.3125f+.0625f*loc,side?.375f:.6875f,height,.375f+.0625f*loc,false));
        }
        boxes.add(WICK);return List.copyOf(boxes);
    }
    public static List<Box> inventory(){return List.of(BODY,WICK);}
    private TallowCandleGeometry(){}
}
