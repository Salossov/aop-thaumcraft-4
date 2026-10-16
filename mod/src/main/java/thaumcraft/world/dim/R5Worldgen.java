package thaumcraft.world.dim;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.*;

public final class R5Worldgen {
    private static final DeferredRegister<MapCodec<? extends ChunkGenerator>> GENERATORS=DeferredRegister.create(Registries.CHUNK_GENERATOR,"thaumcraft");
    public static final DeferredHolder<MapCodec<? extends ChunkGenerator>,MapCodec<OuterChunkGenerator>> OUTER=GENERATORS.register("outer_lands",()->OuterChunkGenerator.CODEC);
    public static void register(IEventBus bus){GENERATORS.register(bus);thaumcraft.world.dim.rooms.RoomBiomeWrites.register(bus);LabyrinthData.register();}
    private R5Worldgen(){}
}
