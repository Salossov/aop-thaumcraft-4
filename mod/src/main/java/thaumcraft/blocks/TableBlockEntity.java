package thaumcraft.blocks;

import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.menu.*;
import thaumcraft.registry.R2Content;
import thaumcraft.api.aspects.Aspect;
import java.util.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;

public final class TableBlockEntity extends BlockEntity implements Container,MenuProvider {
    private NonNullList<ItemStack> items=NonNullList.withSize(11,ItemStack.EMPTY);
    public static final List<Aspect> BONUS_ASPECTS=List.copyOf(Aspect.all().values());
    private final Map<Aspect,Integer> bonuses=new LinkedHashMap<>();
    private int bonusTicks;
    public TableBlockEntity(BlockPos pos,BlockState state) { super(R2Content.TABLE_ENTITY.get(),pos,state); }
    @Override public int getContainerSize() { return 11; }
    @Override public boolean isEmpty() { return items.stream().allMatch(ItemStack::isEmpty); }
    @Override public ItemStack getItem(int slot) { return items.get(slot); }
    @Override public ItemStack removeItem(int slot,int amount) { var result=ContainerHelper.removeItem(items,slot,amount);if(!result.isEmpty()) setChanged();return result; }
    @Override public ItemStack removeItemNoUpdate(int slot) { return ContainerHelper.takeItem(items,slot); }
    @Override public void setItem(int slot,ItemStack stack) { items.set(slot,stack);setChanged(); }
    @Override public void clearContent() { items.clear();setChanged(); }
    @Override public boolean stillValid(Player player) { return level!=null&&level.getBlockEntity(worldPosition)==this&&player.distanceToSqr(worldPosition.getCenter())<=64; }
    @Override protected void loadAdditional(CompoundTag tag,HolderLookup.Provider registries) { super.loadAdditional(tag,registries);items=NonNullList.withSize(11,ItemStack.EMPTY);ContainerHelper.loadAllItems(tag,items,registries);bonuses.clear();var saved=tag.getCompound("BonusAspects");for(var aspect:BONUS_ASPECTS)if(saved.getInt(aspect.getTag())>0)bonuses.put(aspect,1); }
    @Override protected void saveAdditional(CompoundTag tag,HolderLookup.Provider registries) { super.saveAdditional(tag,registries);ContainerHelper.saveAllItems(tag,items,registries);var saved=new CompoundTag();bonuses.forEach((aspect,count)->saved.putInt(aspect.getTag(),count));tag.put("BonusAspects",saved); }
    public int bonus(Aspect aspect) { return bonuses.getOrDefault(aspect,0); }
    public boolean consumeBonuses(Map<Aspect,Integer> amounts) {
        if(amounts.entrySet().stream().anyMatch(e->bonus(e.getKey())<e.getValue()))return false;
        amounts.forEach((aspect,count)->{if(bonus(aspect)==count)bonuses.remove(aspect);else bonuses.put(aspect,bonus(aspect)-count);});
        if(!amounts.isEmpty())changedAndSync();return true;
    }
    private boolean grant(Aspect aspect) { if(bonus(aspect)>0)return false;bonuses.put(aspect,1);return true; }
    public static void tick(net.minecraft.world.level.Level level,BlockPos pos,BlockState state,TableBlockEntity table) {
        if(!(level instanceof ServerLevel server)||!state.is(R2Content.RESEARCH_TABLE.get())||state.getValue(TableBlock.SLAVE))return;
        if(table.bonusTicks++>600) {table.bonusTicks=0;table.recalculateBonuses(server);}
    }
    /** Original 17-cube search, probabilities and one-point caps; never loads adjacent chunks. */
    public void recalculateBonuses(ServerLevel server) {
        boolean changed=false;var random=server.random;var above=worldPosition.above();
        if(!server.isDay()&&server.getMaxLocalRawBrightness(above)<4&&!server.canSeeSky(above)&&random.nextInt(20)==0)changed|=grant(Aspect.getAspect("perditio"));
        // TC4 used a 256-block world ceiling. Preserve its 128/169/192 altitude thresholds.
        for(float fraction:new float[]{0.5f,0.66f,0.75f})if(worldPosition.getY()>256*fraction&&random.nextInt(20)==0)changed|=grant(Aspect.getAspect("aer"));
        search:for(int x=-8;x<=8;x++)for(int z=-8;z<=8;z++)for(int y=-8;y<=8;y++) {
            var p=worldPosition.offset(x,y,z);if(p.getY()<=server.getMinBuildHeight()||p.getY()>=server.getMaxBuildHeight()||!server.hasChunkAt(p))continue;
            var state=server.getBlockState(p);Aspect candidate=null;int chance=20;
            if(state.is(R2Content.INFUSED_STONE.get(0).get()))candidate=Aspect.getAspect("aer");
            else if(state.is(Blocks.FIRE)||state.is(Blocks.SOUL_FIRE)||state.is(Blocks.LAVA)||state.is(R2Content.INFUSED_STONE.get(1).get()))candidate=Aspect.getAspect("ignis");
            else if(state.is(Blocks.DIRT)||state.is(Blocks.COARSE_DIRT)||state.is(Blocks.GRASS_BLOCK)||state.is(Blocks.PODZOL)||state.is(Blocks.MYCELIUM)||state.is(Blocks.FARMLAND)||state.is(R2Content.INFUSED_STONE.get(3).get()))candidate=Aspect.getAspect("terra");
            else if(state.is(Blocks.WATER)) {candidate=Aspect.getAspect("aqua");chance=15;}
            else if(state.is(R2Content.INFUSED_STONE.get(2).get()))candidate=Aspect.getAspect("aqua");
            else if(state.is(net.minecraft.tags.BlockTags.IMPERMEABLE)||state.is(Blocks.ICE)||state.is(Blocks.PACKED_ICE)||state.is(R2Content.INFUSED_STONE.get(4).get()))candidate=Aspect.getAspect("ordo");
            else if(state.is(R2Content.INFUSED_STONE.get(5).get()))candidate=Aspect.getAspect("perditio");
            if(candidate!=null&&bonus(candidate)<1&&random.nextInt(chance)==0) {changed|=grant(candidate);break search;}
            if(state.is(Blocks.BOOKSHELF)&&random.nextInt(300)==0) {changed|=grant(BONUS_ASPECTS.get(random.nextInt(BONUS_ASPECTS.size())));break search;}
        }
        if(changed)changedAndSync();
    }
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider registries) { return saveWithoutMetadata(registries); }
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
    @Override public void setChanged() { super.setChanged();if(level!=null&&!level.isClientSide)level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),3); }
    public void changedAndSync() { setChanged(); }
    @Override public Component getDisplayName() { return Component.translatable(getBlockState().is(R2Content.RESEARCH_TABLE.get())?"block.thaumcraft.research_table":"block.thaumcraft.arcane_workbench"); }
    @Override public AbstractContainerMenu createMenu(int id,Inventory inventory,Player player) {
        return getBlockState().is(R2Content.RESEARCH_TABLE.get())?new ResearchMenu(id,inventory,this):new ArcaneMenu(id,inventory,this);
    }
    @Override public void writeClientSideData(AbstractContainerMenu menu,net.minecraft.network.RegistryFriendlyByteBuf buffer) {
        buffer.writeBlockPos(worldPosition);if(menu instanceof ActionMenu actions) buffer.writeUUID(actions.token());
    }
}
