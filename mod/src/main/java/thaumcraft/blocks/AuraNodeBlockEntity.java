package thaumcraft.blocks;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.nodes.NodeState;
import thaumcraft.items.WandVis;
import thaumcraft.registry.R2Content;

public final class AuraNodeBlockEntity extends BlockEntity {
    public final NodeState state=new NodeState();
    private UUID id=UUID.randomUUID();
    private boolean catchUp=true;
    private UUID drainer;
    private int drainColor=0xffffff,displayColor=0xffffff,previousDisplayColor=0xffffff;
    private long drainUntil,colorTick=-1;
    public AuraNodeBlockEntity(BlockPos pos,BlockState block) { super(R2Content.NODE_ENTITY.get(),pos,block); }
    public UUID id() { return id; }
    public void restoreIdentity(UUID value) { id=value;setChanged(); }
    public int stabilizationLock() {return level!=null&&getBlockState().is(R2Content.AURA_NODE.get())?thaumcraft.visnet.VisDeviceBlock.lock(level,worldPosition):0;}
    public UUID drainer() {return level!=null&&level.getGameTime()<=drainUntil?drainer:null;}
    public int beamColor() {
        if(level==null)return drainColor;long now=level.getGameTime(),ticks=colorTick<0?1:Math.clamp(now-colorTick,0,100);colorTick=now;if(ticks>0)previousDisplayColor=displayColor;
        double old=Math.pow(0.8,ticks);int result=0;for(int shift:new int[]{16,8,0})result|=(int)(((displayColor>>shift)&255)*old+((drainColor>>shift)&255)*(1-old))<<shift;
        return displayColor=result;
    }
    public int beamColor(float partial){int current=beamColor(),result=0;for(int shift:new int[]{16,8,0})result|=(int)net.minecraft.util.Mth.lerp(partial,(previousDisplayColor>>shift)&255,(current>>shift)&255)<<shift;return result;}
    public void stopDrain(ServerPlayer player) {if(player.getUUID().equals(drainer)){drainer=null;drainUntil=0;changedAndSync();}}
    public Map<String,Integer> scanAspects() {
        return scanAspects(state);
    }
    public static Map<String,Integer> scanAspects(NodeState state) {
        var result=new LinkedHashMap<String,Integer>();state.current.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(x->result.put(x.getKey(),Math.max(4,x.getValue()/10)));
        switch(state.type) { case DARK->{result.merge("mortuus",2,Math::max);result.merge("tenebrae",2,Integer::sum);}case PURE->{result.merge("sano",2,Math::max);result.merge("ordo",2,Integer::sum);}case TAINTED->result.merge("vitium",4,Math::max);case HUNGRY->result.merge("fames",4,Math::max);case UNSTABLE->result.merge("perditio",4,Math::max);default->{} }return result;
    }
    public void initialize(Map<String,Integer> aspects,NodeState.Type type,NodeState.Modifier modifier) { state.initialize(aspects);state.type=type;state.modifier=modifier;changedAndSync(); }
    public boolean charge(ServerPlayer player,ItemStack wand) {
        if(!(wand.getItem() instanceof thaumcraft.items.CastingWandItem)||level==null||level.getBlockEntity(worldPosition)!=this||player.distanceToSqr(worldPosition.getCenter())>64) return false;
        // Original TileNode.onUsingWandTick: tap is 1 + NODETAPPER1 + NODETAPPER2 (so a max of 3
        // vis per 5-tick drain pulse), and NODEPRESERVE leaves 1 vis of each aspect behind
        // unless the player is sneaking or the wand is plain wood+iron.
        var knowledge=thaumcraft.knowledge.KnowledgeService.get(player);
        int tap=1;
        if(knowledge.completedResearch().contains("NODETAPPER1"))tap++;
        if(knowledge.completedResearch().contains("NODETAPPER2"))tap++;
        var parts=thaumcraft.items.WandMaterials.parts(wand);
        boolean preserve=!player.isShiftKeyDown()&&knowledge.completedResearch().contains("NODEPRESERVE")&&!"wood".equals(parts.rod())&&!"iron".equals(parts.cap());
        var available=state.primalsWithVis().stream().filter(a->WandVis.get(wand,a)<WandVis.capacity(wand)).filter(a->!preserve||state.amount(a)>1).toList();
        if(available.isEmpty()) {stopDrain(player);return false;}var aspect=available.get(level.random.nextInt(available.size()));
        int amount=Math.min(tap,state.amount(aspect));if(preserve&&amount>=state.amount(aspect))amount--;
        if(amount<=0){stopDrain(player);return false;}
        int leftover=WandVis.add(wand,aspect,amount);
        int taken=amount-leftover;if(taken<=0){stopDrain(player);return false;}
        if(state.take(aspect,taken)){drainer=player.getUUID();drainColor=aspect.getColor();drainUntil=level.getGameTime()+10;changedAndSync();return true;}stopDrain(player);return false;
    }
    public void serverTick() {
        if(!(level instanceof ServerLevel server)) return;var random=new Random(server.random.nextLong());boolean changed=false;
        state.stabilizationLock=stabilizationLock();
        if(drainer!=null){var player=server.getPlayerByUUID(drainer);if(server.getGameTime()>drainUntil||player==null||!player.isAlive()||!player.isUsingItem()){drainer=null;drainUntil=0;changed=true;}}
        if(catchUp) { catchUp=false;changed=state.catchUp(random,System.currentTimeMillis()); }
        changed|=state.recharge(random,System.currentTimeMillis());
        changed|=thaumcraft.world.NodeEffects.tick(this,server,random);
        if(state.current.isEmpty()) { if(getBlockState().is(thaumcraft.registry.R3Content.SILVERWOOD_NODE_LOG.get()))server.setBlockAndUpdate(worldPosition,thaumcraft.registry.R3Content.SILVERWOOD_LOG.get().defaultBlockState().setValue(net.minecraft.world.level.block.RotatedPillarBlock.AXIS,getBlockState().getValue(net.minecraft.world.level.block.RotatedPillarBlock.AXIS)));else if(getBlockState().is(R2Content.AURA_NODE.get()))server.removeBlock(worldPosition,false);return; }
        if(changed) changedAndSync();
    }
    public void changedAndSync() { setChanged();if(level!=null&&!level.isClientSide) level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),3); }
    @Override protected void saveAdditional(CompoundTag tag,HolderLookup.Provider lookup) {
        super.saveAdditional(tag,lookup);tag.putUUID("nodeId",id);tag.putString("nodeType",state.type.name());tag.putString("modifier",state.modifier.name());
        var current=new CompoundTag();state.current.forEach(current::putInt);tag.put("vis",current);var base=new CompoundTag();state.base.forEach(base::putInt);tag.put("baseVis",base);
        tag.putInt("ticks",state.ticks);tag.putInt("wait",state.wait);tag.putLong("lastActive",state.lastActive);
    }
    @Override protected void loadAdditional(CompoundTag tag,HolderLookup.Provider lookup) {
        super.loadAdditional(tag,lookup);if(tag.hasUUID("nodeId")) id=tag.getUUID("nodeId");
        state.type=NodeState.Type.valueOf(tag.getString("nodeType"));state.modifier=NodeState.Modifier.valueOf(tag.getString("modifier"));
        state.current.clear();var current=tag.getCompound("vis");for(var key:current.getAllKeys()) state.current.put(key,current.getInt(key));
        state.base.clear();var base=tag.getCompound("baseVis");for(var key:base.getAllKeys()) state.base.put(key,base.getInt(key));
        state.ticks=tag.getInt("ticks");state.wait=tag.getInt("wait");state.lastActive=tag.getLong("lastActive");catchUp=true;
        drainer=tag.hasUUID("drainer")?tag.getUUID("drainer"):null;drainColor=tag.contains("drainColor")?tag.getInt("drainColor"):0xffffff;drainUntil=tag.getLong("drainUntil");
    }
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider lookup) {var tag=saveWithoutMetadata(lookup);if(drainer!=null){tag.putUUID("drainer",drainer);tag.putInt("drainColor",drainColor);tag.putLong("drainUntil",drainUntil);}return tag;}
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
}
