package thaumcraft.api.world;

/** A live arcane ear's original signed-byte pitch; mutations require the server thread. */
public interface IArcaneEar {
    int note();
    void setNote(byte note);
}
