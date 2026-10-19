package thaumcraft.api.addon;

/** A validated catalog has been replaced. Server events carry the owning server; clients include login and disconnect. */
public final class CatalogChangedEvent extends net.neoforged.bus.api.Event {
    private final long revision;
    private final net.minecraft.server.MinecraftServer server;
    public CatalogChangedEvent(long revision){this(revision,null);}
    public CatalogChangedEvent(long revision,net.minecraft.server.MinecraftServer server){this.revision=revision;this.server=server;}
    public boolean client(){return server==null;}
    public java.util.Optional<net.minecraft.server.MinecraftServer> server(){return java.util.Optional.ofNullable(server);}
    public long revision(){return revision;}
}
