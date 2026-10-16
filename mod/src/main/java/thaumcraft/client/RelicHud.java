package thaumcraft.client;

import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.core.GlobalPos;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import thaumcraft.blocks.AuraNodeBlockEntity;
import thaumcraft.items.EldritchRelics;

@EventBusSubscriber(modid="thaumcraft",value=Dist.CLIENT)
public final class RelicHud {
    private static final Map<GlobalPos,Long> DARK_NODES=new HashMap<>();
    public static void note(AuraNodeBlockEntity node){if(node.state.type==thaumcraft.api.nodes.NodeState.Type.DARK&&node.getLevel()!=null)DARK_NODES.put(GlobalPos.of(node.getLevel().dimension(),node.getBlockPos()),System.currentTimeMillis());}
    public static boolean stoneActive(net.minecraft.world.entity.Entity entity){if(entity==null)return false;long now=System.currentTimeMillis();DARK_NODES.entrySet().removeIf(entry->entry.getValue()<now-10000);var eye=entity.getEyePosition();var look=entity.getLookAngle();for(var node:DARK_NODES.keySet()){if(node.dimension()!=entity.level().dimension())continue;var direction=node.pos().getCenter().subtract(eye);if(direction.lengthSqr()<=256*256&&direction.lengthSqr()>0&&look.dot(direction.normalize())>Math.cos(.66))return true;}return false;}
    @SubscribeEvent public static void render(RenderGuiEvent.Post event){var mc=Minecraft.getInstance();if(mc.player==null||mc.screen!=null||mc.options.hideGui||!(mc.player.getMainHandItem().getItem() instanceof EldritchRelics.SanityChecker))return;var knowledge=thaumcraft.network.ClientKnowledge.snapshot();int total=knowledge.totalWarp();float scale=total>100?100f/total:1;int gap=(int)((100-Math.min(100,total))/100f*48),temporary=(int)(knowledge.temporaryWarp()/100f*48*scale),sticky=(int)(knowledge.stickyWarp()/100f*48*scale);var graphics=event.getGuiGraphics();var texture=ResearchGraphics.texture("gui/hud");graphics.blit(texture,1,1,152,0,20,76);com.mojang.blaze3d.systems.RenderSystem.enableBlend();com.mojang.blaze3d.systems.RenderSystem.defaultBlendFunc();if(temporary>0){graphics.setColor(1,.5f,1,1);graphics.blit(texture,7,21+gap,200,gap,8,temporary);}if(sticky>0){graphics.setColor(.75f,0,.75f,1);graphics.blit(texture,7,21+gap+temporary,200,gap+temporary,8,sticky);}if(knowledge.permanentWarp()>0){graphics.setColor(.5f,0,.5f,1);graphics.blit(texture,7,21+gap+temporary+sticky,200,gap+temporary+sticky,8,Math.max(0,48-gap-temporary-sticky));}graphics.setColor(1,1,1,1);graphics.blit(texture,1,1,176,0,20,76);if(total>=100)graphics.blit(texture,1,1,216,0,20,16);}
    @SubscribeEvent public static void logout(net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent.LoggingOut event){DARK_NODES.clear();}
    private RelicHud(){}
}
