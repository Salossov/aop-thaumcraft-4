package thaumcraft.blocks;

import java.util.UUID;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.api.nodes.NodeState;
import thaumcraft.registry.R3Content;

/** Frozen TileJarNode state. No natural-node ticks, recharge or hungry/taint effects. */
public final class JarredNodeBlockEntity extends BlockEntity {
    public final NodeState state=new NodeState();
    private UUID id=UUID.randomUUID();
    public JarredNodeBlockEntity(BlockPos pos,BlockState block){super(R3Content.NODE_JAR_ENTITY.get(),pos,block);}
    public UUID id(){return id;}
    public void capture(AuraNodeBlockEntity node){
        id=node.id();state.initialize(node.state.current);state.type=node.state.type;state.modifier=node.state.modifier;
        // WandManager.replaceNodeJar: 75% chance to lose one brightness tier.
        if(level!=null&&level.random.nextFloat()<.75f)state.modifier=switch(state.modifier){case BRIGHT->NodeState.Modifier.NONE;case NONE->NodeState.Modifier.PALE;case PALE,FADING->NodeState.Modifier.FADING;};
        setChanged();if(level!=null)level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),3);
    }
    public boolean release(){
        if(level==null||level.isClientSide||level.getBlockEntity(worldPosition)!=this)return false;
        level.setBlockAndUpdate(worldPosition,thaumcraft.registry.R2Content.AURA_NODE.get().defaultBlockState());
        if(level.getBlockEntity(worldPosition) instanceof AuraNodeBlockEntity node){node.initialize(state.current,state.type,state.modifier);node.restoreIdentity(id);node.changedAndSync();}
        level.playSound(null,worldPosition,net.minecraft.sounds.SoundEvents.GLASS_BREAK,net.minecraft.sounds.SoundSource.BLOCKS,1,1);
        return true;
    }
    public ItemStack item(){var stack=new ItemStack(R3Content.NODE_JAR_ITEM.get());stack.set(DataComponents.BLOCK_ENTITY_DATA,CustomData.of(saveWithId(level.registryAccess())));return stack;}
    @Override protected void saveAdditional(CompoundTag tag,HolderLookup.Provider lookup){super.saveAdditional(tag,lookup);tag.putUUID("nodeId",id);tag.putString("nodeType",state.type.name());tag.putString("modifier",state.modifier.name());var vis=new CompoundTag();state.current.forEach(vis::putInt);tag.put("vis",vis);}
    @Override protected void loadAdditional(CompoundTag tag,HolderLookup.Provider lookup){
        super.loadAdditional(tag,lookup);if(tag.hasUUID("nodeId"))id=tag.getUUID("nodeId");
        try{state.type=NodeState.Type.valueOf(tag.getString("nodeType"));}catch(IllegalArgumentException ignored){state.type=NodeState.Type.NORMAL;}
        try{state.modifier=NodeState.Modifier.valueOf(tag.getString("modifier"));}catch(IllegalArgumentException ignored){state.modifier=NodeState.Modifier.NONE;}
        var vis=tag.getCompound("vis");var values=new java.util.LinkedHashMap<String,Integer>();for(var key:vis.getAllKeys())if(thaumcraft.api.aspects.Aspect.getAspect(key)!=null&&vis.getInt(key)>=0)values.put(key,vis.getInt(key));state.initialize(values);
    }
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider lookup){return saveWithoutMetadata(lookup);}
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket(){return ClientboundBlockEntityDataPacket.create(this);}
}
