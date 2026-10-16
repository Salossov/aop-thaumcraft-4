package thaumcraft.knowledge;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import thaumcraft.Thaumcraft;
import thaumcraft.api.knowledge.PlayerKnowledge;

/** One instance per loaded world/server. No name-based or cross-world state. */
public final class WorldKnowledgeStore implements AutoCloseable {
    public static final int SCHEMA_VERSION=1;
    private record Entry(PlayerKnowledge state,CompoundTag original,boolean recovered) {}
    private final Path directory;
    private final Map<UUID,Entry> loaded=new HashMap<>();
    private final java.util.Set<UUID> dirty=new java.util.HashSet<>();
    private boolean closed;
    public WorldKnowledgeStore(Path world) throws IOException {
        directory=world.toAbsolutePath().normalize().resolve("thaumcraft/players");Files.createDirectories(directory);
    }
    public synchronized PlayerKnowledge get(UUID uuid) throws IOException {
        if(closed) throw new IllegalStateException("Store already unloaded");
        var entry=loaded.get(uuid);if(entry==null) { entry=load(uuid);loaded.put(uuid,entry); }return entry.state;
    }
    public synchronized void changed(UUID uuid) { dirty.add(uuid); }
    public synchronized void commit(UUID uuid,PlayerKnowledge state) throws IOException {
        get(uuid);var entry=loaded.get(uuid);loaded.put(uuid,new Entry(state,entry.original,entry.recovered));dirty.add(uuid);
    }
    public Path file(UUID uuid) { return directory.resolve(uuid+".thaum"); }
    private Entry read(Path path,UUID uuid,boolean recovered) throws IOException {
        var root=NbtIo.readCompressed(path,NbtAccounter.create(16*1024*1024));
        int schema=root.getInt("schemaVersion");
        if(schema!=SCHEMA_VERSION) throw new UnsupportedSchemaException("Unsupported knowledge schema "+schema+" in "+path);
        if(!root.hasUUID("playerUUID")||!uuid.equals(root.getUUID("playerUUID"))) throw new IOException("Wrong player UUID in "+path);
        if(!root.contains("data",10)) throw new IOException("Missing knowledge data in "+path);
        return new Entry(KnowledgeNbt.decode(root.getCompound("data")),root,recovered);
    }
    private Entry load(UUID uuid) throws IOException {
        var file=file(uuid);var backup=directory.resolve(uuid+".thaumbak");IOException error=null;
        if(Files.exists(file)) try { return read(file,uuid,false); }
            catch(UnsupportedSchemaException future) { throw future; }
            catch(IOException|RuntimeException bad) { error=new IOException("Cannot load "+file,bad); }
        if(Files.exists(backup)) try {
            var entry=read(backup,uuid,true);Thaumcraft.LOGGER.warn("Recovered TC4 knowledge for {} from {}",uuid,backup);return entry;
        } catch(IOException|RuntimeException bad) { if(error!=null) bad.addSuppressed(error);throw new IOException("Knowledge and backup unavailable for "+uuid,bad); }
        if(error!=null) throw error;
        return new Entry(new PlayerKnowledge(),new CompoundTag(),false);
    }
    public synchronized void save(UUID uuid) throws IOException {
        var entry=loaded.get(uuid);if(entry==null||!dirty.contains(uuid)) return;
        var target=file(uuid);var temporary=directory.resolve(uuid+".thaum.tmp");var backup=directory.resolve(uuid+".thaumbak");
        var root=entry.original.copy();root.putInt("schemaVersion",SCHEMA_VERSION);root.putUUID("playerUUID",uuid);
        var data=entry.original.getCompound("data").copy();data.merge(KnowledgeNbt.encode(entry.state));
        root.put("data",data);NbtIo.writeCompressed(root,temporary);
        try(var channel=FileChannel.open(temporary,StandardOpenOption.WRITE)) { channel.force(true); }
        if(Files.exists(target)) {
            if(entry.recovered) Files.copy(target,directory.resolve(uuid+".corrupt-"+System.currentTimeMillis()),StandardCopyOption.REPLACE_EXISTING);
            else Files.copy(target,backup,StandardCopyOption.REPLACE_EXISTING);
        }
        try { Files.move(temporary,target,StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING); }
        catch(AtomicMoveNotSupportedException unavailable) { Files.move(temporary,target,StandardCopyOption.REPLACE_EXISTING); }
        loaded.put(uuid,new Entry(entry.state,root,false));dirty.remove(uuid);
    }
    public synchronized void flush() throws IOException { for(var uuid:java.util.List.copyOf(dirty)) save(uuid); }
    public synchronized void evict(UUID uuid) throws IOException { save(uuid);loaded.remove(uuid); }
    @Override public synchronized void close() throws IOException { flush();loaded.clear();closed=true; }
    public static final class UnsupportedSchemaException extends IOException { public UnsupportedSchemaException(String message) { super(message); } }
}
