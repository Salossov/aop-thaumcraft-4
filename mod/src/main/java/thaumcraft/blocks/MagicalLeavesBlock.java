package thaumcraft.blocks;
import java.util.Random;
import net.minecraft.core.*;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
/** Original BlockMagicalLeaves: silverwood emits light7 and sparkles; both drip water under sky. */
public final class MagicalLeavesBlock extends LeavesBlock{
    private final boolean silverwood;
    public MagicalLeavesBlock(){this(false);}
    public MagicalLeavesBlock(boolean silverwood){super(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LEAVES).lightLevel(s->silverwood?7:0));this.silverwood=silverwood;}
    public boolean silverwood(){return silverwood;}
    /** Client installs the actual silver sparkle (FXSparkle style7); no-op on the dedicated server. */
    public static java.util.function.Consumer<net.minecraft.world.phys.Vec3> silverSparkle=p->{};
    @Override public int getFlammability(BlockState state,BlockGetter l,BlockPos p,Direction face){return 60;}
    @Override public int getFireSpreadSpeed(BlockState state,BlockGetter l,BlockPos p,Direction face){return 30;}
    @Override public void animateTick(BlockState state,Level level,BlockPos pos,RandomSource random){
        if(level.canSeeSky(pos.above())&&!level.getBlockState(pos.below()).isSolidRender(level,pos.below())&&random.nextInt(15)==1)level.addParticle(net.minecraft.core.particles.ParticleTypes.DRIPPING_WATER,pos.getX()+random.nextFloat(),pos.getY()-0.05,pos.getZ()+random.nextFloat(),0,0,0);
        if(silverwood&&random.nextInt(500)==0)silverSparkle.accept(new net.minecraft.world.phys.Vec3(pos.getX()+.5+random.nextFloat()-random.nextFloat(),pos.getY()+.5+random.nextFloat()-random.nextFloat(),pos.getZ()+.5+random.nextFloat()-random.nextFloat()));
    }
}
