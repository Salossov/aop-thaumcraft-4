package thaumcraft.production;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import thaumcraft.api.aspects.*;
/** Original cube order and five-second rescan delay, scoped to loaded chunks. */
public final class RitualEssentia {
    private final List<BlockPos> sources=new ArrayList<>();
    private long retryAfter;
    public BlockPos drain(ServerLevel level,BlockPos origin,Aspect aspect,int range){
        // A deleted/unloaded cached entry must not hide the valid entries after it.
        // Rebuild a stale cache at most once; genuinely exhausted supply keeps TC4's delay.
        for(int attempt=0;attempt<2;attempt++){
            if(sources.isEmpty()){
                if(System.currentTimeMillis()<retryAfter)return null;
                for(int x=-range;x<=range;x++)for(int z=-range;z<=range;z++)for(int y=-range;y<range;y++){
                    if(x==0&&y==0&&z==0)continue;var p=origin.offset(x,y,z);if(level.hasChunkAt(p)&&level.getCapability(EssentiaCapabilities.SOURCE,p,null)!=null)sources.add(p);
                }
                if(sources.isEmpty()){retryAfter=System.currentTimeMillis()+5000;return null;}
            }
            boolean stale=false;
            for(var iterator=sources.iterator();iterator.hasNext();){
                var p=iterator.next();
                if(!level.hasChunkAt(p)){iterator.remove();stale=true;continue;}
                var source=level.getCapability(EssentiaCapabilities.SOURCE,p,null);
                if(source==null){iterator.remove();stale=true;continue;}
                if(source.takeFromContainer(aspect,1))return p;
            }
            sources.clear();retryAfter=stale?0:System.currentTimeMillis()+5000;
            if(!stale)return null;
        }
        return null;
    }
    public void refresh(){sources.clear();retryAfter=0;}
}
