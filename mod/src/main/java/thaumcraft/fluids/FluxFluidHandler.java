package thaumcraft.fluids;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

/** World capability: never creates an infinite source or rounds a partial quantum up. */
public final class FluxFluidHandler implements IFluidHandler {
    private final Level level;private final BlockPos pos;private final Block block;
    public FluxFluidHandler(Level level,BlockPos pos,Block block) {this.level=level;this.pos=pos.immutable();this.block=block;}
    private int units() {if(!level.hasChunkAt(pos)) return 0;var state=level.getBlockState(pos);return state.is(block)?state.getValue(FiniteFluxFluid.QUANTA):0;}
    private FiniteFluxFluid fluid() {return ((FluxBlock)block).fluid();}
    private boolean writable() {return !level.isClientSide&&level.getServer()!=null&&level.getServer().isSameThread()&&level.hasChunkAt(pos)&&units()>0;}
    @Override public int getTanks() {return 1;}
    @Override public FluidStack getFluidInTank(int tank) {return tank==0&&units()>0?new FluidStack(fluid(),units()*125):FluidStack.EMPTY;}
    @Override public int getTankCapacity(int tank) {return tank==0?2000:0;}
    @Override public boolean isFluidValid(int tank,FluidStack stack) {return tank==0&&!stack.isEmpty()&&stack.is(fluid());}
    @Override public int fill(FluidStack stack,FluidAction action) {
        if(!writable()||!isFluidValid(0,stack)) return 0;int accepted=Math.min(16-units(),stack.getAmount()/125);
        if(accepted>0&&action.execute()) FluxBlock.setUnits(level,pos,(FluxBlock)block,units()+accepted);
        return accepted*125;
    }
    @Override public FluidStack drain(FluidStack stack,FluidAction action) {return isFluidValid(0,stack)?drain(stack.getAmount(),action):FluidStack.EMPTY;}
    @Override public FluidStack drain(int maximum,FluidAction action) {
        if(!writable()||maximum<125) return FluidStack.EMPTY;int drained=Math.min(units(),maximum/125);
        var result=new FluidStack(fluid(),drained*125);if(action.execute()) FluxBlock.setUnits(level,pos,(FluxBlock)block,units()-drained);return result;
    }
}
