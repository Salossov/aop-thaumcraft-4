package thaumcraft.world;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.*;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.*;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.structure.*;
import net.minecraft.world.level.levelgen.structure.placement.*;

/** Resumes vanilla's locate order between candidates, preserving radius, ties and placement offsets. */
public final class ScatteredStructureSearch implements AutoCloseable {
    private static final TicketType<Long> TYPE=TicketType.create("thaumcraft_structure_preparation",Long::compare);
    private static long sequence;
    private record Group(StructurePlacement placement,Set<Holder<Structure>> structures){}
    private final ServerLevel level;
    private final BlockPos origin;
    private final List<Group> rings=new ArrayList<>(),spread=new ArrayList<>();
    private final long token=++sequence;
    private int ringGroup,ringIndex,radius,spreadGroup,candidateIndex;
    private List<ChunkPos> candidates;
    private ChunkPos pending;
    private CompletableFuture<ChunkResult<ChunkAccess>> future;
    private BlockPos best,ringBest;
    private double distance=Double.MAX_VALUE,ringDistance=Double.MAX_VALUE;
    private boolean foundAtRadius,finished;
    public ScatteredStructureSearch(ServerLevel level,BlockPos origin){
        this.level=level;this.origin=origin;
        if(!level.getServer().getWorldData().worldGenOptions().generateStructures()){finished=true;return;}
        var tag=level.registryAccess().registryOrThrow(Registries.STRUCTURE).getTag(TagKey.create(Registries.STRUCTURE,ResourceLocation.parse("thaumcraft:scattered_temples")));
        if(tag.isEmpty()){finished=true;return;}
        var map=new LinkedHashMap<StructurePlacement,Set<Holder<Structure>>>();
        for(var holder:tag.get())for(var placement:level.getChunkSource().getGeneratorState().getPlacementsForStructure(holder))map.computeIfAbsent(placement,p->new LinkedHashSet<>()).add(holder);
        for(var entry:map.entrySet()){var group=new Group(entry.getKey(),entry.getValue());if(group.placement instanceof ConcentricRingsStructurePlacement)rings.add(group);else if(group.placement instanceof RandomSpreadStructurePlacement)spread.add(group);}
    }
    public BlockPos result(){if(!finished)throw new IllegalStateException("Search still pending");return best;}
    public boolean advance(int budget){
        for(int remaining=budget;!finished&&remaining>0;remaining--){
            if(ringGroup<rings.size()){
                var group=rings.get(ringGroup);var positions=level.getChunkSource().getGeneratorState().getRingPositionsFor((ConcentricRingsStructurePlacement)group.placement);
                if(positions==null)throw new IllegalStateException("Missing concentric placement positions");
                if(ringIndex==positions.size()){
                    if(ringBest!=null)accept(ringBest);
                    ringGroup++;ringIndex=0;ringBest=null;ringDistance=Double.MAX_VALUE;continue;
                }
                var p=positions.get(ringIndex);double d=new BlockPos(p.getMiddleBlockX(),32,p.getMiddleBlockZ()).distSqr(origin);
                if(ringBest!=null&&d>=ringDistance){ringIndex++;continue;}
                var value=candidate(group,p);if(value==null)return false;
                if(value){ringBest=group.placement.getLocatePos(p);ringDistance=d;}
                release();ringIndex++;continue;
            }
            if(spread.isEmpty()||radius>100){finished=true;break;}
            if(spreadGroup==spread.size()){
                if(foundAtRadius){finished=true;break;}
                radius++;spreadGroup=0;candidates=null;continue;
            }
            var group=spread.get(spreadGroup);var placement=(RandomSpreadStructurePlacement)group.placement;
            if(candidates==null){
                candidates=new ArrayList<>();candidateIndex=0;int cx=origin.getX()>>4,cz=origin.getZ()>>4;
                for(int x=-radius;x<=radius;x++)for(int z=-radius;z<=radius;z++)if(Math.abs(x)==radius||Math.abs(z)==radius)candidates.add(placement.getPotentialStructureChunk(level.getChunkSource().getGeneratorState().getLevelSeed(),cx+placement.spacing()*x,cz+placement.spacing()*z));
            }
            if(candidateIndex==candidates.size()){spreadGroup++;candidates=null;continue;}
            var p=candidates.get(candidateIndex);var value=candidate(group,p);if(value==null)return false;
            release();candidateIndex++;
            if(value){accept(placement.getLocatePos(p));foundAtRadius=true;spreadGroup++;candidates=null;}
        }
        return finished;
    }
    private void accept(BlockPos pos){double d=origin.distSqr(pos);if(d<distance){distance=d;best=pos;}}
    private Boolean candidate(Group group,ChunkPos pos){
        if(future==null){
            pending=pos;
            level.getChunkSource().addRegionTicket(TYPE,pos,33-ChunkLevel.byStatus(ChunkStatus.STRUCTURE_STARTS),token);
            // checkStructurePresence can itself join disk I/O and run biome generation.
            // Prepare STRUCTURE_STARTS first; all world work is scheduled by vanilla.
            // getChunkFuture's server-thread entry uses managedBlock, so enter off-thread.
            future=CompletableFuture.supplyAsync(()->level.getChunkSource().getChunkFuture(pos.x,pos.z,ChunkStatus.STRUCTURE_STARTS,true)).thenCompose(f->f);
            return null;
        }
        if(!future.isDone())return null;
        var result=future.getNow(null);var chunk=result==null?null:result.orElse(null);
        if(chunk==null){release();return null;}
        for(var holder:group.structures){var start=chunk.getStartForStructure(holder.value());if(start!=null&&start.isValid())return true;}
        return false;
    }
    private void release(){if(pending!=null)level.getChunkSource().removeRegionTicket(TYPE,pending,33-ChunkLevel.byStatus(ChunkStatus.STRUCTURE_STARTS),token);pending=null;future=null;}
    @Override public void close(){release();finished=true;}
}
