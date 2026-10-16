package thaumcraft.verification;

import java.util.*;
import java.util.function.BiConsumer;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import thaumcraft.knowledge.KnowledgeService;
import thaumcraft.production.*;

/** Reproduces removing fixtures while the automatic visual-scene timer runs. */
final class VisualSceneVerification {
    static void run(ServerLevel level,BiConsumer<Boolean,String> check){
        var player=FakePlayerFactory.get(level,new GameProfile(UUID.randomUUID(),"R3_Scene_Regression"));
        KnowledgeService.initialize(player);KnowledgeService.mutate(player,k->{for(var key:thaumcraft.data.ThaumcraftData.get(level.getServer()).research().keySet())k.completeResearch(key);return true;});
        R3VisualScene.prepare(level);var matrix=new BlockPos(0,152,0);var central=matrix.below(2);
        ((PedestalBlockEntity)level.getBlockEntity(central)).setItem(0,new ItemStack(Items.GOLD_INGOT));
        var missing=new ArrayList<BlockPos>(List.of(matrix,central,matrix.offset(-5,-2,0),matrix.offset(5,-2,0)));
        for(var direction:Direction.Plane.HORIZONTAL)missing.add(central.relative(direction,3));
        for(var pos:missing){
            var state=level.getBlockState(pos);var tag=level.getBlockEntity(pos).saveWithoutMetadata(level.registryAccess());level.removeBlock(pos,false);
            check.accept(!R3VisualScene.rearm(level,player),"scene timer safely skips a removed fixture at "+pos.toShortString());
            level.setBlockAndUpdate(pos,state);level.getBlockEntity(pos).loadWithComponents(tag,level.registryAccess());
            check.accept(((PedestalBlockEntity)level.getBlockEntity(central)).getItem(0).is(Items.GOLD_INGOT),"incomplete scene does not overwrite central input after missing "+pos.toShortString());
        }
        var jar=matrix.offset(-5,-2,0);var state=level.getBlockState(jar);var tag=level.getBlockEntity(jar).saveWithoutMetadata(level.registryAccess());level.setBlockAndUpdate(jar,Blocks.FURNACE.defaultBlockState());
        check.accept(!R3VisualScene.rearm(level,player),"scene timer safely skips a fixture replaced with another block entity");
        level.setBlockAndUpdate(jar,state);level.getBlockEntity(jar).loadWithComponents(tag,level.registryAccess());
        check.accept(R3VisualScene.rearm(level,player),"restored complete scene can start a real authorized ritual");
    }
    private VisualSceneVerification(){}
}
