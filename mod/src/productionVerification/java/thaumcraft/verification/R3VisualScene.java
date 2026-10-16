package thaumcraft.verification;

import net.minecraft.core.*;
import net.minecraft.server.level.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import thaumcraft.api.aspects.*;
import thaumcraft.registry.*;
import thaumcraft.production.*;
import thaumcraft.knowledge.KnowledgeService;

/** Isolated dev scene, excluded from gameplay and API JARs. No operator permissions. */
final class R3VisualScene {
 private static final BlockPos MATRIX=new BlockPos(0,152,0);
 private static ServerPlayer observer;
 static void commands(net.neoforged.neoforge.event.RegisterCommandsEvent event){
  var command=net.minecraft.commands.Commands.literal("r3scene");
  for(var name:java.util.List.of("altar","automatic","furnace","bellows","table","scan","charge","birth","jar"))command.then(net.minecraft.commands.Commands.literal(name).executes(ctx->{
   var player=ctx.getSource().getPlayerOrException();var level=player.serverLevel();player.getAbilities().flying=false;player.onUpdateAbilities();int slot=8;
   double x=switch(name){case "automatic"->8.5;case "furnace"->-7.5;case "bellows"->6.5;case "table"->-9.5;case "altar"->6.5;case "jar"->5.5;default->.5;};double z=name.equals("jar")?2.5:name.equals("bellows")?9.5:name.equals("table")?8.5:java.util.List.of("birth","scan","charge").contains(name)?9.5:name.equals("altar")?8.5:4.5;float yaw=name.equals("altar")?143.13f:180,pitch=name.equals("jar")?31.4f:name.equals("altar")?24:name.equals("table")?20:name.equals("charge")?3.5f:java.util.List.of("birth","scan").contains(name)?29.25f:5;double y=name.equals("altar")?154:150;
   if(name.equals("altar")){player.getAbilities().flying=true;player.onUpdateAbilities();if(level.getBlockEntity(MATRIX) instanceof InfusionMatrixBlockEntity matrix){matrix.crafting=false;rearm(level,player);}else return 0;}
   player.teleportTo(level,x,y,z,yaw,pitch);
   if(name.equals("table")){if(level.getBlockEntity(new BlockPos(-10,150,6)) instanceof thaumcraft.blocks.TableBlockEntity table)player.openMenu(table);else return 0;}
   if(name.equals("birth")){level.setBlockAndUpdate(new BlockPos(0,150,7),Blocks.BOOKSHELF.defaultBlockState());slot=1;}
   if(name.equals("scan")){level.setBlockAndUpdate(new BlockPos(0,150,7),Blocks.IRON_BLOCK.defaultBlockState());player.getInventory().setItem(8,new ItemStack(R2Content.THAUMOMETER.get()));}
   if(name.equals("charge")){var pos=new BlockPos(0,151,7);level.setBlockAndUpdate(pos,R2Content.AURA_NODE.get().defaultBlockState());var node=(thaumcraft.blocks.AuraNodeBlockEntity)level.getBlockEntity(pos);for(var aspect:Aspects.PRIMALS){node.state.current.put(aspect.getTag(),50);node.state.base.put(aspect.getTag(),50);}node.changedAndSync();slot=1;for(var aspect:Aspects.PRIMALS)thaumcraft.items.WandVis.set(player.getInventory().getItem(1),aspect,0);}
   if(slot==8&&!name.equals("scan"))player.getInventory().setItem(8,ItemStack.EMPTY);player.getInventory().selected=slot;player.connection.send(new net.minecraft.network.protocol.game.ClientboundSetCarriedItemPacket(slot));player.inventoryMenu.broadcastChanges();return 1;
  }));event.getDispatcher().register(command);
 }

