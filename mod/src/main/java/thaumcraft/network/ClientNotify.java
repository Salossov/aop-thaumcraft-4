package thaumcraft.network;

import net.minecraft.network.chat.Component;

/** Client end of the right-edge TC4 notification stack: resolves lang keys locally. */
public final class ClientNotify {
    public static void accept(NotifyPayload packet){
        if(packet.key().equals("tc.addaspectdiscovery"))thaumcraft.client.ResearchHighlights.aspect();
        var text=resolve(packet);
        if(packet.whisper()){var mc=net.minecraft.client.Minecraft.getInstance();if(mc.player!=null)mc.player.playSound(thaumcraft.registry.R2Sounds.WHISPERS.get(),0.5f,1.0f);}
        var aspect=aspectFrom(packet);
        var player=net.minecraft.client.Minecraft.getInstance().player;
        if(player!=null&&(packet.key().equals("tc.addaspectdiscovery")||packet.key().equals("tc.addaspectpool")))
            player.playSound(net.minecraft.sounds.SoundEvents.EXPERIENCE_ORB_PICKUP,packet.key().equals("tc.addaspectdiscovery")?.2f:.1f,packet.key().equals("tc.addaspectdiscovery")?.6f:1);
        if(packet.fly()&&aspect!=null)thaumcraft.client.ResearchNotifications.aspect(aspect);
        // Keep the lang string's own formatting (§ codes from the original lang file) and
        // tint the whole line with the packet colour, like the original stack entries.
        var styled=text.copy().withStyle(packet.italic()?s->s.withItalic(true):s->s);
        String formatted=(packet.italic()?"§o":"")+(packet.key().equals("tc.addaspectdiscovery")?"§6":"")+styled.getString();
        thaumcraft.client.ResearchNotifications.text(formatted,packet.color(),aspect);
    }
    private static Component resolve(NotifyPayload packet){
        var params=packet.params();
        if(params.isEmpty())return Component.translatable(packet.key());
        if(params.contains("|")){var parts=params.split("\\|",-1);var args=new Object[parts.length];
            for(int i=0;i<parts.length;i++)args[i]=parts[i].startsWith("tc.")?parameter(parts[i]):parts[i];
            return Component.translatable(packet.key(),args);}
        return Component.translatable(packet.key(),parameter(params));
    }
    private static Component parameter(String value){if(value.startsWith("tc.aspect.")){var aspect=thaumcraft.api.aspects.Aspect.getAspect(value.substring(10));if(aspect!=null)return Component.literal(aspect.getName());}return Component.translatable(value);}
    private static thaumcraft.api.aspects.Aspect aspectFrom(NotifyPayload packet){
        var p=packet.params();String tag=null;
        if(p.startsWith("tc.aspect."))tag=p.substring(10);
        else if(p.contains("|")){var last=p.substring(p.lastIndexOf('|')+1);if(last.startsWith("tc.aspect."))tag=last.substring(10);}
        return tag==null?null:thaumcraft.api.aspects.Aspect.getAspect(tag);
    }
    private ClientNotify(){}
}
