package thaumcraft.api.entities;

import java.util.Objects;
import java.util.function.ObjIntConsumer;
import net.minecraft.world.effect.MobEffectInstance;

/** Exact in-place duration edit. Call on the owning world's game thread.
 * Preserves the instance, amplifier, ambient/visibility flags and hidden-effect chain.
 * Unlike update(), this can replace an infinite duration (-1) with a finite one.
 */
public final class EffectDurationApi {
    private static ObjIntConsumer<MobEffectInstance> access;
    public static void install(ObjIntConsumer<MobEffectInstance> implementation) {
        if(access!=null)throw new IllegalStateException("Effect duration API already installed");
        access=Objects.requireNonNull(implementation);
    }
    public static void setDuration(MobEffectInstance effect,int ticks) {
        if(ticks < -1)throw new IllegalArgumentException("Invalid effect duration");
        if(access==null)throw new IllegalStateException("Effect duration API not installed");
        access.accept(Objects.requireNonNull(effect),ticks);
    }
    private EffectDurationApi() {}
}
