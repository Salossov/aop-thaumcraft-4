package thaumcraft.verification;

import java.nio.file.*;
import java.util.*;
import com.google.gson.*;
import net.minecraft.core.*;
import net.minecraft.server.level.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import thaumcraft.api.aspects.Aspects;
import thaumcraft.api.nodes.NodeState;
import thaumcraft.blocks.AuraNodeBlockEntity;
import thaumcraft.registry.R2Content;

/** Only the verification run: raw materials and a bounded local scene, never grants research or wand vis. */
final class ClientCycleFixture {
    static final BlockPos CRAFT=new BlockPos(200,100,200),SHELF=new BlockPos(203,100,200),TABLE=new BlockPos(198,100,203),BENCH=new BlockPos(203,100,203),NODE=new BlockPos(200,101,207);
    private static Path evidence;private static ServerPlayer actor;private static long applied;
    static void register(Path directory){evidence=directory;NeoForge.EVENT_BUS.addListener(ClientCycleFixture::login);NeoForge.EVENT_BUS.addListener(ClientCycleFixture::tick);}
    private static void login(PlayerEvent.PlayerLoggedInEvent event){
        if(!(event.getEntity() instanceof ServerPlayer player)||!player.getGameProfile().getName().startsWith("R2TC4"))return;actor=player;if(System.getProperty("thaumcraft.r2.phase","").equals("client_review")){move("NODE");return;}var level=player.serverLevel();level.setDayTime(6000);level.getGameRules().getRule(net.minecraft.world.level.GameRules.RULE_DOMOBSPAWNING).set(false,level.getServer());
        for(int x=194;x<=210;x++)for(int z=194;z<=212;z++){level.getChunkAt(new BlockPos(x,99,z));level.setBlockAndUpdate(new BlockPos(x,99,z),Blocks.STONE.defaultBlockState());for(int y=100;y<=106;y++)level.setBlockAndUpdate(new BlockPos(x,y,z),Blocks.AIR.defaultBlockState());}
        level.setBlockAndUpdate(CRAFT,Blocks.CRAFTING_TABLE.defaultBlockState());level.setBlockAndUpdate(SHELF,Blocks.BOOKSHELF.defaultBlockState());node(level);
        player.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);player.getInventory().clearContent();
        for(var item:List.of(Items.IRON_NUGGET,Items.STICK,Items.OAK_SLAB,Items.OAK_PLANKS,Items.GLASS_BOTTLE,Items.FEATHER,Items.BLACK_DYE,Items.GOLD_INGOT,Items.GLASS,Items.LEATHER,Items.PAPER))player.getInventory().add(new ItemStack(item,32));
        player.getInventory().add(new ItemStack(R2Content.SHARDS.get(0).get(),16));player.getInventory().add(new ItemStack(R2Content.SHARDS.get(1).get(),16));player.getFoodData().setFoodLevel(20);player.setHealth(20);move("CRAFT");
        try{Files.createDirectories(evidence);Files.writeString(evidence.resolve("client-fixture-ready.json"),new Gson().toJson(Map.of("player",player.getUUID().toString(),"name",player.getGameProfile().getName())));}catch(Exception error){throw new RuntimeException(error);}
    }
    private static void node(ServerLevel level){level.setBlockAndUpdate(NODE,Blocks.AIR.defaultBlockState());level.setBlockAndUpdate(NODE,R2Content.AURA_NODE.get().defaultBlockState());var vis=new LinkedHashMap<String,Integer>();Aspects.PRIMALS.forEach(a->vis.put(a.getTag(),100));((AuraNodeBlockEntity)level.getBlockEntity(NODE)).initialize(vis,NodeState.Type.NORMAL,NodeState.Modifier.BRIGHT);}
    private static void tick(ServerTickEvent.Post event){
        if(actor==null||actor.hasDisconnected())return;var file=evidence.resolve("client-fixture-request.json");if(!Files.exists(file))return;
        try{var request=JsonParser.parseString(Files.readString(file)).getAsJsonObject();if(!request.get("player").getAsString().equals(actor.getUUID().toString()))return;long sequence=request.get("sequence").getAsLong();if(sequence<=applied)return;applied=sequence;String command=request.get("command").getAsString();
            if(command.equals("NEW_NODE"))node(actor.serverLevel());else move(command);
            Files.writeString(evidence.resolve("client-fixture-response.json"),new Gson().toJson(Map.of("sequence",sequence,"command",command)));
        }catch(Exception error){thaumcraft.Thaumcraft.LOGGER.error("Client fixture failed",error);}
    }
    private static void move(String point){BlockPos target=switch(point){case "CRAFT"->CRAFT;case "SHELF"->SHELF;case "RESEARCH"->TABLE;case "BENCH"->BENCH;case "NODE"->NODE;default->throw new IllegalArgumentException(point);};double x=target.getX()+0.5,z=target.getZ()-2.5;actor.teleportTo(actor.serverLevel(),x,100,z,0,point.equals("NODE")?2.3f:25);}
}
