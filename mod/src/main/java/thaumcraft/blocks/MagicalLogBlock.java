package thaumcraft.blocks;
import net.minecraft.core.*;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
public final class MagicalLogBlock extends RotatedPillarBlock{
 public MagicalLogBlock(){super(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LOG).strength(2.5f));}
 @Override public int getFlammability(BlockState state,BlockGetter l,BlockPos p,Direction face){return 5;}
 @Override public int getFireSpreadSpeed(BlockState state,BlockGetter l,BlockPos p,Direction face){return 5;}
}
