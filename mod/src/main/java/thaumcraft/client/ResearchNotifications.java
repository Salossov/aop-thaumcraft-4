package thaumcraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import thaumcraft.api.aspects.Aspect;

/**
 * Original REHNotifyHandler/PlayerNotifications: on aspect gain an aspect icon flies from a
 * random screen position along a bezier toward the Thaumonomicon in the top-right corner,
 * alongside "Learn More" style notifications stacked on the right edge.
 */
@EventBusSubscriber(modid="thaumcraft",value=Dist.CLIENT)
public final class ResearchNotifications {
    private static final long NOTIFICATION_DELAY=6000,ASPECT_LIFE=1500;
    private static final ResearchNotificationQueue<Entry> TEXT=new ResearchNotificationQueue<>(8,NOTIFICATION_DELAY);
    private static final List<Fly> FLYING=new ArrayList<>();
    private record Entry(String text,Aspect aspect,int color){}
    private record Fly(Aspect aspect,float startX,float startY,long created,long expire){}

    public static void aspect(Aspect aspect){
        var level=Minecraft.getInstance().level;if(level==null)return;
        long now=System.nanoTime()/1000000+level.random.nextInt(1000);
        float x=.4f+level.random.nextFloat()*.2f,y=.4f+level.random.nextFloat()*.2f;
        FLYING.add(new Fly(aspect,x,y,now,now+ASPECT_LIFE));
    }
    public static void text(String text,Aspect aspect){text(text,aspect!=null?aspect.getColor():0xffffff,aspect);}
    public static void text(String text,int color){text(text,color,null);}
    public static void text(String text,int color,Aspect aspect){long now=System.nanoTime()/1000000;TEXT.add(new Entry(text,aspect,color),now);}

    @SubscribeEvent public static void render(RenderGuiEvent.Post event){
        var mc=Minecraft.getInstance();if(mc.player==null||mc.options.hideGui)return;
        long now=System.nanoTime()/1000000;var g=event.getGuiGraphics();int sw=g.guiWidth(),sh=g.guiHeight();
        text(g,mc,now,sw,sh);flying(g,mc,now,sw,sh);
    }
    private static void text(net.minecraft.client.gui.GuiGraphics g,Minecraft mc,long now,int sw,int sh){
        TEXT.advance(now);
        if(TEXT.remaining()>0){var more=net.minecraft.network.chat.Component.translatable("thaumcraft.notifications.remaining",TEXT.remaining());g.pose().pushPose();g.pose().translate(sw-10,sh-8*TEXT.rows().size()-14,300);g.pose().scale(.5f,.5f,1);g.drawString(mc.font,more,-mc.font.width(more),0,0xffcccccc,true);g.pose().popPose();}
        int entry=0;
        for(var row:TEXT.rows()){var li=row.value();if(entry>=8)break;
            int width=mc.font.width(li.text),alpha=255;
            // Keep new entries legible, then fade near expiry. Original notification
            // text is white; only its image uses the aspect tint (Perditio is dark).
            // Vanilla Font treats alpha <4 as an unspecified, fully opaque colour.
            alpha=Math.max(4,Math.round(TEXT.alpha(entry,now)*255));float shift=-8f-TEXT.shift(now);
            int color=(alpha<<24)|(li.aspect!=null?0xffffff:li.color&0xffffff);
            // Original layout: half-scale text left of the right edge, larger icon at the edge.
            g.pose().pushPose();g.pose().translate(sw-width/2f-10,sh-entry*8+shift,300);g.pose().scale(0.5f,0.5f,1);
            g.drawString(mc.font,li.text,-4,-8,color,true);g.pose().popPose();
            if(li.aspect!=null&&li.aspect.texture()!=null){g.pose().pushPose();g.pose().translate(sw-9,sh-entry*8+shift-6,300);com.mojang.blaze3d.systems.RenderSystem.enableBlend();com.mojang.blaze3d.systems.RenderSystem.defaultBlendFunc();int c=li.aspect.getColor();g.setColor((c>>16&255)/255f,(c>>8&255)/255f,(c&255)/255f,Math.min(1,alpha/255f*.5f));g.pose().scale(0.5f,0.5f,1);ResearchGraphics.blit(g,net.minecraft.resources.ResourceLocation.parse(li.aspect.texture()),0,0,16,16,0,0,32,32,32,32);g.setColor(1,1,1,1);g.pose().popPose();}
            if(now-row.entered()<1500){float p=(now-row.entered())/1500f;g.pose().pushPose();g.pose().translate(sw-9-width*1.5f*p*p*p,sh-entry*8+shift-8,310);ResearchGraphics.discoveryFlame(g,0,0,(1-p)*.6f);g.flush();g.pose().popPose();}entry++;
        }
    }
    private static void flying(net.minecraft.client.gui.GuiGraphics g,Minecraft mc,long now,int sw,int sh){
        if(FLYING.isEmpty())return;
        FLYING.removeIf(li->li.expire<now);float mainAlpha=0;
        for(var li:FLYING){if(li.created>now)continue;
            int startX=(int)(sw*li.startX),startY=(int)(sh*li.startY),endX=sw,endY=-8,bezierX=(int)(sw*(.25f+li.startX)),bezierY=(int)(sh*li.startY);
            double t=(double)(now-li.created)/(li.expire-li.created);if(t>1)continue;
            double x=(1-t)*(1-t)*startX+2*(1-t)*t*bezierX+t*t*endX,y=(1-t)*(1-t)*startY+2*(1-t)*t*bezierY+t*t*endY;
            float alpha=t<.3f?(float)(t/.3f):t>.66f?(float)(1-(t-.66f)/.34f):1;if(alpha>mainAlpha)mainAlpha=alpha;
            g.pose().pushPose();g.pose().translate(x,y,300);float size=16*alpha;
            if(li.aspect.texture()!=null){int c=li.aspect.getColor();com.mojang.blaze3d.systems.RenderSystem.enableBlend();com.mojang.blaze3d.systems.RenderSystem.defaultBlendFunc();g.setColor((c>>16&255)/255f,(c>>8&255)/255f,(c&255)/255f,Math.min(1,alpha));ResearchGraphics.blit(g,net.minecraft.resources.ResourceLocation.parse(li.aspect.texture()),(int)(-size/2),(int)(-size/2),(int)size,(int)size,0,0,32,32,32,32);g.setColor(1,1,1,1);}
            g.pose().popPose();}
        if(mainAlpha>0){g.pose().pushPose();com.mojang.blaze3d.systems.RenderSystem.enableBlend();com.mojang.blaze3d.systems.RenderSystem.defaultBlendFunc();g.setColor(1,1,1,Math.min(1,mainAlpha));g.pose().translate(sw-16,0,300);ResearchGraphics.blit(g,net.minecraft.resources.ResourceLocation.parse("thaumcraft:textures/items/thaumonomicon.png"),0,0,0,0,16,16,16,16);g.setColor(1,1,1,1);g.pose().popPose();}
    }
    @SubscribeEvent public static void logout(net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent.LoggingOut event){TEXT.clear();FLYING.clear();ResearchHighlights.clear();}
    private ResearchNotifications(){}
}
