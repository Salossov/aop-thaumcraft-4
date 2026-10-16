package thaumcraft.api.wands;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;

/** Add-ons register rods during common setup, after item registration. */
public class WandRod {
    public static final Map<String, WandRod> rods = new LinkedHashMap<>();
    private final String tag;
    private final int capacity, craftCost;
    private final Supplier<ItemStack> item;
    private final java.util.function.BiConsumer<ItemStack, Player> onUpdate;
    public WandRod(String tag, int capacity, Supplier<ItemStack> item, int craftCost,
                   java.util.function.BiConsumer<ItemStack, Player> onUpdate) {
        if (capacity < 1 || craftCost < 1 || rods.containsKey(tag)) throw new IllegalArgumentException("Invalid or duplicate rod " + tag);
        this.tag=tag; this.capacity=capacity; this.item=item; this.craftCost=craftCost; this.onUpdate=onUpdate; rods.put(tag,this);
    }
    public String getTag(){return tag;}
    public int getCapacity(){return capacity;}
    public int getCraftCost(){return craftCost;}
    public ItemStack getItem(){return item.get();}
    public String getResearch(){return "ROD_"+tag;}
    public ResourceLocation getTexture(){return ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/models/wand_rod_"+tag.replace("_staff", "")+".png");}
    public void onUpdate(ItemStack stack,Player player){if(onUpdate!=null)onUpdate.accept(stack,player);}
}
