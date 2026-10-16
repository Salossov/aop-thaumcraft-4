package thaumcraft.items;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;

public final class EquippedCurios {
    public static List<ItemStack> stacks(LivingEntity entity) {
        var result = new ArrayList<ItemStack>();
        CuriosApi.getCuriosInventory(entity).ifPresent(inventory -> {
            for (var slot : inventory.findCurios("necklace", "ring", "belt")) {
                if (inventory.isSlotActive(slot.slotContext().identifier(), slot.slotContext().index())) result.add(slot.stack());
            }
        });
        return result;
    }
    public static int count(LivingEntity entity, ThaumicCurioItem.Kind kind) {
        return (int) stacks(entity).stream().filter(s -> s.getItem() instanceof ThaumicCurioItem item && item.kind() == kind).count();
    }
    private EquippedCurios() {}
}
