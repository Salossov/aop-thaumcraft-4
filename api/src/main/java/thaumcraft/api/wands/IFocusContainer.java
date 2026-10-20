package thaumcraft.api.wands;

import java.util.List;
import net.minecraft.world.item.ItemStack;

/** Focus-pouch identity and access for the wand selector, including larger addon
 * pouches. Implementing items are excluded from nesting in other focus pouches.
 * Inventories may contain other items; the selector exposes only ItemFocusBasic.
 */
public interface IFocusContainer {
    /** Matches the native ItemContainerContents slot limit. */
    int MAX_SLOTS=256;
    /** A detached snapshot of every slot, including empty and non-focus slots.
     * The size must remain stable for this container and be in [1, MAX_SLOTS].
     */
    List<ItemStack> getFocusInventory(ItemStack container);
    /** Replace one slot with an empty stack or one focus. Called on the server
     * game thread. Return false without mutation when the request is invalid;
     * return true only after committing a copy of the replacement.
     */
    boolean setFocus(ItemStack container,int slot,ItemStack replacement);
}
