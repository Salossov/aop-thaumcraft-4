package thaumcraft.api;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import thaumcraft.api.nodes.IRevealer;

/** Original render hooks inspect the head slot; these queries are also available on the server. */
public final class EquipmentVision {
    public static boolean canSeeNodes(LivingEntity wearer) {
        if (wearer == null) return false;
        var stack = wearer.getItemBySlot(EquipmentSlot.HEAD);
        return stack.getItem() instanceof IRevealer item && item.showNodes(stack, wearer);
    }
    public static boolean canSeeAspects(LivingEntity wearer) {
        if (wearer == null) return false;
        var stack = wearer.getItemBySlot(EquipmentSlot.HEAD);
        return stack.getItem() instanceof IGoggles item && item.showIngamePopups(stack, wearer);
    }
    private EquipmentVision() { }
}
