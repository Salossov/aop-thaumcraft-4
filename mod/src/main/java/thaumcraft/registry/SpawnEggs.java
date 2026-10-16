package thaumcraft.registry;

import java.util.List;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import thaumcraft.entities.R2Entities;
import thaumcraft.entities.R3Entities;
import thaumcraft.entities.R5Entities;

/** The 29 TC4 ItemSpawnerEgg mappings, retaining their original two tint colors. */
public final class SpawnEggs {
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems("thaumcraft");
    public static final List<DeferredItem<DeferredSpawnEggItem>> ALL = List.of(
            egg(R2Entities.BRAINY, 0xFFC0FF, 0x008000),
            egg(R2Entities.GIANT_BRAINY, 0xFFC0FF, 0x004000),
            egg(R2Entities.WISP, 0xFFC0FF, 0xFFFFFF),
            egg(R3Entities.FIREBAT, 0xFFC0FF, 0xC00000),
            egg(R5Entities.PECH, 0xFFC0FF, 0x400040),
            egg(R3Entities.MIND_SPIDER, 0xAAAAAA, 0x404040),
            egg(R2Entities.ELDRITCH_GUARDIAN, 0x222222, 0x404040),
            egg(R5Entities.ELDRITCH_WARDEN, 0x552222, 0x404040),
            egg(R5Entities.CULTIST_KNIGHT, 0xFF5055, 0x000080),
            egg(R5Entities.CULTIST_CLERIC, 0xFF5055, 0x800000),
            egg(R5Entities.CULTIST_LEADER, 0xFF5055, 0x505050),
            egg(R5Entities.CULTIST_PORTAL, 0xFF5055, 0xFF50FF),
            egg(R5Entities.ELDRITCH_GOLEM, 0x555555, 0x404040),
            egg(R5Entities.CRAB, 0x555555, 0x550000),
            egg(R5Entities.INHABITED_ZOMBIE, 0x557755, 0x550000),
            egg(R2Entities.THAUMIC_SLIME, 0xFFC0FF, 0xFF80FF),
            egg(R2Entities.TAINT_SPIDER, 0xFFC0FF, 0x404040),
            egg(R2Entities.TAINTACLE, 0xFFC0FF, 0x800080),
            egg(R2Entities.TAINTACLE_SMALL, 0xFFC0FF, 0x800090),
            egg(R2Entities.TAINT_SPORE, 0xFFC0FF, 0x800070),
            egg(R2Entities.TAINT_SWARMER, 0xFFC0FF, 0x800060),
            egg(R2Entities.TAINT_SWARM, 0xFFC0FF, 0x800050),
            egg(R2Entities.TAINT_CHICKEN, 0xFFC0FF, 0xC0C0C0),
            egg(R2Entities.TAINT_COW, 0xFFC0FF, 0x7E3A3B),
            egg(R2Entities.TAINT_CREEPER, 0xFFC0FF, 0x00FF00),
            egg(R2Entities.TAINT_PIG, 0xFFC0FF, 0xEF99EF),
            egg(R2Entities.TAINT_SHEEP, 0xFFC0FF, 0x808080),
            egg(R2Entities.TAINT_VILLAGER, 0xFFC0FF, 0x00FFFF),
            egg(R5Entities.TAINTACLE_GIANT, 0xFFC0FF, 0x808080));

    private static <T extends Mob> DeferredItem<DeferredSpawnEggItem> egg(
            DeferredHolder<EntityType<?>, EntityType<T>> type, int background, int highlight) {
        return ITEMS.register(type.getId().getPath() + "_spawn_egg",
                () -> new DeferredSpawnEggItem(type, background, highlight, new Item.Properties()));
    }

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
        bus.addListener(SpawnEggs::vanillaTab);
    }

    public static void creative(CreativeModeTab.Output output) {
        for (var egg : ALL) output.accept(egg.get());
    }

    private static void vanillaTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey().equals(CreativeModeTabs.SPAWN_EGGS)) creative(event);
    }

    private SpawnEggs() {}
}
