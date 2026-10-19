package thaumcraft.api.aspects;

import java.util.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class AspectApi {
    public interface Access {Map<String,Integer> aspects(Level level,ItemStack stack);boolean scanned(ItemStack stack);
        default List<ItemStack> variants(){return List.of();}
        default ItemStack wispEssence(Aspect aspect){return ItemStack.EMPTY;}
        default Map<String,Integer> aspects(Level level,net.minecraft.world.level.block.state.BlockState state){return Map.of();}
        default Map<String,Integer> aspects(net.minecraft.world.entity.Entity entity){return Map.of();}}
    private static Access access;
    public static void install(Access value){if(access!=null)throw new IllegalStateException("Aspect API already installed");access=Objects.requireNonNull(value);}
    public static Map<String,Integer> aspects(Level level,ItemStack stack){return Map.copyOf(access.aspects(level,stack.copy()));}
    public static Map<String,Integer> aspects(Level level,net.minecraft.world.level.block.state.BlockState state){return Map.copyOf(access.aspects(level,state));}
    public static Map<String,Integer> aspects(net.minecraft.world.entity.Entity entity){return Map.copyOf(access.aspects(entity));}
    /** Filled phials, jars, crystals and essence pieces; copies safe for viewer indexing. */
    public static List<ItemStack> variants(){return access==null?List.of():access.variants().stream().map(ItemStack::copy).toList();}
    /** Fresh native two-unit wisp essence, using the core's real stack components. */
    public static ItemStack wispEssence(Aspect aspect){return access==null?ItemStack.EMPTY:access.wispEssence(Objects.requireNonNull(aspect)).copy();}
    public static boolean scanned(ItemStack stack){return access!=null&&access.scanned(stack.copy());}
    private AspectApi(){}
}
