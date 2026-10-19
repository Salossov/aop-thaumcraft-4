package thaumcraft.registry;

import java.util.EnumMap;
import java.util.Map;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.*;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.*;
import thaumcraft.api.aspects.*;
import thaumcraft.menu.AlchemyFurnaceMenu;
import thaumcraft.production.*;

public final class R3Content {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems("thaumcraft");
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks("thaumcraft");
    private static final DeferredRegister<BlockEntityType<?>> TILES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, "thaumcraft");
    private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, "thaumcraft");
    public static final DeferredBlock<EldritchAltarBlock> ELDRITCH_ALTAR=BLOCKS.register("eldritch_altar",EldritchAltarBlock::new);
    public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<EldritchAltarBlockEntity>> ELDRITCH_ALTAR_ENTITY=TILES.register("eldritch_altar",()->BlockEntityType.Builder.of(EldritchAltarBlockEntity::new,ELDRITCH_ALTAR.get()).build(null));
    public static final DeferredBlock<InfernalFurnaceBlock> INFERNAL_FURNACE=BLOCKS.register("infernal_furnace",InfernalFurnaceBlock::new);
    public static final DeferredItem<BlockItem> INFERNAL_FURNACE_ITEM=ITEMS.registerSimpleBlockItem(INFERNAL_FURNACE);
    public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<InfernalFurnaceBlockEntity>> INFERNAL_ENTITY=TILES.register("infernal_furnace",()->BlockEntityType.Builder.of(InfernalFurnaceBlockEntity::new,INFERNAL_FURNACE.get()).build(null));
    public static final DeferredBlock<ArcaneBoreBlock> BORE_BASE=BLOCKS.register("arcane_bore_base",()->new ArcaneBoreBlock(true));
    public static final DeferredBlock<ArcaneBoreBlock> BORE=BLOCKS.register("arcane_bore",()->new ArcaneBoreBlock(false));
    public static final DeferredItem<BlockItem> BORE_BASE_ITEM=ITEMS.registerSimpleBlockItem(BORE_BASE),BORE_ITEM=ITEMS.registerSimpleBlockItem(BORE);
    public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<ArcaneBoreBlockEntity>> BORE_ENTITY=TILES.register("arcane_bore",()->BlockEntityType.Builder.of(ArcaneBoreBlockEntity::new,BORE.get(),BORE_BASE.get()).build(null));
    public static final DeferredHolder<MenuType<?>,MenuType<thaumcraft.menu.ArcaneBoreMenu>> BORE_MENU=MENUS.register("arcane_bore",()->IMenuTypeExtension.create(thaumcraft.menu.ArcaneBoreMenu::new));
    public static final DeferredBlock<HungryChestBlock> HUNGRY_CHEST=BLOCKS.register("hungry_chest",HungryChestBlock::new);
    public static final DeferredItem<BlockItem> HUNGRY_CHEST_ITEM=ITEMS.registerSimpleBlockItem(HUNGRY_CHEST);
    public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<HungryChestBlockEntity>> HUNGRY_CHEST_ENTITY=TILES.register("hungry_chest",()->BlockEntityType.Builder.of(HungryChestBlockEntity::new,HUNGRY_CHEST.get()).build(null));
    public static final DeferredHolder<MenuType<?>,MenuType<thaumcraft.menu.TrunkMenu>> TRUNK_MENU=MENUS.register("traveling_trunk",()->IMenuTypeExtension.create(thaumcraft.menu.TrunkMenu::new));
    public static final DeferredBlock<thaumcraft.blocks.GolemFetterBlock> GOLEM_FETTER=BLOCKS.register("golem_fetter",thaumcraft.blocks.GolemFetterBlock::new);
    public static final DeferredBlock<Block> TALLOW_BLOCK=BLOCKS.registerSimpleBlock("tallow_block",BlockBehaviour.Properties.of().strength(2,6).sound(SoundType.STONE));
    public static final DeferredItem<BlockItem> TALLOW_BLOCK_ITEM=ITEMS.registerSimpleBlockItem(TALLOW_BLOCK);
    public static final DeferredBlock<thaumcraft.blocks.TaintedFleshBlock> TAINTED_FLESH=BLOCKS.register("tainted_flesh",thaumcraft.blocks.TaintedFleshBlock::new);
    public static final DeferredItem<BlockItem> TAINTED_FLESH_ITEM=ITEMS.registerSimpleBlockItem(TAINTED_FLESH);
    public static final DeferredItem<BlockItem> GOLEM_FETTER_ITEM=ITEMS.registerSimpleBlockItem(GOLEM_FETTER);
    public static final DeferredHolder<MenuType<?>,MenuType<thaumcraft.menu.GolemMenu>> GOLEM_MENU=MENUS.register("golem",()->IMenuTypeExtension.create(thaumcraft.menu.GolemMenu::new));
    public static final DeferredBlock<ArcaneLampBlock> ARCANE_LAMP=lamp("arcane_lamp",ArcaneLampBlock.Kind.LIGHT);
    public static final DeferredBlock<Block> AMBER_BLOCK=BLOCKS.register("amber_block",()->new HalfTransparentBlock(BlockBehaviour.Properties.of().strength(1.5f,5).sound(SoundType.STONE).noOcclusion()));
    public static final DeferredItem<BlockItem> AMBER_BLOCK_ITEM=ITEMS.registerSimpleBlockItem(AMBER_BLOCK);
    public static final DeferredBlock<ArcaneLampBlock> GROWTH_LAMP=lamp("lamp_of_growth",ArcaneLampBlock.Kind.GROWTH);
    public static final DeferredBlock<ArcaneLampBlock> FERTILITY_LAMP=lamp("lamp_of_fertility",ArcaneLampBlock.Kind.FERTILITY);
    public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<ArcaneLampBlockEntity>> LAMP_ENTITY=TILES.register("arcane_lamp",()->BlockEntityType.Builder.of(ArcaneLampBlockEntity::new,ARCANE_LAMP.get(),GROWTH_LAMP.get(),FERTILITY_LAMP.get()).build(null));
    public static final DeferredBlock<LampLightBlock> LAMP_LIGHT=BLOCKS.register("lamp_light",LampLightBlock::new);
    public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<LampLightBlockEntity>> LAMP_LIGHT_ENTITY=TILES.register("lamp_light",()->BlockEntityType.Builder.of(LampLightBlockEntity::new,LAMP_LIGHT.get()).build(null));
    private static DeferredBlock<ArcaneLampBlock> lamp(String id,ArcaneLampBlock.Kind kind){var block=BLOCKS.register(id,()->new ArcaneLampBlock(kind));ITEMS.registerSimpleBlockItem(block);return block;}
    public static final DeferredBlock<MirrorBlock> MIRROR=BLOCKS.register("magic_mirror",MirrorBlock::new);
    public static final DeferredBlock<MirrorBlock> ESSENTIA_MIRROR=BLOCKS.register("essentia_mirror",MirrorBlock::new);
    public static final DeferredItem<thaumcraft.items.MirrorItem> MIRROR_ITEM=ITEMS.register("magic_mirror",()->new thaumcraft.items.MirrorItem(MIRROR.get()));
    public static final DeferredItem<thaumcraft.items.MirrorItem> ESSENTIA_MIRROR_ITEM=ITEMS.register("essentia_mirror",()->new thaumcraft.items.MirrorItem(ESSENTIA_MIRROR.get()));
    public static final DeferredItem<thaumcraft.items.HandMirrorItem> HAND_MIRROR=ITEMS.register("hand_mirror",thaumcraft.items.HandMirrorItem::new);
    public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<MirrorBlockEntity>> MIRROR_ENTITY=TILES.register("magic_mirror",()->BlockEntityType.Builder.of(MirrorBlockEntity::new,MIRROR.get(),ESSENTIA_MIRROR.get()).build(null));
    public static final DeferredHolder<MenuType<?>,MenuType<thaumcraft.menu.HandMirrorMenu>> HAND_MIRROR_MENU=MENUS.register("hand_mirror",()->IMenuTypeExtension.create(thaumcraft.menu.HandMirrorMenu::new));
    public static final DeferredHolder<MenuType<?>,MenuType<thaumcraft.menu.FocusPouchMenu>> POUCH_MENU=MENUS.register("focus_pouch",()->IMenuTypeExtension.create(thaumcraft.menu.FocusPouchMenu::new));
    public static final DeferredBlock<FocalManipulatorBlock> FOCAL_MANIPULATOR=BLOCKS.register("focal_manipulator",FocalManipulatorBlock::new);
    public static final DeferredItem<BlockItem> FOCAL_MANIPULATOR_ITEM=ITEMS.registerSimpleBlockItem(FOCAL_MANIPULATOR);
    public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<FocalManipulatorBlockEntity>> FOCAL_ENTITY=TILES.register("focal_manipulator",()->BlockEntityType.Builder.of(FocalManipulatorBlockEntity::new,FOCAL_MANIPULATOR.get()).build(null));
    public static final DeferredHolder<MenuType<?>,MenuType<thaumcraft.menu.FocalManipulatorMenu>> FOCAL_MENU=MENUS.register("focal_manipulator",()->IMenuTypeExtension.create(thaumcraft.menu.FocalManipulatorMenu::new));
    public static final DeferredBlock<thaumcraft.blocks.ShockFieldBlock> SHOCK_FIELD=BLOCKS.register("shock_field",thaumcraft.blocks.ShockFieldBlock::new);
    public static final DeferredBlock<thaumcraft.blocks.FocusMemoryBlock> PORTABLE_HOLE=BLOCKS.register("portable_hole",()->new thaumcraft.blocks.FocusMemoryBlock(true));
    public static final DeferredBlock<thaumcraft.blocks.FocusMemoryBlock> WARDED_BLOCK=BLOCKS.register("warded_block",()->new thaumcraft.blocks.FocusMemoryBlock(false));
    public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<thaumcraft.blocks.FocusMemoryBlockEntity>> FOCUS_MEMORY_ENTITY=TILES.register("focus_memory",()->BlockEntityType.Builder.of(thaumcraft.blocks.FocusMemoryBlockEntity::new,PORTABLE_HOLE.get(),WARDED_BLOCK.get()).build(null));
    public static final DeferredBlock<BrainJarBlock> BRAIN_JAR=BLOCKS.register("brain_in_a_jar",BrainJarBlock::new);
    public static final DeferredItem<BlockItem> BRAIN_JAR_ITEM=ITEMS.registerSimpleBlockItem(BRAIN_JAR);
    public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<BrainJarBlockEntity>> BRAIN_JAR_ENTITY=TILES.register("brain_in_a_jar",()->BlockEntityType.Builder.of(BrainJarBlockEntity::new,BRAIN_JAR.get()).build(null));
    public static final DeferredBlock<thaumcraft.visnet.VisDeviceBlock> VIS_RELAY=visDevice("vis_relay",thaumcraft.visnet.VisDeviceBlock.Kind.RELAY);
    public static final DeferredBlock<thaumcraft.visnet.VisDeviceBlock> WORKBENCH_CHARGER=visDevice("vis_charge_relay",thaumcraft.visnet.VisDeviceBlock.Kind.CHARGER);
    public static final DeferredBlock<thaumcraft.visnet.VisDeviceBlock> NODE_CONVERTER=visDevice("node_transducer",thaumcraft.visnet.VisDeviceBlock.Kind.CONVERTER);
    public static final DeferredBlock<thaumcraft.visnet.VisDeviceBlock> NODE_STABILIZER=visDevice("node_stabilizer",thaumcraft.visnet.VisDeviceBlock.Kind.STABILIZER);
    public static final DeferredBlock<thaumcraft.visnet.VisDeviceBlock> ADVANCED_NODE_STABILIZER=visDevice("advanced_node_stabilizer",thaumcraft.visnet.VisDeviceBlock.Kind.ADVANCED_STABILIZER);
    public static final DeferredBlock<thaumcraft.visnet.VisDeviceBlock> WAND_PEDESTAL=visDevice("wand_recharge_pedestal",thaumcraft.visnet.VisDeviceBlock.Kind.WAND_PEDESTAL);
    public static final DeferredBlock<thaumcraft.visnet.VisDeviceBlock> ARCANE_RESONATOR=visDevice("compound_recharge_focus",thaumcraft.visnet.VisDeviceBlock.Kind.RESONATOR);
    public static final DeferredBlock<thaumcraft.visnet.VisDeviceBlock> ENERGIZED_NODE=BLOCKS.register("energized_aura_node",()->new thaumcraft.visnet.VisDeviceBlock(thaumcraft.visnet.VisDeviceBlock.Kind.ENERGIZED));
    public static final DeferredItem<BlockItem> ENERGIZED_NODE_ITEM=ITEMS.registerSimpleBlockItem(ENERGIZED_NODE);
    public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<thaumcraft.visnet.VisRelayBlockEntity>> VIS_RELAY_ENTITY=TILES.register("vis_relay",()->BlockEntityType.Builder.of(thaumcraft.visnet.VisRelayBlockEntity::new,VIS_RELAY.get(),WORKBENCH_CHARGER.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<thaumcraft.visnet.NodeStabilizerBlockEntity>> NODE_STABILIZER_ENTITY=TILES.register("node_stabilizer",()->BlockEntityType.Builder.of(thaumcraft.visnet.NodeStabilizerBlockEntity::new,NODE_STABILIZER.get(),ADVANCED_NODE_STABILIZER.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<thaumcraft.visnet.NodeConverterBlockEntity>> NODE_CONVERTER_ENTITY=TILES.register("node_transducer",()->BlockEntityType.Builder.of(thaumcraft.visnet.NodeConverterBlockEntity::new,NODE_CONVERTER.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<thaumcraft.visnet.EnergizedNodeBlockEntity>> ENERGIZED_NODE_ENTITY=TILES.register("energized_aura_node",()->BlockEntityType.Builder.of(thaumcraft.visnet.EnergizedNodeBlockEntity::new,ENERGIZED_NODE.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<thaumcraft.visnet.WandPedestalBlockEntity>> WAND_PEDESTAL_ENTITY=TILES.register("wand_recharge_pedestal",()->BlockEntityType.Builder.of(thaumcraft.visnet.WandPedestalBlockEntity::new,WAND_PEDESTAL.get()).build(null));
    private static DeferredBlock<thaumcraft.visnet.VisDeviceBlock> visDevice(String id,thaumcraft.visnet.VisDeviceBlock.Kind kind){var block=BLOCKS.register(id,()->new thaumcraft.visnet.VisDeviceBlock(kind));ITEMS.registerSimpleBlockItem(block);return block;}
    public static final java.util.List<DeferredBlock<thaumcraft.visnet.CrystalClusterBlock>> CRYSTAL_CLUSTERS=new java.util.ArrayList<>();
    static{var names=java.util.List.of("air","fire","water","earth","order","entropy","mixed");for(int i=0;i<names.size();i++){final int index=i;var block=BLOCKS.register(names.get(i)+"_crystal_cluster",()->new thaumcraft.visnet.CrystalClusterBlock(index));ITEMS.registerSimpleBlockItem(block);CRYSTAL_CLUSTERS.add(block);}}
    public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<thaumcraft.visnet.CrystalClusterBlockEntity>> CRYSTAL_CLUSTER_ENTITY=TILES.register("crystal_cluster",()->BlockEntityType.Builder.of(thaumcraft.visnet.CrystalClusterBlockEntity::new,CRYSTAL_CLUSTERS.stream().map(java.util.function.Supplier::get).toArray(Block[]::new)).build(null));
    public static final Map<ProductionBlock.Kind, DeferredBlock<ProductionBlock>> PRODUCTION = new EnumMap<>(ProductionBlock.Kind.class);
    public static final DeferredBlock<ProductionBlock> CRUCIBLE = production("crucible", ProductionBlock.Kind.CRUCIBLE);
    public static final DeferredBlock<ProductionBlock> ALCHEMY_FURNACE = production("alchemy_furnace", ProductionBlock.Kind.FURNACE);
    public static final DeferredBlock<ProductionBlock> ALEMBIC = production("arcane_alembic", ProductionBlock.Kind.ALEMBIC);
    public static final DeferredBlock<ProductionBlock> WARDED_JAR = production("warded_jar", ProductionBlock.Kind.JAR);
    public static final DeferredBlock<thaumcraft.blocks.JarredNodeBlock> NODE_JAR=BLOCKS.register("node_jar",thaumcraft.blocks.JarredNodeBlock::new);
    public static final DeferredItem<thaumcraft.items.NodeJarItem> NODE_JAR_ITEM=ITEMS.register("node_jar",()->new thaumcraft.items.NodeJarItem(NODE_JAR.get()));
    public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<thaumcraft.blocks.JarredNodeBlockEntity>> NODE_JAR_ENTITY=TILES.register("node_jar",()->BlockEntityType.Builder.of(thaumcraft.blocks.JarredNodeBlockEntity::new,NODE_JAR.get()).build(null));
    public static final DeferredBlock<ProductionBlock> VOID_JAR = production("void_jar", ProductionBlock.Kind.VOID_JAR);
    public static final DeferredBlock<ProductionBlock> TUBE = production("essentia_tube", ProductionBlock.Kind.TUBE);
    public static final DeferredBlock<ProductionBlock> VALVE = production("essentia_valve", ProductionBlock.Kind.VALVE);
    public static final DeferredBlock<ProductionBlock> FILTER_TUBE = production("filtered_essentia_tube", ProductionBlock.Kind.FILTER);
    public static final DeferredBlock<ProductionBlock> RESTRICTED_TUBE = production("restricted_essentia_tube", ProductionBlock.Kind.RESTRICTED);
    public static final DeferredBlock<ProductionBlock> DIRECTIONAL_TUBE = production("directional_essentia_tube", ProductionBlock.Kind.ONEWAY);
    public static final DeferredBlock<ProductionBlock> BUFFER = production("essentia_buffer", ProductionBlock.Kind.BUFFER);
    public static final DeferredBlock<ProductionBlock> CENTRIFUGE = production("alchemical_centrifuge", ProductionBlock.Kind.CENTRIFUGE);
    public static final DeferredBlock<ProductionBlock> CRYSTALLIZER = production("essentia_crystallizer", ProductionBlock.Kind.CRYSTALLIZER);
    public static final DeferredBlock<ProductionBlock> RESERVOIR = production("essentia_reservoir", ProductionBlock.Kind.RESERVOIR);
    public static final DeferredBlock<Block> ARCANE_STONE = BLOCKS.registerSimpleBlock("arcane_stone", BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(4,20).sound(SoundType.STONE));
    public static final DeferredBlock<Block> ARCANE_STONE_BRICKS = BLOCKS.registerSimpleBlock("arcane_stone_bricks", BlockBehaviour.Properties.of().strength(4,20));
    public static final DeferredBlock<SlabBlock> ARCANE_STONE_BRICK_SLAB=BLOCKS.register("arcane_stone_brick_slab",()->new SlabBlock(BlockBehaviour.Properties.of().strength(2,6)));
    public static final DeferredItem<BlockItem> ARCANE_STONE_BRICK_SLAB_ITEM=ITEMS.registerSimpleBlockItem(ARCANE_STONE_BRICK_SLAB);
    public static final DeferredItem<BlockItem> ARCANE_STONE_BRICKS_ITEM=ITEMS.registerSimpleBlockItem(ARCANE_STONE_BRICKS);
    public static final DeferredBlock<InfusionBlock> INFUSION_MATRIX=BLOCKS.register("infusion_matrix",()->new InfusionBlock(true));
    public static final DeferredItem<BlockItem> INFUSION_MATRIX_ITEM=ITEMS.registerSimpleBlockItem(INFUSION_MATRIX);
    public static final DeferredBlock<InfusionBlock> ARCANE_PEDESTAL=BLOCKS.register("arcane_pedestal",()->new InfusionBlock(false));
    public static final DeferredItem<BlockItem> ARCANE_PEDESTAL_ITEM=ITEMS.registerSimpleBlockItem(ARCANE_PEDESTAL);
    public static final DeferredBlock<InfusionPillarBlock> INFUSION_PILLAR=BLOCKS.register("infusion_pillar",InfusionPillarBlock::new);
    public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<InfusionMatrixBlockEntity>> INFUSION_ENTITY=TILES.register("infusion_matrix",()->BlockEntityType.Builder.of(InfusionMatrixBlockEntity::new,INFUSION_MATRIX.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<PedestalBlockEntity>> PEDESTAL_ENTITY=TILES.register("arcane_pedestal",()->BlockEntityType.Builder.of(PedestalBlockEntity::new,ARCANE_PEDESTAL.get()).build(null));
    public static final DeferredBlock<ThaumatoriumBlock> THAUMATORIUM=BLOCKS.register("thaumatorium",ThaumatoriumBlock::new);
    public static final DeferredBlock<BrainboxBlock> BRAINBOX=BLOCKS.register("thaumatorium_brainbox",BrainboxBlock::new);
    public static final DeferredItem<BlockItem> BRAINBOX_ITEM=ITEMS.registerSimpleBlockItem(BRAINBOX);
    public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<ThaumatoriumBlockEntity>> THAUMATORIUM_ENTITY=TILES.register("thaumatorium",()->BlockEntityType.Builder.of(ThaumatoriumBlockEntity::new,THAUMATORIUM.get()).build(null));
    public static final DeferredHolder<MenuType<?>,MenuType<thaumcraft.menu.ThaumatoriumMenu>> THAUMATORIUM_MENU=MENUS.register("thaumatorium",()->IMenuTypeExtension.create(thaumcraft.menu.ThaumatoriumMenu::new));
    public static final DeferredBlock<Block> ALCHEMICAL_CONSTRUCT=BLOCKS.registerSimpleBlock("alchemical_construct",BlockBehaviour.Properties.of().strength(3,15).sound(SoundType.METAL));
    public static final DeferredItem<BlockItem> ALCHEMICAL_CONSTRUCT_ITEM=ITEMS.registerSimpleBlockItem(ALCHEMICAL_CONSTRUCT);
    public static final DeferredItem<BlockItem> ARCANE_STONE_ITEM = ITEMS.registerSimpleBlockItem(ARCANE_STONE);
    public static final DeferredBlock<Block> BELLOWS = BLOCKS.register("arcane_bellows",BellowsBlock::new);
    public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<BellowsBlockEntity>> BELLOWS_ENTITY=TILES.register("arcane_bellows",()->BlockEntityType.Builder.of(BellowsBlockEntity::new,BELLOWS.get()).build(null));
    public static final DeferredItem<BlockItem> BELLOWS_ITEM = ITEMS.registerSimpleBlockItem(BELLOWS);
    public static final DeferredBlock<Block> NITOR_BLOCK = BLOCKS.register("nitor",NitorBlock::new);
    public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<NitorBlockEntity>> NITOR_ENTITY=TILES.register("nitor",()->BlockEntityType.Builder.of(NitorBlockEntity::new,NITOR_BLOCK.get()).build(null));
    public static final DeferredItem<BlockItem> NITOR = ITEMS.registerSimpleBlockItem(NITOR_BLOCK);
    public static final DeferredItem<Item> ALUMENTUM = ITEMS.register("alumentum",thaumcraft.items.AlumentumItem::new);
    public static final DeferredItem<Item> BALANCED_SHARD = ITEMS.registerSimpleItem("balanced_shard");
    public static final DeferredBlock<RotatedPillarBlock> GREATWOOD_LOG = BLOCKS.register("greatwood_log", () -> new thaumcraft.blocks.MagicalLogBlock());
    public static final DeferredItem<BlockItem> GREATWOOD_LOG_ITEM = ITEMS.registerSimpleBlockItem(GREATWOOD_LOG);
    public static final DeferredBlock<RotatedPillarBlock> SILVERWOOD_LOG = BLOCKS.register("silverwood_log", () -> new thaumcraft.blocks.MagicalLogBlock());
    public static final DeferredItem<BlockItem> SILVERWOOD_LOG_ITEM = ITEMS.registerSimpleBlockItem(SILVERWOOD_LOG);
    public static final DeferredBlock<Block> GREATWOOD_PLANKS = BLOCKS.register("greatwood_planks", thaumcraft.blocks.MagicalWoodBlock::new);
    public static final DeferredItem<BlockItem> GREATWOOD_PLANKS_ITEM = ITEMS.registerSimpleBlockItem(GREATWOOD_PLANKS);
    public static final DeferredBlock<Block> SILVERWOOD_PLANKS = BLOCKS.register("silverwood_planks", thaumcraft.blocks.MagicalWoodBlock::new);
    public static final DeferredItem<BlockItem> SILVERWOOD_PLANKS_ITEM = ITEMS.registerSimpleBlockItem(SILVERWOOD_PLANKS);
    public static final DeferredBlock<Block> CINNABAR_ORE=BLOCKS.registerSimpleBlock("cinnabar_ore",BlockBehaviour.Properties.of().strength(1.5f,5).requiresCorrectToolForDrops());
    public static final DeferredItem<BlockItem> CINNABAR_ORE_ITEM=ITEMS.registerSimpleBlockItem(CINNABAR_ORE);
    public static final DeferredBlock<Block> AMBER_ORE=BLOCKS.register("amber_ore",()->new DropExperienceBlock(net.minecraft.util.valueproviders.UniformInt.of(1,4),BlockBehaviour.Properties.of().strength(1.5f,5).requiresCorrectToolForDrops()));
    public static final DeferredItem<BlockItem> AMBER_ORE_ITEM=ITEMS.registerSimpleBlockItem(AMBER_ORE);
    public static final DeferredItem<Item> AMBER=R2Content.AMBER;
    public static final DeferredBlock<thaumcraft.blocks.SilverwoodNodeLogBlock> SILVERWOOD_NODE_LOG=BLOCKS.register("silverwood_node_log",thaumcraft.blocks.SilverwoodNodeLogBlock::new);
    public static final DeferredBlock<LeavesBlock> GREATWOOD_LEAVES=BLOCKS.register("greatwood_leaves",()->new thaumcraft.blocks.MagicalLeavesBlock());
    public static final DeferredItem<BlockItem> GREATWOOD_LEAVES_ITEM=ITEMS.registerSimpleBlockItem(GREATWOOD_LEAVES);
    public static final DeferredBlock<LeavesBlock> SILVERWOOD_LEAVES=BLOCKS.register("silverwood_leaves",()->new thaumcraft.blocks.MagicalLeavesBlock(true));
    public static final DeferredItem<BlockItem> SILVERWOOD_LEAVES_ITEM=ITEMS.registerSimpleBlockItem(SILVERWOOD_LEAVES);
    public static final DeferredBlock<thaumcraft.blocks.MagicalSaplingBlock> GREATWOOD_SAPLING=BLOCKS.register("greatwood_sapling",()->new thaumcraft.blocks.MagicalSaplingBlock(false));
    public static final DeferredItem<BlockItem> GREATWOOD_SAPLING_ITEM=ITEMS.registerSimpleBlockItem(GREATWOOD_SAPLING);
    public static final DeferredBlock<thaumcraft.blocks.MagicalSaplingBlock> SILVERWOOD_SAPLING=BLOCKS.register("silverwood_sapling",()->new thaumcraft.blocks.MagicalSaplingBlock(true));
    public static final DeferredItem<BlockItem> SILVERWOOD_SAPLING_ITEM=ITEMS.registerSimpleBlockItem(SILVERWOOD_SAPLING);
    public static final DeferredBlock<thaumcraft.blocks.MagicalFlowerBlock> SHIMMERLEAF=BLOCKS.register("shimmerleaf",()->new thaumcraft.blocks.MagicalFlowerBlock());
    public static final DeferredItem<BlockItem> SHIMMERLEAF_ITEM=ITEMS.registerSimpleBlockItem(SHIMMERLEAF);
    public static final DeferredItem<Item> TALLOW = ITEMS.registerSimpleItem("tallow");
    public static final DeferredItem<Item> QUICKSILVER = ITEMS.registerSimpleItem("quicksilver");
    public static final DeferredItem<Item> QUICKSILVER_DROP = ITEMS.registerSimpleItem("quicksilver_drop");
    public static final DeferredItem<Item> VIS_FILTER = ITEMS.registerSimpleItem("vis_filter");
    public static final DeferredItem<thaumcraft.items.EssentiaResonatorItem> RESONATOR = ITEMS.register("essentia_resonator", thaumcraft.items.EssentiaResonatorItem::new);
    public static final DeferredItem<thaumcraft.items.JarLabelItem> JAR_LABEL = ITEMS.register("jar_label", thaumcraft.items.JarLabelItem::new);
    public static final DeferredItem<Item> EMPTY_PHIAL = ITEMS.registerSimpleItem("empty_phial");
    public static final DeferredItem<Item> ESSENTIA_PHIAL = ITEMS.register("essentia_phial", () -> new thaumcraft.items.EssentiaItem(8));
    public static final DeferredItem<Item> ESSENTIA_CRYSTAL = ITEMS.register("essentia_crystal", () -> new thaumcraft.items.EssentiaItem(1));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CrucibleBlockEntity>> CRUCIBLE_ENTITY = TILES.register("crucible", () -> BlockEntityType.Builder.of(CrucibleBlockEntity::new, CRUCIBLE.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AlchemyFurnaceBlockEntity>> FURNACE_ENTITY = TILES.register("alchemy_furnace", () -> BlockEntityType.Builder.of(AlchemyFurnaceBlockEntity::new, ALCHEMY_FURNACE.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TransportBlockEntity>> TRANSPORT_ENTITY = TILES.register("essentia_transport", () -> BlockEntityType.Builder.of(TransportBlockEntity::new,
            PRODUCTION.entrySet().stream().filter(e -> e.getKey() != ProductionBlock.Kind.CRUCIBLE && e.getKey() != ProductionBlock.Kind.FURNACE).map(e -> e.getValue().get()).toArray(Block[]::new)).build(null));
    public static final DeferredHolder<MenuType<?>, MenuType<AlchemyFurnaceMenu>> FURNACE_MENU = MENUS.register("alchemy_furnace", () -> IMenuTypeExtension.create(AlchemyFurnaceMenu::new));
    public static final DeferredHolder<MenuType<?>,MenuType<thaumcraft.menu.HarnessMenu>> HARNESS_MENU=MENUS.register("hover_harness",()->IMenuTypeExtension.create(thaumcraft.menu.HarnessMenu::new));
    private static DeferredBlock<ProductionBlock> production(String id, ProductionBlock.Kind kind) {
        var block = BLOCKS.register(id, () -> switch(kind){case TUBE,VALVE,FILTER,RESTRICTED,ONEWAY,BUFFER -> new TubeBlock(kind);default -> new ProductionBlock(kind);}); PRODUCTION.put(kind, block); if(kind==ProductionBlock.Kind.JAR||kind==ProductionBlock.Kind.VOID_JAR)ITEMS.register(id,()->new thaumcraft.items.EssentiaJarItem(block.get()));else ITEMS.registerSimpleBlockItem(block); return block;
    }
    public static Aspect aspect(ItemStack stack) { return Aspect.getAspect(stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getString("Aspect")); }
    public static void setAspect(ItemStack stack, Aspect aspect) { CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putString("Aspect", aspect.getTag())); }
    public static boolean heatSource(Level level, BlockPos pos) {
        if (level == null || !level.hasChunkAt(pos)) return false;
        var state = level.getBlockState(pos);
        return state.is(Blocks.LAVA) || state.is(Blocks.FIRE) || state.is(Blocks.SOUL_FIRE) || state.is(NITOR_BLOCK.get());
    }
    public static int bellows(Level level, BlockPos pos) {
        if (level == null) return 0; int result = 0;
        for (Direction d : Direction.values()) { var adjacent = pos.relative(d); if (level.hasChunkAt(adjacent) && level.getBlockState(adjacent).is(BELLOWS.get())&&level.getBlockState(adjacent).getValue(BellowsBlock.FACING)==d.getOpposite()&&!level.hasNeighborSignal(adjacent)) result++; }
        return result;
    }
    private static void capabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(EssentiaCapabilities.TRANSPORT,LAMP_ENTITY.get(),(tile,side)->tile.kind()==ArcaneLampBlock.Kind.LIGHT?null:tile);
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK,HUNGRY_CHEST_ENTITY.get(),(tile,side)->tile.handler);
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK,INFERNAL_ENTITY.get(),(tile,side)->tile.core()?tile.input:null);
        event.registerBlockEntity(EssentiaCapabilities.TRANSPORT,INFERNAL_ENTITY.get(),(tile,side)->tile.nozzle()?tile:null);
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK,BORE_ENTITY.get(),(tile,side)->tile.base()?null:tile.handler);
        event.registerBlockEntity(EssentiaCapabilities.TRANSPORT,BORE_ENTITY.get(),(tile,side)->tile.base()?tile:null);
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK,MIRROR_ENTITY.get(),(tile,side)->tile.essentia()?null:tile.items);
        event.registerBlockEntity(EssentiaCapabilities.SOURCE,MIRROR_ENTITY.get(),(tile,side)->tile.essentia()?tile:null);
        event.registerBlockEntity(EssentiaCapabilities.CONTAINER,MIRROR_ENTITY.get(),(tile,side)->tile.essentia()?tile:null);
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK,WAND_PEDESTAL_ENTITY.get(),(tile,side)->new net.neoforged.neoforge.items.wrapper.InvWrapper(tile));
        event.registerBlock(EssentiaCapabilities.TRANSPORT,(level,pos,state,tile,side)->ThaumatoriumBlock.main(level,pos,state),THAUMATORIUM.get());
        event.registerBlock(EssentiaCapabilities.CONTAINER,(level,pos,state,tile,side)->ThaumatoriumBlock.main(level,pos,state),THAUMATORIUM.get());
        event.registerBlock(Capabilities.ItemHandler.BLOCK,(level,pos,state,tile,side)->{var main=ThaumatoriumBlock.main(level,pos,state);return main==null?null:new net.neoforged.neoforge.items.wrapper.InvWrapper(main);},THAUMATORIUM.get());
        event.registerBlockEntity(EssentiaCapabilities.SOURCE, TRANSPORT_ENTITY.get(), (tile, side) -> switch(tile.kind){case JAR,VOID_JAR,RESERVOIR -> tile;default -> null;});
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, PEDESTAL_ENTITY.get(), (tile,side)->new net.neoforged.neoforge.items.wrapper.InvWrapper(tile));
        event.registerBlockEntity(EssentiaCapabilities.TRANSPORT, TRANSPORT_ENTITY.get(), (tile, side) -> tile);
        event.registerBlockEntity(EssentiaCapabilities.CONTAINER, TRANSPORT_ENTITY.get(), (tile, side) -> tile);
        event.registerBlockEntity(EssentiaCapabilities.CONTAINER, CRUCIBLE_ENTITY.get(), (tile, side) -> tile);
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, CRUCIBLE_ENTITY.get(), (tile, side) -> tile.fluidHandler);
        event.registerBlockEntity(EssentiaCapabilities.CONTAINER, FURNACE_ENTITY.get(), (tile, side) -> tile);
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, FURNACE_ENTITY.get(), (tile, side) -> side == null ? new net.neoforged.neoforge.items.wrapper.InvWrapper(tile) : new net.neoforged.neoforge.items.wrapper.SidedInvWrapper(tile, side));
    }
    public static void register(IEventBus bus) { BLOCKS.register(bus); ITEMS.register(bus); TILES.register(bus); MENUS.register(bus); bus.addListener(R3Content::capabilities); }
    public static void creative(CreativeModeTab.Output output) {
        for(var item:ITEMS.getEntries())if(item!=ESSENTIA_PHIAL&&item!=ESSENTIA_CRYSTAL&&item!=INFERNAL_FURNACE_ITEM&&item!=ENERGIZED_NODE_ITEM&&item!=NODE_JAR_ITEM)output.accept(item.get());
        for(var aspect:Aspect.all().values().stream().sorted(java.util.Comparator.comparing(Aspect::getTag)).toList()){
            for(var item:java.util.List.of(ESSENTIA_PHIAL.get(),ESSENTIA_CRYSTAL.get())){var stack=new ItemStack(item);setAspect(stack,aspect);output.accept(stack);}
            for(var block:java.util.List.of(WARDED_JAR.get(),VOID_JAR.get())){
                var stack=new ItemStack(block);var tag=new CompoundTag();var contents=new CompoundTag();contents.putInt(aspect.getTag(),64);tag.put("Essentia",contents);tag.putString("AspectFilter",aspect.getTag());tag.putInt("TC4ProductionVersion",1);tag.putString("id","thaumcraft:essentia_transport");stack.set(DataComponents.BLOCK_ENTITY_DATA,CustomData.of(tag));output.accept(stack);
            }
        }
    }
    private R3Content() {}
}
