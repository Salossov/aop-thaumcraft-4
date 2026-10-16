package thaumcraft.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import thaumcraft.api.ThaumcraftIds;

public final class ThaumcraftContent {
    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ThaumcraftIds.MOD_ID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ThaumcraftIds.MOD_ID);
    private static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ThaumcraftIds.MOD_ID);

    // TC4 BlockCosmeticSolid metadata 4: hardness 4, explosion resistance 20, stone sound.
    public static final DeferredBlock<Block> THAUMIUM_BLOCK = BLOCKS.registerSimpleBlock(
            ThaumcraftIds.THAUMIUM_BLOCK,
            BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(4.0F, 20.0F)
                    .sound(SoundType.STONE).requiresCorrectToolForDrops());
    public static final DeferredItem<BlockItem> THAUMIUM_BLOCK_ITEM =
            ITEMS.registerSimpleBlockItem(ThaumcraftIds.THAUMIUM_BLOCK, THAUMIUM_BLOCK);
    // TC4 ItemResource metadata 2. Gameplay recipes are added by later milestones.
    public static final DeferredItem<Item> THAUMIUM_INGOT =
            ITEMS.registerSimpleItem(ThaumcraftIds.THAUMIUM_INGOT, new Item.Properties());

    private static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB = TABS.register(
            "thaumcraft", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.thaumcraft"))
                    .withSearchBar(80).hideTitle()
                    .icon(() -> R2Content.WAND.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        // Collect everything, then emit grouped like the original TC4 tab
                        // instead of raw registration order.
                        var all = new java.util.ArrayList<net.minecraft.world.item.ItemStack>();
                        CreativeModeTab.Output collect = (stack, visibility) -> { if (visibility == CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS) all.add(stack); };
                        collect.accept(THAUMIUM_INGOT.get().getDefaultInstance(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
                        collect.accept(THAUMIUM_BLOCK_ITEM.get().getDefaultInstance(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
                        R2Content.creative(collect);
                        R3Content.creative(collect);
                        R3Equipment.creative(collect);
                        R4Content.creative(collect);
                        R5Content.creative(collect);
                        SpawnEggs.creative(collect);
                        all.sort(java.util.Comparator.comparingInt(ThaumcraftContent::group).thenComparingInt(ThaumcraftContent::subgroup).thenComparing(s->net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(s.getItem()).getPath()).thenComparing(ThaumcraftContent::variant));
                        for (var stack : all) output.accept(stack);
                    }).build());

    /** Materials, implements, equipment, alchemy, infusion, golems, building and forbidden content. */
    public static int section(net.minecraft.world.item.ItemStack stack){return group(stack);}
    private static int group(net.minecraft.world.item.ItemStack stack){
        var item=stack.getItem();String id=net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item).getPath();
        if(item instanceof net.minecraft.world.item.SpawnEggItem)return 7;
        if(id.startsWith("eldritch_")||id.contains("crimson")||id.contains("cultist")||id.startsWith("ancient_")||id.contains("primordial")||id.contains("void_")&&!id.equals("void_jar")||id.contains("sinister"))return 7;
        if(id.contains("golem")||id.startsWith("core_")||id.endsWith("_core")||id.equals("golemancer_bell"))return 5;
        if(item instanceof net.minecraft.world.item.BlockItem block){if(block.getBlock() instanceof thaumcraft.production.ProductionBlock)return 3;if(block.getBlock() instanceof thaumcraft.visnet.VisDeviceBlock||id.equals("compound_recharge_focus")||id.contains("infusion")||id.contains("pedestal")||id.contains("pillar")||id.contains("construct"))return 4;return 6;}
        if(item instanceof thaumcraft.items.CastingWandItem||id.contains("wand_cap")||id.endsWith("_rod")||id.contains("focus")&&!id.equals("compound_recharge_focus")||id.equals("thaumometer")||id.contains("scribing")||id.contains("thaumonomicon")||id.contains("research_notes"))return 1;
        if(item instanceof net.minecraft.world.item.ArmorItem||item instanceof net.minecraft.world.item.DiggerItem||item instanceof net.minecraft.world.item.SwordItem||item instanceof net.minecraft.world.item.BowItem||id.contains("ring")||id.contains("amulet")||id.contains("belt")||id.contains("harness")||id.contains("boots")||id.contains("goggles")||id.contains("mirror")||id.contains("resonator")||id.contains("crusher"))return 2;
        if(id.contains("phial")||id.equals("essentia_crystal")||id.contains("jar_label"))return 3;
        return 0;
    }
    private static int subgroup(net.minecraft.world.item.ItemStack stack){
        if(stack.getItem() instanceof net.minecraft.world.item.SpawnEggItem)return 1;
        if(stack.getItem() instanceof thaumcraft.items.CastingWandItem wand)return wand.kind().ordinal();
        String id=net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
        if(group(stack)==1)return id.endsWith("_rod")?3:id.contains("cap")?4:id.contains("focus")?5:6;
        if(group(stack)==0)return id.contains("ingot")?0:id.contains("nugget")?1:id.contains("shard")?2:3;
        return 0;
    }
    private static String variant(net.minecraft.world.item.ItemStack stack){
        if(stack.getItem() instanceof thaumcraft.items.CastingWandItem){var parts=thaumcraft.items.WandMaterials.parts(stack);return String.format(java.util.Locale.ROOT,"%02d:%02d",rank(java.util.List.of("wood","greatwood","silverwood","obsidian","reed","blaze","ice","quartz","bone","primal"),parts.rod().replace("_staff","")),rank(java.util.List.of("iron","copper","gold","silver","thaumium","void"),parts.cap()));}
        var data=stack.getOrDefault(net.minecraft.core.component.DataComponents.BLOCK_ENTITY_DATA,net.minecraft.world.item.component.CustomData.EMPTY).copyTag();if(data.contains("AspectFilter"))return data.getString("AspectFilter");
        data=stack.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA,net.minecraft.world.item.component.CustomData.EMPTY).copyTag();return data.getString("Aspect")+data.getString("EssenceAspect");
    }
    private static int rank(java.util.List<String> order,String value){int rank=order.indexOf(value);return rank<0?order.size():rank;}

    public static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        TABS.register(modBus);
    }

    private ThaumcraftContent() {}
}
