package thaumcraft.production;

import java.util.HashSet;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.data.ThaumcraftData;
import thaumcraft.menu.AlchemyFurnaceMenu;
import thaumcraft.registry.R3Content;

public final class AlchemyFurnaceBlockEntity extends EssentiaBlockEntity implements WorldlyContainer, MenuProvider {
    private NonNullList<ItemStack> items = NonNullList.withSize(2, ItemStack.EMPTY);
    public int burnTime, totalBurnTime, cookTime, smeltTime = 100;
    private boolean speedBoost;
    private int count;
    public final ContainerData data = new ContainerData() {
        public int get(int i) { return switch (i) { case 0 -> burnTime; case 1 -> totalBurnTime; case 2 -> cookTime; case 3 -> smeltTime; case 4 -> store.amount(); default -> 0; }; }
        public void set(int i, int n) { switch (i) { case 0 -> burnTime = n; case 1 -> totalBurnTime = n; case 2 -> cookTime = n; case 3 -> smeltTime = n; } }
        public int getCount() { return 5; }
    };
    public AlchemyFurnaceBlockEntity(BlockPos pos, BlockState state) { super(R3Content.FURNACE_ENTITY.get(), pos, state, 50, true, false); }
    private boolean canSmelt() {
        if (items.get(0).isEmpty()) return false;
        var aspects = ThaumcraftData.aspects(level.getServer(), items.get(0)); int size = aspects.values().stream().mapToInt(Integer::intValue).sum();
        if (size <= 0 || size > store.capacity() - store.amount()) return false;
        smeltTime = Math.max(1, (int) (size * 10 * (1.0f - 0.125f * R3Content.bellows(level, worldPosition)))); return true;
    }
    @Override public void serverTick() {
        boolean wasBurning = burnTime > 0; count++;
        if (burnTime > 0) burnTime--;
        if (count % (speedBoost ? 20 : 40) == 0 && store.amount() > 0) distill();
        if (burnTime == 0 && canSmelt()) {
            var fuel = items.get(1); totalBurnTime = burnTime = fuel.getBurnTime(RecipeType.SMELTING);
            if (burnTime > 0) { speedBoost = fuel.is(R3Content.ALUMENTUM.get()); var remainder = fuel.getCraftingRemainingItem(); fuel.shrink(1); if (fuel.isEmpty()) items.set(1, remainder); sync(); }
        }
        if (burnTime > 0 && canSmelt()) {
            cookTime++;
            if (cookTime >= smeltTime) { cookTime = 0; ThaumcraftData.aspects(level.getServer(), items.get(0)).forEach((tag, n) -> store.addToContainer(Aspect.getAspect(tag), n)); items.get(0).shrink(1); sync(); }
        } else cookTime = 0;
        if (wasBurning != (burnTime > 0)) { level.setBlock(worldPosition, getBlockState().setValue(ProductionBlock.LIT, burnTime > 0), 3); sync(); }
        setChanged();
    }
    private void distill() {
        var excluded = new HashSet<Aspect>();
        for (int height = 1; height <= 5; height++) {
            var pos = worldPosition.above(height); if (!level.hasChunkAt(pos) || !(level.getBlockEntity(pos) instanceof TransportBlockEntity alembic) || alembic.kind != ProductionBlock.Kind.ALEMBIC) break;
            var type = alembic.store.first();
            if (type != null && alembic.store.amount() < 32 && store.containerContains(type) > 0) { takeFromContainer(type, 1); alembic.addToContainer(type, 1); excluded.add(type); }
        }
        for (int height = 1; height <= 5; height++) {
            var pos = worldPosition.above(height); if (!level.hasChunkAt(pos) || !(level.getBlockEntity(pos) instanceof TransportBlockEntity alembic) || alembic.kind != ProductionBlock.Kind.ALEMBIC) break;
            if (alembic.store.amount() > 0) continue;
            Aspect a = alembic.store.filter();
            if (a == null) { var candidates = store.getAspects().asMap().keySet().stream().filter(type -> !excluded.contains(type)).toList(); if (candidates.isEmpty()) continue; a = candidates.get(level.random.nextInt(candidates.size())); }
            if (store.containerContains(a) == 0) continue;
            takeFromContainer(a, 1); alembic.addToContainer(a, 1); break;
        }
    }
    @Override public int getContainerSize() { return 2; }
    @Override public boolean isEmpty() { return items.stream().allMatch(ItemStack::isEmpty); }
    @Override public ItemStack getItem(int i) { return items.get(i); }
    @Override public ItemStack removeItem(int i, int n) { var result = ContainerHelper.removeItem(items, i, n); if (!result.isEmpty()) sync(); return result; }
    @Override public ItemStack removeItemNoUpdate(int i) { return ContainerHelper.takeItem(items, i); }
    @Override public void setItem(int i, ItemStack stack) { items.set(i, stack); stack.limitSize(getMaxStackSize(stack)); sync(); }
    @Override public boolean stillValid(Player p) { return level != null && level.getBlockEntity(worldPosition) == this && p.distanceToSqr(worldPosition.getCenter()) <= 64; }
    @Override public void clearContent() { items.clear(); sync(); }
    @Override public boolean canPlaceItem(int i, ItemStack stack) { return i == 0 || i == 1 && stack.getBurnTime(RecipeType.SMELTING) > 0; }
    @Override public int[] getSlotsForFace(Direction face) { return face == Direction.UP ? new int[]{0} : new int[]{1}; }
    @Override public boolean canPlaceItemThroughFace(int i, ItemStack stack, Direction d) { return canPlaceItem(i, stack); }
    @Override public boolean canTakeItemThroughFace(int i, ItemStack stack, Direction d) { return d == Direction.DOWN && i == 1 && stack.is(Items.BUCKET); }
    @Override public Component getDisplayName() { return Component.translatable("block.thaumcraft.alchemy_furnace"); }
    @Override public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) { return new AlchemyFurnaceMenu(id, inventory, this, data); }
    @Override protected void saveAdditional(CompoundTag tag, HolderLookup.Provider r) { super.saveAdditional(tag, r); ContainerHelper.saveAllItems(tag, items, r); tag.putInt("BurnTime", burnTime); tag.putInt("TotalBurnTime", totalBurnTime); tag.putInt("CookTime", cookTime); tag.putInt("SmeltTime", smeltTime); tag.putBoolean("SpeedBoost", speedBoost); tag.putInt("Count", count); }
    @Override protected void loadAdditional(CompoundTag tag, HolderLookup.Provider r) { super.loadAdditional(tag, r); items = NonNullList.withSize(2, ItemStack.EMPTY); ContainerHelper.loadAllItems(tag, items, r); burnTime = Math.max(0, tag.getInt("BurnTime")); totalBurnTime = Math.max(0, tag.getInt("TotalBurnTime")); cookTime = Math.max(0, tag.getInt("CookTime")); smeltTime = Math.max(1, tag.getInt("SmeltTime")); speedBoost = tag.getBoolean("SpeedBoost"); count = tag.getInt("Count"); }
}
