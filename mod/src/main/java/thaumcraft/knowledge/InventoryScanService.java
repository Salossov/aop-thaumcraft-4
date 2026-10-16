package thaumcraft.knowledge;

import java.util.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import thaumcraft.api.knowledge.PlayerKnowledge.ScanKind;
import thaumcraft.data.ThaumcraftData;
import thaumcraft.items.ThaumometerItem;
import thaumcraft.network.InventoryScanPayload;

public final class InventoryScanService {
    private static final class Active {final AbstractContainerMenu menu;final int slot;final ItemStack stack;final boolean creativeCursor;int ticks;Active(ServerPlayer player,InventoryScanPayload request){menu=player.containerMenu;slot=request.slot();creativeCursor=request.creativeCursor()&&player.hasInfiniteMaterials();stack=slot==-2?thaumcraft.data.StackAspectRules.inventoryScanStack(request.catalog()):menu.slots.get(slot).getItem().copy();}}
    private static final Map<ServerPlayer,Active> ACTIVE=new WeakHashMap<>();
    private static final Map<ServerPlayer,Long> SEQUENCES=new WeakHashMap<>();
    public static void register(){NeoForge.EVENT_BUS.addListener(InventoryScanService::tick);NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent event)->{ACTIVE.remove(event.getEntity());SEQUENCES.remove(event.getEntity());});}
    public static boolean holding(ServerPlayer player){return player.containerMenu.getCarried().getItem() instanceof ThaumometerItem;}
    public static boolean begin(ServerPlayer player,InventoryScanPayload request){if(!KnowledgeService.session(player).equals(request.session())||request.sequence()<=SEQUENCES.getOrDefault(player,0L))return false;SEQUENCES.put(player,request.sequence());ACTIVE.remove(player);if(request.slot()==-1)return true;var menu=player.containerMenu;boolean catalog=request.slot()==-2,creativeCursor=request.creativeCursor()&&player.hasInfiniteMaterials()&&menu==player.inventoryMenu;
        if(!player.isAlive()||(!holding(player)&&!creativeCursor)||menu.containerId!=request.containerId()||!menu.stillValid(player))return false;
        if(catalog&&(!player.hasInfiniteMaterials()||menu!=player.inventoryMenu||request.catalog().isEmpty()||!request.catalog().getItem().isEnabled(player.level().enabledFeatures())))return false;
        if(!catalog&&(request.slot()<0||request.slot()>=menu.slots.size()))return false;
        var stack=catalog?thaumcraft.data.StackAspectRules.inventoryScanStack(request.catalog()):menu.slots.get(request.slot()).getItem();if(stack.isEmpty()||KnowledgeService.get(player).scanned(ScanKind.OBJECT).contains(ThaumcraftData.objectScanKey(player.server,stack)))return false;
        ACTIVE.put(player,new Active(player,request));return true;
    }
    private static void tick(ServerTickEvent.Post event){for(var entry:new ArrayList<>(ACTIVE.entrySet()))if(entry.getKey().server==event.getServer())advance(entry.getKey());}
    public static void advance(ServerPlayer player){var active=ACTIVE.get(player);if(active==null)return;boolean creative=active.creativeCursor&&player.hasInfiniteMaterials()&&active.menu==player.inventoryMenu;if(!player.isAlive()||(!holding(player)&&!creative)||player.containerMenu!=active.menu||!active.menu.stillValid(player)||active.slot!=-2&&!ItemStack.matches(active.stack,active.menu.slots.get(active.slot).getItem())){ACTIVE.remove(player);return;}if(++active.ticks<20)return;ACTIVE.remove(player);var stack=thaumcraft.data.StackAspectRules.inventoryScanStack(active.stack);ScanService.scan(player,ScanKind.OBJECT,ThaumcraftData.objectScanKey(player.server,stack),ThaumcraftData.aspects(player.server,stack));}
    public static boolean active(ServerPlayer player){return ACTIVE.containsKey(player);}
    private InventoryScanService(){}
}
