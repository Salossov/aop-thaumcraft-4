package thaumcraft.entities;

import net.minecraft.core.*;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.phys.Vec3;

/** Minecraft 1.7.10 EntityLivingBase.func_110145_l: reposition without detaching. */
public final class LegacyRiderPosition {
    public static Vec3 choose(Entity rider,Entity vehicle){var level=rider.level();var result=new Vec3(vehicle.getX(),vehicle.getBoundingBox().minY+vehicle.getBbHeight(),vehicle.getZ());for(int dx=-1;dx<=1;dx++)for(int dz=-1;dz<1;dz++){if(dx==0&&dz==0)continue;int x=(int)(rider.getX()+dx),y=(int)rider.getY(),z=(int)(rider.getZ()+dz);if(level.getBlockCollisions(rider,rider.getBoundingBox().move(dx,1,dz)).iterator().hasNext())continue;var current=new BlockPos(x,y,z);if(level.getBlockState(current).isFaceSturdy(level,current,Direction.UP))return rider.position().add(dx,1,dz);var below=current.below();var state=level.getBlockState(below);if(state.isFaceSturdy(level,below,Direction.UP)||state.getBlock() instanceof LiquidBlock&&state.getFluidState().is(FluidTags.WATER))result=rider.position().add(dx,1,dz);}return result;}
    private LegacyRiderPosition(){}
}
