package thaumcraft.api.entities;

import java.util.Objects;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.*;

/** TC4's living-attack timing: at LivingEntity.hurt entry, before its rejection checks.
 * Posted on both logical sides on the calling game thread. This is an attempt, not
 * proof of damage or a player attack; it can fire for invulnerable or dead targets.
 * Subscribe to BUS, not the NeoForge bus. Cancellation rejects the hurt call.
 */
public final class LivingAttackAttemptEvent extends Event implements ICancellableEvent {
    public static final IEventBus BUS=BusBuilder.builder().build();
    private final LivingEntity target;
    private final DamageSource source;
    private final float amount;
    public LivingAttackAttemptEvent(LivingEntity target,DamageSource source,float amount){this.target=Objects.requireNonNull(target);this.source=Objects.requireNonNull(source);this.amount=amount;}
    public LivingEntity target(){return target;}
    public DamageSource source(){return source;}
    public float amount(){return amount;}
}
