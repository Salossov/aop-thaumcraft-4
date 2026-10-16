package thaumcraft.items;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import thaumcraft.blocks.*;

public final class NodeJarActions {
    public static List<BlockPos> shell(BlockPos center){var result=new ArrayList<BlockPos>();for(int x=-1;x<=1;x++)for(int y=-1;y<=2;y++)for(int z=-1;z<=1;z++)if(x!=0||y!=0||z!=0)result.add(center.offset(x,y,z));return List.copyOf(result);}
    public static boolean fits(Level level,BlockPos center){
        if(!(level.getBlockEntity(center) instanceof AuraNodeBlockEntity)||!level.getBlockState(center).is(thaumcraft.registry.R2Content.AURA_NODE.get()))return false;
        for(var pos:shell(center)){if(!level.hasChunkAt(pos))return false;var block=level.getBlockState(pos);if(pos.getY()==center.getY()+2?!block.is(BlockTags.WOODEN_SLABS):!block.is(Blocks.GLASS))return false;}return true;
    }
    public static boolean pack(ServerPlayer player,ItemStack wand,BlockPos clicked){
        if(!thaumcraft.knowledge.KnowledgeService.get(player).completedResearch().contains("NODEJAR"))return false;
        var level=player.serverLevel();
        for(int x=-1;x<=1;x++)for(int y=-2;y<=1;y++)for(int z=-1;z<=1;z++){
            var center=clicked.offset(x,y,z);if(!fits(level,center))continue;var shell=shell(center);
            if(!level.mayInteract(player,center)||shell.stream().anyMatch(p->!level.mayInteract(player,p)||net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(new net.neoforged.neoforge.event.level.BlockEvent.BreakEvent(level,p,level.getBlockState(p),player)).isCanceled()))return false;
            if(net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(new net.neoforged.neoforge.event.level.BlockEvent.BreakEvent(level,center,level.getBlockState(center),player)).isCanceled())return false;
            var costs=new LinkedHashMap<String,Integer>();for(var aspect:thaumcraft.api.aspects.Aspects.PRIMALS)costs.put(aspect.getTag(),70);
            if(!WandVis.consume(wand,costs,player,true))return false;
            var node=(AuraNodeBlockEntity)level.getBlockEntity(center);
            level.setBlockAndUpdate(center,thaumcraft.registry.R3Content.NODE_JAR.get().defaultBlockState());
            var jar=(JarredNodeBlockEntity)level.getBlockEntity(center);jar.capture(node);
            for(var pos:shell)level.removeBlock(pos,false);
            level.playSound(null,center,thaumcraft.registry.R2Sounds.CRAFT_FINISH.get(),net.minecraft.sounds.SoundSource.BLOCKS,1,1);
            level.sendParticles(net.minecraft.core.particles.ParticleTypes.END_ROD,center.getX()+.5,center.getY()+.5,center.getZ()+.5,40,1,1,1,.03);
            return true;
        }return false;
    }
    private NodeJarActions(){}
}
