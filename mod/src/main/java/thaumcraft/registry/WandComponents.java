package thaumcraft.registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.*;
import net.neoforged.bus.api.IEventBus;
import thaumcraft.api.wands.WandParts;
public final class WandComponents {
    private static final DeferredRegister<DataComponentType<?>> TYPES=DeferredRegister.create(Registries.DATA_COMPONENT_TYPE,"thaumcraft");
    public static final DeferredHolder<DataComponentType<?>,DataComponentType<WandParts>> PARTS=TYPES.register("wand_parts",()->DataComponentType.<WandParts>builder().persistent(WandParts.CODEC).networkSynchronized(WandParts.STREAM_CODEC).build());
    public static final DeferredHolder<DataComponentType<?>,DataComponentType<net.minecraft.world.item.component.ItemContainerContents>> FOCUS=TYPES.register("wand_focus",()->DataComponentType.<net.minecraft.world.item.component.ItemContainerContents>builder().persistent(net.minecraft.world.item.component.ItemContainerContents.CODEC).networkSynchronized(net.minecraft.world.item.component.ItemContainerContents.STREAM_CODEC).build());
    public static void register(IEventBus bus){TYPES.register(bus);}
    private WandComponents(){}
}
