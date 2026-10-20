package thaumcraft.api.client;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import thaumcraft.api.wands.WandApi;

/** Local visual callbacks for actual continuous wand use. No server effects or vis payment. */
public final class FocusClientVisuals {
    @FunctionalInterface public interface Tick {
        void render(Player player,ItemStack wand,ItemStack focus,int remaining);
    }
    private record Active(ItemStack wand,ItemStack focus,InteractionHand hand,Tick tick){}
    private static final Map<Item,Tick> VISUALS=new IdentityHashMap<>();
    private static final Map<Player,Active> ACTIVE=new WeakHashMap<>();
    public static synchronized void register(Item focus,Tick tick){
        if(VISUALS.putIfAbsent(Objects.requireNonNull(focus),Objects.requireNonNull(tick))!=null)
            throw new IllegalArgumentException("Duplicate focus client visual");
    }
    /** Called by the wand only after selecting focus use, rather than a device interaction. */
    public static void begin(Player player,ItemStack wand,InteractionHand hand){
        if(!player.level().isClientSide())return;
        var focus=WandApi.focus(wand);var tick=VISUALS.get(focus.getItem());
        if(tick==null)ACTIVE.remove(player);else ACTIVE.put(player,new Active(wand,focus.copy(),hand,tick));
    }
    public static void tick(Player player,ItemStack wand,int remaining){
        if(!player.level().isClientSide())return;
        var active=ACTIVE.get(player);if(active==null)return;
        var focus=WandApi.focus(wand);
        // Native synchronization replaces a client's held stack when server vis
        // changes. LivingEntity.canContinueUsing accepts the same item; keep
        // its visual session across that replacement, but not a changed focus.
        if(!ItemStack.isSameItem(active.wand(),wand)||!ItemStack.isSameItemSameComponents(active.focus(),focus)
                ||!player.isUsingItem()||player.getUseItem()!=wand||player.getUsedItemHand()!=active.hand()){ACTIVE.remove(player);return;}
        // Callbacks receive copies so a visual cannot mutate the authoritative held components.
        active.tick().render(player,wand.copy(),focus.copy(),remaining);
    }
    public static void end(Player player){ACTIVE.remove(player);}
    private FocusClientVisuals(){}
}
