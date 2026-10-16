package thaumcraft.world;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import thaumcraft.registry.R4Content;

/** Original 1/30 chunk attempt followed by eighteen triangular-offset placements. */
public final class LegacyCinderpearlFlowers {
    public static boolean populate(WorldGenLevel level,RandomSource random,BlockPos origin){
        int x=origin.getX()+random.nextInt(16),z=origin.getZ()+random.nextInt(16),y=level.getHeight(Heightmap.Types.MOTION_BLOCKING,x,z);var pos=new BlockPos(x,y,z);
        var ground=level.getBlockState(pos.below());if(y>=level.getMaxBuildHeight()||!(ground.is(Blocks.SAND)||ground.is(Blocks.RED_SAND))||level.getBiome(pos).value().getBaseTemperature()<=1||random.nextInt(30)!=0)return false;
        return place(level,random,pos);
    }
    public static boolean place(net.minecraft.world.level.LevelAccessor level,RandomSource random,BlockPos origin){
        boolean placed=false;
        for(int i=0;i<18;i++){var pos=origin.offset(random.nextInt(8)-random.nextInt(8),random.nextInt(4)-random.nextInt(4),random.nextInt(8)-random.nextInt(8));if(level.isOutsideBuildHeight(pos)||!level.isEmptyBlock(pos))continue;var below=level.getBlockState(pos.below());if(below.is(Blocks.GRASS_BLOCK)||below.is(Blocks.SAND)||below.is(Blocks.RED_SAND))placed|=level.setBlock(pos,R4Content.CINDERPEARL.get().defaultBlockState(),3);}
        return placed;
    }
    private LegacyCinderpearlFlowers(){}
}
