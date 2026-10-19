package thaumcraft.world;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.*;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import thaumcraft.config.ThaumcraftConfig;

/** Source population runs on the server after FULL, outside Feature write-radius restrictions. */
public final class NaturalPopulation {
    private static final class Pending {
        final NaturalPopulationData.Request request;
        final ChunkPreparation chunks;
        final boolean background;
        boolean templeResolved;
        ChunkPreparation structureChunks;
        net.minecraft.core.BlockPos structure;
        Pending(ServerLevel level,NaturalPopulationData.Request request,boolean background){this.request=request;this.background=background;var p=new ChunkPos(request.chunk());chunks=new ChunkPreparation(level,p.x-2,p.x+2,p.z-2,p.z+2);}
        void close(){chunks.close();if(structureChunks!=null)structureChunks.close();}
    }
    private static final java.util.Map<ServerLevel,Pending> PENDING=new java.util.IdentityHashMap<>();
    public static final String MARK="thaumcraft_population_version";
    private static final net.neoforged.neoforge.registries.DeferredRegister<net.neoforged.neoforge.attachment.AttachmentType<?>> ATTACHMENTS=net.neoforged.neoforge.registries.DeferredRegister.create(net.neoforged.neoforge.registries.NeoForgeRegistries.Keys.ATTACHMENT_TYPES,"thaumcraft");
    public static final java.util.function.Supplier<net.neoforged.neoforge.attachment.AttachmentType<String>> VERSION=ATTACHMENTS.register("population_version",()->net.neoforged.neoforge.attachment.AttachmentType.builder(()->"").serialize(com.mojang.serialization.Codec.STRING).build());
    public static void register(net.neoforged.bus.api.IEventBus bus){ATTACHMENTS.register(bus);NeoForge.EVENT_BUS.addListener(NaturalPopulation::loaded);NeoForge.EVENT_BUS.addListener(NaturalPopulation::tick);NeoForge.EVENT_BUS.addListener(NaturalPopulation::unloaded);}
    private static void unloaded(net.neoforged.neoforge.event.level.LevelEvent.Unload event){if(event.getLevel() instanceof ServerLevel level)cancel(level);}
    private static void cancel(ServerLevel level){var pending=PENDING.remove(level);if(pending!=null)pending.close();}
    private static boolean dimension(ServerLevel level){return !level.dimension().equals(Level.END)&&!level.dimension().location().equals(net.minecraft.resources.ResourceLocation.parse("thaumcraft:outer_lands"));}
    public static boolean regeneration(){return ThaumcraftConfig.REGENERATE_STRUCTURES.get()||ThaumcraftConfig.REGENERATE_AURA.get()||ThaumcraftConfig.REGENERATE_TREES.get()||ThaumcraftConfig.REGENERATE_CINNABAR.get()||ThaumcraftConfig.REGENERATE_AMBER.get()||ThaumcraftConfig.REGENERATE_INFUSED_STONE.get();}
    public static void enqueue(ServerLevel level,LevelChunk chunk,boolean fresh){
        if(!dimension(level))return;String version=ThaumcraftConfig.REGENERATION_VERSION.get();
        var temples=net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.STRUCTURE,net.minecraft.resources.ResourceLocation.parse("thaumcraft:scattered_temples"));
        var structures=level.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.STRUCTURE);
        for(var start:chunk.getAllStarts().values())if(start.isValid()&&structures.wrapAsHolder(start.getStructure()).is(temples))NaturalPopulationData.get(level).rememberTemple(new net.minecraft.core.BlockPos(chunk.getPos().getMiddleBlockX(),level.getSeaLevel(),chunk.getPos().getMiddleBlockZ()));
        for(var tile:chunk.getBlockEntities().values())if(tile instanceof thaumcraft.production.EldritchAltarBlockEntity altar){var at=tile.getBlockPos().below();var index=StructureIndexData.get(level);index.record(level,StructureIndexData.Kind.RING_ALTAR,at.getX(),at.getY(),at.getZ());if(altar.guardianSpawner&&altar.spawnType==0)index.record(level,StructureIndexData.Kind.CULTIST_RING,at.getX(),at.getY(),at.getZ());}else if(tile instanceof thaumcraft.blocks.AuraNodeBlockEntity&&tile.getBlockState().is(thaumcraft.registry.R4Content.OBSIDIAN_TOTEM_NODE.get())){var at=tile.getBlockPos();StructureIndexData.get(level).record(level,StructureIndexData.Kind.TOTEM,at.getX(),at.getY(),at.getZ());}
        if(!fresh&&!regeneration()||chunk.getData(VERSION).equals(version))return;
        var pos=chunk.getPos();NaturalPopulationData.get(level).enqueue(new NaturalPopulationData.Request(pos.toLong(),PopulationPolicy.populationSeed(level.getSeed(),pos.x,pos.z),fresh,version));
    }
    private static void loaded(ChunkEvent.Load event){
        if(!(event.getChunk() instanceof LevelChunk chunk)||!(event.getLevel() instanceof ServerLevel level))return;
        if(level.getServer().isSameThread())enqueue(level,chunk,event.isNewChunk());
        else level.getServer().execute(()->enqueue(level,chunk,event.isNewChunk()));
    }
    private static void tick(LevelTickEvent.Post event){
        if(!(event.getLevel() instanceof ServerLevel level)||!dimension(level))return;
        var pending=PENDING.get(level);
        if((pending==null||!pending.background)&&(level.players().isEmpty()||pending!=null&&!level.getChunkSource().isPositionTicking(pending.request.chunk()))){cancel(level);return;}
        if(!event.hasTime())return;
        if(pending!=null){advance(level,pending.request,2);return;}
        // Only population around a ticking origin is eligible. Neighbor preloads must not
        // recursively expand the active generation frontier away from players.
        for(var request:NaturalPopulationData.get(level).requests())if(level.getChunkSource().isPositionTicking(request.chunk())){advance(level,request,2);break;}
    }
    /** Automatic population calls this once per tick. The original writes run only after all 25 columns are FULL. */
    public static boolean advance(ServerLevel level,NaturalPopulationData.Request request,int budget){
        return advance(level,request,budget,false);
    }
    /** Locate uses the same saved population request, without pretending remote chunks are player-ticking. */
    public static boolean advanceLocatedChunk(ServerLevel level,ChunkPos pos,int budget){
        var request=NaturalPopulationData.get(level).requests().stream().filter(r->r.chunk()==pos.toLong()).findFirst().orElse(null);
        if(request==null)return true;
        var pending=PENDING.get(level);if(pending!=null&&!pending.request.equals(request))return false;
        return advance(level,request,budget,true);
    }
    public static void cancelLocatedChunk(ServerLevel level,ChunkPos pos){var pending=PENDING.get(level);if(pending!=null&&pending.background&&pending.request.chunk()==pos.toLong())cancel(level);}
    private static boolean advance(ServerLevel level,NaturalPopulationData.Request request,int budget,boolean background){
        if(!dimension(level))return true;
        var pending=PENDING.get(level);
        if(pending!=null&&!pending.request.equals(request))throw new IllegalStateException("Population already preparing another origin");
        var p=new ChunkPos(request.chunk());var existing=level.getChunkSource().getChunkNow(p.x,p.z);
        if(existing!=null&&existing.getData(VERSION).equals(request.version())){NaturalPopulationData.get(level).completed(request.chunk());cancel(level);return true;}
        if(pending==null){pending=new Pending(level,request,background);PENDING.put(level,pending);}
        try{
            if(!pending.chunks.advanceCold(budget))return false;
            if(LegacyNaturalPopulation.requiresStructureSearch(level,p.x,p.z,request.fresh())){
                if(!pending.templeResolved){pending.structure=NaturalPopulationData.get(level).nearestTemple(new net.minecraft.core.BlockPos(p.getMiddleBlockX(),level.getSeaLevel(),p.getMiddleBlockZ()));pending.templeResolved=true;}
                if(pending.structure!=null){
                    if(pending.structureChunks==null){int x=pending.structure.getX(),z=pending.structure.getZ();pending.structureChunks=new ChunkPreparation(level,(x-5)>>4,(x+5)>>4,(z-5)>>4,(z+5)>>4);}
                    if(!pending.structureChunks.advanceCold(budget))return false;
                }
            }
            commit(level,request,pending.structure,true);cancel(level);return true;
        }catch(RuntimeException|Error failure){cancel(level);throw failure;}
    }
    public static void apply(ServerLevel level,NaturalPopulationData.Request request){
        commit(level,request,null,false);
    }
    private static void commit(ServerLevel level,NaturalPopulationData.Request request,net.minecraft.core.BlockPos structure,boolean prepared){
        if(!dimension(level))return;var pos=new ChunkPos(request.chunk());var chunk=level.getChunk(pos.x,pos.z);
        if(!chunk.getData(VERSION).equals(request.version())){
            // Source mound/tree writes reach the second neighboring chunk. Preload the
            // complete area before touching blocks so original write order is retained.
            for(int x=-2;x<=2;x++)for(int z=-2;z<=2;z++)level.getChunk(pos.x+x,pos.z+z);
            var random=RandomSource.create(request.seed());
            LegacyNaturalPopulation.populate(level,random,pos.x,pos.z,request.fresh(),structure,prepared);
            if(request.fresh())NativeStructures.finish(level,pos);
            chunk.setData(VERSION,request.version());chunk.setUnsaved(true);
        }
        NaturalPopulationData.get(level).completed(request.chunk());
    }
    private NaturalPopulation(){}
}
