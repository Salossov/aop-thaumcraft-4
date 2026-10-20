package thaumcraft.items;

import java.util.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.wands.*;
import thaumcraft.registry.WandComponents;

/** The wand owns its focus stack. Inventory changes are committed on the server. */
public final class WandFocus {
    public record Entry(int slot,ItemStack stack){}
    private static final int POUCH_BASE=1<<16,CURIOS_BASE=1<<24,STRIDE=IFocusContainer.MAX_SLOTS;
    public static List<Entry> choices(Player player) {
        var entries=new ArrayList<Entry>();
        for(int slot=0;slot<player.getInventory().getContainerSize();slot++) {
            var stack=player.getInventory().getItem(slot);
            if(stack.getItem() instanceof ItemFocusBasic)entries.add(new Entry(slot,stack.copy()));
            else if(slot<(CURIOS_BASE-POUCH_BASE)/STRIDE)addPouch(entries,stack,POUCH_BASE+slot*STRIDE);
        }
        var curios=EquippedCurios.stacks(player);
        for(int slot=0;slot<curios.size()&&slot<=(Integer.MAX_VALUE-CURIOS_BASE-(STRIDE-1))/STRIDE;slot++)addPouch(entries,curios.get(slot),CURIOS_BASE+slot*STRIDE);
        return List.copyOf(entries);
    }
    private static void addPouch(List<Entry> entries,ItemStack pouch,int base) {
        if(!(pouch.getItem() instanceof IFocusContainer container))return;
        var items=container.getFocusInventory(pouch);
        if(items.size()<1||items.size()>STRIDE)return;
        for(int slot=0;slot<items.size();slot++)if(items.get(slot).getItem() instanceof ItemFocusBasic&&items.get(slot).getCount()==1)entries.add(new Entry(base+slot,items.get(slot).copy()));
    }
    public static ItemStack get(ItemStack wand){var contents=wand.getOrDefault(WandComponents.FOCUS.get(),net.minecraft.world.item.component.ItemContainerContents.EMPTY);return contents.getSlots()>0?contents.getStackInSlot(0):ItemStack.EMPTY;}
    public static boolean supports(ItemStack wand){return wand.getItem() instanceof CastingWandItem item&&item.kind()!=CastingWandItem.Kind.SCEPTRE;}
    public static void set(ItemStack wand,ItemStack focus){if(focus.isEmpty())wand.remove(WandComponents.FOCUS.get());else if(supports(wand)&&focus.getItem() instanceof ItemFocusBasic)wand.set(WandComponents.FOCUS.get(),net.minecraft.world.item.component.ItemContainerContents.fromItems(java.util.List.of(focus.copyWithCount(1))));else throw new IllegalArgumentException("Invalid wand focus");}
    public static boolean select(ServerPlayer player,InteractionHand hand,int slot){
        var wand=player.getItemInHand(hand);if(!supports(wand)||player.isUsingItem()||player.containerMenu!=player.inventoryMenu)return false;
        var current=get(wand);if(slot==-1){if(current.isEmpty())return false;var returned=current.copy();if(!player.getInventory().add(returned))return false;set(wand,ItemStack.EMPTY);return true;}
        if(slot>=POUCH_BASE) {
            int base=slot>=CURIOS_BASE?CURIOS_BASE:POUCH_BASE,index=(slot-base)/STRIDE,inside=(slot-base)%STRIDE;
            ItemStack pouch;
            if(base==CURIOS_BASE) {var curios=EquippedCurios.stacks(player);if(index>=curios.size())return false;pouch=curios.get(index);}
            else {if(index>=player.getInventory().getContainerSize())return false;pouch=player.getInventory().getItem(index);}
            if(!(pouch.getItem() instanceof IFocusContainer container))return false;
            var items=container.getFocusInventory(pouch);
            if(items.size()<1||items.size()>STRIDE||inside>=items.size())return false;
            var replacement=items.get(inside);
            if(!(replacement.getItem() instanceof ItemFocusBasic)||replacement.getCount()!=1||!container.setFocus(pouch,inside,current.copy()))return false;
            set(wand,replacement);player.getInventory().setChanged();player.containerMenu.broadcastChanges();return true;
        }
        if(slot<0||slot>=player.getInventory().getContainerSize()||hand==InteractionHand.MAIN_HAND&&slot==player.getInventory().selected)return false;
        var replacement=player.getInventory().getItem(slot);if(!(replacement.getItem() instanceof ItemFocusBasic)||replacement.getCount()!=1)return false;
        set(wand,replacement);player.getInventory().setItem(slot,current.copy());player.getInventory().setChanged();player.containerMenu.broadcastChanges();return true;
    }
    public static Map<Aspect,Integer> costs(ItemStack wand,Player player,Map<Aspect,Integer> base){
        var focus=get(wand);int frugal=focus.getItem() instanceof ItemFocusBasic item?item.getUpgradeLevel(focus,FocusUpgradeType.frugal):0;
        var result=new LinkedHashMap<Aspect,Integer>();base.forEach((aspect,amount)->{if(aspect==null||!aspect.isPrimal()||amount<0)throw new IllegalArgumentException("Invalid focus cost");result.put(aspect,(int)(amount*WandMaterials.modifier(wand,aspect,WandVis.discount(player,aspect)+frugal*.1f)));});return result;
    }
    public static boolean consume(ItemStack wand,Player player,Map<Aspect,Integer> base,boolean commit){
        if(!supports(wand)||get(wand).isEmpty())return false;var costs=costs(wand,player,base);for(var entry:costs.entrySet())if(WandVis.get(wand,entry.getKey())<entry.getValue())return false;
        if(commit)costs.forEach((aspect,amount)->WandVis.set(wand,aspect,WandVis.get(wand,aspect)-amount));return true;
    }
    private WandFocus(){}
}
