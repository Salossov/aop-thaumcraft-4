package thaumcraft.client;

import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
import thaumcraft.network.FocusDigPayload;

@EventBusSubscriber(modid="thaumcraft",value=Dist.CLIENT)
public final class FocusDigEffects {
    private record Beam(Vec3 endpoint,long expires){}
    private static final Map<Integer,Beam> beams=new HashMap<>();
    public static void accept(FocusDigPayload packet){var mc=Minecraft.getInstance();if(mc.level==null)return;if(packet.active())beams.put(packet.player(),new Beam(packet.endpoint(),mc.level.getGameTime()+6));else beams.remove(packet.player());}
    @SubscribeEvent public static void render(RenderLevelStageEvent event){
        if(event.getStage()!=RenderLevelStageEvent.Stage.AFTER_PARTICLES)return;var mc=Minecraft.getInstance();if(mc.level==null)return;beams.entrySet().removeIf(e->e.getValue().expires<mc.level.getGameTime());if(beams.isEmpty())return;
        var pose=WorldOverlayPose.create(event);var camera=event.getCamera().getPosition();pose.translate(-camera.x,-camera.y,-camera.z);var buffers=mc.renderBuffers().bufferSource();float partial=event.getPartialTick().getGameTimeDeltaPartialTick(false),time=mc.level.getGameTime()+partial;
        for(var entry:beams.entrySet())if(mc.level.getEntity(entry.getKey()) instanceof Player player&&player.isUsingItem()){
            float side=player.getMainArm()==net.minecraft.world.entity.HumanoidArm.RIGHT?-.15f:.15f;if(player.getUsedItemHand()==net.minecraft.world.InteractionHand.OFF_HAND)side=-side;
            var offset=new Vec3(side,-.15,.5).xRot(-player.getXRot()*(float)Math.PI/180).yRot(-player.getYRot()*(float)Math.PI/180);var start=player.getEyePosition(partial).add(offset);var end=entry.getValue().endpoint;
            VisDeviceRenderers.beam(pose,buffers,start,end,time,65382,.035f,.8f);
        }
    }
    @SubscribeEvent public static void tick(ClientTickEvent.Post event){var mc=Minecraft.getInstance();if(mc.level==null)return;for(var beam:beams.values())if(beam.expires>=mc.level.getGameTime()){var p=beam.endpoint;GolemClientEffects.spark(p,.12f,0,1,.4f,.8f);}}
    @SubscribeEvent public static void logout(ClientPlayerNetworkEvent.LoggingOut event){beams.clear();}
    private FocusDigEffects(){}
}
