package thaumcraft.blocks;
import net.minecraft.core.*;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;

/** Original wooden-device fire rules, including stairs and slabs. */
public final class MagicalWoodBlock extends Block {
    private static BlockBehaviour.Properties woodProperties(){return BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).strength(2.5f,6);}
    public MagicalWoodBlock(){super(woodProperties());}
    @Override public int getFlammability(BlockState s,BlockGetter l,BlockPos p,Direction f){return 20;}
    @Override public int getFireSpreadSpeed(BlockState s,BlockGetter l,BlockPos p,Direction f){return 5;}
    public static final class Stairs extends StairBlock {
        public Stairs(BlockState base){super(base,woodProperties());}
        @Override public int getFlammability(BlockState s,BlockGetter l,BlockPos p,Direction f){return 20;}
        @Override public int getFireSpreadSpeed(BlockState s,BlockGetter l,BlockPos p,Direction f){return 5;}
    }
    public static final class Slab extends SlabBlock {
        public Slab(){super(woodProperties().strength(2,3));}
        @Override public int getFlammability(BlockState s,BlockGetter l,BlockPos p,Direction f){return 20;}
        @Override public int getFireSpreadSpeed(BlockState s,BlockGetter l,BlockPos p,Direction f){return 5;}
    }
}
