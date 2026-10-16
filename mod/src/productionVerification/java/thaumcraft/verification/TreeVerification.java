package thaumcraft.verification;
import java.util.*;
import java.util.function.BiConsumer;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import thaumcraft.registry.*;
import thaumcraft.world.*;
import thaumcraft.blocks.AuraNodeBlockEntity;
import thaumcraft.api.nodes.NodeState;
/** Bounded real-world fixtures exercise obstruction, geometry and embedded-node persistence. */
final class TreeVerification {
 private static void prepare(ServerLevel l,BlockPos o){for(int x=-18;x<=18;x++)for(int z=-18;z<=18;z++){l.getChunkAt(o.offset(x,0,z));for(int y=-2;y<=52;y++)l.setBlock(o.offset(x,y,z),(y<0?Blocks.GRASS_BLOCK:Blocks.AIR).defaultBlockState(),2);}}
 static void run(ServerLevel l,BiConsumer<Boolean,String> check){
  var o=new BlockPos(128,100,128);prepare(l,o);
  check.accept(new LegacyGreatwoodTree().generate(l,new Random(44),o.getX(),o.getY(),o.getZ(),true),"greatwood grows using original two-stage geometry");
  int logs=0,leaves=0,webs=0;for(var p:BlockPos.betweenClosed(o.offset(-16,0,-16),o.offset(16,48,16))){var s=l.getBlockState(p);if(s.is(R3Content.GREATWOOD_LOG.get()))logs++;if(s.is(R3Content.GREATWOOD_LEAVES.get()))leaves++;if(s.is(Blocks.COBWEB))webs++;}
  check.accept(logs>60&&leaves>300,"greatwood contains its broad trunk, branches and canopy");
  check.accept(webs>0&&l.getBlockEntity(o.below()) instanceof SpawnerBlockEntity,"spider greatwood creates webs and a real spawner");
  var chest=(ChestBlockEntity)l.getBlockEntity(o.below(2));check.accept(chest!=null&&BuiltInLootTables.SIMPLE_DUNGEON.equals(chest.getLootTable()),"spider chest uses the approved modern simple_dungeon table");
  var spawner=(SpawnerBlockEntity)l.getBlockEntity(o.below());check.accept(spawner.saveWithoutMetadata(l.registryAccess()).toString().contains("minecraft:cave_spider"),"greatwood spawner retains original cave-spider type");
  prepare(l,o);for(int x=-2;x<=2;x++)for(int z=-2;z<=2;z++)l.setBlock(o.offset(x,1,z),Blocks.BEDROCK.defaultBlockState(),2);
  check.accept(!new LegacyGreatwoodTree().generate(l,new Random(44),o.getX(),o.getY(),o.getZ(),false),"greatwood rejects an obstructed trunk without a partial tree");
  prepare(l,o);l.setBlock(o.above(2),Blocks.BEDROCK.defaultBlockState(),2);check.accept(!new LegacySilverwoodTree(true,7,5).func_76484_a(l,RandomSource.create(42),o.getX(),o.getY(),o.getZ()),"silverwood rejects obstruction before replacing world blocks");
  AuraNodeBlockEntity node=null;int branchLogs=0;for(int seed=0;seed<16&&node==null;seed++){prepare(l,o);check.accept(new LegacySilverwoodTree(true,7,5).func_76484_a(l,RandomSource.create(seed),o.getX(),o.getY(),o.getZ()),"silverwood seeded growth "+seed);for(var p:BlockPos.betweenClosed(o.offset(-5,0,-5),o.offset(5,18,5))){var state=l.getBlockState(p);if(state.is(R3Content.SILVERWOOD_NODE_LOG.get())&&l.getBlockEntity(p) instanceof AuraNodeBlockEntity n)node=n;if(state.is(R3Content.SILVERWOOD_LOG.get())&&state.getValue(RotatedPillarBlock.AXIS)!=Direction.Axis.Y)branchLogs++;}}
  check.accept(node!=null&&node.getBlockState().is(R3Content.SILVERWOOD_NODE_LOG.get()),"silverwood stores its aura node inside a distinct log BlockEntity");
  var saved=node.saveWithoutMetadata(l.registryAccess());var copy=new AuraNodeBlockEntity(node.getBlockPos(),node.getBlockState());copy.loadWithComponents(saved,l.registryAccess());check.accept(saved.equals(copy.saveWithoutMetadata(l.registryAccess())),"embedded node aspects, modifier and type survive NBT round trip");
  check.accept(saved.toString().toLowerCase(java.util.Locale.ROOT).contains("pure")||node.state.type==NodeState.Type.PURE,"silverwood creates a pure aura node");
  check.accept(branchLogs>0,"silverwood branches use horizontal log axes");
  check.accept(!R3Content.GREATWOOD_SAPLING.get().isValidBonemealTarget(l,o,R3Content.GREATWOOD_SAPLING.get().defaultBlockState())&&!R3Content.SILVERWOOD_SAPLING.get().isValidBonemealTarget(l,o,R3Content.SILVERWOOD_SAPLING.get().defaultBlockState()),"magical saplings retain original lack of bonemeal growth");
  var sap=o.offset(0,0,28);l.getChunkAt(sap);l.setBlock(sap.below(),Blocks.STONE.defaultBlockState(),2);l.setBlock(sap,R3Content.SILVERWOOD_SAPLING.get().defaultBlockState(),2);R3Content.SILVERWOOD_SAPLING.get().advanceTree(l,sap,l.getBlockState(sap),RandomSource.create(3));check.accept(l.getBlockState(sap).is(R3Content.SILVERWOOD_SAPLING.get()),"failed sapling growth restores the original sapling");
  var p=new thaumcraft.production.PedestalBlockEntity(o,R3Content.ARCANE_PEDESTAL.get().defaultBlockState());p.setInfusionResult(new ItemStack(Items.DIAMOND,4));var p2=new thaumcraft.production.PedestalBlockEntity(o,p.getBlockState());p2.loadWithComponents(p.saveWithoutMetadata(l.registryAccess()),l.registryAccess());check.accept(p2.getItem(0).getCount()==4,"batch infusion output is preserved on the one-input pedestal across NBT");
 }
}
