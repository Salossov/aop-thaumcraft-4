package thaumcraft.client;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import thaumcraft.visnet.VisRelayBlockEntity;

@EventBusSubscriber(modid="thaumcraft",value=Dist.CLIENT)
public final class VisRelayOverlay {
    @SubscribeEvent public static void render(RenderGuiEvent.Post event){
        var mc=Minecraft.getInstance();if(mc.player==null||mc.level==null||mc.screen!=null||mc.options.hideGui||!(mc.hitResult instanceof BlockHitResult hit)||!(mc.level.getBlockEntity(hit.getBlockPos()) instanceof VisRelayBlockEntity relay))return;
        var g=event.getGuiGraphics();int y=g.guiHeight()-104;
        boolean connected=relay.getRootSource()!=null;
        var status=Component.translatable(connected?"thaumcraft.vis_relay.connected":"thaumcraft.vis_relay.disconnected").withStyle(connected?net.minecraft.ChatFormatting.GREEN:net.minecraft.ChatFormatting.RED);
        line(g,Component.translatable("thaumcraft.vis_relay.configured",relay.attunementName(),status),y,0xffffe0a0);
        line(g,Component.translatable("thaumcraft.vis_relay.tune"),y+12,0xffe0d6c5);
        int width=(g.guiWidth()-30)/2;
        wrapped(g,Component.translatable("thaumcraft.vis_relay.automatic"),10,y+29,width);
        wrapped(g,Component.translatable("thaumcraft.vis_relay.reset"),g.guiWidth()-width-10,y+29,width);
    }
    private static void wrapped(net.minecraft.client.gui.GuiGraphics g,Component text,int x,int y,int width){var font=Minecraft.getInstance().font;for(var line:font.split(text,width)){g.drawString(font,line,x,y,0xffe0d6c5,true);y+=font.lineHeight;}}
    private static void line(net.minecraft.client.gui.GuiGraphics g,Component text,int y,int color){var font=Minecraft.getInstance().font;float scale=Math.min(1,(g.guiWidth()-12f)/Math.max(1,font.width(text)));g.pose().pushPose();g.pose().translate(g.guiWidth()/2f,y,0);g.pose().scale(scale,scale,1);g.drawCenteredString(font,text,0,0,color);g.pose().popPose();}
    private VisRelayOverlay(){}
}
