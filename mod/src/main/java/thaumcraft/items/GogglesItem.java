package thaumcraft.items;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;

/** Original helmet stats come from its material; TC4 also displayed its 5% discount. */
public final class GogglesItem extends ArmorItem implements thaumcraft.api.IRepairable,thaumcraft.api.IRunicArmor,thaumcraft.api.IVisDiscountGear,thaumcraft.api.IGoggles,thaumcraft.api.nodes.IRevealer {
    public GogglesItem(Holder<ArmorMaterial> material,Properties properties) {super(material,Type.HELMET,properties);}
    public int getRunicCharge(ItemStack stack){return 0;}
    public boolean showNodes(ItemStack stack,net.minecraft.world.entity.LivingEntity wearer){return true;}
    public boolean showIngamePopups(ItemStack stack,net.minecraft.world.entity.LivingEntity wearer){return true;}
    public int getVisDiscount(ItemStack stack,net.minecraft.world.entity.player.Player player,thaumcraft.api.aspects.Aspect aspect){return 5;}
    @Override public void appendHoverText(ItemStack stack,TooltipContext context,List<Component> lines,TooltipFlag flags) {
        super.appendHoverText(stack,context,lines,flags);
        lines.add(Component.translatable("tc.visdiscount").append(": 5%").withStyle(ChatFormatting.DARK_PURPLE));
    }
}
