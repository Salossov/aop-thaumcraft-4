package thaumcraft.client;
import net.minecraft.world.item.ItemStack;
import thaumcraft.registry.*;
public final class CreativeSectionIcons {
    public static ItemStack section(int section){String id=switch(section){case 0->"thaumium_ingot";case 1->"iron_capped_wooden_wand";case 2->"goggles_of_revealing";case 3->"warded_jar";case 4->"infusion_matrix";case 5->"golemancers_bell";case 6->"arcane_stone";default->"crimson_rites";};return new ItemStack(net.minecraft.core.registries.BuiltInRegistries.ITEM.get(net.minecraft.resources.ResourceLocation.parse("thaumcraft:"+id)));}
    public static ItemStack tab(){return new ItemStack(switch(thaumcraft.config.ThaumcraftClientConfig.CREATIVE_TAB_ICON.get()){case WAND->R2Content.WAND.get();case THAUMONOMICON->R2Content.BOOK.get();case THAUMOMETER->R2Content.THAUMOMETER.get();});}
    private CreativeSectionIcons(){}
}
