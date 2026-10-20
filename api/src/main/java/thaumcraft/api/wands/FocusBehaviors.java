package thaumcraft.api.wands;

import java.util.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;

public final class FocusBehaviors {
    public record Registration(FocusBehavior behavior,ResourceLocation model,ResourceLocation ornament,ResourceLocation staffModel){
        public Registration(FocusBehavior behavior,ResourceLocation model,ResourceLocation ornament){this(behavior,model,ornament,null);}
        public Registration(FocusBehavior behavior,ResourceLocation model){this(behavior,model,null,null);}
        /** An explicit staff model uses staff-base coordinates; null retains the legacy wand-model fallback. */
        public ResourceLocation modelFor(boolean staff){return staff&&staffModel!=null?staffModel:model;}
    }
    private static final Map<Item,Registration> VALUES=new IdentityHashMap<>();
    public static void register(Item item,FocusBehavior behavior,ResourceLocation attachedModel){register(item,behavior,attachedModel,null);}
    public static void register(Item item,FocusBehavior behavior,ResourceLocation attachedModel,ResourceLocation ornament){register(item,behavior,attachedModel,ornament,null);}
    /** Register separate attached geometry where a staff needs different shell and inner-layer proportions. */
    public static synchronized void register(Item item,FocusBehavior behavior,ResourceLocation attachedModel,ResourceLocation ornament,ResourceLocation staffModel){if(!(item instanceof ItemFocusBasic))throw new IllegalArgumentException("Focus must extend ItemFocusBasic");if(VALUES.putIfAbsent(item,new Registration(Objects.requireNonNull(behavior),Objects.requireNonNull(attachedModel),ornament,staffModel))!=null)throw new IllegalArgumentException("Duplicate focus behavior");}
    public static Collection<Registration> all(){return List.copyOf(VALUES.values());}
    public static Registration get(ItemStack focus){return VALUES.get(focus.getItem());}
    private FocusBehaviors(){}
}
