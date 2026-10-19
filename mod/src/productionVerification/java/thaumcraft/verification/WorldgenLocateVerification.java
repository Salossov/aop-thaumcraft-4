package thaumcraft.verification;

import java.nio.file.*;
import java.util.*;
import net.minecraft.commands.*;
import net.minecraft.core.*;
import net.minecraft.network.chat.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import thaumcraft.world.*;
import thaumcraft.registry.*;

/** Ordinary native terrain and source RNG, no forced structure choices, rates or prepared surfaces. */
final class WorldgenLocateVerification {
    private final MinecraftServer server;
    private final ServerLevel level;
    private final List<String> checks=new ArrayList<>();
    private final Map<String,Object> measurements=new LinkedHashMap<>(),positions=new LinkedHashMap<>();
    private final List<Component> messages=new ArrayList<>();
    private static final String[] TARGETS={"ring_altar","cultists","hilltop","mound","totem","spider_tree"};
    private final BlockPos origin=new BlockPos(20008,100,20008);
    private int ticks,stage,which,started,chunkIndex,netherNodes;private boolean finished;
    private ChunkPreparation chunks;private BlockPos forest,cultist,located;
    private net.neoforged.neoforge.common.util.FakePlayer player;
    private long previous=System.nanoTime();private double maxTickGap;
    private final CommandSource output=new CommandSource(){
        public void sendSystemMessage(Component text){messages.add(text.copy());System.out.println("WORLDGEN_QA "+text.getString());}
        public boolean acceptsSuccess(){return true;}public boolean acceptsFailure(){return true;}public boolean shouldInformAdmins(){return false;}
    };
    private CommandSourceStack source(BlockPos pos){return new CommandSourceStack(output,pos.getCenter(),Vec2.ZERO,level,4,"WorldgenQA",Component.literal("WorldgenQA"),server,null);}
    private WorldgenLocateVerification(MinecraftServer server){this.server=server;level=server.overworld();}
    static void register(MinecraftServer server){var test=new WorldgenLocateVerification(server);NeoForge.EVENT_BUS.addListener(test::tick);}
    private void check(boolean value,String text){if(!value)throw new AssertionError(text);checks.add(text);System.out.println("WORLDGEN_CHECK "+text);}
    private Path root(){return Path.of(System.getProperty("thaumcraft.r3.root"),".cache/worldgen-locate-2026-10-17");}
    private void save(boolean passed,Throwable error)throws Exception{var result=new LinkedHashMap<String,Object>();result.put("passed",passed);result.put("finished",finished);result.put("ticks",ticks);result.put("stage",stage);result.put("target",which<TARGETS.length?TARGETS[which]:"post-locate");result.put("seed",level.getSeed());result.put("checks",checks);result.put("positions",positions);result.put("measurements",measurements);result.put("maxTickGapMs",maxTickGap);if(error!=null)result.put("error",error.toString());Files.createDirectories(root());Files.writeString(root().resolve("result.json"),new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(result));}
    private void tick(ServerTickEvent.Post event){
        if(finished||event.getServer()!=server)return;long now=System.nanoTime();maxTickGap=Math.max(maxTickGap,(now-previous)/1e6);previous=now;ticks++;
        try{
            if(ticks>36000)throw new AssertionError("World generation verification exceeded 36000 ticks");
            if(ticks%200==0)save(false,null);
            if(stage==0){
                check(!(level.getChunkSource().getGenerator() instanceof net.minecraft.world.level.levelgen.FlatLevelSource),"real ordinary noise terrain, not a flat placement fixture");
                check(thaumcraft.config.ThaumcraftConfig.GENERATE_STRUCTURES.get()&&thaumcraft.config.ThaumcraftConfig.GENERATE_AURA.get(),"default natural structures and aura enabled");
                check(level.getChunkSource().getChunkNow(origin.getX()>>4,origin.getZ()>>4)==null,"locate origin initially cold and unexplored");
                stage=1;begin();return;
            }
            if(stage==1){
                var found=messages.stream().filter(m->m.getString().contains(" found at ")).findFirst().orElse(null);
                if(found==null){if(messages.stream().anyMatch(m->m.getString().contains("timed out")||m.getString().startsWith("Locate failed")||m.getString().startsWith("No ")))throw new AssertionError(messages.getLast().getString());return;}
                var click=findClick(found);check(click!=null&&click.getAction()==ClickEvent.Action.SUGGEST_COMMAND,"actual locate response suggests a teleport for "+TARGETS[which]);
                String[] xyz=click.getValue().substring("/tp @s ".length()).split(" ");located=new BlockPos(Integer.parseInt(xyz[0]),Integer.parseInt(xyz[1]),Integer.parseInt(xyz[2]));positions.put(TARGETS[which],List.of(located.getX(),located.getY(),located.getZ()));measurements.put(TARGETS[which]+"Ticks",ticks-started);
                if(which==0)check(level.getChunkSource().getChunkNow(located.getX()>>4,located.getZ()>>4)==null,"native locate predicts an unexplored site without generating FULL terrain");
                var chunk=new ChunkPos(located);chunks=new ChunkPreparation(level,chunk.x,chunk.x,chunk.z,chunk.z);stage=10;return;
            }
            if(stage==10){
                if(!chunks.advance(2)||!NaturalPopulation.advanceLocatedChunk(level,new ChunkPos(located),2))return;
                var wanted=switch(TARGETS[which]){case "ring_altar"->NativeStructures.Kind.RING_ALTAR;case "cultists"->NativeStructures.Kind.CULTIST_RING;case "hilltop"->NativeStructures.Kind.HILLTOP;case "mound"->NativeStructures.Kind.MOUND;case "totem"->NativeStructures.Kind.TOTEM;default->NativeStructures.Kind.SPIDER_TREE;};
                NativeStructures.Piece piece=null;for(var start:level.getChunk(located).getAllStarts().values())if(start.isValid())for(var part:start.getPieces())if(part instanceof NativeStructures.Piece p&&(p.kind==wanted||which==0&&p.kind==NativeStructures.Kind.CULTIST_RING))piece=p;
                check(piece!=null,"located native start survives FULL generation for "+TARGETS[which]);var at=piece.anchor;
                switch(TARGETS[which]){
                    case "ring_altar","cultists"->{check(level.getBlockEntity(at.above()) instanceof thaumcraft.production.EldritchAltarBlockEntity,"located natural ring has a real altar");check(level.getBlockState(at.above(3)).is(R5Content.OBELISK.get())&&level.getBlockState(at.above(7)).is(R5Content.ELDRITCH_BODY.get()),"natural ring has complete floating obelisk geometry");if(which==1){var altar=(thaumcraft.production.EldritchAltarBlockEntity)level.getBlockEntity(at.above());check(altar.guardianSpawner&&altar.spawnType==0,"cultists locate filters to a cultist-enabled ring");cultist=at.above();}}
                    case "hilltop"->checkLoot(at.above(2),"natural hilltop circle");
                    case "mound"->checkLoot(at.offset(10,1,9),"natural mound");
                    case "totem"->{boolean found=false;for(int n=2;n<=5;n++)if(level.getBlockEntity(at.above(n)) instanceof thaumcraft.blocks.AuraNodeBlockEntity node&&node.state.type==thaumcraft.api.nodes.NodeState.Type.DARK)found=true;check(found,"natural obsidian totem contains its embedded sinister aura node");}
                    case "spider_tree"->check(level.getBlockEntity(at.below()) instanceof SpawnerBlockEntity&&level.getBlockEntity(at.below(2)) instanceof ChestBlockEntity,"natural spider greatwood contains its spawner and loot chest");
                }
                chunks.close();chunks=null;save(false,null);if(++which<TARGETS.length){stage=1;begin();return;}
                level.setChunkForced(cultist.getX()>>4,cultist.getZ()>>4,true);player=net.neoforged.neoforge.common.util.FakePlayerFactory.get(level,new com.mojang.authlib.GameProfile(UUID.randomUUID(),"WorldgenSpawnQA"));player.moveTo(cultist.getCenter().add(8,2,8));level.addNewPlayer(player);started=ticks;stage=2;return;
            }
            if(stage==2){if(ticks-started<240)return;check(level.getEntitiesOfClass(thaumcraft.entities.CultistCleric.class,new AABB(cultist).inflate(16)).size()>=3,"natural cultist altar actually spawns ritualists under ordinary server ticks");player.discard();player=null;level.setChunkForced(cultist.getX()>>4,cultist.getZ()>>4,false);stage=3;return;}
            if(stage==3){
                var generator=level.getChunkSource().getGenerator();var state=level.getChunkSource().randomState();
                for(int i=0;i<32;i++){int n=chunkIndex++,x=origin.getX()+(n%64-32)*128,z=origin.getZ()+(n/64-32)*128;int y=generator.getBaseHeight(x,z,Heightmap.Types.WORLD_SURFACE_WG,level,state);if(generator.getBiomeSource().getNoiseBiome(x>>2,y>>2,z>>2,state.sampler()).is(WorldBiomes.MAGICAL)){forest=new BlockPos(x,y,z);break;}}
                if(forest==null){check(chunkIndex<4096,"magical forest exists in native climate map");return;}
                positions.put("magical_forest",List.of(forest.getX(),forest.getY(),forest.getZ()));var p=new ChunkPos(forest);chunks=new ChunkPreparation(level,p.x-3,p.x+3,p.z-3,p.z+3);stage=4;return;
            }
            if(stage==4){if(!chunks.advance(4))return;int trees=0,magic=0;var center=new ChunkPos(forest);for(int x=(center.x-3)*16;x<(center.x+4)*16;x++)for(int z=(center.z-3)*16;z<(center.z+4)*16;z++){
                    int top=level.getHeight(Heightmap.Types.MOTION_BLOCKING,x,z);if(level.getBiome(new BlockPos(x,top,z)).is(WorldBiomes.MAGICAL))magic++;
                    for(int y=top;y>=top-40;y--){var p=new BlockPos(x,y,z);if(level.getBlockState(p).is(net.minecraft.tags.BlockTags.LOGS)&&level.getBlockState(p.below()).is(net.minecraft.tags.BlockTags.DIRT)){trees++;break;}}
                }measurements.put("forestSurfaceColumns",magic);measurements.put("forestGroundedTreeColumns",trees);check(magic>128&&trees>=8,"native magical forest is a biome with multiple grounded trees, not one isolated magic tree: columns="+magic+", trunks="+trees);chunks.close();chunks=null;chunkIndex=0;stage=5;return;}
            if(stage==5){
                var nether=server.getLevel(Level.NETHER);var pos=new ChunkPos(900+chunkIndex%16,900+chunkIndex/16);
                if(chunks==null)chunks=new ChunkPreparation(nether,pos.x,pos.x,pos.z,pos.z);
                if(!chunks.advance(1)||!NaturalPopulation.advanceLocatedChunk(nether,pos,2))return;
                for(var tile:nether.getChunk(pos.x,pos.z).getBlockEntities().values())if(tile instanceof thaumcraft.blocks.AuraNodeBlockEntity)netherNodes++;
                chunks.close();chunks=null;chunkIndex++;
                if(netherNodes<3&&chunkIndex<256)return;
                measurements.put("netherPopulatedChunks",chunkIndex);measurements.put("netherAuraNodes",netherNodes);check(netherNodes>0,"aura nodes actually populate native Nether terrain at the unchanged rarity");stage=6;started=ticks;messages.clear();server.getCommands().performPrefixedCommand(source(new BlockPos(-160000,100,160000)),"thaumcraft locate mound");return;
            }
            if(stage==6){if(ticks-started<60)return;server.getCommands().performPrefixedCommand(source(origin),"thaumcraft locate cancel");check(messages.stream().anyMatch(m->m.getString().equals("Locate cancelled")),"cold locate can be cancelled while generation is pending");
                var data=NaturalPopulationData.get(level);var restored=NaturalPopulationData.load(data.save(new net.minecraft.nbt.CompoundTag(),level.registryAccess()),level.registryAccess());check(restored.requests().equals(data.requests()),"saved population backlog survives round trip without duplication");
                var index=StructureIndexData.get(level);var restoredIndex=StructureIndexData.load(index.save(new net.minecraft.nbt.CompoundTag(),level.registryAccess()),level.registryAccess());check(restoredIndex.nearest(level,StructureIndexData.Kind.TOTEM,origin.getX(),origin.getZ())!=null&&restoredIndex.nearest(level,StructureIndexData.Kind.CULTIST_RING,origin.getX(),origin.getZ())!=null,"totem and cultist indexes survive SavedData round trip");finish(null);
            }
        }catch(Throwable error){error.printStackTrace();finish(error);}
    }
    private void begin(){messages.clear();started=ticks;long time=System.nanoTime();server.getCommands().performPrefixedCommand(source(origin),"thaumcraft locate "+TARGETS[which]);measurements.put(TARGETS[which]+"CommandMs",(System.nanoTime()-time)/1e6);check(messages.stream().anyMatch(m->m.getString().startsWith("Searching")),"actual command starts cooperative search for "+TARGETS[which]);}
    private static ClickEvent findClick(Component text){if(text.getStyle().getClickEvent()!=null)return text.getStyle().getClickEvent();for(var child:text.getSiblings()){var click=findClick(child);if(click!=null)return click;}return null;}
    private void checkLoot(BlockPos pos,String label){check(level.getBlockEntity(pos) instanceof ChestBlockEntity,label+" has an actual chest");var chest=(ChestBlockEntity)level.getBlockEntity(pos);boolean loot=false;for(int i=0;i<chest.getContainerSize();i++)loot|=!chest.getItem(i).isEmpty();check(loot,label+" unpacks nonempty TC4 dungeon loot");}
    private void finish(Throwable error){finished=true;if(chunks!=null)chunks.close();if(player!=null)player.discard();if(cultist!=null)level.setChunkForced(cultist.getX()>>4,cultist.getZ()>>4,false);NaturalStructureLocate.start(source(origin),"cancel");try{save(error==null,error);server.saveEverything(false,true,true);}catch(Exception e){throw new RuntimeException(e);}finally{server.halt(false);}}
}
