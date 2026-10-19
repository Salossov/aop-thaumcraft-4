package thaumcraft.api.entities;

import java.util.Objects;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.BusBuilder;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.IEventBus;

/** Equipment update at LivingEntity.aiStep entry, on either logical side's game thread.
 * Subscribe to BUS. This phase precedes jumping, travel and player air-speed selection.
 */
public final class LivingEquipmentTickEvent extends Event {
    public static final IEventBus BUS=BusBuilder.builder().build();
    private final LivingEntity entity;
    public LivingEquipmentTickEvent(LivingEntity entity) {this.entity=Objects.requireNonNull(entity);}
    public LivingEntity entity() {return entity;}
}
