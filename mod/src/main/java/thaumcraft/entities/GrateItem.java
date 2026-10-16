package thaumcraft.entities;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import thaumcraft.production.ItemGrateBlock;

/** Source EntityItemGrate: suppress solid-block ejection inside either grate state. */
public final class GrateItem extends ItemEntity {
    public GrateItem(EntityType<? extends GrateItem> type,Level level){super(type,level);}
    public GrateItem(Level level,BlockPos grate,ItemStack stack){
        this(R5Entities.GRATE_ITEM.get(),level);
        setPos(grate.getX()+.5,grate.getY()+.6,grate.getZ()+.5);
        setItem(stack);setDeltaMovement(0,-.1,0);
    }
    public BlockPos legacyBlockPosition(){return BlockPos.containing(getX(),getY()+getBbHeight()/2,getZ());}
    @Override protected double getDefaultGravity(){return (double).04f;}
    @Override protected void moveTowardsClosestSpace(double x,double y,double z){
        if(level().getBlockState(legacyBlockPosition()).getBlock() instanceof ItemGrateBlock)return;
        super.moveTowardsClosestSpace(x,y,z);
    }
}
