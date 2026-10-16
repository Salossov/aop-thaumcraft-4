package thaumcraft.visnet;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.api.aspects.*;
import thaumcraft.blocks.AuraNodeBlockEntity;
import thaumcraft.items.*;
import thaumcraft.production.PedestalBlockEntity;
import thaumcraft.registry.R3Content;

public final class WandPedestalBlockEntity extends PedestalBlockEntity {
    private final List<BlockPos> nodes = new ArrayList<>();
    private BlockPos drainSource;
    private int drainColor;private long nextNodeSearch;
    private long drainUntil;
    public WandPedestalBlockEntity(BlockPos pos,BlockState state) { super(R3Content.WAND_PEDESTAL_ENTITY.get(),pos,state); }
    public static boolean accepts(ItemStack stack) { return stack.getItem() instanceof CastingWandItem || stack.getItem() instanceof ThaumicCurioItem curio && curio.storesVis(); }
    @Override public boolean canPlaceItem(int slot,ItemStack stack) { return accepts(stack); }
    @Override public boolean canPlaceItemThroughFace(int slot,ItemStack stack,Direction face) { return isEmpty()&&accepts(stack); }
    public BlockPos drainSource() { return level!=null&&level.getGameTime()<=drainUntil?drainSource:null; }
    public int drainColor() { return drainColor; }
    private static boolean containsPrimal(Aspect aspect,Aspect primal) { return aspect==primal||aspect.getComponents().stream().anyMatch(part->containsPrimal(part,primal)); }
    private void findNodes() {
        nodes.clear();
        for(var pos:BlockPos.betweenClosed(worldPosition.offset(-8,-8,-8),worldPosition.offset(8,8,8)))if(level.hasChunkAt(pos)&&level.getBlockEntity(pos) instanceof AuraNodeBlockEntity)nodes.add(pos.immutable());
    }
    public void serverTick() {
        if(level==null||level.isClientSide)return;
        if(level.getGameTime()>=nextNodeSearch){findNodes();nextNodeSearch=level.getGameTime()+100;}
        if(level.getGameTime()%5!=0||isEmpty())return;
        var stack=getItem(0);if(!accepts(stack))return;
        int minimum=1;
        if(stack.getItem() instanceof CastingWandItem && (WandMaterials.cap(stack).getTag().equals("iron")||WandMaterials.rod(stack).getTag().equals("wood")))minimum=0;
        boolean resonator=level.getBlockState(worldPosition.above()).is(R3Content.ARCANE_RESONATOR.get());
        for(var pos:nodes) {
            if(!level.hasChunkAt(pos)||!(level.getBlockEntity(pos) instanceof AuraNodeBlockEntity node))continue;
            for(var primal:Aspects.PRIMALS) {
                if(WandVis.get(stack,primal)>=WandVis.capacity(stack))continue;
                Aspect drawn=primal;
                if(node.state.amount(primal)<=minimum) {
                    if(!resonator)continue;
                    drawn=null;
                    for(var entry:node.state.current.entrySet()) { var aspect=Aspect.getAspect(entry.getKey());if(aspect!=null&&!aspect.isPrimal()&&entry.getValue()>minimum&&containsPrimal(aspect,primal)){drawn=aspect;break;} }
                    if(drawn==null)continue;
                }
                if(node.state.take(drawn,1)) {
                    WandVis.set(stack,primal,Math.min(WandVis.capacity(stack),WandVis.get(stack,primal)+100));node.changedAndSync();
                    drainSource=pos;drainColor=drawn.getColor();drainUntil=level.getGameTime()+8;changed();return;
                }
            }
        }
    }
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider lookup) {var tag=super.getUpdateTag(lookup);if(drainSource!=null){tag.putLong("DrainSource",drainSource.asLong());tag.putInt("DrainColor",drainColor);tag.putLong("DrainUntil",drainUntil);}return tag;}
    @Override protected void loadAdditional(CompoundTag tag,HolderLookup.Provider lookup) {super.loadAdditional(tag,lookup);drainSource=tag.contains("DrainSource")?BlockPos.of(tag.getLong("DrainSource")):null;drainColor=tag.getInt("DrainColor");drainUntil=tag.getLong("DrainUntil");nodes.clear();}
}
