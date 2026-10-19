package thaumcraft.items;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IAspectItem;
import thaumcraft.registry.R3Content;

/** Read-only filter identity; labels do not contain transferable essentia. */
public final class JarLabelItem extends Item implements IAspectItem {
    public JarLabelItem() { super(new Properties()); }
    @Override public AspectList getAspects(ItemStack stack) {
        var aspect = R3Content.aspect(stack);
        return aspect == null ? null : new AspectList().add(aspect, 1);
    }
}
