package thaumcraft.production;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.visnet.VisNetHandler;
import thaumcraft.api.wands.*;
import thaumcraft.registry.R3Content;
import thaumcraft.registry.R2Sounds;

public final class FocalManipulatorBlockEntity extends PedestalBlockEntity {
    private final Map<Aspect,Integer> remaining=new LinkedHashMap<>();
    private int total,rank,upgrade=-1;
    private ItemStack expected=ItemStack.EMPTY;
    public FocalManipulatorBlockEntity(BlockPos pos,BlockState state){super(R3Content.FOCAL_ENTITY.get(),pos,state);}
    public boolean active(){return total>0;}
    public int total(){return total;}
    public int upgrade(){return upgrade;}
    public Map<Aspect,Integer> remaining(){return Collections.unmodifiableMap(remaining);}
    public int nextRank(){if(!(getItem(0).getItem() instanceof ItemFocusBasic focus))return 0;var upgrades=focus.getAppliedUpgrades(getItem(0));for(int i=0;i<5;i++)if(upgrades[i]<0)return i+1;return 0;}
    public static Map<Aspect,Integer> cost(FocusUpgradeType upgrade,int rank){var base=new LinkedHashMap<String,Integer>();upgrade.aspects().forEach((aspect,amount)->base.put(aspect.getTag(),amount*(200<<(rank-1))));return thaumcraft.world.NodeEffects.reduce(base);}
    public boolean start(ServerPlayer player,int id){
        if(active()||!stillValid(player)||!thaumcraft.knowledge.KnowledgeService.get(player).completedResearch().contains("FOCALMANIPULATION"))return false;
        int next=nextRank();var choice=FocusUpgradeType.get(id);var stack=getItem(0);if(next==0||choice==null||!(stack.getItem() instanceof ItemFocusBasic focus)||!(focus instanceof thaumcraft.items.ThaumicFocusItem item?item.canApplyForPlayer(stack,player,choice,next):focus.canApplyUpgrade(stack,choice,next))||!player.isCreative()&&player.experienceLevel<next*8)return false;
        rank=next;upgrade=id;expected=stack.copy();remaining.clear();remaining.putAll(cost(choice,rank));total=remaining.values().stream().mapToInt(Integer::intValue).sum();
        if(!player.isCreative())player.giveExperienceLevels(-rank*8);changed();level.playSound(null,worldPosition,R2Sounds.CRAFT_START.get(),net.minecraft.sounds.SoundSource.BLOCKS,.25f,1);return true;
    }
    public void serverTick(){
        if(level==null||level.isClientSide||!active()||level.getGameTime()%5!=0)return;
        if(!(getItem(0).getItem() instanceof ItemFocusBasic focus)||!ItemStack.isSameItemSameComponents(getItem(0),expected)){cancel();return;}
        boolean changed=false;for(var entry:new ArrayList<>(remaining.entrySet())){int taken=VisNetHandler.drainVis(level,worldPosition,entry.getKey(),Math.min(100,entry.getValue()));if(taken>0){remaining.compute(entry.getKey(),(aspect,amount)->amount<=taken?null:amount-taken);changed=true;}}
        if(remaining.isEmpty()){var choice=FocusUpgradeType.get(upgrade);boolean applied=choice!=null&&focus.applyUpgrade(getItem(0),choice,rank);level.playSound(null,worldPosition,applied?R2Sounds.CRAFT_FINISH.get():R2Sounds.CRAFT_FAIL.get(),net.minecraft.sounds.SoundSource.BLOCKS,1,1);total=0;upgrade=-1;rank=0;expected=ItemStack.EMPTY;changed=true;}
        if(changed)changed();
    }
    private void cancel(){remaining.clear();total=0;rank=0;upgrade=-1;expected=ItemStack.EMPTY;changed();if(level!=null)level.playSound(null,worldPosition,R2Sounds.CRAFT_FAIL.get(),net.minecraft.sounds.SoundSource.BLOCKS,.33f,1);}
    @Override public boolean canPlaceItem(int slot,ItemStack stack){return !active()&&stack.getItem() instanceof ItemFocusBasic;}
    @Override public boolean canPlaceItemThroughFace(int slot,ItemStack stack,Direction side){return isEmpty()&&canPlaceItem(slot,stack);}
    @Override public boolean canTakeItemThroughFace(int slot,ItemStack stack,Direction side){return !active();}
    @Override protected void saveAdditional(CompoundTag tag,HolderLookup.Provider registries){super.saveAdditional(tag,registries);var values=new CompoundTag();remaining.forEach((aspect,amount)->values.putInt(aspect.getTag(),amount));tag.put("RemainingVis",values);tag.putInt("Total",total);tag.putInt("Rank",rank);tag.putInt("Upgrade",upgrade);if(!expected.isEmpty())tag.put("Expected",expected.save(registries));}
    @Override protected void loadAdditional(CompoundTag tag,HolderLookup.Provider registries){super.loadAdditional(tag,registries);remaining.clear();var values=tag.getCompound("RemainingVis");for(var key:values.getAllKeys()){var aspect=Aspect.getAspect(key);int amount=values.getInt(key);if(aspect!=null&&aspect.isPrimal()&&amount>0&&amount<=100000)remaining.put(aspect,amount);}total=Math.max(0,tag.getInt("Total"));rank=tag.getInt("Rank");upgrade=tag.getInt("Upgrade");expected=ItemStack.parseOptional(registries,tag.getCompound("Expected"));if(rank<1||rank>5||FocusUpgradeType.get(upgrade)==null||expected.isEmpty()){total=0;remaining.clear();rank=0;upgrade=-1;}}
}
