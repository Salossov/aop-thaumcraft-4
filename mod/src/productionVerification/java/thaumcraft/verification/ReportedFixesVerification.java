package thaumcraft.verification;

import java.util.*;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import thaumcraft.api.aspects.Aspects;
import thaumcraft.items.WandVis;
import thaumcraft.registry.*;

/** A fresh single-biome world proves vegetation exists before queued population runs. */
final class ReportedFixesVerification {
    static void run(ServerLevel level,BiConsumer<Boolean,String> check){
        int woods=0;var logBlocks=Set.of(R3Content.GREATWOOD_LOG.get(),R3Content.SILVERWOOD_LOG.get(),net.minecraft.world.level.block.Blocks.OAK_LOG);
        for(int cx=-2;cx<=2;cx++)for(int cz=-2;cz<=2;cz++){
            var chunk=level.getChunk(cx,cz);
            for(var pos:BlockPos.betweenClosed(new BlockPos(cx*16,50,cz*16),new BlockPos(cx*16+15,150,cz*16+15)))
                if(logBlocks.contains(chunk.getBlockState(pos).getBlock()))woods++;
        }
        check.accept(level.getBiome(new BlockPos(8,80,8)).is(thaumcraft.world.WorldBiomes.MAGICAL),"isolated test world uses the magical forest biome");
        check.accept(woods>100,"trees already exist in freshly generated chunks without players or post-load population: "+woods+" log blocks");
        for(var request:thaumcraft.world.NaturalPopulationData.get(level).requests()){
            if(!request.fresh())continue;
            var pos=new net.minecraft.world.level.ChunkPos(request.chunk());
            if(Math.abs(pos.x)<=2&&Math.abs(pos.z)<=2)check.accept(!level.getChunk(pos.x,pos.z).getData(thaumcraft.world.NaturalPopulation.VERSION).equals(request.version()),"tree-filled chunk predates server population "+pos);
        }
        for(var item:List.of(R2Content.WAND.get(),R3Equipment.WAND.get(),R3Equipment.STAFF.get(),R3Equipment.SCEPTRE.get())){
            var before=new ItemStack(item);var after=before.copy();WandVis.set(after,Aspects.AIR,100);
            check.accept(!item.shouldCauseReequipAnimation(before,after,false),"vis updates preserve held pose for "+item);
            check.accept(item.shouldCauseReequipAnimation(before,after,true),"slot changes still equip "+item);
        }
        var before=new ItemStack(R2Content.THAUMOMETER.get());var after=before.copy();net.minecraft.world.item.component.CustomData.update(net.minecraft.core.component.DataComponents.CUSTOM_DATA,after,tag->tag.putInt("scan",1));
        check.accept(!before.getItem().shouldCauseReequipAnimation(before,after,false),"thaumometer data update preserves held pose");
        check.accept(before.getItem().shouldCauseReequipAnimation(before,after,true),"thaumometer slot change still equips");
    }
}
