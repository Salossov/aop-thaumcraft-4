package thaumcraft.blocks;

import java.util.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.storage.loot.*;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import thaumcraft.registry.R2Content;

public final class InfusedStoneBlock extends DropExperienceBlock {
    private final int element;
    public InfusedStoneBlock(int element) { super(UniformInt.of(0,3),Properties.of().mapColor(MapColor.STONE).strength(1.5f,5).sound(SoundType.STONE).requiresCorrectToolForDrops());this.element=element; }
    @Override protected List<ItemStack> getDrops(BlockState state,LootParams.Builder builder) {
        var tool=builder.getOptionalParameter(LootContextParams.TOOL);var enchantments=builder.getLevel().registryAccess().registryOrThrow(Registries.ENCHANTMENT);
        if(tool!=null&&EnchantmentHelper.getItemEnchantmentLevel(enchantments.getHolderOrThrow(Enchantments.SILK_TOUCH),tool)>0) return List.of(new ItemStack(asItem()));
        int fortune=tool==null?0:EnchantmentHelper.getItemEnchantmentLevel(enchantments.getHolderOrThrow(Enchantments.FORTUNE),tool);
        int amount=1+builder.getLevel().random.nextInt(2+fortune);var result=new ArrayList<ItemStack>();
        for(int i=0;i<amount;i++) result.add(new ItemStack(R2Content.SHARDS.get(element).get()));return result;
    }
}
