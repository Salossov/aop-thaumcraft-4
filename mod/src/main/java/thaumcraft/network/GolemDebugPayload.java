package thaumcraft.network;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.*;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

/** Bounded, operator-only snapshots of actual server AI. No client path simulation. */
public record GolemDebugPayload(boolean enabled,ResourceLocation dimension,List<Entry> golems) implements CustomPacketPayload {
    public record Entry(int id,UUID uuid,BlockPos home,BlockPos source,Vec3 target,List<BlockPos> path,int next,
                        List<BlockPos> markers,String reason,List<Component> lines){}
    public static final Type<GolemDebugPayload> TYPE=new Type<>(ResourceLocation.parse("thaumcraft:golem_debug"));
    public static final StreamCodec<RegistryFriendlyByteBuf,GolemDebugPayload> CODEC=StreamCodec.of((b,p)->{
        b.writeBoolean(p.enabled);b.writeResourceLocation(p.dimension);b.writeVarInt(p.golems.size());
        for(var e:p.golems){
            b.writeVarInt(e.id);b.writeUUID(e.uuid);b.writeBlockPos(e.home);b.writeBlockPos(e.source);b.writeBoolean(e.target!=null);
            if(e.target!=null){b.writeDouble(e.target.x);b.writeDouble(e.target.y);b.writeDouble(e.target.z);}
            positions(b,e.path);b.writeVarInt(e.next);positions(b,e.markers);b.writeUtf(e.reason,64);b.writeVarInt(e.lines.size());
            for(var line:e.lines)ComponentSerialization.STREAM_CODEC.encode(b,line);
        }
    },b->{
        boolean enabled=b.readBoolean();var dimension=b.readResourceLocation();int size=count(b,24);var entries=new ArrayList<Entry>(size);
        for(int i=0;i<size;i++){
            int id=b.readVarInt();var uuid=b.readUUID();var home=b.readBlockPos();var source=b.readBlockPos();
            Vec3 target=b.readBoolean()?new Vec3(b.readDouble(),b.readDouble(),b.readDouble()):null;
            var path=positions(b,256);int next=b.readVarInt();var markers=positions(b,32);var reason=b.readUtf(64);
            int lines=count(b,8);var text=new ArrayList<Component>(lines);for(int j=0;j<lines;j++)text.add(ComponentSerialization.STREAM_CODEC.decode(b));
            entries.add(new Entry(id,uuid,home,source,target,path,Math.clamp(next,0,path.size()),markers,reason,List.copyOf(text)));
        }
        return new GolemDebugPayload(enabled,dimension,List.copyOf(entries));
    });
    private static int count(RegistryFriendlyByteBuf b,int max){int n=b.readVarInt();if(n<0||n>max)throw new IllegalArgumentException("Golem debug collection too large");return n;}
    private static void positions(RegistryFriendlyByteBuf b,List<BlockPos> positions){b.writeVarInt(positions.size());for(var p:positions)b.writeBlockPos(p);}
    private static List<BlockPos> positions(RegistryFriendlyByteBuf b,int max){int n=count(b,max);var result=new ArrayList<BlockPos>(n);for(int i=0;i<n;i++)result.add(b.readBlockPos());return List.copyOf(result);}
    @Override public Type<? extends CustomPacketPayload> type(){return TYPE;}
}
