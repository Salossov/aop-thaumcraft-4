package thaumcraft.production;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import thaumcraft.registry.R4Content;

/** No inventory is retained: an accepted insertion is immediately emitted underneath the plate. */
public final class ItemGrateBlockEntity extends BlockEntity {
    public final IItemHandler input=new IItemHandler(){
        @Override public int getSlots(){return open()?1:0;}
        @Override public ItemStack getStackInSlot(int slot){return ItemStack.EMPTY;}
        @Override public int getSlotLimit(int slot){return open()?64:0;}
        @Override public boolean isItemValid(int slot,ItemStack stack){return slot==0&&open();}
        @Override public ItemStack extractItem(int slot,int amount,boolean simulate){return ItemStack.EMPTY;}
        @Override public ItemStack insertItem(int slot,ItemStack stack,boolean simulate){
            if(stack.isEmpty())return ItemStack.EMPTY;if(slot!=0||!open()||level==null||level.isClientSide)return stack;
            int accepted=Math.min(64,stack.getMaxStackSize());accepted=Math.min(accepted,stack.getCount());
            if(!simulate){var entity=new thaumcraft.entities.GrateItem(level,worldPosition,stack.copyWithCount(accepted));if(!level.addFreshEntity(entity))return stack;}
            return stack.getCount()==accepted?ItemStack.EMPTY:stack.copyWithCount(stack.getCount()-accepted);
        }
    };
    public ItemGrateBlockEntity(BlockPos pos,BlockState state){super(R4Content.GRATE_ENTITY.get(),pos,state);}
    private boolean open(){return !isRemoved()&&getBlockState().getValue(ItemGrateBlock.OPEN);}
}
