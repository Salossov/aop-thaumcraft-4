package thaumcraft.api.wands;

import java.util.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;

public final class FocusBehaviors {
    public record Registration(FocusBehavior behavior,ResourceLocation model,ResourceLocation ornament){
        public Registration(FocusBehavior behavior,ResourceLocation model){this(behavior,model,null);}
    }
    private static final Map<Item,Registration> VALUES=new IdentityHashMap<>();
    public static void register(Item item,FocusBehavior behavior,ResourceLocation attachedModel){register(item,behavior,attachedModel,null);}
    public static synchronized void register(Item item,FocusBehavior behavior,ResourceLocation attachedModel,ResourceLocation ornament){if(!(item instanceof ItemFocusBasic))throw new IllegalArgumentException("Focus must extend ItemFocusBasic");if(VALUES.putIfAbsent(item,new Registration(Objects.requireNonNull(behavior),Objects.requireNonNull(attachedModel),ornament))!=null)throw new IllegalArgumentException("Duplicate focus behavior");}
    public static Collection<Registration> all(){return List.copyOf(VALUES.values());}
    public static Registration get(ItemStack focus){return VALUES.get(focus.getItem());}
    private FocusBehaviors(){}
}
