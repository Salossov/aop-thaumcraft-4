package thaumcraft.production;

import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.crafting.R3Recipes;
import thaumcraft.data.ThaumcraftData;
import thaumcraft.fluids.*;
import thaumcraft.knowledge.KnowledgeService;
import thaumcraft.registry.*;

public final class CrucibleBlockEntity extends EssentiaBlockEntity {
    public int heat;
    private int water;
    private long counter = -100;
    public final IFluidHandler fluidHandler = new IFluidHandler() {
        public int getTanks() { return 1; }
        public FluidStack getFluidInTank(int tank) { return water == 0 ? FluidStack.EMPTY : new FluidStack(Fluids.WATER, water); }
        public int getTankCapacity(int tank) { return 1000; }
        public boolean isFluidValid(int tank, FluidStack stack) { return stack.is(Fluids.WATER); }
        public int fill(FluidStack stack, FluidAction action) {
            if (!isFluidValid(0, stack)) return 0;
            int accepted = Math.min(1000 - water, stack.getAmount()); if (action.execute() && accepted > 0) fillWater(accepted); return accepted;
        }
        public FluidStack drain(FluidStack stack, FluidAction action) { return stack.is(Fluids.WATER) ? drain(stack.getAmount(), action) : FluidStack.EMPTY; }
        public FluidStack drain(int maximum, FluidAction action) {
            int taken = Math.min(water, Math.max(0, maximum)); if (taken == 0) return FluidStack.EMPTY;
            if (action.execute()) { water -= taken; sync(); } return new FluidStack(Fluids.WATER, taken);
        }
    };
    public CrucibleBlockEntity(BlockPos pos, BlockState state) { super(R3Content.CRUCIBLE_ENTITY.get(), pos, state, 1_048_576, true, false); }
    public int water() { return water; }
    public int fillWater(int n) { if (n < 0) throw new IllegalArgumentException("Negative water"); int accepted = Math.min(n, 1000 - water); water += accepted; if (accepted > 0) sync(); return accepted; }
    public float fluidHeight() { float base=.3f+.5f*water/1000f,out=base+store.amount()/100f*(1-base);return out>1?1.001f:out==1?.9999f:out; }
    public void clientTick(){if(level!=null&&level.isClientSide&&water>0)thaumcraft.client.CrucibleEffects.tick(this);}
    public boolean heated() { return R3Content.heatSource(level, worldPosition.below()); }
    @Override public void serverTick() {
        counter++;
        int previous = heat;
        if (water > 0 && heated()) { if (heat < 200) heat = Math.min(200, heat + 1 + R3Content.bellows(level, worldPosition) * 2); }
        else if (heat > 0) heat--;
        if (store.amount() > 100 && counter % 5 == 0) { removeRandom(); spill(); }
        if (counter > 100 && heat > 150) {
            counter = 0;
            if (store.amount() > 0) {
                var keys = store.getAspects().getAspects(); var a = keys[level.random.nextInt(keys.length)];
                if (a.isPrimal()) a = keys[level.random.nextInt(keys.length)];
                water = Math.max(0, water - 2); store.takeFromContainer(a, 1);
                if (a.isPrimal()) spill(); else { var parts = a.getComponents(); store.addToContainer(parts.get(level.random.nextBoolean() ? 0 : 1), 1); }
            }
            sync();
        }
        setChanged();
        if ((previous <= 150) != (heat <= 150)) sync();
    }
    private void removeRandom() { var types = store.getAspects().getAspects(); if (types.length > 0) store.takeFromContainer(types[level.random.nextInt(types.length)], 1); }
    public void spill() {
        if (level.random.nextInt(4) != 0) return;
        var above = worldPosition.above(); var state = level.getBlockState(above);
        if (state.isAir()) FluxBlock.setUnits(level, above, level.random.nextBoolean() ? R2Content.FLUX_GAS.get() : R2Content.FLUX_GOO.get(), 1);
        else if (state.getBlock() instanceof FluxBlock flux && state.getValue(FiniteFluxFluid.QUANTA) < 8) FluxBlock.setUnits(level, above, flux, state.getValue(FiniteFluxFluid.QUANTA) + 1);
    }
    public void empty() { if (water > 0 || store.amount() > 0) { for (int n = store.amount() / 2; n > 0; n--) spill(); water = 0; store.clear(); sync(); } }
    public void smelt(ItemEntity entity) {
        if (level == null || level.isClientSide || heat <= 150 || water == 0 || entity.getPersistentData().getBoolean("TC4AlchemyProduct")) return;
        var stack = entity.getItem(); int remaining = stack.getCount();
        ServerPlayer thrower = entity.getOwner() instanceof ServerPlayer player ? player : null;
        boolean changed = false;
        // Original TC4 processes part of a thrown stack per collision; leftovers remain an entity.
        for (int n = 0; n < remaining; n++) {
            // Original matches per-unit, against the current tank contents AND the item:
            // a recipe the tank can't pay is skipped (item just dissolves), and when
            // several recipes match, the one with the most aspect entries wins.
            var recipe = thrower == null ? null : level.getRecipeManager().getAllRecipesFor(R3Recipes.CRUCIBLE_TYPE.get()).stream()
                    .filter(holder -> KnowledgeService.get(thrower).completedResearch().contains(holder.value().research()) && holder.value().matches(this, stack))
                    .max(java.util.Comparator.comparingInt(holder -> holder.value().aspects().size())).orElse(null);
            if (recipe != null && water > 0) {
                recipe.value().consume(this); water = Math.max(0, water - 50); eject(recipe.value().getRecipeOutput()); remaining--; counter = -250; changed = true;
            } else {
                var values = ThaumcraftData.aspects(level.getServer(), stack);
                if (values.isEmpty()) {
                    entity.setDeltaMovement((level.random.nextFloat() - level.random.nextFloat()) * 0.2, 0.35, (level.random.nextFloat() - level.random.nextFloat()) * 0.2); break;
                }
                values.forEach((tag, amount) -> store.addToContainer(Aspect.getAspect(tag), amount)); remaining--; counter = -150; changed = true;
            }
        }
        if (remaining == 0) entity.discard(); else { stack.setCount(remaining); entity.setItem(stack); }
        if (changed) { level.playSound(null, worldPosition, SoundEvents.BUBBLE_COLUMN_BUBBLE_POP, SoundSource.BLOCKS, 0.2f, 1.0f + level.random.nextFloat() * 0.4f); sync(); }
    }
    public void eject(ItemStack result) {
        var entity = new ItemEntity(level, worldPosition.getX() + 0.5, worldPosition.getY() + 0.71, worldPosition.getZ() + 0.5, result);
        entity.getPersistentData().putBoolean("TC4AlchemyProduct", true); entity.setDeltaMovement(0, 0.1, 0); level.addFreshEntity(entity);
    }
    @Override protected void saveAdditional(CompoundTag tag, HolderLookup.Provider r) { super.saveAdditional(tag, r); tag.putInt("Heat", heat); tag.putInt("Water", water); tag.putLong("Counter", counter); }
    @Override protected void loadAdditional(CompoundTag tag, HolderLookup.Provider r) { super.loadAdditional(tag, r); heat = Math.clamp(tag.getInt("Heat"), 0, 200); water = Math.clamp(tag.getInt("Water"), 0, 1000); counter = tag.contains("Counter") ? tag.getLong("Counter") : -100; }
}
