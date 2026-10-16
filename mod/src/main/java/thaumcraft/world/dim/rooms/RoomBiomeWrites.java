package thaumcraft.world.dim.rooms;

import java.util.*;
import com.mojang.serialization.Codec;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.registries.*;
import thaumcraft.world.*;

/** Durable room columns travel with protochunks; SavedData writes run on the server thread. */
public final class RoomBiomeWrites {
    private static final DeferredRegister<AttachmentType<?>> TYPES=DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES,"thaumcraft");
    public static final DeferredHolder<AttachmentType<?>,AttachmentType<List<Integer>>> COLUMNS=TYPES.register("outer_room_taint_columns",()->AttachmentType.<List<Integer>>builder(()->List.of()).serialize(Codec.INT.listOf()).build());
    public static void register(IEventBus bus){TYPES.register(bus);NeoForge.EVENT_BUS.addListener(RoomBiomeWrites::loaded);}
    public static void put(WorldGenLevel world,int x,int z){
        var chunk=world.getChunk(x>>4,z>>4);int column=(x&15)|((z&15)<<4);
        synchronized(chunk){var old=chunk.getData(COLUMNS.get());if(!old.contains(column)){var values=new ArrayList<>(old);values.add(column);chunk.setData(COLUMNS.get(),List.copyOf(values));chunk.setUnsaved(true);}}
        if(!WorldBiomes.exactMode())quart(world,chunk,x,z);
        if(chunk instanceof LevelChunk)applyOnServer(world.getLevel(),chunk);
    }
    private static void quart(WorldGenLevel world,ChunkAccess chunk,int x,int z){
        var replacement=world.registryAccess().registryOrThrow(Registries.BIOME).getHolderOrThrow(WorldBiomes.TAINT);
        int qx=QuartPos.fromBlock(x),qz=QuartPos.fromBlock(z);
        var sampler=world.getLevel().getChunkSource().randomState().sampler();
        synchronized(chunk){for(int index=0;index<chunk.getSectionsCount();index++){
            var section=chunk.getSection(index);int qy=QuartPos.fromBlock(chunk.getSectionYFromSectionIndex(index)<<4);
            var old=new ArrayList<net.minecraft.core.Holder<net.minecraft.world.level.biome.Biome>>();
            for(int a=0;a<4;a++)for(int b=0;b<4;b++)for(int c=0;c<4;c++)old.add(section.getNoiseBiome(a,b,c));
            section.fillBiomesFromNoise((a,b,c,s)->a==qx&&c==qz?replacement:old.get((a&3)*16+(b&3)*4+(c&3)),sampler,chunk.getPos().x*4,qy,chunk.getPos().z*4);
        }chunk.setUnsaved(true);}
    }
    private static void loaded(ChunkEvent.Load event){if(event.getLevel() instanceof ServerLevel level)applyOnServer(level,event.getChunk());}
    private static void applyOnServer(ServerLevel level,ChunkAccess chunk){
        if(!level.getServer().isSameThread()){level.getServer().execute(()->applyOnServer(level,chunk));return;}
        if(!WorldBiomes.exactMode())return;
        List<Integer> values;synchronized(chunk){values=chunk.getExistingDataOrNull(COLUMNS.get());}
        if(values==null||values.isEmpty())return;
        var data=WorldBiomes.data(level);boolean changed=false;
        for(int column:values)changed|=data.columns.set(chunk.getPos().getMinBlockX()+(column&15),chunk.getPos().getMinBlockZ()+((column>>4)&15),WorldBiomes.TAINT.location().toString());
        if(changed){data.setDirty();BiomeColumnSync.changed(level,chunk.getPos().x,chunk.getPos().z);}
    }
    private RoomBiomeWrites(){}
}
