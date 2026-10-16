package thaumcraft.world.dim.rooms;

import java.util.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.*;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.Difficulty;
import thaumcraft.world.dim.*;

/** Canonical whole-component plans retain conditional neighbor reads without loading native chunks. */
public final class RoomPlans {
    private static final Map<ServerLevel,Cache> WORLDS=Collections.synchronizedMap(new WeakHashMap<>());
    private static final Comparator<LabyrinthData.Position> ORDER=Comparator.comparingInt(LabyrinthData.Position::x).thenComparingInt(LabyrinthData.Position::z);
    private static final class Cache {
        long revision=-1;Difficulty difficulty;
        Map<LabyrinthData.Position,Short> cells=Map.of();
        final Map<LabyrinthData.Position,RoomPlan> plans=new HashMap<>();
        synchronized void apply(WorldGenLevel world,ChunkAccess chunk,LabyrinthData data){
            if(revision!=data.revision()||difficulty!=world.getDifficulty()){var snapshot=data.planSnapshot();revision=snapshot.revision();cells=snapshot.cells();difficulty=world.getDifficulty();plans.clear();}
            var area=chunk.getPos();var selected=new LinkedHashSet<RoomPlan>();
            // Original room loops include the east/south border at local coordinate16.
            for(int dx=-1;dx<=0;dx++)for(int dz=-1;dz<=0;dz++){
                var position=new LabyrinthData.Position(area.x+dx,area.z+dz);if(!cells.containsKey(position))continue;
                var plan=plans.get(position);if(plan==null){var component=component(position);plan=build(world,component);for(var p:component)plans.put(p,plan);}selected.add(plan);
            }
            for(var plan:selected)plan.apply(world,chunk);
        }
        private SortedSet<LabyrinthData.Position> component(LabyrinthData.Position start){
            var result=new TreeSet<>(ORDER);var queue=new ArrayDeque<LabyrinthData.Position>();result.add(start);queue.add(start);
            while(!queue.isEmpty()){var p=queue.remove();for(int dx=-1;dx<=1;dx++)for(int dz=-1;dz<=1;dz++){if(dx==0&&dz==0)continue;var next=new LabyrinthData.Position(p.x()+dx,p.z()+dz);if(cells.containsKey(next)&&result.add(next))queue.add(next);}}
            return result;
        }
        private RoomPlan build(WorldGenLevel world,SortedSet<LabyrinthData.Position> component){
            var plan=new RoomPlan();for(var p:component){long seed=OuterChunkGenerator.roomSeed(world.getSeed(),p.x(),p.z());var cell=new MazeCell(cells.get(p));var sink=new RoomWorldSink(world,cell,new Random(seed),plan);RoomLayouts.generateCell(sink,new Random(seed),p.x(),p.z(),cell);sink.finish();}
            plan.freeze();return plan;
        }
    }
    public static void apply(WorldGenLevel world,ChunkAccess chunk){var level=world.getLevel();Cache cache;synchronized(WORLDS){cache=WORLDS.computeIfAbsent(level,unused->new Cache());}cache.apply(world,chunk,LabyrinthData.get(level));}
    private RoomPlans(){}
}
