package thaumcraft.verification;

import java.nio.file.*;
import net.neoforged.fml.common.Mod;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import thaumcraft.api.aspects.*;
import thaumcraft.knowledge.KnowledgeService;

/** Runs only in a separate, disposable QA profile. */
@Mod("thaumcraft_viewer_qa")
public final class ViewerReview {
 public ViewerReview(IEventBus bus){
  if(net.neoforged.fml.loading.FMLEnvironment.dist.isClient())ViewerReviewClient.install(bus);
  NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.event.tick.ServerTickEvent.Post e)->{if(Files.exists(Path.of(System.getProperty("thaumcraft.viewerqa.output"),"stop-server")))e.getServer().halt(false);});
  NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedInEvent e)->{
   if(e.getEntity() instanceof net.minecraft.server.level.ServerPlayer p){
    p.getInventory().add(new net.minecraft.world.item.ItemStack(thaumcraft.registry.R2Content.BOOK.get()));
    KnowledgeService.mutate(p,k->{k.discover(Aspects.LIGHT);k.discover(Aspects.MAGIC);k.recordScan(thaumcraft.api.knowledge.PlayerKnowledge.ScanKind.OBJECT,thaumcraft.data.ThaumcraftData.objectScanKey(p.server,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.COAL)));return true;});
   }
  });
 }
}
