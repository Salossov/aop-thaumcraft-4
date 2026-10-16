package thaumcraft.verification;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;

/** A FULL block chunk can still lack its entity sections until the next server ticks. */
final class EncounterVerificationChunks {
    private static final Set<ChunkPos> HELD=new LinkedHashSet<>();
    static void prepare(ServerLevel level,String phase){
        if(phase.equals("arena")){for(int k=0;k<4;k++)hold(level,new BlockPos((360+k*8)*16+16,52,2896),2);hold(level,new BlockPos(6120,54,3020),3);hold(level,new BlockPos(6336,80,3024),2);}
        else if(phase.equals("round-two"))hold(level,new BlockPos(408,251,403),1);
        else hold(level,new BlockPos(phase.equals("boss-champions")?4020:3820,160,80),4);
    }
    private static void hold(ServerLevel level,BlockPos center,int radius){int cx=center.getX()>>4,cz=center.getZ()>>4;for(int x=-radius;x<=radius;x++)for(int z=-radius;z<=radius;z++){var chunk=new ChunkPos(cx+x,cz+z);if(HELD.add(chunk))level.setChunkForced(chunk.x,chunk.z,true);level.getChunk(chunk.x,chunk.z);}}
    static boolean ready(ServerLevel level){return !HELD.isEmpty()&&HELD.stream().allMatch(p->level.areEntitiesLoaded(p.toLong()));}
    static void release(ServerLevel level){for(var p:HELD)level.setChunkForced(p.x,p.z,false);HELD.clear();}
}
