package thaumcraft.world;

import java.util.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.*;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.ChunkWatchEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import thaumcraft.api.world.BiomeColumns;
import thaumcraft.config.ThaumcraftConfig;
import thaumcraft.knowledge.KnowledgeService;
import thaumcraft.network.BiomeColumnsPayload;

public final class BiomeColumnSync {
    private record Watched(ServerLevel level,int x,int z) {}
    private static final Map<ServerPlayer,Set<Watched>> WATCHED=new IdentityHashMap<>();
    private static final Map<MinecraftServer,String> MODES=new IdentityHashMap<>();
    public static void register() {
        NeoForge.EVENT_BUS.addListener(BiomeColumnSync::sent);NeoForge.EVENT_BUS.addListener(BiomeColumnSync::unwatch);
        NeoForge.EVENT_BUS.addListener(BiomeColumnSync::login);NeoForge.EVENT_BUS.addListener(BiomeColumnSync::logout);
        NeoForge.EVENT_BUS.addListener(BiomeColumnSync::tick);NeoForge.EVENT_BUS.addListener(BiomeColumnSync::stopped);
    }
    private static void send(ServerPlayer player,ServerLevel level,int x,int z,boolean reset,boolean forget) {
        if(player.connection==null) return;
        var chunk=reset||forget?new BiomeColumns.Chunk(0,Map.of()):WorldBiomes.data(level).columns.chunk(x,z);
        PacketDistributor.sendToPlayer(player,new BiomeColumnsPayload(KnowledgeService.session(player),level.dimension().location(),WorldBiomes.exactMode(),ThaumcraftConfig.BIOME_OVERLAY_GLOBAL.get(),reset,forget,x,z,chunk));
    }
    private static void login(PlayerEvent.PlayerLoggedInEvent event) {
        if(event.getEntity() instanceof ServerPlayer player) {
            send(player,player.serverLevel(),0,0,true,false);
            for(var watched:WATCHED.getOrDefault(player,Set.of())) send(player,watched.level,watched.x,watched.z,false,false);
        }
    }
    private static void logout(PlayerEvent.PlayerLoggedOutEvent event) { if(event.getEntity() instanceof ServerPlayer player) WATCHED.remove(player); }
    private static void sent(ChunkWatchEvent.Sent event) {
        WATCHED.computeIfAbsent(event.getPlayer(),p->new HashSet<>()).add(new Watched(event.getLevel(),event.getPos().x,event.getPos().z));
        send(event.getPlayer(),event.getLevel(),event.getPos().x,event.getPos().z,false,false);
    }
    private static void unwatch(ChunkWatchEvent.UnWatch event) {
        var watched=WATCHED.get(event.getPlayer());if(watched!=null) watched.remove(new Watched(event.getLevel(),event.getPos().x,event.getPos().z));
        send(event.getPlayer(),event.getLevel(),event.getPos().x,event.getPos().z,false,true);
    }
    public static void changed(ServerLevel level,int x,int z) {
        for(var entry:WATCHED.entrySet()) if(entry.getKey().server==level.getServer()&&entry.getValue().contains(new Watched(level,x,z))) send(entry.getKey(),level,x,z,false,false);
    }
    private static void tick(ServerTickEvent.Post event) {
        String mode=ThaumcraftConfig.BIOME_CHANGE_MODE.get()+":"+ThaumcraftConfig.BIOME_OVERLAY_GLOBAL.get();
        var old=MODES.put(event.getServer(),mode);if(old==null||old.equals(mode)) return;
        for(var entry:WATCHED.entrySet()) if(entry.getKey().server==event.getServer()) {
            send(entry.getKey(),entry.getKey().serverLevel(),0,0,true,false);
            for(var watched:entry.getValue()) send(entry.getKey(),watched.level,watched.x,watched.z,false,false);
        }
    }
    private static void stopped(ServerStoppedEvent event) { MODES.remove(event.getServer());WATCHED.keySet().removeIf(player->player.server==event.getServer()); }
    private BiomeColumnSync() {}
}
