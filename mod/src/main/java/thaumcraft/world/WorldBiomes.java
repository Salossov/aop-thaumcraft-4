package thaumcraft.world;

import java.lang.ref.WeakReference;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.game.ClientboundChunksBiomesPacket;
import net.minecraft.resources.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.*;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.LevelEvent;
import thaumcraft.config.ThaumcraftConfig;
import thaumcraft.network.ClientBiomeColumns;

public final class WorldBiomes {
    public static final ResourceKey<Biome> EERIE=key("eerie"),TAINT=key("tainted_land"),MAGICAL=key("magical_forest");
    private static final Map<BiomeManager,WeakReference<Level>> LEVELS=Collections.synchronizedMap(new WeakHashMap<>());
    private static final Map<ServerLevel,BiomeColumnData> DATA=Collections.synchronizedMap(new WeakHashMap<>());
    private static ResourceKey<Biome> key(String name) { return ResourceKey.create(Registries.BIOME,ResourceLocation.fromNamespaceAndPath("thaumcraft",name)); }
    public static boolean exactMode() { return ThaumcraftConfig.BIOME_CHANGE_MODE.get()==ThaumcraftConfig.BiomeChangeMode.TC4_1X1; }
    public static void register() {
        NeoForge.EVENT_BUS.addListener(WorldBiomes::load);NeoForge.EVENT_BUS.addListener(WorldBiomes::unload);BiomeColumnSync.register();
    }
    private static void load(LevelEvent.Load event) {
        if(event.getLevel() instanceof Level level) {
            LEVELS.put(level.getBiomeManager(),new WeakReference<>(level));
            if(level instanceof ServerLevel server) DATA.put(server,BiomeColumnData.get(server));
        }
    }
    private static void unload(LevelEvent.Unload event) {
        if(event.getLevel() instanceof Level level) { LEVELS.remove(level.getBiomeManager());if(level instanceof ServerLevel server) DATA.remove(server); }
    }
    public static BiomeColumnData data(ServerLevel level) { return DATA.computeIfAbsent(level,BiomeColumnData::get); }
    /** TC4 mechanics see the overlay even with global integration disabled. */
    public static Holder<Biome> effective(Level level,BlockPos pos) {
        var override=overlay(level,pos,false);if(override!=null)return override;
        // Read the quart column that TC4 edits. BiomeManager.getBiome jitters into
        // neighboring columns, which otherwise hides a freshly changed column.
        return !exactMode()?level.getNoiseBiome(QuartPos.fromBlock(pos.getX()),QuartPos.fromBlock(pos.getY()),QuartPos.fromBlock(pos.getZ())):level.getBiome(pos);
    }
    public static boolean is(ServerLevel level,BlockPos pos,ResourceKey<Biome> biome) { return effective(level,pos).is(biome); }
    /** Called at BiomeManager.getBiome HEAD without recursively querying biomes. */
    public static Holder<Biome> globalOverride(BiomeManager manager,BlockPos pos) {
        var reference=LEVELS.get(manager);var level=reference==null?null:reference.get();return level==null?null:overlay(level,pos,true);
    }
    private static Holder<Biome> overlay(Level level,BlockPos pos,boolean globalOnly) {
        String name;
        if(level instanceof ServerLevel server) {
            if(!exactMode()||(globalOnly&&!ThaumcraftConfig.BIOME_OVERLAY_GLOBAL.get())) return null;
            var data=DATA.get(server);if(data==null) return null;name=data.columns.get(pos.getX(),pos.getZ());
        } else {
            if(globalOnly&&!ClientBiomeColumns.global()) return null;
            name=ClientBiomeColumns.get(level.dimension().location(),pos.getX(),pos.getZ());
        }
        if(name==null) return null;
        return level.registryAccess().registryOrThrow(Registries.BIOME).getHolder(ResourceKey.create(Registries.BIOME,ResourceLocation.parse(name))).orElse(null);
    }
    public static boolean change(ServerLevel level,int x,int z,ResourceKey<Biome> biome) {
        if(!level.getServer().isSameThread()) throw new IllegalStateException("Biome changes require server thread");
        if(!ThaumcraftConfig.NODE_BIOME_CHANGES.get()||!level.hasChunk(x>>4,z>>4)) return false;
        var replacement=level.registryAccess().registryOrThrow(Registries.BIOME).getHolderOrThrow(biome);
        if(exactMode()) {
            var data=data(level);if(!data.columns.set(x,z,biome.location().toString())) return false;
            data.setDirty();BiomeColumnSync.changed(level,x>>4,z>>4);return true;
        }
        var chunk=level.getChunk(x>>4,z>>4);
        int qx=QuartPos.fromBlock(x),qz=QuartPos.fromBlock(z);boolean changed=false;
        var sampler=level.getChunkSource().randomState().sampler();
        for(int index=0;index<chunk.getSectionsCount();index++) {
            var section=chunk.getSection(index);int qy=QuartPos.fromBlock(chunk.getSectionYFromSectionIndex(index)<<4);
            boolean same=true;for(int y=0;y<4;y++) same&=section.getNoiseBiome(qx&3,y,qz&3).is(biome);
            if(same) continue;
            var old=new java.util.ArrayList<net.minecraft.core.Holder<Biome>>();for(int a=0;a<4;a++) for(int b=0;b<4;b++) for(int c=0;c<4;c++) old.add(section.getNoiseBiome(a,b,c));
            section.fillBiomesFromNoise((a,b,c,s)->a==qx&&c==qz?replacement:old.get((a&3)*16+(b&3)*4+(c&3)),sampler,chunk.getPos().x*4,qy,chunk.getPos().z*4);changed=true;
        }
        if(changed) {
            chunk.setUnsaved(true);var packet=ClientboundChunksBiomesPacket.forChunks(List.of(chunk));
            for(var player:level.players()) if(player.chunkPosition().getChessboardDistance(chunk.getPos())<=level.getServer().getPlayerList().getViewDistance()+1&&player.connection!=null) player.connection.send(packet);
        }return changed;
    }
    private WorldBiomes() {}
}
