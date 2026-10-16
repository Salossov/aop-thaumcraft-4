package thaumcraft.network;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.nodes.NodeState;

public record NodeCreatePayload(BlockPos pos,int nodeType,int modifier,Map<String,Integer> aspects) implements CustomPacketPayload {
    public NodeCreatePayload {
        aspects=Map.copyOf(aspects);pos=pos.immutable();
        if(aspects.isEmpty()||aspects.size()>64||nodeType<0||nodeType>=NodeState.Type.values().length||modifier<0||modifier>=NodeState.Modifier.values().length||aspects.entrySet().stream().anyMatch(e->e.getKey().length()>32||e.getValue()<1||e.getValue()>32767))throw new IllegalArgumentException("Invalid node configuration");
    }
    public static final Type<NodeCreatePayload> TYPE=new Type<>(ResourceLocation.parse("thaumcraft:create_node"));
    public static final StreamCodec<RegistryFriendlyByteBuf,NodeCreatePayload> CODEC=StreamCodec.of((b,p)->{b.writeBlockPos(p.pos);b.writeVarInt(p.nodeType);b.writeVarInt(p.modifier);b.writeVarInt(p.aspects.size());p.aspects.forEach((tag,n)->{b.writeUtf(tag,32);b.writeVarInt(n);});},b->{var pos=b.readBlockPos();int type=b.readVarInt(),modifier=b.readVarInt(),count=b.readVarInt();if(count<1||count>64)throw new IllegalArgumentException("Invalid aspect count");var values=new LinkedHashMap<String,Integer>();for(int i=0;i<count;i++)if(values.put(b.readUtf(32),b.readVarInt())!=null)throw new IllegalArgumentException("Duplicate aspect");return new NodeCreatePayload(pos,type,modifier,values);});
    public boolean apply(ServerPlayer player){
        if(!player.hasPermissions(2))return false;var level=player.serverLevel();
        if(level.isOutsideBuildHeight(pos)||!level.getWorldBorder().isWithinBounds(pos)||!level.hasChunkAt(pos)||aspects.keySet().stream().anyMatch(tag->Aspect.getAspect(tag)==null))return false;
        if(!level.setBlockAndUpdate(pos,thaumcraft.registry.R2Content.AURA_NODE.get().defaultBlockState())&&!(level.getBlockEntity(pos) instanceof thaumcraft.blocks.AuraNodeBlockEntity))return false;
        if(!(level.getBlockEntity(pos) instanceof thaumcraft.blocks.AuraNodeBlockEntity node))return false;
        node.initialize(aspects,NodeState.Type.values()[nodeType],NodeState.Modifier.values()[modifier]);node.setChanged();level.sendBlockUpdated(pos,level.getBlockState(pos),level.getBlockState(pos),3);return true;
    }
    @Override public Type<? extends CustomPacketPayload> type(){return TYPE;}
}
