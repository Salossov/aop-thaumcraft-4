package thaumcraft.items;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import thaumcraft.api.aspects.*;
import thaumcraft.registry.R2Content;

public final class WispEssenceItem extends Item implements IAspectItem {
    public WispEssenceItem(Properties properties) { super(properties); }
    @Override public AspectList getAspects(ItemStack stack) { return aspects(stack); }
    @Override public void appendHoverText(ItemStack stack,TooltipContext context,java.util.List<net.minecraft.network.chat.Component> lines,TooltipFlag flag){for(var entry:aspects(stack).asMap().entrySet())lines.add(net.minecraft.network.chat.Component.literal(entry.getKey().getName()+" x"+entry.getValue()).withStyle(style->style.withColor(entry.getKey().getColor())));}
    public static ItemStack of(Aspect aspect) {
        var stack=new ItemStack(R2Content.WISP_ESSENCE.get());var data=new CompoundTag();
        data.putString("EssenceAspect",aspect.getTag());data.putInt("EssenceAmount",2);stack.set(DataComponents.CUSTOM_DATA,CustomData.of(data));return stack;
    }
    public static AspectList aspects(ItemStack stack) {
        var data=stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();
        var aspect=Aspect.getAspect(data.getString("EssenceAspect"));int amount=data.getInt("EssenceAmount");
        return aspect==null||amount<=0?new AspectList():new AspectList().add(aspect,Math.min(amount,64));
    }
}
