package thaumcraft.api.addon;

/** Startup registration on an active, dedicated bus, after the core wand materials are ready. */
public final class AddonRegistrationEvent extends net.neoforged.bus.api.Event {
    /** Subscribe from the addon constructor; the NeoForge game bus is shut down during setup. */
    public static final net.neoforged.bus.api.IEventBus BUS = net.neoforged.bus.api.BusBuilder.builder().build();
}
