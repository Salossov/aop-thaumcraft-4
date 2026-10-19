package thaumcraft.api.world;

/**
 * Marker for a Block whose air behavior must not prevent its first insertion into
 * an empty chunk section. Keep NeoForge's isEmpty(state) false (the default for
 * custom blocks), so section counting and serialization retain the state.
 * This does not alter isAir, collision, rendering, lighting or placement rules.
 */
public interface IPersistentAir { }
