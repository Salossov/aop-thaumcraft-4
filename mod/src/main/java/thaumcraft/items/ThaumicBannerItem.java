package thaumcraft.items;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.registry.R4Content;

/** One original item identity, with colour and aspect in native item components. */
public final class ThaumicBannerItem extends BlockItem {
    public static final String[] COLORS={"white","orange","magenta","light_blue","yellow","lime","pink","gray","light_gray","cyan","purple","blue","brown","green","red","black"};
    public static final int[] RGB={0xF0F0F0,15435844,12801229,6719955,14602026,4312372,14188952,0x434343,0xA0A0A0,2651799,8073150,2437522,5320730,3887386,11743532,0x1E1B1B};
    public ThaumicBannerItem(){super(R4Content.BANNER.get(),new Properties());}
    public static int color(ItemStack stack){var tag=stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();int c=tag.contains("color",99)?tag.getByte("color"):-1;return c>=0&&c<16?c:-1;}
    public static Aspect aspect(ItemStack stack){return Aspect.getAspect(stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getString("aspect"));}
    public static ItemStack stack(int color,Aspect aspect){var stack=new ItemStack(R4Content.BANNER_ITEM.get());if(color>=0||aspect!=null)CustomData.update(DataComponents.CUSTOM_DATA,stack,t->{t.putByte("color",(byte)color);if(aspect!=null)t.putString("aspect",aspect.getTag());});return stack;}
    @Override public Component getName(ItemStack stack){int color=color(stack);return Component.translatable(color<0?"block.thaumcraft.cultist_banner":"block.thaumcraft.banner."+COLORS[color]);}
}
