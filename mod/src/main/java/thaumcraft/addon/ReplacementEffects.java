package thaumcraft.addon;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.Map;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.Vec3;

/** Operation-local item/XP escrow; hooks run before joining the world or merging XP. */
public final class ReplacementEffects implements AutoCloseable {
    private interface Deferred {void publish(ServerLevel level);}
    private static final Map<ServerLevel,ReplacementEffects> ACTIVE=new IdentityHashMap<>();
    private final ServerLevel level;
    private final ArrayList<Deferred> effects=new ArrayList<>();
    private boolean closed;

    static ReplacementEffects open(ServerLevel level){
        if(!level.getServer().isSameThread())throw new IllegalStateException("Server thread required");
        if(ACTIVE.containsKey(level))throw new IllegalStateException("Nested block drop escrow");
        var scope=new ReplacementEffects(level);ACTIVE.put(level,scope);return scope;
    }
    private ReplacementEffects(ServerLevel level){this.level=level;}
    /** True asks the narrow addEntity hook to report provisional success without insertion. */
    public static boolean captureEntity(ServerLevel level,Entity entity){
        if(!level.getServer().isSameThread()||entity.isRemoved()||!(entity instanceof ItemEntity||entity instanceof ExperienceOrb))return false;
        var scope=ACTIVE.get(level);if(scope==null)return false;
        scope.effects.add(world->world.addFreshEntity(entity));return true;
    }
    /** Capture award itself, before vanilla can increment a pre-existing orb. */
    public static boolean captureExperience(ServerLevel level,Vec3 pos,int amount){
        if(!level.getServer().isSameThread())return false;
        var scope=ACTIVE.get(level);if(scope==null)return false;
        if(amount>0)scope.effects.add(world->ExperienceOrb.award(world,pos,amount));return true;
    }
    void publish(){
        if(closed)throw new IllegalStateException("Block drop escrow closed");
        // Native insertion/merge must run outside capture so each event fires once.
        ACTIVE.remove(level,this);closed=true;RuntimeException problem=null;
        try{
            for(var effect:effects)try{effect.publish(level);}
            catch(RuntimeException failure){if(problem==null)problem=failure;else problem.addSuppressed(failure);}
        }finally{effects.clear();}
        if(problem!=null)throw problem;
    }
    @Override public void close(){
        if(closed)return;closed=true;ACTIVE.remove(level,this);effects.clear();
    }
}
