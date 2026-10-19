package thaumcraft.api.wands;

import java.util.*;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import thaumcraft.api.aspects.Aspect;

public abstract class ItemFocusBasic extends Item {
    protected ItemFocusBasic(){this(new Properties().stacksTo(1).rarity(Rarity.RARE));}
    protected ItemFocusBasic(Properties properties){super(properties);}
    public abstract Map<Aspect,Integer> getVisCost(ItemStack stack);
    public boolean isVisCostPerTick(ItemStack stack){return false;}
    public int getActivationCooldown(ItemStack stack){return 0;}
    public int getFocusColor(ItemStack stack){return 0xffffff;}
    /** Optional original selector grouping key. null keeps the core's native grouping. */
    public String getSortingHelper(ItemStack stack){return null;}
    public abstract List<FocusUpgradeType> getPossibleUpgradesByRank(ItemStack stack,int rank);
    public int[] getAppliedUpgrades(ItemStack stack){int[] result={-1,-1,-1,-1,-1};var stored=stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getIntArray("FocusUpgrades");System.arraycopy(stored,0,result,0,Math.min(5,stored.length));return result;}
    public int getUpgradeLevel(ItemStack stack,FocusUpgradeType upgrade){return (int)Arrays.stream(getAppliedUpgrades(stack)).filter(id->id==upgrade.id()).count();}
    public boolean isUpgradedWith(ItemStack stack,FocusUpgradeType upgrade){return getUpgradeLevel(stack,upgrade)>0;}
    public boolean canApplyUpgrade(ItemStack stack,FocusUpgradeType upgrade,int rank){return getPossibleUpgradesByRank(stack,rank).contains(upgrade);}
    public boolean applyUpgrade(ItemStack stack,FocusUpgradeType upgrade,int rank){if(rank<1||rank>5)return false;var upgrades=getAppliedUpgrades(stack);if(upgrades[rank-1]!=-1||!canApplyUpgrade(stack,upgrade,rank))return false;for(int i=0;i<rank-1;i++)if(upgrades[i]<0)return false;upgrades[rank-1]=upgrade.id();CustomData.update(DataComponents.CUSTOM_DATA,stack,tag->tag.putIntArray("FocusUpgrades",upgrades));return true;}
    @Override public void appendHoverText(ItemStack stack,TooltipContext context,List<Component> lines,TooltipFlag flag){super.appendHoverText(stack,context,lines,flag);lines.add(Component.translatable(isVisCostPerTick(stack)?"item.Focus.cost2":"item.Focus.cost1"));for(var entry:getVisCost(stack).entrySet().stream().sorted(Map.Entry.comparingByKey(Comparator.comparing(Aspect::getTag))).toList()){var aspect=entry.getKey();var color=ChatFormatting.getByCode(aspect.getChatColor().charAt(0));lines.add(Component.literal(" ").append(Component.literal(aspect.getName()).withStyle(color==null?ChatFormatting.WHITE:color)).append(" x "+new java.text.DecimalFormat("#####.##").format(entry.getValue()/100f)));}var counts=new LinkedHashMap<Integer,Integer>();for(int id:getAppliedUpgrades(stack))if(FocusUpgradeType.get(id)!=null)counts.merge(id,1,Integer::sum);counts.forEach((id,count)->lines.add(Component.translatable(FocusUpgradeType.get(id).name()).append(count>1?Component.literal(" ").append(Component.translatable("enchantment.level."+count)):Component.empty()).withStyle(ChatFormatting.DARK_PURPLE)));}
}
