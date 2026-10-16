package thaumcraft.production;

import net.minecraft.core.*;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.wrapper.InvWrapper;

public final class HungryChestBlockEntity extends BlockEntity implements MenuProvider {
    public final SimpleContainer items=new SimpleContainer(27){@Override public void setChanged(){super.setChanged();HungryChestBlockEntity.this.setChanged();}};
    public final InvWrapper handler=new InvWrapper(items);
    private final java.util.Set<java.util.UUID> viewers=new java.util.HashSet<>();
    private int eating;
    public float lid,oldLid;
    public HungryChestBlockEntity(BlockPos pos,BlockState state){super(thaumcraft.registry.R3Content.HUNGRY_CHEST_ENTITY.get(),pos,state);}
    public static void tick(Level level,BlockPos pos,BlockState state,HungryChestBlockEntity chest){chest.oldLid=chest.lid;if(!level.isClientSide&&level.getGameTime()%20==0){chest.viewers.removeIf(uuid->{var player=level.getPlayerByUUID(uuid);return player==null||!(player.containerMenu instanceof ChestMenu menu)||menu.getContainer()!=chest.items;});}float target=chest.eating>0?.2f:chest.viewers.isEmpty()?0:1;chest.lid=chest.lid<target?Math.min(target,chest.lid+.1f):Math.max(target,chest.lid-.1f);if(chest.eating>0)chest.eating--;}
    public void eat(ItemEntity entity){var remaining=items.addItem(entity.getItem().copy());if(remaining.getCount()==entity.getItem().getCount())return;if(remaining.isEmpty())entity.discard();else entity.setItem(remaining);level.playSound(null,worldPosition,SoundEvents.GENERIC_EAT,SoundSource.BLOCKS,.25f,1+level.random.nextFloat()*.2f);level.blockEvent(worldPosition,getBlockState().getBlock(),2,2);setChanged();}
    @Override public boolean triggerEvent(int id,int value){if(id==1){if(level.isClientSide){viewers.clear();if(value>0)viewers.add(new java.util.UUID(0,0));}return true;}if(id==2){eating=2;lid=Math.max(lid,value/10f);return true;}return super.triggerEvent(id,value);}
    @Override public Component getDisplayName(){return Component.translatable("block.thaumcraft.hungry_chest");}
    @Override public AbstractContainerMenu createMenu(int id,Inventory inventory,Player player){viewers.add(player.getUUID());level.blockEvent(worldPosition,getBlockState().getBlock(),1,viewers.size());level.playSound(null,worldPosition,SoundEvents.CHEST_OPEN,SoundSource.BLOCKS,.5f,.9f+level.random.nextFloat()*.1f);return new ChestMenu(MenuType.GENERIC_9x3,id,inventory,items,3){@Override public boolean stillValid(Player player){return level.getBlockEntity(worldPosition)==HungryChestBlockEntity.this&&player.distanceToSqr(worldPosition.getCenter())<64;}@Override public void removed(Player player){super.removed(player);viewers.remove(player.getUUID());level.blockEvent(worldPosition,getBlockState().getBlock(),1,viewers.size());level.playSound(null,worldPosition,SoundEvents.CHEST_CLOSE,SoundSource.BLOCKS,.5f,.9f+level.random.nextFloat()*.1f);}};}
    @Override protected void saveAdditional(CompoundTag tag,HolderLookup.Provider registries){super.saveAdditional(tag,registries);tag.put("Items",ContainerNbt.save(items,registries));}
    @Override protected void loadAdditional(CompoundTag tag,HolderLookup.Provider registries){super.loadAdditional(tag,registries);ContainerNbt.load(items,tag.getList("Items",Tag.TAG_COMPOUND),registries);}
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider registries){return new CompoundTag();}
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket(){return ClientboundBlockEntityDataPacket.create(this);}
}
