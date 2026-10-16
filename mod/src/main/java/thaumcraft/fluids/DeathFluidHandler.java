package thaumcraft.fluids;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import thaumcraft.registry.R4Content;

/** Finite world fluid capability. Transfers accept whole TC4 quanta only. */
final class DeathFluidHandler implements IFluidHandler {
    private final Level level;private final BlockPos pos;
    DeathFluidHandler(Level level,BlockPos pos){this.level=level;this.pos=pos.immutable();}
    private int amount(){var s=level.getBlockState(pos);return s.is(R4Content.LIQUID_DEATH.get())?s.getValue(FiniteDeathFluid.QUANTA)*250:0;}
    public int getTanks(){return 1;}
    public FluidStack getFluidInTank(int tank){int n=amount();return n==0?FluidStack.EMPTY:new FluidStack(R4Fluids.DEATH.get(),n);}
    public int getTankCapacity(int tank){return 1000;}
    public boolean isFluidValid(int tank,FluidStack stack){return stack.is(R4Fluids.DEATH.get());}
    public int fill(FluidStack stack,FluidAction action){if(!isFluidValid(0,stack)||!level.getBlockState(pos).is(R4Content.LIQUID_DEATH.get()))return 0;int n=Math.min(1000-amount(),stack.getAmount()/250*250);if(n>0&&action.execute())LiquidDeathBlock.setUnits(level,pos,(amount()+n)/250);return n;}
    public FluidStack drain(FluidStack stack,FluidAction action){return stack.is(R4Fluids.DEATH.get())?drain(stack.getAmount(),action):FluidStack.EMPTY;}
    public FluidStack drain(int maximum,FluidAction action){int stored=amount(),n=Math.min(stored,Math.max(0,maximum)/250*250);if(n==0)return FluidStack.EMPTY;if(action.execute())LiquidDeathBlock.setUnits(level,pos,(stored-n)/250);return new FluidStack(R4Fluids.DEATH.get(),n);}
}
