package thaumcraft.api.entities;

import java.util.Objects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Creeper;

/** In-place legacy goal mutations, not saved to NBT and not undone automatically.
 * Visits both goal selectors and preserves goal identity, priority, predicates,
 * running state and random intervals. Server game thread only.
 */
public final class MobBehaviorApi {
    public interface Access {
        int redirectAvoidance(Mob mob,Class<? extends LivingEntity> from,Class<? extends LivingEntity> to);
        int redirectNearestTarget(Mob mob,Class<? extends Entity> from,Class<? extends Entity> to);
        void setSwell(Creeper creeper,int ticks);
    }
    private static Access access;
    public static void install(Access implementation) {
        if(access!=null)throw new IllegalStateException("Mob behavior API already installed");
        access=Objects.requireNonNull(implementation);
    }
    private static Access access(Mob mob) {
        Objects.requireNonNull(mob);
        if(mob.level().isClientSide()||mob.getServer()==null||!mob.getServer().isSameThread())throw new IllegalStateException("Server game thread required");
        if(access==null)throw new IllegalStateException("Mob behavior API not installed");return access;
    }
    public static int redirectAvoidance(Mob mob,Class<? extends LivingEntity> from,Class<? extends LivingEntity> to) {
        return access(mob).redirectAvoidance(mob,Objects.requireNonNull(from),Objects.requireNonNull(to));
    }
    /** Non-living destination classes produce no eligible new targets, matching
     * the old nearest-target filter. Existing mob targets are left untouched.
     */
    public static int redirectNearestTarget(Mob mob,Class<? extends Entity> from,Class<? extends Entity> to) {
        return access(mob).redirectNearestTarget(mob,Objects.requireNonNull(from),Objects.requireNonNull(to));
    }
    /** Changes only current fuse progress, not ignition, direction or previous progress. */
    public static void setSwell(Creeper creeper,int ticks) {access(creeper).setSwell(creeper,ticks);}
    private MobBehaviorApi() {}
}
