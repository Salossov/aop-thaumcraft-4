package thaumcraft.verification;

import java.util.*;
import java.nio.file.*;
import java.util.function.BiConsumer;
import com.google.gson.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import thaumcraft.world.dim.*;

/** Independent goldens come from original JAR bytecode, not from the port's implementation. */
final class MazeVerification {
    static void run(ServerLevel level,BiConsumer<Boolean,String> check)throws Exception {
        var root=Path.of(System.getProperty("thaumcraft.r3.root"));var rows=JsonParser.parseString(Files.readString(root.resolve("analysis/full_port/original-maze-layouts.json"))).getAsJsonArray();check.accept(rows.size()==400,"400 original JAR maze controls cover all odd widths and heights 11..29");
        int rejected=0;
        for(var entry:rows){var row=entry.getAsJsonObject();int width=row.get("width").getAsInt(),height=row.get("height").getAsInt();long seed=row.get("requested_seed").getAsLong();var data=new LabyrinthData();var result=data.generate(0,0,width,height,seed);var expected=row.getAsJsonArray("grid");boolean equal=result.acceptedSeed()==row.get("accepted_seed").getAsLong()&&result.attempts()==row.get("attempts").getAsInt();for(int z=0;z<height;z++)for(int x=0;x<width;x++)equal&=result.grid()[z][x]==expected.get(z).getAsJsonArray().get(x).getAsInt()&&data.packed(x-(1+width/2),z-(1+height/2))==(short)Math.max(0,expected.get(z).getAsJsonArray().get(x).getAsInt());check.accept(equal,"every packed cell and retry matches original bytecode: "+width+"x"+height+" seed "+seed);if(result.attempts()>1)rejected++;
            
        }
        check.accept(rejected>0,"golden controls exercise actual rejected-seed retry paths");
        boolean bits=true;for(int value=0;value<65536;value++)bits&=new MazeCell((short)value).pack()==(short)(value&0xff3f);check.accept(bits,"all packed cell bit patterns retain six directional flags and signed feature byte");
        var isolated=new LabyrinthData();var first=isolated.generate(100,200,11,21,42);var snapshot=isolated.snapshot();check.accept(isolated.isDirty()&&isolated.cell(100,200)!=null,"maze generation marks native SavedData dirty and places the portal at its original chunk");check.accept(isolated.generate(100,200,11,21,77)==null&&snapshot.equals(isolated.snapshot()),"overlapping generation is rejected atomically without overwriting existing rooms");
        check.accept(!isolated.inRange(1000,2000,11,21)&&isolated.generate(1000,2000,29,15,1)!=null,"separate maze locations can coexist in one world");
        var portal=isolated.cell(100,200);var before=isolated.packed(100,200);portal.feature=99;first.grid()[first.grid().length-1][0]=99;check.accept(isolated.packed(100,200)==before,"mutable returned cell or generation grid cannot alter saved maze cells");
        var encoded=isolated.save(new CompoundTag(),level.registryAccess());var copy=LabyrinthData.load(encoded,level.registryAccess());check.accept(isolated.snapshot().equals(copy.snapshot()),"native NBT preserves all maze chunk coordinates and packed room states");
        var unrelated=new LabyrinthData();check.accept(!unrelated.inRange(100,200,29,29),"independent world data does not inherit another world's global maze map");
        var shared=LabyrinthData.get(level);check.accept(shared==LabyrinthData.get(level.getServer().getLevel(net.minecraft.world.level.Level.NETHER)),"Outer Lands and overworld resolve the same owning world's SavedData");
        for(var origin:new int[][]{{40000,40000},{-40000,-40000}}){for(int x=-30;x<=30;x++)for(int z=-30;z<=30;z++)shared.remove(origin[0]+x,origin[1]+z);check.accept(shared.generate(origin[0],origin[1],11,21,42)!=null,"persistent native world maze created at "+Arrays.toString(origin));}
    }
    static void reload(ServerLevel level,BiConsumer<Boolean,String> check){var data=LabyrinthData.get(level);for(var origin:new int[][]{{40000,40000},{-40000,-40000}}){var reference=new LegacyMazeGenerator(11,21,42);long seed=42;while(!reference.generate())reference=new LegacyMazeGenerator(11,21,++seed);boolean same=true;for(int z=0;z<21;z++)for(int x=0;x<11;x++)same&=data.packed(origin[0]+x-6,origin[1]+z-11)==(short)Math.max(0,reference.grid[z][x]);check.accept(same,"all positive/negative maze coordinates survive an actual server restart: "+Arrays.toString(origin));check.accept(data.cell(origin[0],origin[1])!=null&&data.cell(origin[0],origin[1]).feature==1,"portal room retains original feature type through restart: "+Arrays.toString(origin));}check.accept(data==LabyrinthData.get(level.getServer().getLevel(net.minecraft.world.level.Level.END)),"reloaded native SavedData remains shared by this world's dimensions");}
}
