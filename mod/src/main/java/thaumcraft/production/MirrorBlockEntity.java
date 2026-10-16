package thaumcraft.production;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.nbt.*;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import thaumcraft.api.aspects.*;
import thaumcraft.api.visnet.VisNetHandler;
import thaumcraft.registry.R3Content;

public final class MirrorBlockEntity extends BlockEntity implements IAspectContainer {
    private MirrorLink link;private boolean linked;
    private final List<ItemStack> output=new ArrayList<>();
    private int instability,count,retry=40;
    public int pulse;
    public MirrorBlockEntity(BlockPos pos,BlockState state){super(R3Content.MIRROR_ENTITY.get(),pos,state);}
    public boolean essentia(){return getBlockState().is(R3Content.ESSENTIA_MIRROR.get());}
    public MirrorLink address(){return new MirrorLink(level.dimension(),worldPosition);}
    public MirrorLink link(){return link;}
    public boolean linked(){return linked;}
    private MirrorBlockEntity target(boolean load){return level instanceof ServerLevel server&&link!=null?link.target(server.getServer(),load):null;}
    private boolean paired(MirrorBlockEntity target){return target!=null&&target!=this&&target.essentia()==essentia()&&target.linked&&address().equals(target.link);}
    public boolean valid(){return linked&&paired(target(true));}
    public boolean setLink(MirrorLink destination){if(level==null||level.isClientSide||address().equals(destination))return false;invalidateLink();link=destination;linked=false;restoreLink();changed();return linked;}
    private void restoreLink(){var target=target(true);if(target==null||target==this||target.essentia()!=essentia())return;if(target.linked&&target.paired(target.target(false))&&!address().equals(target.link))return;target.link=address();target.linked=true;linked=true;target.changed();changed();}
    public void invalidateLink(){var target=target(false);if(target!=null&&address().equals(target.link)){target.linked=false;target.changed();}linked=false;changed();}
    public boolean transport(ItemStack stack){if(essentia()||stack.isEmpty()||!valid())return false;var target=target(true);if(target==null)return false;target.output.add(stack.copy());target.changed();instability=Math.min(Integer.MAX_VALUE-stack.getCount(),instability)+stack.getCount();changed();level.blockEvent(worldPosition,getBlockState().getBlock(),1,0);return true;}
    public boolean transport(ItemEntity entity){if(entity.hasPickUpDelay()||!transport(entity.getItem()))return false;entity.discard();return true;}
    public boolean eject(ItemStack stack){if(!(level instanceof ServerLevel server)||essentia()||stack.isEmpty())return false;var face=getBlockState().getValue(MirrorBlock.FACING);var offset=net.minecraft.world.phys.Vec3.atLowerCornerOf(face.getNormal());var centre=worldPosition.getCenter().subtract(offset.scale(.3));var entity=new ItemEntity(level,centre.x,centre.y,centre.z,stack.copy());entity.setDeltaMovement(offset.scale(.15));entity.setPickUpDelay(20);if(!server.addFreshEntity(entity))return false;level.blockEvent(worldPosition,getBlockState().getBlock(),1,0);return true;}
    public List<ItemStack> outputs(){return output.stream().map(ItemStack::copy).toList();}
    public void serverTick(){
        if(!(level instanceof ServerLevel))return;count++;
        if(count%retry==0){if(!paired(target(true))){linked=false;retry=Math.min(600,retry+20);restoreLink();}else retry=40;}
        if(essentia())return;
        int delay=instability/50;long interval=(long)delay*delay;if(!output.isEmpty()&&count>20&&(interval==0||count%interval==0)){int index=level.random.nextInt(output.size());var stack=output.get(index);if(eject(stack.copyWithCount(1))){stack.shrink(1);if(stack.isEmpty())output.remove(index);instability=Math.min(Integer.MAX_VALUE-1,instability)+1;changed();}}
        if(instability>0&&count%20==0){instability--;changed();}
        if(instability>0){int drained=VisNetHandler.drainVis(level,worldPosition,Aspects.ORDER,1);if(drained>0){instability-=drained;var target=target(false);if(target!=null){target.instability=Math.max(0,target.instability-drained);target.changed();}changed();}}
    }
    public final IItemHandler items=new IItemHandler(){public int getSlots(){return 1;}public ItemStack getStackInSlot(int slot){return ItemStack.EMPTY;}public ItemStack insertItem(int slot,ItemStack stack,boolean simulate){if(slot!=0||essentia()||!valid())return stack;if(!simulate&&!transport(stack))return stack;return ItemStack.EMPTY;}public ItemStack extractItem(int slot,int amount,boolean simulate){return ItemStack.EMPTY;}public int getSlotLimit(int slot){return 64;}public boolean isItemValid(int slot,ItemStack stack){return slot==0&&!essentia();}};
    @Override public AspectList getAspects(){return new AspectList();}
    @Override public void setAspects(AspectList aspects){}
    @Override public boolean doesContainerAccept(Aspect aspect){return false;}
    @Override public int addToContainer(Aspect aspect,int amount){return amount;}
    private static final ThreadLocal<Boolean> DRAINING=ThreadLocal.withInitial(()->false);
    @Override public boolean takeFromContainer(Aspect aspect,int amount){
        if(!essentia()||aspect==null||amount!=1||DRAINING.get()||!valid())return false;var target=target(true);if(target==null||!(target.level instanceof ServerLevel remote))return false;
        var face=target.getBlockState().getValue(MirrorBlock.FACING);DRAINING.set(true);
        try{for(int x=-8;x<=8;x++)for(int z=-8;z<=8;z++)for(int y=-8;y<8;y++){var pos=target.worldPosition.offset(x,y,z);if(!remote.hasChunkAt(pos)||remote.getBlockEntity(pos) instanceof MirrorBlockEntity)continue;if(face.getAxis()==Direction.Axis.X&&x*face.getStepX()<0||face.getAxis()==Direction.Axis.Y&&y*face.getStepY()<0||face.getAxis()==Direction.Axis.Z&&z*face.getStepZ()<0)continue;var source=remote.getCapability(EssentiaCapabilities.SOURCE,pos,null);if(source!=null&&source.takeFromContainer(aspect,1)){remote.blockEvent(target.worldPosition,target.getBlockState().getBlock(),1,aspect.getColor());level.blockEvent(worldPosition,getBlockState().getBlock(),1,aspect.getColor());return true;}}return false;}finally{DRAINING.set(false);}
    }
    @Override public boolean doesContainerContainAmount(Aspect aspect,int amount){return false;}
    @Override public int containerContains(Aspect aspect){return 0;}
    public void changed(){setChanged();if(level!=null)level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),3);}
    @Override public boolean triggerEvent(int id,int parameter){if(id==1){pulse=10;return true;}return super.triggerEvent(id,parameter);}
    @Override protected void saveAdditional(CompoundTag tag,HolderLookup.Provider registries){super.saveAdditional(tag,registries);if(link!=null)tag.put("Link",link.save());tag.putBoolean("Linked",linked);tag.putInt("Instability",instability);var list=new ListTag();for(var stack:output)if(!stack.isEmpty())list.add(stack.save(registries));tag.put("Output",list);}
    @Override protected void loadAdditional(CompoundTag tag,HolderLookup.Provider registries){super.loadAdditional(tag,registries);link=MirrorLink.load(tag.getCompound("Link"));linked=link!=null&&tag.getBoolean("Linked");instability=Math.max(0,tag.getInt("Instability"));output.clear();for(var entry:tag.getList("Output",Tag.TAG_COMPOUND)){var stack=ItemStack.parseOptional(registries,(CompoundTag)entry);if(!stack.isEmpty())output.add(stack);}}
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider registries){var tag=new CompoundTag();if(link!=null)tag.put("Link",link.save());tag.putBoolean("Linked",linked);tag.putInt("Instability",instability);return tag;}
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket(){return ClientboundBlockEntityDataPacket.create(this);}
}
