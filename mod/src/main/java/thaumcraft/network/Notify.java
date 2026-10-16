package thaumcraft.network;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

/** Server side of the right-edge TC4 notification stack. Colors are 0xRRGGBB. */
public final class Notify {
    /** Purple italic, like original warp/research notices. */
    public static void purple(ServerPlayer player,String key){send(player,key,"",0xaa00aa,true);}
    public static void purple(ServerPlayer player,String key,String paramKey){send(player,key,paramKey,0xaa00aa,true);}
    /** Warp notices play the whisper sound alongside the purple text, as in the original. */
    public static void warp(ServerPlayer player,String key){send(player,key,"",0xaa00aa,true,true);}
    /** Gold "New aspect discovered" line that also flies its icon to the book. */
    public static void discovery(ServerPlayer player,thaumcraft.api.aspects.Aspect aspect){
        PacketDistributor.sendToPlayer(player,new NotifyPayload("tc.addaspectdiscovery","tc.aspect."+aspect.getTag(),0xffaa00,false,false,true));
    }
    /** "Gained N research point(s) for X" in the aspect's own colour, plus the flying icon. */
    public static void pool(ServerPlayer player,thaumcraft.api.aspects.Aspect aspect,int amount){
        PacketDistributor.sendToPlayer(player,new NotifyPayload("tc.addaspectpool",amount+"|tc.aspect."+aspect.getTag(),aspect.getColor(),false,false,true));
    }
    /** Same, when the aspect key is already a plain tag. */
    public static void pool(ServerPlayer player,String aspectTag,int amount){
        var aspect=thaumcraft.api.aspects.Aspect.getAspect(aspectTag);if(aspect!=null)pool(player,aspect,amount);
    }
    public static void send(net.minecraft.world.entity.player.Player player,String key,String paramKey,int color,boolean italic){
        if(player instanceof ServerPlayer server)send(server,key,paramKey,color,italic);
    }
    public static void send(ServerPlayer player,String key,String paramKey,int color,boolean italic){
        send(player,key,paramKey,color,italic,false);
    }
    public static void send(ServerPlayer player,String key,String paramKey,int color,boolean italic,boolean whisper){
        PacketDistributor.sendToPlayer(player,new NotifyPayload(key,paramKey,color,italic,whisper));
    }
    private Notify(){}
}
