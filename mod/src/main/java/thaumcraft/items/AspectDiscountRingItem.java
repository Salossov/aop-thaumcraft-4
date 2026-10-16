package thaumcraft.items;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import thaumcraft.api.*;
import thaumcraft.api.aspects.Aspect;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

/** Original loot-only iron rings: one percent discount to exactly one primal aspect. */
public final class AspectDiscountRingItem extends Item implements ICurioItem,IVisDiscountGear,IRunicArmor {
    private final Aspect aspect;
    public AspectDiscountRingItem(Aspect aspect){super(new Properties().stacksTo(1));this.aspect=aspect;}
    public Aspect aspect(){return aspect;}
    @Override public int getVisDiscount(ItemStack stack,Player player,Aspect queried){return aspect==queried?1:0;}
    @Override public int getRunicCharge(ItemStack stack){return 0;}
    @Override public void appendHoverText(ItemStack stack,TooltipContext c,List<Component> lines,TooltipFlag f){super.appendHoverText(stack,c,lines,f);lines.add(Component.literal(aspect.getName()+" ").append(Component.translatable("tc.discount")).append(": 1%").withStyle(ChatFormatting.DARK_PURPLE));}
}
