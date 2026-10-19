package thaumcraft.api.world;

/** Opt-in passenger callbacks for identity-preserving block movers. */
public interface IMovableBlockEntity {
    /** Original AE-style callback; the mover's integration decides whether false vetoes. */
    default boolean prepareToMove(){return true;}
    default void doneMoving(){}
}
