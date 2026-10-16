package thaumcraft.blocks;
import net.minecraft.core.*;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.phys.shapes.*;
import thaumcraft.api.crafting.IInfusionStabiliser;

/** A single always-lit candle, using TC4's metadata palette. */
public final class TallowCandleBlock extends Block implements IInfusionStabiliser {
    private static final VoxelShape SHAPE=Block.box(6,0,6,10,8,10);
    private static final int[] COLORS={0xF0F0F0,15435844,12801229,6719955,14602026,4312372,14188952,0x434343,0xA0A0A0,2651799,8073150,2437522,5320730,3887386,11743532,0x1E1B1B};
    public final int color;
    public TallowCandleBlock(int metadata){super(BlockBehaviour.Properties.of().strength(.1f).sound(SoundType.WOOL).noOcclusion().noCollission().lightLevel(s->14));color=COLORS[metadata];}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return SHAPE;}
    @Override protected boolean canSurvive(BlockState s,LevelReader l,BlockPos p){return Block.canSupportRigidBlock(l,p.below());}
    @Override public BlockState getStateForPlacement(BlockPlaceContext c){return canSurvive(defaultBlockState(),c.getLevel(),c.getClickedPos())?defaultBlockState():null;}
    @Override protected BlockState updateShape(BlockState s,Direction d,BlockState n,LevelAccessor l,BlockPos p,BlockPos np){return d==Direction.DOWN&&!canSurvive(s,l,p)?Blocks.AIR.defaultBlockState():s;}
    @Override public void animateTick(BlockState s,Level l,BlockPos p,RandomSource r){l.addParticle(ParticleTypes.SMOKE,p.getX()+.5,p.getY()+.7,p.getZ()+.5,0,0,0);l.addParticle(ParticleTypes.FLAME,p.getX()+.5,p.getY()+.7,p.getZ()+.5,0,0,0);}
    @Override public boolean canStabaliseInfusion(Level l,BlockPos p){return true;}
}
