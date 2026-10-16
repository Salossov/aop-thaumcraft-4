package thaumcraft.verification;

import com.google.gson.GsonBuilder;
import java.nio.file.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import thaumcraft.config.ThaumcraftConfig;
import thaumcraft.network.*;
import thaumcraft.world.*;

/** Finite dedicated-server check. Never launches or connects a client. */
@Mod("thaumcraft_r2_test")
public final class BiomeVerification {
    private final List<String> checks=new ArrayList<>();
    private final Path evidence=Path.of(System.getProperty("thaumcraft.r2.root"),"analysis/r2/evidence");
    private final String phase=System.getProperty("thaumcraft.r2.phase","seed");
    private static final ResourceKey<Biome> PLAINS=ResourceKey.create(Registries.BIOME,ResourceLocation.parse("minecraft:plains"));
    private static final ResourceKey<Biome> DESERT=ResourceKey.create(Registries.BIOME,ResourceLocation.parse("minecraft:desert"));
    public BiomeVerification() { NeoForge.EVENT_BUS.addListener(this::started);if(phase.startsWith("client_"))ClientCycleFixture.register(evidence); }
    private void require(boolean value,String description) { if(!value) throw new AssertionError(description);checks.add(description); }
    private void started(ServerStartedEvent event) {
        var server=event.getServer();var level=server.overworld();if(phase.startsWith("client_"))return;Throwable failure=null;
        try {
            require(server.isDedicatedServer(),"dedicated server, no client");
            level.getChunk(-1,-1);level.getChunk(0,0);level.getChunk(1,0);
            if(phase.startsWith("gameplay")) GameplayVerification.run(level,evidence,phase.equals("gameplay_reload"),this::require);
            else if(phase.equals("flux")) FluxVerification.run(level,this::require);
            else if(phase.equals("ambient")) AmbientVerification.run(level,this::require);
            else if(phase.equals("data")) DataVerification.run(level,this::require);
            else if(phase.equals("nodes")) NodeVerification.run(level,this::require);
            else if(phase.equals("seed")) seed(level);else verifyReload(level);
            server.saveEverything(false,true,true);
        } catch(Throwable error) { failure=error;thaumcraft.Thaumcraft.LOGGER.error("R2 biome verification failed",error); }
        try {
            Files.createDirectories(evidence);var result=new LinkedHashMap<String,Object>();
            result.put("passed",failure==null);result.put("phase",phase);result.put("checks",checks);result.put("time",java.time.Instant.now().toString());
            result.put("client","Separate client QA; this report covers dedicated server only");if(failure!=null) result.put("error",failure.toString());
            Files.writeString(evidence.resolve((phase.startsWith("gameplay")?"gameplay":phase.equals("flux")?"fluids":phase.equals("ambient")?"creatures":phase.equals("data")?"definitions":phase.equals("nodes")?"nodes":"biomes")+"-"+phase+".json"),new GsonBuilder().setPrettyPrinting().create().toJson(result)+"\n");
        } catch(java.io.IOException error) { throw new java.io.UncheckedIOException(error); }
        server.halt(false);
    }
    private void seed(ServerLevel level) {
        ThaumcraftConfig.NODE_BIOME_CHANGES.set(true);ThaumcraftConfig.BIOME_CHANGE_MODE.set(ThaumcraftConfig.BiomeChangeMode.TC4_1X1);ThaumcraftConfig.BIOME_OVERLAY_GLOBAL.set(false);
        WorldBiomes.change(level,-1,-1,PLAINS);
        var pos=new BlockPos(-1,100,-1);var neighbour=pos.west();var nativeBefore=level.getBiome(pos);var neighbourBefore=level.getBiome(neighbour);
        require(WorldBiomes.change(level,-1,-1,DESERT),"exact negative column changed");
        require(WorldBiomes.effective(level,pos).is(DESERT),"TC4 sees 1x1 with global off");
        require(level.getBiome(pos).equals(nativeBefore),"general biome query unchanged with global off");
        require(WorldBiomes.effective(level,neighbour).equals(neighbourBefore),"adjacent column unaffected inside same quart");
        require(WorldBiomes.effective(level,pos.atY(-60)).is(DESERT)&&WorldBiomes.effective(level,pos.atY(300)).is(DESERT),"overlay covers every height");
        ThaumcraftConfig.BIOME_OVERLAY_GLOBAL.set(true);
        require(level.getBiome(pos).is(DESERT),"Mixin exposes exact column with global on");
        require(level.getBiome(neighbour).equals(neighbourBefore),"Mixin preserves adjacent column");
        require(level.getChunk(-1,-1).getNoiseBiome(-1,25,-1).equals(nativeBefore)||!level.getChunk(-1,-1).getNoiseBiome(-1,25,-1).is(DESERT),"overlay does not replace native quart");
        ThaumcraftConfig.NODE_BIOME_CHANGES.set(false);require(!WorldBiomes.change(level,-2,-1,DESERT),"biome-change master switch respected");ThaumcraftConfig.NODE_BIOME_CHANGES.set(true);
        require(!WorldBiomes.change(level,100000,100000,DESERT)&&!level.hasChunk(100000>>4,100000>>4),"change does not force-load distant chunk");
        var saved=WorldBiomes.data(level).save(new CompoundTag(),level.registryAccess());var restored=BiomeColumnData.load(saved,level.registryAccess());
        require(restored.columns.get(-1,-1).equals("minecraft:desert")&&restored.columns.get(-2,-1)==null,"NBT round trip preserves exact coordinates");
        var end=level.getServer().getLevel(net.minecraft.world.level.Level.END);require(WorldBiomes.data(end).columns.get(-1,-1)==null,"dimensions have separate maps");
        var session=UUID.randomUUID();var dimension=level.dimension().location();
        ClientBiomeColumns.accept(new BiomeColumnsPayload(session,dimension,true,false,true,false,0,0,new thaumcraft.api.world.BiomeColumns.Chunk(0,Map.of())));
        var snapshot=new BiomeColumnsPayload(session,dimension,true,false,false,false,-1,-1,restored.columns.chunk(-1,-1));
        require(ClientBiomeColumns.accept(snapshot)&&ClientBiomeColumns.get(dimension,-1,-1).equals("minecraft:desert"),"client mirror receives bounded chunk snapshot without client process");
        require(!ClientBiomeColumns.global(),"global-off flag reaches client mirror");
        require(!ClientBiomeColumns.accept(new BiomeColumnsPayload(UUID.randomUUID(),dimension,true,true,false,false,-1,-1,snapshot.chunk())),"other server session snapshot rejected");
        require(!ClientBiomeColumns.accept(snapshot),"replayed chunk snapshot rejected");ClientBiomeColumns.clear();
        require(ClientBiomeColumns.get(dimension,-1,-1)==null,"disconnect mirror cleared");
        ThaumcraftConfig.BIOME_CHANGE_MODE.set(ThaumcraftConfig.BiomeChangeMode.REAL_4X4);
        require(!level.getBiome(pos).is(DESERT),"switching to real mode deactivates overlay");
        WorldBiomes.change(level,16,0,PLAINS);
        require(WorldBiomes.change(level,16,0,DESERT),"native 4x4 column changed");
        var chunk=level.getChunk(1,0);
        for(int s=0;s<chunk.getSectionsCount();s++) for(int y=0;y<4;y++) require(chunk.getSection(s).getNoiseBiome(0,y,0).is(DESERT),"native column all quart heights section="+s+" y="+y);
        ThaumcraftConfig.BIOME_CHANGE_MODE.set(ThaumcraftConfig.BiomeChangeMode.TC4_1X1);
        require(level.getBiome(pos).is(DESERT),"switching back restores retained overlay");
        require(WorldBiomes.data(level).isDirty(),"overlay changes scheduled for world save");
    }
    private void verifyReload(ServerLevel level) {
        require(WorldBiomes.data(level).columns.get(-1,-1).equals("minecraft:desert"),"exact column survives dedicated server restart");
        require(WorldBiomes.data(level).columns.get(-2,-1)==null,"untouched neighbour survives restart");
        ThaumcraftConfig.BIOME_CHANGE_MODE.set(ThaumcraftConfig.BiomeChangeMode.TC4_1X1);ThaumcraftConfig.BIOME_OVERLAY_GLOBAL.set(true);
        require(level.getBiome(new BlockPos(-1,100,-1)).is(DESERT),"global Mixin works after restart");
        require(level.getChunk(1,0).getSection(0).getNoiseBiome(0,0,0).is(DESERT),"native 4x4 change survives restart");
        ThaumcraftConfig.BIOME_OVERLAY_GLOBAL.set(false);
        require(WorldBiomes.effective(level,new BlockPos(-1,100,-1)).is(DESERT)&&!level.getBiome(new BlockPos(-1,100,-1)).is(DESERT),"TC4-only mode after restart");
    }
}
