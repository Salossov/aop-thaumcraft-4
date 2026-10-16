package thaumcraft.fluids;

import net.minecraft.core.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.*;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.*;
import net.neoforged.neoforge.fluids.FluidType;
import thaumcraft.registry.R4Content;

/** The original death liquid has four finite quanta per bucket (250 mB each). */
public final class FiniteDeathFluid extends Fluid {
    public static final IntegerProperty QUANTA=IntegerProperty.create("quanta",1,4);
    public FiniteDeathFluid(){registerDefaultState(stateDefinition.any().setValue(QUANTA,4));}
    @Override protected void createFluidStateDefinition(StateDefinition.Builder<Fluid,FluidState> b){b.add(QUANTA);}
    @Override public FluidType getFluidType(){return R4Fluids.DEATH_TYPE.get();}
    @Override public Item getBucket(){return R4Content.DEATH_BUCKET.get();}
    @Override public int getTickDelay(LevelReader level){return 7;}
    @Override protected float getExplosionResistance(){return 100;}
    @Override protected boolean canBeReplacedWith(FluidState s,BlockGetter level,BlockPos pos,Fluid incoming,Direction side){return false;}
    @Override public boolean isSource(FluidState s){return s.getValue(QUANTA)==4;}
    @Override public int getAmount(FluidState s){return s.getValue(QUANTA)*2;}
    @Override public float getOwnHeight(FluidState s){return s.getValue(QUANTA)/4f*.875f;}
    @Override public float getHeight(FluidState s,BlockGetter level,BlockPos pos){return level.getFluidState(pos.above()).is(this)?1:getOwnHeight(s);}
    @Override protected BlockState createLegacyBlock(FluidState s){return R4Content.LIQUID_DEATH.get().defaultBlockState().setValue(QUANTA,s.getValue(QUANTA));}
    @Override public VoxelShape getShape(FluidState s,BlockGetter level,BlockPos pos){return Shapes.box(0,0,0,1,getHeight(s,level,pos),1);}
    @Override protected Vec3 getFlow(BlockGetter level,BlockPos pos,FluidState s){
        var flow=Vec3.ZERO;int amount=s.getValue(QUANTA);
        for(var direction:Direction.Plane.HORIZONTAL){var target=pos.relative(direction);var other=level.getFluidState(target);int q=other.is(this)?other.getValue(QUANTA):level.getBlockState(target).blocksMotion()?amount:0;flow=flow.add(direction.getStepX()*(amount-q),0,direction.getStepZ()*(amount-q));}
        return flow.normalize();
    }
}
