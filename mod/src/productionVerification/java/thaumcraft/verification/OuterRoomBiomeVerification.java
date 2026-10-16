package thaumcraft.verification;

import java.util.function.BiConsumer;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import thaumcraft.config.ThaumcraftConfig;
import thaumcraft.world.WorldBiomes;
import thaumcraft.world.dim.*;
import thaumcraft.world.dim.rooms.RoomBiomeWrites;

/** The default native4x4 mode is verified independently of exact-column fixtures. */
final class OuterRoomBiomeVerification {
    static void run(ServerLevel level,boolean reload,BiConsumer<Boolean,String> check){
        var world=level.getServer().getLevel(EldritchTeleporter.OUTER);var mode=ThaumcraftConfig.BIOME_CHANGE_MODE.get();ThaumcraftConfig.BIOME_CHANGE_MODE.set(ThaumcraftConfig.BiomeChangeMode.REAL_4X4);
        try{var data=LabyrinthData.get(world);int cx=2100,cz=-2100;
            if(!reload){check.accept(data.cell(cx,cz)==null,"native biome test uses a fresh unassigned room");var cell=new MazeCell();cell.feature=13;cell.north=true;data.put(cx,cz,cell.pack());}
            var chunk=world.getChunk(cx,cz);check.accept(!chunk.getData(RoomBiomeWrites.COLUMNS.get()).isEmpty(),"default mode keeps durable taint columns on generated native chunk");
            int x=cx*16+8,z=cz*16+8;for(int y=0;y<128;y+=4)check.accept(world.getNoiseBiome(QuartPos.fromBlock(x),QuartPos.fromBlock(y),QuartPos.fromBlock(z)).is(WorldBiomes.TAINT),"default mode generated native taint biome at height"+y+(reload?" after restart":""));
            check.accept(WorldBiomes.effective(world,new BlockPos(x,54,z)).is(WorldBiomes.TAINT),"default mode room taint is visible without exact biome overlay");
            check.accept(world.getNoiseBiome(QuartPos.fromBlock(x+20),13,QuartPos.fromBlock(z)).is(net.minecraft.resources.ResourceLocation.parse("thaumcraft:eldritch")),"generated taint does not replace unrelated native quart columns");
        }finally{ThaumcraftConfig.BIOME_CHANGE_MODE.set(mode);}
    }
    private OuterRoomBiomeVerification(){}
}
