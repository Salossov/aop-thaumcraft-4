package thaumcraft.verification;

import java.util.*;
import java.util.function.BiConsumer;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import thaumcraft.api.aspects.*;
import thaumcraft.blocks.*;
import thaumcraft.items.ManaBeanItem;
import thaumcraft.registry.*;
import thaumcraft.world.WorldBiomes;

final class ManaPodVerification {
    static void run(ServerLevel level,BiConsumer<Boolean,String> check){
        var pos=new BlockPos(6408,150,3080);level.getChunkAt(pos);for(int x=-4;x<=4;x++)for(int y=-1;y<=3;y++)for(int z=-4;z<=4;z++)level.setBlock(pos.offset(x,y,z),Blocks.AIR.defaultBlockState(),2);
        WorldBiomes.change(level,pos.getX(),pos.getZ(),WorldBiomes.MAGICAL);var log=pos.above();level.setBlock(log,R3Content.GREATWOOD_LOG.get().defaultBlockState(),2);
        check.accept(ManaPodBlock.magic(level,pos),"native magical biome permits mana pods");var player=FakePlayerFactory.get(level,new GameProfile(UUID.randomUUID(),"ManaPodQA"));player.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);var bean=ManaBeanItem.of(Aspects.WATER);bean.setCount(2);player.setItemInHand(InteractionHand.MAIN_HAND,bean);
        var context=new UseOnContext(player,InteractionHand.MAIN_HAND,new BlockHitResult(log.getCenter(),Direction.DOWN,log,false));check.accept(bean.useOn(context).consumesAction()&&bean.getCount()==1&&level.getBlockState(pos).is(R5Content.MANA_POD.get()),"real mana bean plants under magical log and consumes one");var tile=(ManaPodBlockEntity)level.getBlockEntity(pos);check.accept(tile.aspect==Aspects.WATER&&tile.getBlockState().canSurvive(level,pos),"planted pod retains offered aspect and native support");
        for(int age=0;age<8;age++){level.setBlock(pos,tile.getBlockState().setValue(ManaPodBlock.AGE,age),2);var state=level.getBlockState(pos);var shape=state.getShape(level,pos).bounds();check.accept(shape.minX==.25&&shape.maxX==.75&&shape.maxY==1,"source pod bounds for age"+age);var drops=net.minecraft.world.level.block.Block.getDrops(state,level,pos,tile);check.accept(age<2?drops.isEmpty():drops.size()==1&&ManaBeanItem.aspect(drops.getFirst())==Aspects.WATER&&drops.getFirst().getCount()>0,"pod harvest respects minimum stage and aspect age"+age);check.accept(state.getLightEmission(level,pos)==age,"original pod light tracks growth stage"+age);}
        var saved=tile.saveWithoutMetadata(level.registryAccess());var copy=new ManaPodBlockEntity(pos,tile.getBlockState());copy.loadWithComponents(saved,level.registryAccess());check.accept(copy.aspect==Aspects.WATER,"pod aspect survives native NBT");check.accept(tile.getAspects().getAmount(Aspects.WATER)==1,"mature pod advertises original single aspect");
        level.setBlock(pos,tile.getBlockState().setValue(ManaPodBlock.AGE,2),2);tile.checkGrowth();check.accept(level.getBlockState(pos).getValue(ManaPodBlock.AGE)==3&&tile.aspect==Aspects.WATER,"third growth stage preserves lone initialized aspect");for(int i=0;i<8;i++)tile.checkGrowth();check.accept(tile.getBlockState().getValue(ManaPodBlock.AGE)==7,"growth caps at mature stage7");
        level.removeBlock(log,false);check.accept(!tile.getBlockState().canSurvive(level,pos),"removing log invalidates native hanging pod support");level.destroyBlock(pos,true);check.accept(level.getBlockEntity(pos)==null,"harvesting removes pod tile");
        player.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);
        var mushroom=pos.offset(3,0,0);level.setBlock(mushroom.below(),Blocks.GRASS_BLOCK.defaultBlockState(),2);level.setBlock(mushroom,R5Content.VISHROOM.get().defaultBlockState(),2);
        var mushroomState=level.getBlockState(mushroom);player.removeEffect(net.minecraft.world.effect.MobEffects.CONFUSION);mushroomState.entityInside(level,mushroom,player);
        check.accept(player.getEffect(net.minecraft.world.effect.MobEffects.CONFUSION).getDuration()==200&&player.getEffect(net.minecraft.world.effect.MobEffects.CONFUSION).getAmplifier()==0,"actual vishroom contact applies original ten-second nausea");
        check.accept(mushroomState.getLightEmission(level,mushroom)==8&&net.minecraft.world.level.block.Block.getDrops(mushroomState,level,mushroom,null).getFirst().is(R5Content.VISHROOM_ITEM.get()),"vishroom original light8 and actual harvest drop");
        check.accept(thaumcraft.data.ThaumcraftData.aspects(level.getServer(),new ItemStack(R5Content.VISHROOM_ITEM.get())).equals(Map.of("herba",2,"venenum",1,"praecantatio",2)),"actual vishroom scan aspects match original plant metadata5");
        var wisp=thaumcraft.blocks.VishroomBlock.clientWisp;var positions=new ArrayList<Vec3>();thaumcraft.blocks.VishroomBlock.clientWisp=positions::add;
        try{for(int seed=0;seed<32;seed++)R5Content.VISHROOM.get().animateTick(mushroomState,level,mushroom,net.minecraft.util.RandomSource.create(seed));check.accept(!positions.isEmpty()&&positions.stream().allMatch(p->Math.abs(p.x-mushroom.getX()-.5)<=.4&&Math.abs(p.z-mushroom.getZ()-.5)<=.4&&Math.abs(p.y-mushroom.getY()-.3)<1e-5),"vishroom sends native glow callbacks inside original spawn envelope");}finally{thaumcraft.blocks.VishroomBlock.clientWisp=wisp;}
        player.removeEffect(net.minecraft.world.effect.MobEffects.CONFUSION);level.removeBlock(mushroom,false);level.removeBlock(mushroom.below(),false);
    }
    private ManaPodVerification(){}
}
