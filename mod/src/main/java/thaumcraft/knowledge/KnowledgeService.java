package thaumcraft.knowledge;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import thaumcraft.Thaumcraft;
import thaumcraft.api.knowledge.PlayerKnowledge;
import thaumcraft.api.aspects.Aspects;
import thaumcraft.network.KnowledgePayload;
import net.neoforged.neoforge.network.PacketDistributor;

public final class KnowledgeService {
    private static final class WorldState {final WorldKnowledgeStore store;final UUID session;long snapshotSequence;WorldState(WorldKnowledgeStore store,UUID session){this.store=store;this.session=session;}}
    private static final Map<MinecraftServer,WorldState> WORLDS=new IdentityHashMap<>();
    public static void register() {
        NeoForge.EVENT_BUS.addListener(KnowledgeService::login);NeoForge.EVENT_BUS.addListener(KnowledgeService::logout);
        NeoForge.EVENT_BUS.addListener(KnowledgeService::respawn);NeoForge.EVENT_BUS.addListener(KnowledgeService::dimension);
        NeoForge.EVENT_BUS.addListener(KnowledgeService::tick);NeoForge.EVENT_BUS.addListener(KnowledgeService::stopped);
        NeoForge.EVENT_BUS.addListener(KnowledgeService::dataReload);
    }
    private static WorldState world(MinecraftServer server) {
        return WORLDS.computeIfAbsent(server,s->{ try { return new WorldState(new WorldKnowledgeStore(s.getWorldPath(LevelResource.ROOT)),UUID.randomUUID()); }
            catch(IOException error) { throw new UncheckedIOException(error); } });
    }
    public static PlayerKnowledge get(ServerPlayer player) {
        try { return world(player.server).store.get(player.getUUID()); }catch(IOException error) { throw new UncheckedIOException(error); }
    }
    public static UUID session(ServerPlayer player) { return world(player.server).session; }
    public static UUID session(MinecraftServer server) { return world(server).session; }
    public static java.util.List<String> completedResearch(MinecraftServer server,UUID author) {
        if(!server.isSameThread())throw new IllegalStateException("Knowledge lookup requires server thread");
        try{return java.util.List.copyOf(world(server).store.get(author).completedResearch());}
        catch(IOException error){throw new UncheckedIOException(error);}
    }
    public static boolean completeSharedResearch(ServerPlayer player,String key) {
        return mutate(player,k->k.completeResearch(key),false);
    }
    public static boolean mutate(ServerPlayer player,Predicate<PlayerKnowledge> mutation) {
        return mutate(player,mutation,true);
    }
    private static boolean mutate(ServerPlayer player,Predicate<PlayerKnowledge> mutation,boolean expandSiblings) {
        if(!player.server.isSameThread()) throw new IllegalStateException("Knowledge changes require server thread");
        var before=get(player);var state=before.copy();if(!mutation.test(state)) return false;
        var definitions=thaumcraft.data.ThaumcraftData.get(player.server).research();boolean discovered;
        do {discovered=thaumcraft.api.research.ResearchLinks.completeAliases(state,definitions);
            if(expandSiblings)for(var key:java.util.List.copyOf(state.completedResearch())){var definition=definitions.get(key);if(definition!=null)for(var sibling:definition.siblings())discovered|=state.completeResearch(sibling);}
        }while(discovered);
        int permanent=state.permanentWarp(),sticky=state.stickyWarp();
        // Original grants warp per completed forbidden research: each gets its own
        // perm packet (whispers) and a sticky packet when its sticky half is nonzero,
        // so a bulk grant floods the stack just like the original did.
        int perms=0,stickies=0;var warpNotices=new java.util.ArrayList<String>();
        for(var key:state.completedResearch()){if(before.completedResearch().contains(key))continue;var definition=definitions.get(key);if(definition!=null&&definition.linkedResearch().isEmpty()&&definition.warp()>0&&!thaumcraft.config.ThaumcraftConfig.WUSS_MODE.get()){int w=definition.warp();permanent+=(w+1)/2;sticky+=w/2;perms++;warpNotices.add("tc.addwarp");if(w/2>0){stickies++;warpNotices.add("tc.addwarpsticky");}}}
        boolean gainedWarp=perms>0||stickies>0;
        if(gainedWarp)state.setWarp(permanent,sticky,state.temporaryWarp(),permanent+sticky+state.temporaryWarp());
        state.changed();
        try { world(player.server).store.commit(player.getUUID(),state); }catch(IOException error) { throw new UncheckedIOException(error); }
        sync(player);
        // Every successful knowledge transaction can award points (scanning,
        // research-table refunds, knowledge rewards). Notify the actual delta,
        // rather than relying on individual callers or the client's high-water mark.
        if(player.connection!=null&&before.revision()>0){
            ScanService.notifyDiscoveries(player,before,state);
            for(var entry:state.aspectPools().entrySet()){
                int gained=entry.getValue()-before.aspectPools().getOrDefault(entry.getKey(),0);
                if(gained>0)thaumcraft.network.Notify.pool(player,entry.getKey(),gained);
            }
        }
        // Original TC4 announces fresh warp per research in the right-edge notice stack:
        // one PacketWarpMessage per completed forbidden entry, perm ones with whispers.
        for(var notice:warpNotices)thaumcraft.network.Notify.warp(player,notice);
        return true;
    }
    public static void sync(ServerPlayer player) {
        if(player.connection!=null) {var world=world(player.server);var data=KnowledgeNbt.encode(get(player));var definitions=thaumcraft.data.ThaumcraftData.get(player.server);thaumcraft.data.ResearchDataSync.encode(data,definitions.research());thaumcraft.data.AspectDataSync.encode(data,definitions);thaumcraft.addon.BookData.encode(data,thaumcraft.data.ThaumcraftData.books(player.server));data.putLong("snapshotSequence",++world.snapshotSequence);PacketDistributor.sendToPlayer(player,new KnowledgePayload(player.getUUID(),world.session,data));NeoForge.EVENT_BUS.post(new thaumcraft.api.addon.KnowledgeChangedEvent(player.getUUID(),get(player),false));}
    }
    private static void dataReload(net.neoforged.neoforge.event.OnDatapackSyncEvent event){if(event.getPlayer()==null)NeoForge.EVENT_BUS.post(new thaumcraft.api.addon.CatalogChangedEvent(event.getPlayerList().getServer().getTickCount(),event.getPlayerList().getServer()));if(event.getPlayer()!=null)sync(event.getPlayer());else for(var player:event.getPlayerList().getPlayers()){initialize(player);sync(player);}}
    private static void login(PlayerEvent.PlayerLoggedInEvent event) {
        if(event.getEntity() instanceof ServerPlayer player) try { initialize(player);sync(player);if(player.hasPermissions(2)&&player.getPersistentData().getBoolean("ThaumcraftInfusionDebug"))PacketDistributor.sendToPlayer(player,new thaumcraft.network.AdminUiPayload(1,player.blockPosition(),true)); }
        catch(UncheckedIOException error) { Thaumcraft.LOGGER.error("TC4 knowledge was not loaded; files retained",error);player.connection.disconnect(Component.literal("Thaumcraft: cannot load your knowledge file. See server log; your files were retained.")); }
    }
    private static void logout(PlayerEvent.PlayerLoggedOutEvent event) {
        if(event.getEntity() instanceof ServerPlayer player) try { world(player.server).store.evict(player.getUUID()); }
        catch(IOException error) { Thaumcraft.LOGGER.error("Cannot save TC4 knowledge",error); }
    }
    private static void respawn(PlayerEvent.PlayerRespawnEvent event) { if(event.getEntity() instanceof ServerPlayer player) sync(player); }
    private static void dimension(PlayerEvent.PlayerChangedDimensionEvent event) { if(event.getEntity() instanceof ServerPlayer player) sync(player); }
    private static void tick(ServerTickEvent.Post event) {
        if(event.getServer().getTickCount()%200!=0) return;
        var world=WORLDS.get(event.getServer());if(world!=null) try { world.store.flush(); }catch(IOException error) { Thaumcraft.LOGGER.error("TC4 knowledge autosave failed; dirty data retained",error); }
    }
    private static void stopped(ServerStoppedEvent event) {
        var world=WORLDS.remove(event.getServer());if(world!=null) try { world.store.close(); }catch(IOException error) { Thaumcraft.LOGGER.error("TC4 knowledge final save failed",error); }
    }
    public static int loadedWorldCount() { return WORLDS.size(); }
    public static void initialize(ServerPlayer player) {
        var definitions=thaumcraft.data.ThaumcraftData.get(player.server).research();
        mutate(player,k->{
            boolean changed=false;
            // TC4 ResearchManager.loadPlayerData: grant 15 + nextInt(5) once to a new player.
            if(k.revision()==0) {
                for(var aspect:Aspects.PRIMALS) k.setPool(aspect.getTag(),15+player.level().random.nextInt(5));
                changed=true;
            }
            for(var definition:definitions.values()) if(definition.autoUnlock()&&definition.linkedResearch().isEmpty()) changed|=k.completeResearch(definition.key());
            changed|=thaumcraft.api.research.ResearchLinks.completeAliases(k,definitions);
            return changed;
        });
    }
    private KnowledgeService() {}
}
