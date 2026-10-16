package thaumcraft.items;

import net.minecraft.world.item.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.network.chat.Component;

public final class NodeJarItem extends BlockItem {
    public NodeJarItem(thaumcraft.blocks.JarredNodeBlock block){super(block,new Properties().stacksTo(1));}
    @Override public void appendHoverText(ItemStack stack,TooltipContext context,java.util.List<Component> lines,TooltipFlag flags){
        super.appendHoverText(stack,context,lines,flags);var tag=stack.getOrDefault(DataComponents.BLOCK_ENTITY_DATA,CustomData.EMPTY).copyTag();
        if(tag.contains("nodeType"))lines.add(Component.translatable("nodetype."+tag.getString("nodeType")+".name").withStyle(net.minecraft.ChatFormatting.GRAY));
        if(tag.contains("modifier")&&!tag.getString("modifier").equals("NONE"))lines.add(Component.translatable("nodemod."+tag.getString("modifier")+".name").withStyle(net.minecraft.ChatFormatting.GRAY));
        var vis=tag.getCompound("vis");for(var key:vis.getAllKeys()){var aspect=thaumcraft.api.aspects.Aspect.getAspect(key);if(aspect!=null)lines.add(Component.literal(aspect.getName()+" × "+vis.getInt(key)).withStyle(net.minecraft.ChatFormatting.DARK_AQUA));}
    }
}
