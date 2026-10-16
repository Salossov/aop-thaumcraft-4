package thaumcraft.verification;

import java.util.*;
import java.nio.file.*;
import net.minecraft.core.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.*;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import thaumcraft.api.aspects.*;
import thaumcraft.api.knowledge.PlayerKnowledge.ScanKind;
import thaumcraft.blocks.*;
import thaumcraft.knowledge.*;
import thaumcraft.network.*;
import thaumcraft.registry.*;

final class InterfaceFollowupScene {
    private static final List<String> checks=new ArrayList<>();
    private static void check(boolean ok,String label){if(!ok)throw new AssertionError(label);checks.add(label);}
    static void register(MinecraftServer server){NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedInEvent event)->{if(event.getEntity() instanceof ServerPlayer player){try{prepare(player);verify(player);prepare(player);write(true,null);}catch(Throwable failure){failure.printStackTrace();write(false,failure.toString());prepare(player);}}});NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedOutEvent event)->server.halt(false));}
    private static void prepare(ServerPlayer player){
        player.setGameMode(net.minecraft.world.level.GameType.CREATIVE);player.getAbilities().flying=true;player.onUpdateAbilities();player.server.getPlayerList().op(player.getGameProfile());player.removeAllEffects();player.server.getCommands().performPrefixedCommand(player.createCommandSourceStack().withPermission(4),"execute in minecraft:overworld run tp @s 608 117 603 0 20");
        for(var boss:player.serverLevel().getEntitiesOfClass(thaumcraft.entities.EldritchWarden.class,new AABB(600,110,608,620,121,620)))if(boss.isNoAi()&&boss.isNoGravity()&&boss.hasCustomName()&&Set.of("Zathog","Second Warden").contains(boss.getCustomName().getString()))boss.discard();
        player.getInventory().clearContent();player.getInventory().selected=0;player.connection.send(new net.minecraft.network.protocol.game.ClientboundSetCarriedItemPacket(0));player.getInventory().setItem(0,new ItemStack(R2Content.BOOK.get()));player.getInventory().setItem(1,new ItemStack(R2Content.THAUMOMETER.get()));player.getInventory().setItem(9,new ItemStack(R3Equipment.KNOWLEDGE_FRAGMENT.get()));player.inventoryMenu.setCarried(ItemStack.EMPTY);
        KnowledgeService.mutate(player,k->{for(var key:List.copyOf(k.completedResearch()))k.forgetResearch(key);for(var d:thaumcraft.data.ThaumcraftData.get(player.server).research().values())if(!Set.of("PAVEWARD","NITOR").contains(d.key()))k.completeResearch(d.key());for(var aspect:Aspect.all().values())k.setPool(aspect.getTag(),100);k.clearScans();k.recordScan(ScanKind.OBJECT,"minecraft:water");k.recordScan(ScanKind.OBJECT,"minecraft:ice");return true;});player.inventoryMenu.sendAllDataToRemote();
        try{var f=InventoryScanService.class.getDeclaredField("SEQUENCES");f.setAccessible(true);((Map<?,?>)f.get(null)).remove(player);}catch(Exception error){throw new RuntimeException(error);}
    }
    private static void verify(ServerPlayer player){
        var level=player.serverLevel();var pos=ResearchUiScene.POS;
        for(var direction:List.of(Direction.NORTH,Direction.SOUTH,Direction.EAST,Direction.WEST)){
            level.setBlock(pos,R2Content.RESEARCH_TABLE.get().defaultBlockState().setValue(TableBlock.FACING,direction),2);var slave=pos.relative(direction);level.setBlock(slave,R2Content.RESEARCH_TABLE.get().defaultBlockState().setValue(TableBlock.FACING,direction.getOpposite()).setValue(TableBlock.SLAVE,true),2);
            for(var half:List.of(pos,slave)){var state=level.getBlockState(half);var shape=state.getShape(level,half,net.minecraft.world.phys.shapes.CollisionContext.empty());var hit=shape.clip(Vec3.atCenterOf(half).add(0,2,0),Vec3.atCenterOf(half),half);check(hit!=null&&Math.abs(hit.getLocation().y-(half.getY()+1))<.001,"research tabletop ray hits full visible height for "+direction+" "+(half==pos?"master":"slave"));state.useWithoutItem(level,player,new BlockHitResult(Vec3.atCenterOf(half),Direction.UP,half,false));check(player.containerMenu instanceof thaumcraft.menu.ResearchMenu menu&&menu.table==level.getBlockEntity(pos),"both research halves open the master container for "+direction);player.closeContainer();}
        }
        player.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);var session=KnowledgeService.session(player);
        check(!InventoryScanService.begin(player,new InventoryScanPayload(session,0,-2,1,new ItemStack(Items.STONE),true)),"survival cannot forge a creative catalog/cursor scan");player.setGameMode(net.minecraft.world.level.GameType.CREATIVE);
        var catalog=new ItemStack(Items.STONE,64);net.minecraft.world.item.component.CustomData.update(net.minecraft.core.component.DataComponents.CUSTOM_DATA,catalog,tag->tag.putInt("forgedAspectAmount",999999));
        int before=KnowledgeService.get(player).pool(Aspects.EARTH);int expected=ScanService.reward(KnowledgeService.get(player).copy(),Aspects.EARTH,thaumcraft.data.ThaumcraftData.aspects(player.server,new ItemStack(Items.STONE)).get("terra"));check(InventoryScanService.begin(player,new InventoryScanPayload(session,0,-2,2,catalog,true)),"creative cursor scanner accepts a catalog prototype");for(int i=0;i<19;i++)InventoryScanService.advance(player);check(!KnowledgeService.get(player).scanned(ScanKind.OBJECT).contains("minecraft:stone"),"catalog item requires the complete 20-tick scan");InventoryScanService.advance(player);check(KnowledgeService.get(player).scanned(ScanKind.OBJECT).contains("minecraft:stone")&&KnowledgeService.get(player).pool(Aspects.EARTH)-before==expected,"catalog reward derives only from server item rules, ignoring client count and custom data");
        before=KnowledgeService.get(player).pool(Aspects.EARTH);check(!InventoryScanService.begin(player,new InventoryScanPayload(session,0,-2,3,new ItemStack(Items.STONE),true))&&KnowledgeService.get(player).pool(Aspects.EARTH)==before,"known catalog items do not start a second scan or reward");
        player.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);player.inventoryMenu.setCarried(new ItemStack(R2Content.THAUMOMETER.get()));player.getInventory().setItem(9,new ItemStack(Items.DIRT));check(InventoryScanService.begin(player,new InventoryScanPayload(session,0,9,4)),"survival authorizes the actual cursor scanner and owned slot");for(int i=0;i<20;i++)InventoryScanService.advance(player);check(KnowledgeService.get(player).scanned(ScanKind.OBJECT).contains("minecraft:dirt"),"owned inventory hover completes the native server scan");player.inventoryMenu.setCarried(ItemStack.EMPTY);player.setGameMode(net.minecraft.world.level.GameType.CREATIVE);
        var buffer=new net.minecraft.network.RegistryFriendlyByteBuf(io.netty.buffer.Unpooled.buffer(),player.registryAccess());try{var request=new InventoryScanPayload(session,0,-2,5,new ItemStack(Items.STONE),true);InventoryScanPayload.CODEC.encode(buffer,request);var decoded=InventoryScanPayload.CODEC.decode(buffer);check(decoded.creativeCursor()&&decoded.slot()==-2&&ItemStack.matches(decoded.catalog(),request.catalog())&&!buffer.isReadable(),"catalog scan codec retains bounded item identity and cursor flag");}finally{buffer.release();}
        var definition=thaumcraft.data.ThaumcraftData.get(player.server).research().get("PAVEWARD");check(definition!=null&&definition.secondary(),"warding paving research uses the aspect-purchase path");
    }
    private static void write(boolean pass,String failure){try{var result=new LinkedHashMap<String,Object>();result.put("passed",pass);result.put("count",checks.size());result.put("checks",checks);if(failure!=null)result.put("error",failure);Files.writeString(Path.of(System.getProperty("thaumcraft.r3.root"),"analysis/r3/evidence/interface-followup-server.json"),new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(result));}catch(Exception error){throw new RuntimeException(error);}}
}
