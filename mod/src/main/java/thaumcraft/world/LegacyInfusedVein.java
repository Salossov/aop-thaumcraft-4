package thaumcraft.world;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/** The 1.7.10 WorldGenMinable ellipsoids, including its +8 offset and inclusive last step. */
public final class LegacyInfusedVein {
    public static boolean place(WorldGenLevel level, RandomSource random, BlockPos origin, BlockState ore, boolean deep) {
        final int size=6;
        float angle=random.nextFloat()*(float)Math.PI;
        double x0=(float)(origin.getX()+8)+Mth.sin(angle)*size/8f;
        double x1=(float)(origin.getX()+8)-Mth.sin(angle)*size/8f;
        double z0=(float)(origin.getZ()+8)+Mth.cos(angle)*size/8f;
        double z1=(float)(origin.getZ()+8)-Mth.cos(angle)*size/8f;
        double y0=origin.getY()+random.nextInt(3)-2, y1=origin.getY()+random.nextInt(3)-2;
        int floor=deep?level.getMinBuildHeight():Math.max(0,level.getMinBuildHeight());
        boolean placed=false;
        for(int step=0;step<=size;step++) {
            double x=x0+(x1-x0)*step/size, y=y0+(y1-y0)*step/size, z=z0+(z1-z0)*step/size;
            double scale=random.nextDouble()*size/16;
            double diameter=(Mth.sin(step*(float)Math.PI/size)+1f)*scale+1;
            double radius=diameter/2;
            for(int bx=Mth.floor(x-radius);bx<=Mth.floor(x+radius);bx++) {
                double dx=(bx+0.5-x)/radius;if(dx*dx>=1)continue;
                for(int by=Math.max(floor,Mth.floor(y-radius));by<=Mth.floor(y+radius)&&by<level.getMaxBuildHeight();by++) {
                    double dy=(by+0.5-y)/radius;if(dx*dx+dy*dy>=1)continue;
                    for(int bz=Mth.floor(z-radius);bz<=Mth.floor(z+radius);bz++) {
                        double dz=(bz+0.5-z)/radius;if(dx*dx+dy*dy+dz*dz>=1)continue;
                        var pos=new BlockPos(bx,by,bz);if(!level.ensureCanWrite(pos))continue;
                        var state=level.getBlockState(pos);
                        if(state.is(Blocks.STONE)||(deep&&state.is(Blocks.DEEPSLATE)))placed|=level.setBlock(pos,ore,2);
                    }
                }
            }
        }
        return placed;
    }
    private LegacyInfusedVein() {}
}
