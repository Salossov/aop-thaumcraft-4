package thaumcraft.production;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.*;
import net.minecraft.world.*;

/** Vanilla indexed container format; older development saves used packed SimpleContainer lists. */
public final class ContainerNbt {
    public static ListTag save(SimpleContainer container,HolderLookup.Provider registries){
        var tag=new CompoundTag();ContainerHelper.saveAllItems(tag,container.getItems(),registries);return tag.getList("Items",Tag.TAG_COMPOUND);
    }
    public static void load(SimpleContainer container,ListTag items,HolderLookup.Provider registries){
        container.clearContent();
        if(items.isEmpty())return;
        if(!items.getCompound(0).contains("Slot")){container.fromTag(items,registries);return;}
        var tag=new CompoundTag();tag.put("Items",items);ContainerHelper.loadAllItems(tag,container.getItems(),registries);container.setChanged();
    }
    private ContainerNbt(){}
}
