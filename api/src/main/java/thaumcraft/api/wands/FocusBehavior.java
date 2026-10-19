package thaumcraft.api.wands;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

/** Automatic effects run after vis reservation; false or an exception refunds it. */
public interface FocusBehavior {
    /** Delayed/conditional effects own their affordability checks and payment through WandApi. */
    default boolean managesVis(){return false;}
    default boolean continuous(ItemStack focus){return false;}
    default boolean begin(ServerPlayer player,ItemStack wand,ItemStack focus,InteractionHand hand){return true;}
    boolean cast(ServerPlayer player,ItemStack wand,ItemStack focus);
    default void end(ServerPlayer player,ItemStack wand,ItemStack focus){}
}