 static void prepare(ServerLevel level){
  for(var p:BlockPos.betweenClosed(new BlockPos(-14,149,-10),new BlockPos(14,154,12))){level.getChunkAt(p);level.setBlock(p,p.getY()==149?Blocks.SMOOTH_STONE.defaultBlockState():Blocks.AIR.defaultBlockState(),2);}
  level.setBlockAndUpdate(MATRIX,R3Content.INFUSION_MATRIX.get().defaultBlockState());
  level.setBlockAndUpdate(MATRIX.below(2),R3Content.ARCANE_PEDESTAL.get().defaultBlockState());
  for(int x:new int[]{-1,1})for(int z:new int[]{-1,1}){var s=R3Content.INFUSION_PILLAR.get().defaultBlockState().setValue(InfusionPillarBlock.ORIENTATION,x<0?(z<0?2:3):(z<0?4:5));level.setBlock(MATRIX.offset(x,-2,z),s,2);level.setBlockAndUpdate(MATRIX.offset(x,-1,z),s.setValue(InfusionPillarBlock.HALF,DoubleBlockHalf.UPPER));}
  for(var d:Direction.Plane.HORIZONTAL)level.setBlockAndUpdate(MATRIX.below(2).relative(d,3),R3Content.ARCANE_PEDESTAL.get().defaultBlockState());
  jar(level,MATRIX.offset(-5,-2,0),Aspects.FIRE,32);jar(level,MATRIX.offset(5,-2,0),Aspects.WATER,32);
  var f=new BlockPos(-8,150,0);level.setBlockAndUpdate(f,R3Content.ALCHEMY_FURNACE.get().defaultBlockState().setValue(ProductionBlock.FACING,Direction.SOUTH));level.setBlockAndUpdate(f.above(),R3Content.ALEMBIC.get().defaultBlockState());
  var furnace=(AlchemyFurnaceBlockEntity)level.getBlockEntity(f);furnace.setItem(0,new ItemStack(Items.OAK_LOG,32));furnace.setItem(1,new ItemStack(Items.COAL,32));
  level.setBlockAndUpdate(f.west(),R3Content.BELLOWS.get().defaultBlockState().setValue(BellowsBlock.FACING,Direction.EAST));
  var crucible=new BlockPos(8,150,0);level.setBlockAndUpdate(crucible,R3Content.CRUCIBLE.get().defaultBlockState());level.setBlockAndUpdate(crucible.below(),R3Content.NITOR_BLOCK.get().defaultBlockState());
  var s=R3Content.THAUMATORIUM.get().defaultBlockState().setValue(ThaumatoriumBlock.FACING,Direction.SOUTH);level.setBlockAndUpdate(crucible.above(),s);level.setBlockAndUpdate(crucible.above(2),s.setValue(ThaumatoriumBlock.HALF,DoubleBlockHalf.UPPER));
  level.setBlockAndUpdate(crucible.above().east(),R3Content.BRAINBOX.get().defaultBlockState().setValue(BrainboxBlock.FACING,Direction.WEST));
  level.setBlockAndUpdate(new BlockPos(-6,150,5),R3Content.NITOR_BLOCK.get().defaultBlockState());
  level.setBlockAndUpdate(new BlockPos(-5,150,5),R3Content.NITOR_BLOCK.get().defaultBlockState());
  var vf=new BlockPos(6,150,5);level.setBlockAndUpdate(vf,Blocks.FURNACE.defaultBlockState().setValue(FurnaceBlock.FACING,Direction.SOUTH));var vanilla=(net.minecraft.world.level.block.entity.FurnaceBlockEntity)level.getBlockEntity(vf);vanilla.setItem(0,new ItemStack(Items.IRON_ORE,32));vanilla.setItem(1,new ItemStack(Items.COAL,32));level.setBlockAndUpdate(vf.west(),R3Content.BELLOWS.get().defaultBlockState().setValue(BellowsBlock.FACING,Direction.EAST));
  var tableState=R2Content.RESEARCH_TABLE.get().defaultBlockState().setValue(thaumcraft.blocks.TableBlock.FACING,Direction.EAST);level.setBlock(new BlockPos(-10,150,6),tableState,2);level.setBlock(new BlockPos(-9,150,6),tableState.setValue(thaumcraft.blocks.TableBlock.FACING,Direction.WEST).setValue(thaumcraft.blocks.TableBlock.SLAVE,true),2);
  var research=(thaumcraft.blocks.TableBlockEntity)level.getBlockEntity(new BlockPos(-10,150,6));research.setItem(0,new ItemStack(R2Content.SCRIBING_TOOLS.get()));var note=new ItemStack(R2Content.NOTES.get());thaumcraft.items.ResearchNotes.write(note,thaumcraft.api.research.ResearchNote.create("GOGGLES",1,java.util.List.of(Aspects.AIR,Aspects.FIRE,Aspects.WATER),new java.util.Random(42)));research.setItem(1,note);
  var matrix=(InfusionMatrixBlockEntity)level.getBlockEntity(MATRIX);matrix.active=true;matrix.getSurroundings();matrix.setChanged();level.setWeatherParameters(0,6000,false,false);level.getGameRules().getRule(GameRules.RULE_WEATHER_CYCLE).set(false,level.getServer());level.setDayTime(6000);level.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(false,level.getServer());
 }
 private static void jar(ServerLevel l,BlockPos p,Aspect a,int amount){l.setBlockAndUpdate(p,R3Content.WARDED_JAR.get().defaultBlockState().setValue(ProductionBlock.FACING,Direction.SOUTH));var jar=(TransportBlockEntity)l.getBlockEntity(p);jar.addToContainer(a,amount);jar.applyLabel(a);}
 static void login(PlayerEvent.PlayerLoggedInEvent event){
  if(!(event.getEntity() instanceof ServerPlayer player))return;observer=player;var level=player.serverLevel();player.setGameMode(GameType.CREATIVE);player.teleportTo(level,0.5,150,9.5,180,15);KnowledgeService.initialize(player);KnowledgeService.mutate(player,k->{for(var key:thaumcraft.data.ThaumcraftData.get(player.server).research().keySet())k.completeResearch(key);for(var a:Aspect.all().values())k.setPool(a.getTag(),50);for(var id:java.util.List.of("minecraft:stone","minecraft:iron_ingot","minecraft:coal","minecraft:oak_log","minecraft:glass"))k.recordScan(thaumcraft.api.knowledge.PlayerKnowledge.ScanKind.OBJECT,thaumcraft.data.ThaumcraftData.get(player.server).scanGroups().getOrDefault(id,id));return true;});
  var items=new ItemStack[]{new ItemStack(R2Content.BOOK.get()),new ItemStack(R2Content.WAND.get()),new ItemStack(R3Content.RESONATOR.get()),new ItemStack(R3Content.NITOR.get()),new ItemStack(R3Content.BELLOWS_ITEM.get()),new ItemStack(R2Content.GOGGLES.get()),new ItemStack(Items.QUARTZ,64),new ItemStack(R3Content.ESSENTIA_PHIAL.get()),ItemStack.EMPTY};R3Content.setAspect(items[7],Aspects.FIRE);for(int i=0;i<items.length;i++)player.getInventory().setItem(i,items[i]);player.getInventory().selected=0;player.connection.send(new net.minecraft.network.protocol.game.ClientboundSetCarriedItemPacket(0));player.inventoryMenu.broadcastChanges();if(level.getBlockEntity(new BlockPos(8,151,0)) instanceof ThaumatoriumBlockEntity automatic){automatic.selections.put(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("thaumcraft","nitor"),player.getUUID());automatic.sync();}rearm(level,player);
 }
 static void tick(ServerTickEvent.Post event){var stop=java.nio.file.Path.of(System.getProperty("thaumcraft.r3.root"),"analysis/r3/evidence/stop-visual-server");if(java.nio.file.Files.exists(stop)){event.getServer().saveEverything(false,true,true);event.getServer().halt(false);try{java.nio.file.Files.delete(stop);}catch(java.io.IOException e){throw new java.io.UncheckedIOException(e);}return;}if(observer!=null&&!observer.isRemoved()&&event.getServer().getTickCount()%500==0)rearm(observer.serverLevel(),observer);}
 static boolean rearm(ServerLevel level,ServerPlayer player){
  if(!(level.getBlockEntity(MATRIX) instanceof InfusionMatrixBlockEntity matrix)||matrix.crafting)return false;
  if(!(level.getBlockEntity(MATRIX.below(2)) instanceof PedestalBlockEntity centre))return false;
  var pedestals=new java.util.ArrayList<PedestalBlockEntity>();
  for(var direction:Direction.Plane.HORIZONTAL){if(!(level.getBlockEntity(MATRIX.below(2).relative(direction,3)) instanceof PedestalBlockEntity pedestal))return false;pedestals.add(pedestal);}
  if(!(level.getBlockEntity(MATRIX.offset(-5,-2,0)) instanceof TransportBlockEntity fireJar)||fireJar.kind!=ProductionBlock.Kind.JAR)return false;
  if(!(level.getBlockEntity(MATRIX.offset(5,-2,0)) instanceof TransportBlockEntity waterJar)||waterJar.kind!=ProductionBlock.Kind.JAR)return false;
  centre.setItem(0,new ItemStack(Items.IRON_INGOT));for(var pedestal:pedestals)pedestal.setItem(0,new ItemStack(Items.QUARTZ));
  fireJar.addToContainer(Aspects.FIRE,32);waterJar.addToContainer(Aspects.WATER,32);return matrix.start(player);
 }
}
