package thaumcraft.registry;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.*;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import thaumcraft.blocks.TableBlock;
import thaumcraft.blocks.TableBlockEntity;
import thaumcraft.blocks.AuraNodeBlock;
import thaumcraft.blocks.AuraNodeBlockEntity;
import thaumcraft.blocks.TaintFibresBlock;
import thaumcraft.blocks.TaintSolidBlock;
import thaumcraft.items.*;
import thaumcraft.menu.*;

public final class R2Content {
    public static final DeferredRegister.Items ITEMS=DeferredRegister.createItems("thaumcraft");
    public static final DeferredRegister.Blocks BLOCKS=DeferredRegister.createBlocks("thaumcraft");
    private static final DeferredRegister<BlockEntityType<?>> TILES=DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE,"thaumcraft");
    private static final DeferredRegister<MenuType<?>> MENUS=DeferredRegister.create(Registries.MENU,"thaumcraft");
    private static final DeferredRegister<ArmorMaterial> MATERIALS=DeferredRegister.create(Registries.ARMOR_MATERIAL,"thaumcraft");
    public static final DeferredHolder<ArmorMaterial,ArmorMaterial> GOGGLES_MATERIAL=MATERIALS.register("goggles",()->{
        var defense=new EnumMap<ArmorItem.Type,Integer>(ArmorItem.Type.class);for(var type:ArmorItem.Type.values()) defense.put(type,0);defense.put(ArmorItem.Type.HELMET,1);
        return new ArmorMaterial(defense,25,SoundEvents.ARMOR_EQUIP_LEATHER,()->Ingredient.of(Items.GOLD_INGOT),List.of(new ArmorMaterial.Layer(ResourceLocation.fromNamespaceAndPath("thaumcraft","goggles"))),0,0);
    });
    public static final DeferredItem<Item> IRON_CAP=ITEMS.registerSimpleItem("iron_wand_cap");
    public static final DeferredItem<Item> ZOMBIE_BRAIN=ITEMS.register("zombie_brain",thaumcraft.items.ZombieBrainItem::new);
    public static final DeferredItem<Item> TAINT_TENDRIL=ITEMS.registerSimpleItem("taint_tendril");
    public static final DeferredItem<Item> TAINT_GOO=ITEMS.registerSimpleItem("taint_goo");
    public static final DeferredItem<Item> AMBER=ITEMS.registerSimpleItem("amber");
    public static final DeferredItem<Item> COIN=ITEMS.registerSimpleItem("coin");
    public static final DeferredItem<WispEssenceItem> WISP_ESSENCE=ITEMS.register("wisp_essence",()->new WispEssenceItem(new Item.Properties()));
    public static final DeferredItem<thaumcraft.items.EldritchRelics.Eye> ELDRITCH_EYE=ITEMS.register("eldritch_eye",()->new thaumcraft.items.EldritchRelics.Eye());
    public static final DeferredItem<CastingWandItem> WAND=ITEMS.register("iron_capped_wooden_wand",()->new CastingWandItem());
    public static final DeferredItem<ThaumonomiconItem> BOOK=ITEMS.register("thaumonomicon",ThaumonomiconItem::new);
    public static final DeferredItem<ThaumometerItem> THAUMOMETER=ITEMS.register("thaumometer",ThaumometerItem::new);
    public static final DeferredItem<ScribingToolsItem> SCRIBING_TOOLS=ITEMS.register("scribing_tools",ScribingToolsItem::new);
    public static final DeferredItem<ResearchNotesItem> NOTES=ITEMS.register("research_notes",ResearchNotesItem::new);
    public static final DeferredItem<ArmorItem> GOGGLES=ITEMS.register("goggles_of_revealing",()->new GogglesItem(GOGGLES_MATERIAL,new Item.Properties().durability(350).rarity(Rarity.RARE)));
    public static final DeferredBlock<TableBlock> TABLE=BLOCKS.register("table",()->new TableBlock(TableBlock.Kind.TABLE));
    public static final DeferredBlock<TableBlock> RESEARCH_TABLE=BLOCKS.register("research_table",()->new TableBlock(TableBlock.Kind.RESEARCH));
    public static final DeferredBlock<TableBlock> ARCANE_WORKBENCH=BLOCKS.register("arcane_workbench",()->new TableBlock(TableBlock.Kind.ARCANE));
    public static final DeferredItem<BlockItem> TABLE_ITEM=ITEMS.registerSimpleBlockItem(TABLE);
    public static final DeferredItem<BlockItem> RESEARCH_TABLE_ITEM=ITEMS.registerSimpleBlockItem(RESEARCH_TABLE);
    public static final DeferredItem<BlockItem> ARCANE_WORKBENCH_ITEM=ITEMS.registerSimpleBlockItem(ARCANE_WORKBENCH);
    /** Hidden reference item: the linked-storage mechanic is not implemented by this visual port. */
    public static final DeferredItem<Item> MAGIC_BOX=ITEMS.registerSimpleItem("magic_box");
    public static final DeferredBlock<AuraNodeBlock> AURA_NODE=BLOCKS.register("aura_node",AuraNodeBlock::new);
    public static final DeferredBlock<TaintFibresBlock> TAINT_FIBRES=BLOCKS.register("taint_fibres",()->new TaintFibresBlock(TaintFibresBlock.Kind.FIBRES));
    public static final DeferredBlock<TaintFibresBlock> TAINT_GROWTH=BLOCKS.register("taint_growth",()->new TaintFibresBlock(TaintFibresBlock.Kind.GROWTH));
    public static final DeferredBlock<TaintFibresBlock> TAINT_TENDRILS=BLOCKS.register("taint_tendrils",()->new TaintFibresBlock(TaintFibresBlock.Kind.TENDRILS));
    public static final DeferredBlock<TaintFibresBlock> TAINT_SPORE_STALK=BLOCKS.register("taint_spore_stalk",()->new TaintFibresBlock(TaintFibresBlock.Kind.SPORE));
    public static final DeferredBlock<TaintSolidBlock> FIBROUS_TAINT=BLOCKS.register("fibrous_taint",()->new TaintSolidBlock(true));
    public static final DeferredBlock<TaintSolidBlock> TAINTED_SOIL=BLOCKS.register("tainted_soil",()->new TaintSolidBlock(false));
    public static final DeferredBlock<thaumcraft.fluids.FluxBlock> FLUX_GOO=BLOCKS.register("flux_goo",()->new thaumcraft.fluids.FluxBlock(false));
    public static final DeferredBlock<thaumcraft.fluids.FluxBlock> FLUX_GAS=BLOCKS.register("flux_gas",()->new thaumcraft.fluids.FluxBlock(true));
    public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<TableBlockEntity>> TABLE_ENTITY=TILES.register("table",()->BlockEntityType.Builder.of(TableBlockEntity::new,TABLE.get(),RESEARCH_TABLE.get(),ARCANE_WORKBENCH.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<AuraNodeBlockEntity>> NODE_ENTITY=TILES.register("aura_node",()->BlockEntityType.Builder.of(AuraNodeBlockEntity::new,AURA_NODE.get(),R3Content.SILVERWOOD_NODE_LOG.get(),R4Content.OBSIDIAN_TOTEM_NODE.get()).build(null));
    public static final DeferredHolder<MenuType<?>,MenuType<ResearchMenu>> RESEARCH_MENU=MENUS.register("research_table",()->IMenuTypeExtension.create(ResearchMenu::new));
    public static final DeferredHolder<MenuType<?>,MenuType<ArcaneMenu>> ARCANE_MENU=MENUS.register("arcane_workbench",()->IMenuTypeExtension.create(ArcaneMenu::new));
    public static final DeferredHolder<MenuType<?>,MenuType<ThaumonomiconMenu>> BOOK_MENU=MENUS.register("thaumonomicon",()->IMenuTypeExtension.create(ThaumonomiconMenu::new));
    public static final String[] ELEMENTS={"air","fire","water","earth","order","entropy"};
    public static final List<DeferredItem<Item>> SHARDS=java.util.Arrays.stream(ELEMENTS).map(name->ITEMS.registerSimpleItem(name+"_shard")).toList();
    public static final List<DeferredBlock<thaumcraft.blocks.InfusedStoneBlock>> INFUSED_STONE=java.util.stream.IntStream.range(0,6).mapToObj(i->BLOCKS.register(ELEMENTS[i]+"_infused_stone",()->new thaumcraft.blocks.InfusedStoneBlock(i))).toList();
    static { for(var block:INFUSED_STONE) ITEMS.registerSimpleBlockItem(block); }
    public static void register(IEventBus bus) { MATERIALS.register(bus);BLOCKS.register(bus);ITEMS.register(bus);TILES.register(bus);MENUS.register(bus); }
    public static void creative(CreativeModeTab.Output output) { for(var item:ITEMS.getEntries())if(item!=WISP_ESSENCE&&item!=MAGIC_BOX&&!java.util.Set.of("aura_node","flux_goo","flux_gas").contains(item.getId().getPath()))output.accept(item.get());thaumcraft.api.aspects.Aspect.all().values().stream().sorted(java.util.Comparator.comparing(thaumcraft.api.aspects.Aspect::getTag)).forEach(aspect->output.accept(thaumcraft.items.WispEssenceItem.of(aspect))); }
    static {for(var block:java.util.List.of(TAINT_FIBRES,TAINT_GROWTH,TAINT_TENDRILS,TAINT_SPORE_STALK,TAINTED_SOIL,FLUX_GOO,FLUX_GAS,AURA_NODE))ITEMS.registerSimpleBlockItem(block);}
    private R2Content() {}
}
