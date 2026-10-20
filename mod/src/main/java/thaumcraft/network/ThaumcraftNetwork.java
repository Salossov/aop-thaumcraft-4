package thaumcraft.network;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public final class ThaumcraftNetwork {
    public static void register(RegisterPayloadHandlersEvent event) {
        var registrar=event.registrar("2");
        registrar.playToServer(BookOpenPayload.TYPE,BookOpenPayload.CODEC,(packet,context)->{if(context.player() instanceof net.minecraft.server.level.ServerPlayer p)packet.open(p);});
        registrar.playToServer(thaumcraft.api.crafting.ArcaneTransferRequest.TYPE,thaumcraft.api.crafting.ArcaneTransferRequest.CODEC,(packet,context)->{if(context.player() instanceof net.minecraft.server.level.ServerPlayer player&&player.containerMenu instanceof thaumcraft.menu.ArcaneMenu menu)menu.fill(player,packet);});
        registrar.playToClient(GolemDebugPayload.TYPE,GolemDebugPayload.CODEC,(packet,context)->thaumcraft.client.GolemDebugOverlay.accept(packet));
        registrar.playToClient(FocusDigPayload.TYPE,FocusDigPayload.CODEC,(packet,context)->thaumcraft.client.FocusDigEffects.accept(packet));
        registrar.playToClient(AdminUiPayload.TYPE,AdminUiPayload.CODEC,(packet,context)->{if(packet.kind()==0)thaumcraft.client.NodeEditorScreen.open(packet.pos());else if(packet.kind()==1)thaumcraft.client.InfusionDebugHud.enabled(packet.enabled());});
        registrar.playToServer(InfusionDebugRequestPayload.TYPE,InfusionDebugRequestPayload.CODEC,(packet,context)->{if(context.player() instanceof net.minecraft.server.level.ServerPlayer player&&player.hasPermissions(2)&&player.getPersistentData().getBoolean("ThaumcraftInfusionDebug")&&player.distanceToSqr(packet.pos().getCenter())<4096&&player.serverLevel().hasChunkAt(packet.pos())&&player.serverLevel().getBlockEntity(packet.pos()) instanceof thaumcraft.production.InfusionMatrixBlockEntity matrix){if(!matrix.crafting)matrix.getSurroundings();matrix.sync();}});
        registrar.playToServer(NodeCreatePayload.TYPE,NodeCreatePayload.CODEC,(packet,context)->{if(context.player() instanceof net.minecraft.server.level.ServerPlayer player)player.displayClientMessage(net.minecraft.network.chat.Component.translatable(packet.apply(player)?"thaumcraft.node.created":"thaumcraft.node.invalid"),true);});
        registrar.playToServer(GolemBellResetPayload.TYPE,GolemBellResetPayload.CODEC,(packet,context)->thaumcraft.items.GolemBellItem.resetMarkers(context.player()));
        registrar.playToClient(ResearchNoteCreatedPayload.TYPE,ResearchNoteCreatedPayload.CODEC,(packet,context)->thaumcraft.client.ThaumonomiconScreen.noteCreated(packet));
        registrar.playToClient(BossStatusPayload.TYPE,BossStatusPayload.CODEC,(packet,context)->thaumcraft.client.BossStatusBar.accept(packet));
        registrar.playToClient(ResearchCompletePayload.TYPE,ResearchCompletePayload.CODEC,(packet,context)->thaumcraft.client.ResearchCompletionOverlay.accept(packet.key()));
        registrar.playToClient(ChampionShieldPayload.TYPE,ChampionShieldPayload.CODEC,(packet,context)->thaumcraft.client.ChampionShieldEffects.receive(packet));
        registrar.playToClient(BottleTaintBreakPayload.TYPE,BottleTaintBreakPayload.CODEC,(packet,context)->thaumcraft.client.BottleTaintEffects.burst(packet));
        registrar.playToClient(FluxScrubPayload.TYPE,FluxScrubPayload.CODEC,(packet,context)->thaumcraft.client.FluxScrubberRenderer.sparkle(packet));
        registrar.playToClient(InfusionZapPayload.TYPE,InfusionZapPayload.CODEC,(packet,context)->thaumcraft.client.InfusionLightning.accept(packet));
        registrar.playToClient(NodeZapPayload.TYPE,NodeZapPayload.CODEC,(packet,context)->thaumcraft.client.NodeExchangeEffects.accept(packet));
        registrar.playToClient(FocusLightningPayload.TYPE,FocusLightningPayload.CODEC,(packet,context)->thaumcraft.client.FocusLightningEffects.accept(packet));
        registrar.playToClient(ParticleEffectPayload.TYPE,ParticleEffectPayload.CODEC,(packet,context)->thaumcraft.client.AddonParticleEffects.receive(packet));
        registrar.playToClient(SmokeSpiralPayload.TYPE,SmokeSpiralPayload.CODEC,(packet,context)->thaumcraft.client.AddonParticleEffects.receiveSmoke(packet));
        registrar.playToServer(WandMiscPayload.TYPE,WandMiscPayload.CODEC,(packet,context)->{if(context.player() instanceof net.minecraft.server.level.ServerPlayer player){if(thaumcraft.items.WandArea.toggle(player))return;var stack=player.getMainHandItem();if(stack.is(thaumcraft.registry.R3Equipment.TOOLS.get("elemental_shovel").get()))net.minecraft.world.item.component.CustomData.update(net.minecraft.core.component.DataComponents.CUSTOM_DATA,stack,tag->tag.putByte("or",(byte)((tag.getByte("or")+1)%3)));}});
        registrar.playToServer(FocusSelectPayload.TYPE,FocusSelectPayload.CODEC,(packet,context)->{if(context.player() instanceof net.minecraft.server.level.ServerPlayer player)thaumcraft.items.WandFocus.select(player,packet.offHand()?net.minecraft.world.InteractionHand.OFF_HAND:net.minecraft.world.InteractionHand.MAIN_HAND,packet.slot());});
        registrar.playToServer(HarnessTogglePayload.TYPE,HarnessTogglePayload.CODEC,(packet,context)->{if(context.player() instanceof net.minecraft.server.level.ServerPlayer player)thaumcraft.equipment.HarnessFlight.toggle(player);});
        registrar.playToClient(RunicShieldPayload.TYPE,RunicShieldPayload.CODEC,(packet,context)->thaumcraft.client.RunicShieldHud.accept(packet));
        registrar.playToClient(OreScanPayload.TYPE,OreScanPayload.CODEC,(packet,context)->thaumcraft.client.OreScanEffects.accept(packet));
        registrar.playToClient(BlockArcPayload.TYPE,BlockArcPayload.CODEC,(packet,context)->thaumcraft.client.WardenClientEffects.arc(packet));
        registrar.playToClient(SonicPayload.TYPE,SonicPayload.CODEC,(packet,context)->thaumcraft.client.WardenClientEffects.sonic(packet));
        registrar.playToClient(BlockSparklePayload.TYPE,BlockSparklePayload.CODEC,(packet,context)->thaumcraft.client.WorldScanEffects.blockSparkle(packet));
        registrar.playToClient(BookBirthPayload.TYPE,BookBirthPayload.CODEC,(packet,context)->thaumcraft.client.WorldScanEffects.birth(packet));
        registrar.playToClient(GuardianMistPayload.TYPE,GuardianMistPayload.CODEC,(packet,context)->ClientGuardianMist.accept());
        registrar.playToClient(BiomeColumnsPayload.TYPE,BiomeColumnsPayload.CODEC,(packet,context)->ClientBiomeColumns.accept(packet));
        registrar.playToClient(KnowledgePayload.TYPE,KnowledgePayload.CODEC,(packet,context)->{if(context.player().getUUID().equals(packet.player())) ClientKnowledge.accept(packet);});
        registrar.playToClient(NotifyPayload.TYPE,NotifyPayload.CODEC,(packet,context)->ClientNotify.accept(packet));
        registrar.playToServer(InventoryScanPayload.TYPE,InventoryScanPayload.CODEC,(packet,context)->{if(context.player() instanceof net.minecraft.server.level.ServerPlayer player)thaumcraft.knowledge.InventoryScanService.begin(player,packet);});
        registrar.playToServer(ResearchActionPayload.TYPE,ResearchActionPayload.CODEC,(packet,context)->{
            if(context.player() instanceof net.minecraft.server.level.ServerPlayer player&&player.containerMenu.containerId==packet.containerId()
                &&player.containerMenu instanceof thaumcraft.menu.ActionMenu menu) menu.accept(player,packet);
        });
    }
    private ThaumcraftNetwork() {}
}
