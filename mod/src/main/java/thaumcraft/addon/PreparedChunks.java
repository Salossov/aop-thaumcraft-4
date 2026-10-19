package thaumcraft.addon;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import thaumcraft.api.world.ChunkPreparationApi;
import thaumcraft.world.ChunkPreparation;

/** Owner lifetime is independent of the FULL tickets held for its operation. */
@EventBusSubscriber(modid="thaumcraft")
public final class PreparedChunks implements ChunkPreparationApi.Access {
    private static final Set<Job> ACTIVE=Collections.newSetFromMap(new IdentityHashMap<>());
    @Override public ChunkPreparationApi.Request prepare(BlockEntity owner,Collection<ChunkPos> positions){
        if(!(owner.getLevel() instanceof ServerLevel level)||!level.getServer().isSameThread())throw new IllegalStateException("Registered server owner and server thread required");
        var job=new Job(level,owner,positions);
        if(!job.present()){job.close();throw new IllegalStateException("Chunk preparation owner is not registered");}
        ACTIVE.add(job);return job;
    }
    private static final class Job implements ChunkPreparationApi.Request {
        final ServerLevel level;final BlockEntity owner;final BlockPos origin;
        final List<ChunkPos> positions;final ChunkPreparation preparation;boolean closed;
        Job(ServerLevel level,BlockEntity owner,Collection<ChunkPos> positions){
            this.level=level;this.owner=owner;origin=owner.getBlockPos().immutable();
            var unique=new LinkedHashSet<ChunkPos>();for(var pos:positions)unique.add(Objects.requireNonNull(pos));
            this.positions=List.copyOf(unique);preparation=new ChunkPreparation(level,this.positions);
        }
        boolean present(){
            if(owner.isRemoved()||owner.getLevel()!=level||!owner.getBlockPos().equals(origin))return false;
            var chunk=level.getChunkSource().getChunkNow(origin.getX()>>4,origin.getZ()>>4);
            return chunk!=null&&chunk.getBlockEntities().get(origin)==owner;
        }
        @Override public boolean advance(int coldBudget){
            if(!level.getServer().isSameThread())throw new IllegalStateException("Server thread required");
            if(closed)return false;
            if(!present()){close();return false;}
            if(!preparation.advanceCold(coldBudget))return false;
            for(var pos:positions)if(level.getChunkSource().getChunkNow(pos.x,pos.z)==null)return false;
            return true;
        }
        @Override public boolean closed(){return closed;}
        @Override public int submitted(){return preparation.submitted();}
        @Override public int total(){return positions.size();}
        @Override public void close(){if(closed)return;if(!level.getServer().isSameThread())throw new IllegalStateException("Server thread required");closed=true;preparation.close();ACTIVE.remove(this);}
    }
    @SubscribeEvent public static void tick(LevelTickEvent.Post event){
        if(event.getLevel() instanceof ServerLevel level)for(var job:List.copyOf(ACTIVE))
            if(job.level==level&&(!job.present()||!level.shouldTickBlocksAt(ChunkPos.asLong(job.origin))))job.close();
    }
    @SubscribeEvent public static void unloaded(LevelEvent.Unload event){for(var job:List.copyOf(ACTIVE))if(job.level==event.getLevel())job.close();}
    @SubscribeEvent public static void stopped(ServerStoppedEvent event){for(var job:List.copyOf(ACTIVE))if(job.level.getServer()==event.getServer())job.close();}
}
