package thaumcraft.world;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import thaumcraft.registry.R3Content;
public final class MagicalTreeFlowers {
    public static void place(WorldGenLevel world,RandomSource random,BlockPos centre){for(int i=0;i<18;i++){var p=centre.offset(random.nextInt(8)-random.nextInt(8),random.nextInt(4)-random.nextInt(4),random.nextInt(8)-random.nextInt(8));if(world.isEmptyBlock(p)&&(world.getBlockState(p.below()).is(Blocks.GRASS_BLOCK)||world.getBlockState(p.below()).is(Blocks.SAND)))world.setBlock(p,R3Content.SHIMMERLEAF.get().defaultBlockState(),3);}}
    private MagicalTreeFlowers(){}
}
