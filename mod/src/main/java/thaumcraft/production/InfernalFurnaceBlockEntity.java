package thaumcraft.production;

import net.minecraft.core.*;
import net.minecraft.nbt.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.visnet.VisNetHandler;

public final class InfernalFurnaceBlockEntity extends BlockEntity {
    public final SimpleContainer items=new SimpleContainer(32){@Override public void setChanged(){super.setChanged();InfernalFurnaceBlockEntity.this.setChanged();}};
    public final IItemHandler input=new IItemHandler(){public int getSlots(){return 32;}public ItemStack getStackInSlot(int slot){return items.getItem(slot);}public int getSlotLimit(int slot){return 64;}public boolean isItemValid(int slot,ItemStack stack){return true;}public ItemStack extractItem(int slot,int amount,boolean simulate){return ItemStack.EMPTY;}public ItemStack insertItem(int slot,ItemStack stack,boolean simulate){var current=items.getItem(slot);if(!current.isEmpty()&&!ItemStack.isSameItemSameComponents(current,stack))return stack;int count=Math.min(stack.getCount(),Math.min(64,stack.getMaxStackSize())-current.getCount());if(count<=0)return stack;if(!simulate){items.setItem(slot,stack.copyWithCount(current.getCount()+count));discardInvalid(slot);}return stack.copyWithCount(stack.getCount()-count);}};
    public int cookTime,maxCookTime,boost;
    public InfernalFurnaceBlockEntity(BlockPos pos,BlockState state){super(thaumcraft.registry.R3Content.INFERNAL_ENTITY.get(),pos,state);}
    public boolean core(){return getBlockState().getValue(InfernalFurnaceBlock.PART)==13;}
    public Direction facing(){return getBlockState().getValue(InfernalFurnaceBlock.FACING);}
    public static void tick(Level level,BlockPos pos,BlockState state,InfernalFurnaceBlockEntity tile){if(!(level instanceof ServerLevel server))return;if(level.getGameTime()%40==0){var center=InfernalFurnaceBlock.core(pos,state);if(level.hasChunkAt(center)&&!level.getBlockState(center).is(thaumcraft.registry.R3Content.INFERNAL_FURNACE.get())){level.setBlockAndUpdate(pos,InfernalFurnaceBlock.original(state.getValue(InfernalFurnaceBlock.PART),tile.facing()));return;}}if(tile.core())tile.serverTick(server);}
    public int bellows(){int count=0;for(var direction:Direction.values()){if(direction==Direction.UP)continue;var pos=worldPosition.relative(direction,2);if(level.hasChunkAt(pos)&&level.getBlockEntity(pos) instanceof BellowsBlockEntity bellows&&bellows.getBlockState().getValue(BellowsBlock.FACING)==direction.getOpposite()&&!level.hasNeighborSignal(pos))count++;}return Math.min(3,count);}
    private RecipeHolder<SmeltingRecipe> recipe(ItemStack stack){return stack.isEmpty()?null:level.getRecipeManager().getRecipeFor(RecipeType.SMELTING,new SingleRecipeInput(stack),level).orElse(null);}
    private void serverTick(ServerLevel level){boolean wasCooking=cookTime>0;if(wasCooking){cookTime--;if(boost>0)boost--;}if(boost<=0)boost=VisNetHandler.drainVis(level,worldPosition,thaumcraft.api.aspects.Aspects.FIRE,5);int speed=(boost>0?80:140)-20*bellows();if(maxCookTime==0)maxCookTime=speed;if(cookTime>maxCookTime)cookTime=maxCookTime;if(cookTime==0){for(int slot=0;slot<32;slot++){var stack=items.getItem(slot);var recipe=recipe(stack);if(recipe==null)continue;if(wasCooking){var output=recipe.value().assemble(new SingleRecipeInput(stack),level.registryAccess());eject(level,output);var bonus=level.getRecipeManager().getAllRecipesFor(thaumcraft.crafting.R3Recipes.INFERNAL_BONUS_TYPE.get()).stream().filter(holder->holder.value().ingredient().test(stack)).sorted(java.util.Comparator.comparing(holder->holder.id().toString())).findFirst().orElse(null);if(bonus!=null){var definition=bonus.value();int count=definition.baseCount(),bellows=bellows();if(bellows==0){if(level.random.nextInt(4)==0)count++;}else for(int i=0;i<bellows;i++)if(level.random.nextFloat()<.44)count++;if(count>0)eject(level,definition.result().copyWithCount(count));}float xp=recipe.value().getExperience()*output.getCount();int amount=(int)xp;if(level.random.nextFloat()<xp-amount)amount++;if(amount>0)ExperienceOrb.award(level,worldPosition.getCenter().add(facing().getStepX()*1.2,-.1,facing().getStepZ()*1.2),amount);stack.shrink(1);items.setChanged();}else{cookTime=maxCookTime=speed;}break;}}if(wasCooking||cookTime>0)setChanged();}
    private void eject(ServerLevel level,ItemStack stack){if(stack.isEmpty())return;var position=worldPosition.getCenter().add(facing().getStepX()*1.2,-.1,facing().getStepZ()*1.2);var item=new ItemEntity(level,position.x,position.y,position.z,stack);item.setDeltaMovement(facing().getStepX()==0?(level.random.nextFloat()-level.random.nextFloat())*.03:facing().getStepX()*.13,0,facing().getStepZ()==0?(level.random.nextFloat()-level.random.nextFloat())*.03:facing().getStepZ()*.13);level.addFreshEntity(item);}
    public void eat(ItemEntity entity){var remaining=items.addItem(entity.getItem().copy());if(remaining.getCount()==entity.getItem().getCount())return;for(int slot=0;slot<32;slot++)discardInvalid(slot);if(remaining.isEmpty())entity.discard();else entity.setItem(remaining);setChanged();}
    private void discardInvalid(int slot){if(level==null||items.getItem(slot).isEmpty()||recipe(items.getItem(slot))!=null)return;items.setItem(slot,ItemStack.EMPTY);level.playSound(null,worldPosition,net.minecraft.sounds.SoundEvents.FIRE_EXTINGUISH,net.minecraft.sounds.SoundSource.BLOCKS,.3f,2.6f);if(level instanceof ServerLevel server)server.sendParticles(net.minecraft.core.particles.ParticleTypes.LAVA,worldPosition.getX()+.5,worldPosition.getY()+1,worldPosition.getZ()+.5,2,.3,0,.3,0);}
    @Override protected void saveAdditional(CompoundTag tag,HolderLookup.Provider registries){super.saveAdditional(tag,registries);if(core()){tag.put("Items",ContainerNbt.save(items,registries));tag.putInt("CookTime",cookTime);tag.putInt("MaxCookTime",maxCookTime);tag.putInt("Boost",boost);}}
    @Override protected void loadAdditional(CompoundTag tag,HolderLookup.Provider registries){super.loadAdditional(tag,registries);ContainerNbt.load(items,tag.getList("Items",Tag.TAG_COMPOUND),registries);cookTime=Math.max(0,tag.getInt("CookTime"));maxCookTime=Math.max(0,tag.getInt("MaxCookTime"));boost=Math.max(0,tag.getInt("Boost"));}
}
