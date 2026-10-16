package thaumcraft.blocks;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.*;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.phys.shapes.*;

public final class CinderpearlBlock extends BushBlock {
    public static final MapCodec<CinderpearlBlock> CODEC=simpleCodec(CinderpearlBlock::new);
    public CinderpearlBlock(){this(Properties.ofFullCopy(Blocks.DANDELION).lightLevel(s->8));}
    public CinderpearlBlock(Properties properties){super(properties);}
    @Override protected MapCodec<? extends BushBlock> codec(){return CODEC;}
    @Override protected boolean mayPlaceOn(BlockState ground,BlockGetter level,BlockPos pos){return ground.is(BlockTags.SAND);}
    @Override protected VoxelShape getShape(BlockState state,BlockGetter level,BlockPos pos,CollisionContext context){return Block.box(1.6,0,1.6,14.4,12.8,14.4);}
    @Override public int getFlammability(BlockState state,BlockGetter level,BlockPos pos,Direction face){return 100;}
    @Override public int getFireSpreadSpeed(BlockState state,BlockGetter level,BlockPos pos,Direction face){return 60;}
    @Override public void animateTick(BlockState s,Level l,BlockPos p,RandomSource random){if(random.nextBoolean()){var r=l.random;float x=(float)p.getX()+.5f+(r.nextFloat()-r.nextFloat())*.1f,y=(float)p.getY()+.6f+(r.nextFloat()-r.nextFloat())*.1f,z=(float)p.getZ()+.5f+(r.nextFloat()-r.nextFloat())*.1f;l.addParticle(ParticleTypes.SMOKE,x,y,z,0,0,0);l.addParticle(ParticleTypes.FLAME,x,y,z,0,0,0);}}
}
