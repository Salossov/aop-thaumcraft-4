package thaumcraft.api.world;

/** Read-only live bore state, preserving the original peripheral's distinct working predicate. */
public interface IArcaneBore {
    boolean base();
    State inspectBore();
    record State(boolean hasPickaxe, boolean hasFocus, boolean pickaxeBroken, boolean working,
                 int diameter, int speed, boolean nativeClusters, int fortune, boolean silkTouch) { }
}
