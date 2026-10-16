package thaumcraft.blocks;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.nbt.*;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.registry.R4Content;

/** UUIDs decide access. Display names never grant ownership or permissions. */
public final class OwnedBlockEntity extends BlockEntity {
    private UUID owner;
    private String ownerName="";
    private final Map<UUID,Integer> access=new LinkedHashMap<>();
    public int setting;
    public OwnedBlockEntity(BlockPos pos,BlockState state){super(R4Content.OWNED_ENTITY.get(),pos,state);}
    public UUID owner(){return owner;}
    public String ownerName(){return ownerName;}
    public boolean ownedBy(UUID id){return id!=null&&id.equals(owner);}
    public boolean permits(UUID id){return ownedBy(id)||access.containsKey(id);}
    public boolean administers(UUID id){return ownedBy(id)||access.getOrDefault(id,-1)==1;}
    public Set<UUID> users(){var ids=new LinkedHashSet<>(access.keySet());if(owner!=null)ids.add(owner);return Collections.unmodifiableSet(ids);}
    public void assign(Player player){owner=player.getUUID();ownerName=player.getGameProfile().getName();access.clear();sync();}
    public void grant(UUID id,int rank){access.merge(id,rank==1?1:0,Math::max);sync();}
    public void copyAccess(OwnedBlockEntity source){owner=source.owner;ownerName=source.ownerName;access.clear();access.putAll(source.access);sync();}
    public void sync(){setChanged();if(level!=null)level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),3);}
    private CompoundTag publicTag(){var t=new CompoundTag();if(owner!=null)t.putUUID("Owner",owner);t.putString("OwnerName",ownerName);t.putByte("Setting",(byte)setting);return t;}
    @Override protected void saveAdditional(CompoundTag t,HolderLookup.Provider lookup){super.saveAdditional(t,lookup);t.merge(publicTag());var entries=new ListTag();access.forEach((id,rank)->{var entry=new CompoundTag();entry.putUUID("Player",id);entry.putByte("Rank",rank.byteValue());entries.add(entry);});t.put("Access",entries);}
    @Override protected void loadAdditional(CompoundTag t,HolderLookup.Provider lookup){super.loadAdditional(t,lookup);owner=t.hasUUID("Owner")?t.getUUID("Owner"):null;ownerName=t.getString("OwnerName");setting=Math.max(0,Math.min(2,t.getByte("Setting")));access.clear();for(var value:t.getList("Access",Tag.TAG_COMPOUND)){var entry=(CompoundTag)value;if(entry.hasUUID("Player"))access.put(entry.getUUID("Player"),entry.getByte("Rank")==1?1:0);}}
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider lookup){return publicTag();}
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket(){return ClientboundBlockEntityDataPacket.create(this);}
}
