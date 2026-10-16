package thaumcraft.verification;

import java.nio.file.*;
import java.util.*;
import com.google.gson.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import thaumcraft.api.aspects.*;
import thaumcraft.blocks.*;
import thaumcraft.production.*;
import thaumcraft.registry.*;

/** Separate-profile native capture scene. Never part of the distributed mod. */
final class ModelAuditScene {
    static final BlockPos CENTER=new BlockPos(0,64,0);
    static Path root(){return Path.of(System.getProperty("thaumcraft.r3.root"),"analysis/comparison/model-audit");}
    private static List<JsonObject> scenes;private static int current=-1;private static ServerPlayer player;
    static void register(MinecraftServer server){
        try{scenes=JsonParser.parseString(Files.readString(root().resolve("port-manifest.json"))).getAsJsonArray().asList().stream().map(JsonElement::getAsJsonObject).toList();Files.deleteIfExists(root().resolve("port-request.json"));Files.deleteIfExists(root().resolve("port-ready.json"));}catch(Exception e){throw new RuntimeException(e);}
        NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedInEvent e)->{if(e.getEntity() instanceof ServerPlayer p){player=p;p.setGameMode(GameType.CREATIVE);p.getAbilities().flying=true;p.onUpdateAbilities();p.teleportTo(server.overworld(),3.2,66,3.2,Set.of(),135,18);}});
        NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedOutEvent e)->server.halt(false));
        NeoForge.EVENT_BUS.addListener((ServerTickEvent.Post e)->{if(player==null)return;try{
            var path=root().resolve("port-request.json");if(!Files.exists(path))return;int next=JsonParser.parseString(Files.readString(path)).getAsJsonObject().get("index").getAsInt();if(next==current)return;prepare(next);current=next;
        }catch(java.nio.file.NoSuchFileException|com.google.gson.JsonParseException transientWrite){}catch(Throwable failure){failure.printStackTrace();try{Files.writeString(root().resolve("port-server-error.txt"),failure.toString());}catch(Exception ignored){}}});
    }
    @SuppressWarnings({"rawtypes","unchecked"}) private static BlockState property(BlockState state,String name,String value){for(var p:state.getProperties())if(p.getName().equals(name)){var parsed=p.getValue(value);if(parsed.isPresent())return state.setValue((Property)p,(Comparable)parsed.get());}return state;}
    static BlockState state(JsonObject scene){
        var block=BuiltInRegistries.BLOCK.get(ResourceLocation.parse("thaumcraft:"+scene.get("modern").getAsString()));var state=block.defaultBlockState();String legacy=scene.get("legacy").getAsString();int meta=scene.get("metadata").getAsInt();
        if(legacy.equals("blockAiry")&&meta==12)return Blocks.BARRIER.defaultBlockState();
        if(legacy.equals("blockMirror"))state=property(state,"facing","up");
        if(legacy.equals("blockTable")){state=property(state,"facing",meta==2?"east":meta==1?"east":"west");}
        if(legacy.startsWith("blockCosmeticDoubleSlab"))state=property(state,"type","double");
        if(legacy.equals("blockCosmeticSolid")&&meta==10)state=property(state,"powered","true");
        if(legacy.equals("blockLootUrn")||legacy.equals("blockLootCrate"))state=property(state,"rarity",Integer.toString(meta));
        if(legacy.equals("blockMetalDevice")&&meta==6)state=property(state,"open","false");
        if(legacy.equals("blockArcaneDoor"))state=property(state,"open",meta==4?"true":"false");
        return state;
    }
    private static void prepare(int index)throws Exception{
        var scene=scenes.get(index);var level=player.serverLevel();
        level.setDayTime(6000);level.setWeatherParameters(6000,6000,false,false);
        for(var rule:List.of(GameRules.RULE_DAYLIGHT,GameRules.RULE_WEATHER_CYCLE,GameRules.RULE_DOMOBSPAWNING,GameRules.RULE_MOBGRIEFING))level.getGameRules().getRule(rule).set(false,level.getServer());level.getGameRules().getRule(GameRules.RULE_RANDOMTICKING).set(0,level.getServer());
        for(var entity:level.getEntitiesOfClass(net.minecraft.world.entity.Entity.class,new net.minecraft.world.phys.AABB(-15,50,-15,15,90,15)))if(!(entity instanceof net.minecraft.world.entity.player.Player))entity.discard();
        for(var pos:BlockPos.betweenClosed(-24,64,-24,24,72,24))level.setBlock(pos,Blocks.AIR.defaultBlockState(),2);
        for(var pos:BlockPos.betweenClosed(-24,63,-24,24,63,24))level.setBlock(pos,Blocks.STONE.defaultBlockState(),2);
        var state=state(scene);boolean missing=state.isAir();String legacy=scene.get("legacy").getAsString(),group=scene.get("group").getAsString();int meta=scene.get("metadata").getAsInt();
        if(state.getBlock() instanceof TaintFibresBlock fibres){var hit=new net.minecraft.world.phys.BlockHitResult(CENTER.below().getCenter().add(0,.5,0),Direction.UP,CENTER.below(),false);var placed=fibres.getStateForPlacement(new net.minecraft.world.item.context.BlockPlaceContext(player,net.minecraft.world.InteractionHand.MAIN_HAND,new ItemStack(Items.STONE),hit));if(placed!=null)state=placed;if(meta==4)state=state.setValue(TaintFibresBlock.OCCUPIED,true);}
        level.setBlock(CENTER,state,2);
        if(state.getBlock() instanceof BushBlock)level.setBlock(CENTER.below(),Blocks.DIRT.defaultBlockState(),2);
        if(index==36)level.setBlock(CENTER.below(),Blocks.REDSTONE_BLOCK.defaultBlockState(),2);
        if(state.getBlock() instanceof DoorBlock)level.setBlock(CENTER.above(),state.setValue(DoorBlock.HALF,DoubleBlockHalf.UPPER),2);
        if(legacy.equals("blockTable")&&meta==2)level.setBlock(CENTER.east(),state.setValue(TableBlock.SLAVE,true).setValue(TableBlock.FACING,Direction.WEST),2);
        if(group.equals("supplementary")&&legacy.equals("blockTube"))for(var dir:Direction.values())level.setBlock(CENTER.relative(dir),R3Content.TUBE.get().defaultBlockState(),3);
        if(group.equals("assembled")&&state.getBlock() instanceof InfernalFurnaceBlock){for(int part=0;part<27;part++){var pos=CENTER.offset(InfernalFurnaceBlock.offset(part));level.setBlock(pos,state.setValue(InfernalFurnaceBlock.PART,part),2);}}
        if(state.getBlock() instanceof AdvancedAlchemyBlock){for(int part=0;part<18;part++)if(part!=13)level.setBlock(CENTER.offset(AdvancedAlchemyBlock.offset(part)),state.setValue(AdvancedAlchemyBlock.PART,part),2);}
        if(!group.isEmpty()&&legacy.equals("blockJar")&&level.getBlockEntity(CENTER) instanceof TransportBlockEntity jar){jar.applyLabel(Aspects.AIR);jar.addToContainer(Aspects.AIR,48);}
        if(level.getBlockEntity(CENTER) instanceof AuraNodeBlockEntity node){node.state.base.put("aer",50);node.state.current.put("aer",50);node.setChanged();if(!group.isEmpty())player.setItemSlot(net.minecraft.world.entity.EquipmentSlot.HEAD,new ItemStack(R2Content.GOGGLES.get()));}
        if(legacy.equals("blockWarded")&&level.getBlockEntity(CENTER) instanceof FocusMemoryBlockEntity copy)copy.remember(Blocks.STONE.defaultBlockState(),player.getUUID(),420,1,null);
        if(group.equals("assembled")&&legacy.equals("blockTable")&&level.getBlockEntity(CENTER) instanceof TableBlockEntity table){table.setItem(0,new ItemStack(R2Content.SCRIBING_TOOLS.get()));table.setItem(1,new ItemStack(R2Content.NOTES.get()));}
        if(level.getBlockEntity(CENTER) instanceof thaumcraft.visnet.EnergizedNodeBlockEntity energized){var source=new AuraNodeBlockEntity(CENTER,R2Content.AURA_NODE.get().defaultBlockState());for(var tag:java.util.List.of("aer","ignis","terra","aqua","ordo","perditio")){source.state.base.put(tag,20);source.state.current.put(tag,20);}energized.initialize(source);}
        if(level.getBlockEntity(CENTER) instanceof thaumcraft.blocks.EtherealBloomBlockEntity bloom){bloom.growth=100;}
        var te=level.getBlockEntity(CENTER);if(te!=null){te.setChanged();level.sendBlockUpdated(CENTER,state,level.getBlockState(CENTER),3);}
        player.teleportTo(level,3.2,66,3.2,Set.of(),135,18);
        var result=new JsonObject();result.addProperty("index",index);result.addProperty("missing",missing);result.addProperty("actual",BuiltInRegistries.BLOCK.getKey(level.getBlockState(CENTER).getBlock()).toString());result.addProperty("state",level.getBlockState(CENTER).toString());if(te!=null)result.addProperty("tile",te.getClass().getName());
        Files.writeString(root().resolve("port-ready.tmp"),result.toString());Files.move(root().resolve("port-ready.tmp"),root().resolve("port-ready.json"),StandardCopyOption.REPLACE_EXISTING);
    }
}
