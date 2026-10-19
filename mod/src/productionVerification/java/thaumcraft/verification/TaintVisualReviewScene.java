package thaumcraft.verification;

import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import thaumcraft.entities.*;
import thaumcraft.registry.*;
import thaumcraft.world.*;

/** Isolated fixtures for the user's taint, hilltop and champion screenshots. */
final class TaintVisualReviewScene {
 static BlockPos naturalAnchor;static boolean spawned;static java.util.List<BlockPos> fibres;
 static float yaw(int scene){if(scene>=130&&scene<=133)return scene==130?180:scene==131?90:scene==132?0:-90;return scene==124||scene==128?142:scene==126?136:180;}
 static float pitch(int scene){return scene==124||scene==128?32:scene==126?35:12;}
 static void prepare(int scene){TaintNaturalReview.prepare(scene);var p=PhotoLiveScene.player;var l=p.serverLevel();
  if(scene==128){PhotoLiveScene.check(l.getChunkSource().getChunkNow(19672>>4,20392>>4)==null,"native hilltop target has no loaded full chunk before teleport");p.teleportTo(19680.5,95,20402.5);}
  if(scene==123){int slot=0;for(var block:java.util.List.of(R2Content.TAINT_FIBRES,R2Content.TAINT_GROWTH,R2Content.TAINT_TENDRILS,R2Content.TAINT_SPORE_STALK,R2Content.TAINTED_SOIL))p.getInventory().setItem(slot++,new ItemStack(block.get()));p.inventoryMenu.broadcastChanges();}
  if(scene==124){var o=new BlockPos(0,100,0);for(var at:BlockPos.betweenClosed(o.offset(-5,-2,-5),o.offset(5,6,5)))l.setBlock(at,at.getY()<100?Blocks.STONE.defaultBlockState():Blocks.AIR.defaultBlockState(),2);PhotoLiveScene.check(new LegacyHilltopStones().generateNative(l,RandomSource.create(4235),o),"actual hilltop generator succeeds");int tile=0,totem=0;for(var at:BlockPos.betweenClosed(o.offset(-3,0,-3),o.offset(3,4,3))){var state=l.getBlockState(at);if(state.is(R4Content.OBSIDIAN_TILE))tile++;if(state.is(R4Content.OBSIDIAN_TOTEM)){totem++;PhotoLiveScene.check(state.equals(R4Content.OBSIDIAN_TOTEM.get().at(l,at)),"generated totem has position-dependent texture and final pillar cap: "+at);}PhotoLiveScene.check(!state.is(R3Content.ARCANE_STONE)&&!state.is(R3Content.ARCANE_STONE_BRICKS),"hilltop contains no substituted arcane stone");}PhotoLiveScene.check(tile>10&&totem>=16,"hilltop uses original obsidian tiles and all eight totem pillars");PhotoLiveScene.check(l.getBlockState(o.above()).is(R4Content.OBSIDIAN_TILE)&&l.getBlockState(o.above(2)).is(Blocks.CHEST)&&l.getBlockState(o).is(Blocks.SPAWNER),"original hilltop center retains chest and wisp spawner");p.teleportTo(8.5,108,10.5);}
  if(scene==125){var boss=R5Entities.ELDRITCH_WARDEN.get().create(l);boss.moveTo(-1,65,0,0,0);boss.finalizeSpawn(l,l.getCurrentDifficultyAt(boss.blockPosition()),MobSpawnType.COMMAND,null);ChampionSystem.makeChampion(boss,0,true);boss.setNoAi(true);l.addFreshEntity(boss);var swarm=R2Entities.TAINT_SWARM.get().create(l);swarm.moveTo(2,66,0,0,0);swarm.setNoAi(true);swarm.setPersistenceRequired();l.addFreshEntity(swarm);p.teleportTo(.5,66,8.5);}
  if(scene==126){spawned=false;naturalAnchor=new BlockPos(22923,63,17529);fibres=new java.util.ArrayList<>();l.setDayTime(18000);p.setGameMode(GameType.SURVIVAL);p.getAbilities().mayfly=true;p.getAbilities().flying=true;p.onUpdateAbilities();p.teleportTo(naturalAnchor.getX()+50.5,85,naturalAnchor.getZ()+30.5);}
  if(scene==127){var o=PhotoLiveScene.O;for(int x=-3;x<=3;x++)for(int z=-3;z<=3;z++)WorldBiomes.change(l,x,z,WorldBiomes.TAINT);l.setBlockAndUpdate(o,R2Content.TAINT_SPORE_STALK.get().defaultBlockState());for(int i=0;i<1000&&l.getEntitiesOfClass(TaintSpore.class,new AABB(o).inflate(3)).isEmpty();i++)l.getBlockState(o).randomTick(l,o,l.random);PhotoLiveScene.check(!l.getEntitiesOfClass(TaintSpore.class,new AABB(o).inflate(3)).isEmpty(),"native stalk random ticks actually create a spore mob");}
 }
 static void tick(int scene,int age)throws Exception{TaintNaturalReview.tick(scene,age);var p=PhotoLiveScene.player;var l=p.serverLevel();
  if(scene==128&&age==120){var o=new BlockPos(19672,87,20392);var chunk=l.getChunk(o);var pieces=chunk.getAllStarts().values().stream().flatMap(start->start.getPieces().stream()).filter(part->part instanceof NativeStructures.Piece piece&&piece.kind==NativeStructures.Kind.HILLTOP).toList();PhotoLiveScene.check(pieces.size()==1,"fresh FULL noise chunk has its actual native hilltop structure start");o=((NativeStructures.Piece)pieces.get(0)).anchor;int count=0;for(var at:BlockPos.betweenClosed(o.offset(-3,1,-3),o.offset(3,4,3)))if(l.getBlockState(at).is(R4Content.OBSIDIAN_TOTEM)){count++;PhotoLiveScene.check(l.getBlockState(at).equals(R4Content.OBSIDIAN_TOTEM.get().at(l,at)),"WorldGenRegion pillar caps and patterns match final neighbors: "+at);}PhotoLiveScene.check(count>=16&&l.getBlockState(o.above()).is(R4Content.OBSIDIAN_TILE),"naturally generated hilltop uses obsidian tile and eight complete textured totem pillars");}
  if(scene==126&&age==40){for(var at:BlockPos.betweenClosed(naturalAnchor.offset(-30,-15,-30),naturalAnchor.offset(30,30,30)))if(l.getBlockState(at).is(R2Content.TAINT_FIBRES)&&l.getBiome(at).is(WorldBiomes.TAINT))fibres.add(at.immutable());PhotoLiveScene.check(!fibres.isEmpty(),"spawn audit uses actual naturally generated taint fibres, no terrain fixture");}
  if(scene==126&&age>=45&&age<230&&!spawned){for(int i=0;i<12;i++)NaturalSpawner.spawnCategoryForPosition(MobCategory.MONSTER,l,fibres.get(l.random.nextInt(fibres.size())));var mobs=l.getEntitiesOfClass(Taintacle.class,new AABB(naturalAnchor).inflate(90));if(!mobs.isEmpty()){spawned=true;var target=mobs.get(0);mobs.forEach(e->e.setNoAi(true));p.setGameMode(GameType.CREATIVE);p.teleportTo(target.getX()+6, target.getY()+4,target.getZ()+8);}}
  if(scene==126&&age==240)PhotoLiveScene.check(spawned,"native NaturalSpawner creates taintacles at night on untouched generated tainted land");
 }
 private TaintVisualReviewScene(){}
}
