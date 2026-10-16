package thaumcraft.verification;

import java.util.*;
import java.util.function.BiConsumer;
import java.nio.file.*;
import com.google.gson.JsonParser;
import net.minecraft.core.Direction;
import thaumcraft.world.dim.MazeCell;
import thaumcraft.world.dim.rooms.*;

/** Geometry controls intentionally precede native room block behavior/integration. */
final class RoomLayoutVerification {
    static void run(BiConsumer<Boolean,String> check)throws Exception{
        var root=Path.of(System.getProperty("thaumcraft.r3.root"));var controls=JsonParser.parseString(Files.readString(root.resolve("analysis/full_port/original-room-layouts.json"))).getAsJsonObject().getAsJsonArray("rooms");
        check.accept(controls.size()==160,"160 actual original JAR room layouts cover five room parts, all exit flags and negative chunks");
        for(var value:controls){var row=value.getAsJsonObject();int flags=row.get("flags").getAsInt(),cx=row.get("cx").getAsInt(),cz=row.get("cz").getAsInt();var kind=RoomLayouts.Kind.valueOf(row.get("kind").getAsString().toUpperCase(Locale.ROOT));var cell=new MazeCell();cell.north=(flags&1)!=0;cell.south=(flags&2)!=0;cell.east=(flags&4)!=0;cell.west=(flags&8)!=0;
            var actions=new ArrayList<int[]>();RoomLayouts.generate(kind,new RoomLayoutSink(){public void place(int x,int y,int z,int code,Direction face){actions.add(new int[]{x,y,z,code,face==null?6:face.get3DDataValue()});}public void raw(int x,int y,int z,int block,int meta,int update){actions.add(new int[]{x,y,z,1000+block*16+meta,update});}},new Random(42),cx,cz,row.get("y").getAsInt(),cell);
            var expected=row.getAsJsonArray("actions");boolean equal=expected.size()==actions.size();if(equal)for(int i=0;i<actions.size()&&equal;i++){var entry=expected.get(i).getAsJsonArray();for(int j=0;j<5;j++)equal&=entry.get(j).getAsInt()==actions.get(i)[j];}
            check.accept(equal,"ordered native room geometry equals actual TC4 bytecode: "+kind+" flags="+flags+" chunk="+cx+","+cz);
        }
    }
    private RoomLayoutVerification(){}
}
