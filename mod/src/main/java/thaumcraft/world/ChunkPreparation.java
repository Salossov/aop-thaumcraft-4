package thaumcraft.world;

import java.util.*;
import net.minecraft.server.level.*;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;

/** FULL tickets are scheduled by the normal chunk manager; polling never joins a future. */
@EventBusSubscriber(modid="thaumcraft")
public final class ChunkPreparation implements AutoCloseable {
    private static final TicketType<Long> TYPE=TicketType.create("thaumcraft_preparation",Long::compare);
    private static final Set<ChunkPreparation> ACTIVE=Collections.newSetFromMap(new IdentityHashMap<>());
    private static long sequence;
    private final ServerLevel level;
    private final long token=++sequence;
    private final List<ChunkPos> positions=new ArrayList<>();
    private final Set<Long> completed=new HashSet<>();
    private int submitted;
    private boolean closed;

    public ChunkPreparation(ServerLevel level,int minX,int maxX,int minZ,int maxZ){
        this.level=level;
        for(int x=minX;x<=maxX;x++)for(int z=minZ;z<=maxZ;z++)positions.add(new ChunkPos(x,z));
        ACTIVE.add(this);
    }
    public boolean advance(int budget){
        if(closed)throw new IllegalStateException("Closed chunk preparation");
        for(int i=0;i<submitted;i++){
            var p=positions.get(i);
            if(level.getChunkSource().getChunkNow(p.x,p.z)!=null)completed.add(p.toLong());
        }
        // Bound both requests per tick and unfinished generation, including overlapping jobs.
        int allowance=Math.min(Math.max(0,budget),8-(submitted-completed.size()));
        while(allowance-->0&&submitted<positions.size()){
            var p=positions.get(submitted++);
            level.getChunkSource().addRegionTicket(TYPE,p,0,token);
            if(level.getChunkSource().getChunkNow(p.x,p.z)!=null)completed.add(p.toLong());
        }
        return completed.size()==positions.size();
    }
    public int submitted(){return submitted;}
    public int total(){return positions.size();}
    @Override public void close(){
        if(closed)return;
        closed=true;
        for(int i=0;i<submitted;i++)level.getChunkSource().removeRegionTicket(TYPE,positions.get(i),0,token);
        ACTIVE.remove(this);
    }
    @SubscribeEvent public static void unloaded(LevelEvent.Unload event){for(var job:List.copyOf(ACTIVE))if(job.level==event.getLevel())job.close();}
    @SubscribeEvent public static void stopped(ServerStoppedEvent event){for(var job:List.copyOf(ACTIVE))if(job.level.getServer()==event.getServer())job.close();}
}
