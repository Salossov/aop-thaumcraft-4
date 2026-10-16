package thaumcraft.items;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import thaumcraft.registry.R3Content;

public final class EssentiaItem extends Item {
    private final int units;
    public EssentiaItem(int units) { super(new Properties()); this.units = units; }
    @Override public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flags) {
        var aspect = R3Content.aspect(stack);
        if (aspect != null) {
            lines.add(Component.literal(aspect.getName() + " x" + units).withStyle(style -> style.withColor(aspect.getColor())));
            lines.add(Component.translatable("tc.aspect." + aspect.getTag()).withStyle(ChatFormatting.GRAY));
        }
    }
}
