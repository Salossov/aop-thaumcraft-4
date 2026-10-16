package thaumcraft.fluids;

import net.minecraft.core.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.*;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.*;
import net.neoforged.neoforge.fluids.FluidType;
import thaumcraft.registry.R2Content;

/** One stored quantum is 125 mB. TC4 metadata 0..15 corresponds to 1..16 quanta. */
public final class FiniteFluxFluid extends Fluid {
    public static final IntegerProperty QUANTA=IntegerProperty.create("quanta",1,16);
    private final boolean gas;
    public FiniteFluxFluid(boolean gas) { this.gas=gas;registerDefaultState(stateDefinition.any().setValue(QUANTA,8)); }
    public boolean gas() { return gas; }
    @Override protected void createFluidStateDefinition(StateDefinition.Builder<Fluid,FluidState> builder) { builder.add(QUANTA); }
    @Override public FluidType getFluidType() { return gas?R2Fluids.GAS_TYPE.get():R2Fluids.GOO_TYPE.get(); }
    @Override public Item getBucket() { return Items.AIR; }
    @Override public int getTickDelay(LevelReader level) { return gas?12:30; }
    @Override protected float getExplosionResistance() { return 100; }
    @Override protected boolean canBeReplacedWith(FluidState state,BlockGetter level,BlockPos pos,Fluid incoming,Direction side) { return false; }
    @Override public boolean isSource(FluidState state) { return state.getValue(QUANTA)==8; }
    @Override public int getAmount(FluidState state) { return state.getValue(QUANTA); }
    @Override public float getOwnHeight(FluidState state) { return Math.min(1,state.getValue(QUANTA)/8f*.875f); }
    @Override public float getHeight(FluidState state,BlockGetter level,BlockPos pos) { return level.getFluidState(pos.above()).is(this)?1:getOwnHeight(state); }
    @Override protected BlockState createLegacyBlock(FluidState state) { return (gas?R2Content.FLUX_GAS.get():R2Content.FLUX_GOO.get()).defaultBlockState().setValue(QUANTA,state.getValue(QUANTA)); }
    @Override public VoxelShape getShape(FluidState state,BlockGetter level,BlockPos pos) { return Shapes.box(0,0,0,1,getHeight(state,level,pos),1); }
    @Override protected Vec3 getFlow(BlockGetter level,BlockPos pos,FluidState state) {
        if(gas) return Vec3.ZERO;
        int decay=8-getAmount(state);Vec3 flow=Vec3.ZERO;
        for(var dir:Direction.Plane.HORIZONTAL) {
            var other=pos.relative(dir);var fluid=level.getFluidState(other);int otherDecay=fluid.is(this)?8-getAmount(fluid):9;
            if(otherDecay>=8&&!level.getBlockState(other).blocksMotion()) {
                var below=level.getFluidState(other.below());otherDecay=below.is(this)?8-getAmount(below):9;
                if(otherDecay>=0) flow=flow.add(dir.getStepX()*(otherDecay-decay+8),0,dir.getStepZ()*(otherDecay-decay+8));
            } else if(otherDecay>=0&&otherDecay<8) flow=flow.add(dir.getStepX()*(otherDecay-decay),0,dir.getStepZ()*(otherDecay-decay));
        }
        if(level.getFluidState(pos.above()).is(this)) for(var dir:Direction.Plane.HORIZONTAL) {
            if(level.getBlockState(pos.relative(dir)).isFaceSturdy(level,pos.relative(dir),dir.getOpposite())
                ||level.getBlockState(pos.relative(dir).above()).isFaceSturdy(level,pos.relative(dir).above(),dir.getOpposite())) return flow.normalize().add(0,-6,0).normalize();
        }
        return flow.normalize();
    }
}
