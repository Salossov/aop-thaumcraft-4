package thaumcraft.items;

import java.util.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.BlockItemStateProperties;
import thaumcraft.blocks.AncientLootBlock;

public final class AncientLootBlockItem extends BlockItem {
    public AncientLootBlockItem(AncientLootBlock block){super(block,new Properties());}
    public static ItemStack stack(AncientLootBlock block,int rarity){var s=new ItemStack(block);s.set(DataComponents.BLOCK_STATE,new BlockItemStateProperties(Map.of("rarity",Integer.toString(rarity))));s.set(DataComponents.RARITY,rarity==2?Rarity.RARE:rarity==1?Rarity.UNCOMMON:Rarity.COMMON);return s;}
    @Override public void appendHoverText(ItemStack s,TooltipContext c,List<Component> lines,TooltipFlag f){super.appendHoverText(s,c,lines,f);var value=s.getOrDefault(DataComponents.BLOCK_STATE,BlockItemStateProperties.EMPTY).properties().getOrDefault("rarity","0");lines.add(Component.translatable("thaumcraft.loot.rarity."+value));}
}
