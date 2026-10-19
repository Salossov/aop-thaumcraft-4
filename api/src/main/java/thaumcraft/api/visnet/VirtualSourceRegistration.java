package thaumcraft.api.visnet;

/** Server-thread registration. Closing is idempotent; a closed registration cannot be refreshed. */
public interface VirtualSourceRegistration extends AutoCloseable {
    void refresh();
    @Override void close();
}
