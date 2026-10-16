package thaumcraft.verification;
import java.util.*;
import java.util.function.BiConsumer;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import thaumcraft.api.aspects.*;
import thaumcraft.production.*;
import thaumcraft.registry.*;
import thaumcraft.knowledge.KnowledgeService;
import thaumcraft.menu.ThaumatoriumMenu;
import thaumcraft.network.ResearchActionPayload;
final class ThaumatoriumVerification {
 private static TransportBlockEntity source(ServerLevel l,BlockPos p,Aspect a){l.setBlock(p,R3Content.ALEMBIC.get().defaultBlockState().setValue(ProductionBlock.FACING,Direction.SOUTH),2);var t=(TransportBlockEntity)l.getBlockEntity(p);t.addToContainer(a,6);return t;}
 static void run(ServerLevel l,BiConsumer<Boolean,String> check){
  var p=new BlockPos(96,100,96);for(var c:BlockPos.betweenClosed(p.offset(-3,-3,-3),p.offset(3,3,3))){l.getChunkAt(c);l.setBlock(c,Blocks.AIR.defaultBlockState(),2);}l.setBlock(p.below(),R3Content.CRUCIBLE.get().defaultBlockState(),2);var state=R3Content.THAUMATORIUM.get().defaultBlockState().setValue(ThaumatoriumBlock.FACING,Direction.NORTH);l.setBlock(p,state,2);l.setBlock(p.above(),state.setValue(ThaumatoriumBlock.HALF,DoubleBlockHalf.UPPER),3);var tile=(ThaumatoriumBlockEntity)l.getBlockEntity(p);tile.setItem(0,new ItemStack(Items.GLOWSTONE_DUST,2));
  var player=FakePlayerFactory.get(l,new GameProfile(UUID.randomUUID(),"R3_Automation_Test"));player.setPos(p.getX()+.5,p.getY(),p.getZ()+2);KnowledgeService.initialize(player);var recipe=ResourceLocation.parse("thaumcraft:nitor");
  check.accept(!tile.toggle(player,recipe),"taumatorium rejects recipe selection without its research");KnowledgeService.mutate(player,k->{k.completeResearch("NITOR");return true;});check.accept(tile.toggle(player,recipe)&&tile.selections.size()==1,"researched Nitor can be selected on a real formed multiblock");
  var sources=List.of(source(l,p.west(),Aspects.FIRE),source(l,p.east(),Aspects.ENERGY),source(l,p.above().west(),Aspects.LIGHT));
  for(int t=0;t<60;t++)tile.serverTick();check.accept(sources.stream().mapToInt(s->s.store.amount()).sum()==18&&tile.getItem(0).getCount()==2,"cold automatic alchemy consumes neither catalysts nor essentia");
  l.setBlock(p.below(2),R3Content.NITOR_BLOCK.get().defaultBlockState(),2);l.setBlock(p.north(),Blocks.CHEST.defaultBlockState(),3);var chest=(ChestBlockEntity)l.getBlockEntity(p.north());for(int s=0;s<chest.getContainerSize();s++)chest.setItem(s,new ItemStack(Items.COBBLESTONE,64));for(int t=0;t<80;t++)tile.serverTick();check.accept(sources.stream().mapToInt(s->s.store.amount()).sum()==18&&tile.getItem(0).getCount()==2,"full output inventory pauses before drawing any essentia");
  chest.clearContent();l.setBlock(p.south(),Blocks.REDSTONE_BLOCK.defaultBlockState(),3);for(int t=0;t<20;t++)tile.serverTick();check.accept(sources.stream().mapToInt(s->s.store.amount()).sum()==18,"redstone pauses automatic alchemy on both multiblock levels");l.removeBlock(p.south(),false);for(int t=0;t<10;t++)tile.serverTick();check.accept(tile.store.amount()==2&&sources.stream().mapToInt(s->s.store.amount()).sum()==16,"automatic alchemy draws exactly one demanded unit per five ticks");
  var copy=new ThaumatoriumBlockEntity(p,state);copy.loadWithComponents(tile.saveWithoutMetadata(l.registryAccess()),l.registryAccess());l.setBlockEntity(copy);tile=copy;check.accept(tile.store.amount()==2&&tile.selections.containsKey(recipe)&&tile.getItem(0).getCount()==2,"partial automatic alchemy saves catalyst, selected recipe and essentia");
  check.accept(l.getCapability(EssentiaCapabilities.TRANSPORT,p.above(),Direction.WEST)==tile&&l.getCapability(Capabilities.ItemHandler.BLOCK,p.above(),Direction.UP)!=null,"upper half exposes the same real transport and input inventory");
  for(int t=0;t<100;t++)tile.serverTick();int output=0;for(int s=0;s<chest.getContainerSize();s++)if(chest.getItem(s).is(R3Content.NITOR.get()))output+=chest.getItem(s).getCount();check.accept(output==2&&tile.isEmpty()&&tile.store.amount()==0&&sources.stream().mapToInt(s->s.store.amount()).sum()==0,"restored automatic cycle creates two outputs with exact total cost and no duplication");
  l.setBlock(p.above().east(),R3Content.BRAINBOX.get().defaultBlockState().setValue(BrainboxBlock.FACING,Direction.WEST),3);for(int t=0;t<40;t++)tile.serverTick();check.accept(tile.maxRecipes==3,"one correctly facing mnemonic matrix adds original two recipe slots");l.setBlock(p.above().east(),R3Content.BRAINBOX.get().defaultBlockState().setValue(BrainboxBlock.FACING,Direction.EAST),3);for(int t=0;t<40;t++)tile.serverTick();check.accept(tile.maxRecipes==1,"a reversed mnemonic matrix does not provide recipe slots");
  var menu=new ThaumatoriumMenu(7,player.getInventory(),tile);check.accept(!menu.accept(player,new ResearchActionPayload(7,UUID.randomUUID(),1,1,recipe.toString(),"",0,0))&&tile.selections.containsKey(recipe),"forged menu token cannot alter automatic recipes");check.accept(!menu.accept(player,new ResearchActionPayload(7,menu.token(),2,1,recipe.toString(),"",0,0)),"out-of-order automatic menu actions are rejected");
  l.removeBlock(p.above(),false);check.accept(l.getBlockState(p).is(R3Content.ALCHEMICAL_CONSTRUCT.get()),"breaking the upper half reverts the remaining construct");
 }
}
