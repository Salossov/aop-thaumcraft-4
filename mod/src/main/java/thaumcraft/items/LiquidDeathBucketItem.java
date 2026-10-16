package thaumcraft.items;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import thaumcraft.fluids.R4Fluids;

/** Vanilla's bucket only places FlowingFluid; TC4 death liquid has finite quanta. */
public final class LiquidDeathBucketItem extends BucketItem {
    public LiquidDeathBucketItem(){super(R4Fluids.DEATH.get(),new Properties().stacksTo(1).craftRemainder(Items.BUCKET));}
    @Override public boolean emptyContents(Player player,Level level,BlockPos pos,BlockHitResult hit,ItemStack container){
        var state=level.getBlockState(pos);
        if(!state.isAir()&&!state.canBeReplaced(R4Fluids.DEATH.get()))return hit!=null&&emptyContents(player,level,hit.getBlockPos().relative(hit.getDirection()),null,container);
        if(!level.isClientSide&&!state.isAir()&&state.getFluidState().isEmpty())level.destroyBlock(pos,true);
        if(!level.setBlock(pos,R4Fluids.DEATH.get().defaultFluidState().createLegacyBlock(),11))return false;
        playEmptySound(player,level,pos);return true;
    }
}
