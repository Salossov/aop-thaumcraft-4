package thaumcraft.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class ThaumcraftConfig {
    public enum BiomeChangeMode implements net.neoforged.neoforge.common.TranslatableEnum { REAL_4X4, TC4_1X1;
        @Override public net.minecraft.network.chat.Component getTranslatedName(){return net.minecraft.network.chat.Component.translatable("thaumcraft.configuration.biome."+name().toLowerCase(java.util.Locale.ROOT));}}
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue ENABLE_COPPER_CONTENT,MODERN_BLUE_DYE,RAW_ORE_PURIFICATION;
    public static final ModConfigSpec.IntValue ASPECT_TOTAL_CAP;
    public static final ModConfigSpec.BooleanValue NODE_BIOME_CHANGES,HARD_MODE_NODES,DEEP_INFUSED_STONE;
    public static final ModConfigSpec.IntValue NODE_RARITY,SPECIAL_NODE_RARITY,TAINT_SPREAD_RATE;
    public static final ModConfigSpec.EnumValue<BiomeChangeMode> BIOME_CHANGE_MODE;
    public static final ModConfigSpec.BooleanValue BIOME_OVERLAY_GLOBAL;
    public static final ModConfigSpec.BooleanValue TAINT_FROM_FLUX,SPAWN_TAINT_SPORE;
    public static final ModConfigSpec.BooleanValue BIOME_CREATURE_SPAWNS;
    public static final ModConfigSpec.BooleanValue LEGACY_EERIE_BAT_CATEGORY;
    public static final ModConfigSpec.BooleanValue GENERATE_CINNABAR,GENERATE_AMBER,GENERATE_MAGIC_TREES;
    public static final ModConfigSpec.BooleanValue GENERATE_STRUCTURES,GENERATE_AURA,GENERATE_INFUSED_STONE;
    public static final ModConfigSpec.BooleanValue REGENERATE_STRUCTURES,REGENERATE_AURA,REGENERATE_TREES,REGENERATE_CINNABAR,REGENERATE_AMBER,REGENERATE_INFUSED_STONE;
    public static final ModConfigSpec.ConfigValue<java.util.List<? extends String>> DIMENSION_BLACKLIST,BIOME_BLACKLIST;
    public static final ModConfigSpec.ConfigValue<String> REGENERATION_VERSION;
    public static final ModConfigSpec.BooleanValue MODERN_WEAPON_COOLDOWN;
    public static final ModConfigSpec.BooleanValue ALLOW_MIRRORS=ThaumcraftContentConfig.ALLOW_MIRRORS;
    public static final ModConfigSpec.BooleanValue WUSS_MODE;
    public static final ModConfigSpec.BooleanValue CHAMPION_MOBS;
    public static final ModConfigSpec.BooleanValue MODERN_PECH_DUNGEON;
    public static final ModConfigSpec.BooleanValue WARDED_STONE;
    public static final ModConfigSpec.IntValue RUNIC_RECHARGE_MS,RUNIC_RECHARGE_DELAY,RUNIC_VIS_COST;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.push("compatibility");
        MODERN_PECH_DUNGEON=builder.comment("Use modern simple_dungeon rewards for Pech high-tier trades. Default false preserves TC4's original entry table and rejection rules. Both modes retain random rewards.").translation("thaumcraft.configuration.compatibility.modernPechDungeon").define("modernPechDungeon",false);
        CHAMPION_MOBS=builder.comment("Original champion selection and thirteen modifiers for naturally spawned monsters; bosses retain their forced modifiers.").translation("thaumcraft.configuration.compatibility.championMobs").define("championMobs",true);
        WARDED_STONE=builder.comment("Original allow_warded_stone: protect arcane doors, warded glass and arcane pressure plates from mining. Their owner can remove them with a wand. Disable to allow ordinary mining.").translation("thaumcraft.configuration.compatibility.wardedStone").define("wardedStone",true);
        RAW_ORE_PURIFICATION=builder.comment("Allow raw iron, gold and copper in TC4 purification recipes and special ore drops. Ore blocks remain supported when disabled.").translation("thaumcraft.configuration.compatibility.rawOrePurification").define("rawOrePurification",true);
        WUSS_MODE=builder.comment("Original TC4 wuss mode: disable new warp and periodic warp events. Existing knowledge is retained.").translation("thaumcraft.configuration.compatibility.wussMode").define("wussMode",false);
        MODERN_WEAPON_COOLDOWN=builder.comment("Use Minecraft 1.21.1 attack cooldowns for Thaumcraft weapons. Disable for the original TC4 attack frequency.").translation("thaumcraft.configuration.compatibility.modernWeaponCooldown").define("modernWeaponCooldown",true);
        MODERN_BLUE_DYE=builder.comment("Directional essentia tubes use modern blue dye by default; disable to require TC4 lapis lazuli.").translation("thaumcraft.configuration.compatibility.modernBlueDye").define("modernBlueDye",true);
        ENABLE_COPPER_CONTENT = builder
                .comment("Enable TC4 copper caps, purification and transmutation using vanilla copper.",
                        "Changing this setting never unregisters items or removes saved progress.")
                .translation("thaumcraft.configuration.compatibility.enableCopperContent").define("enableCopperContent", true);
        builder.pop();
        builder.push("world");
        GENERATE_STRUCTURES=builder.comment("Original ordered mound/ring/hilltop population followed by obsidian totems. Flat worlds do not receive these structures.").translation("thaumcraft.configuration.world.generateStructures").define("generateStructures",true);
        GENERATE_AURA=builder.translation("thaumcraft.configuration.world.generateAura").define("generateAura",true);
        GENERATE_INFUSED_STONE=builder.translation("thaumcraft.configuration.world.generateInfusedStone").define("generateInfusedStone",true);
        DIMENSION_BLACKLIST=builder.comment("Resource location and blacklist level separated by =. Levels 0 and 2 exclude trees, structures, ores and aura; other listed levels exclude trees and structures. Nether/End keep original special dimension dispatch.").defineListAllowEmpty("dimensionBlacklist",java.util.List.of(),value->value instanceof String text&&thaumcraft.world.PopulationPolicy.validBlacklist(text));
        BIOME_BLACKLIST=builder.comment("Biome resource location=level. Any listed level excludes trees; 0 and 2 also exclude ores.").defineListAllowEmpty("biomeBlacklist",java.util.List.of(),value->value instanceof String text&&thaumcraft.world.PopulationPolicy.validBlacklist(text));
        REGENERATE_STRUCTURES=builder.define("regenerateStructures",false);
        REGENERATE_AURA=builder.define("regenerateAura",false);
        REGENERATE_TREES=builder.define("regenerateTrees",false);
        REGENERATE_CINNABAR=builder.define("regenerateCinnabar",false);
        REGENERATE_AMBER=builder.define("regenerateAmber",false);
        REGENERATE_INFUSED_STONE=builder.define("regenerateInfusedStone",false);
        REGENERATION_VERSION=builder.comment("Changing this marker enables one additional configured regeneration pass per existing chunk. Default regeneration switches are off.").define("regenerationVersion","DEFAULT");
        GENERATE_CINNABAR=builder.comment("Original TC4 cinnabar: 18 single-block attempts per chunk between Y=0 and 50.").translation("thaumcraft.configuration.world.generateCinnabar").define("generateCinnabar",true);
        GENERATE_AMBER=builder.comment("Original TC4 amber: 20 single-block attempts per chunk within 24 blocks below the surface.").translation("thaumcraft.configuration.world.generateAmber").define("generateAmber",true);
        GENERATE_MAGIC_TREES=builder.comment("Generate original greatwood and silverwood trees.").translation("thaumcraft.configuration.world.generateMagicTrees").define("generateMagicTrees",true);
        DEEP_INFUSED_STONE=builder.comment("Allow infused stone below Y=0; disable for the original TC4 vertical range.").translation("thaumcraft.configuration.world.deepInfusedStone").define("deepInfusedStone",true);
        NODE_BIOME_CHANGES=builder.comment("Allow nodes and taint to change biomes in the selected mode.").translation("thaumcraft.configuration.world.nodeBiomeChanges").define("nodeBiomeChanges",true);
        BIOME_CHANGE_MODE=builder.comment("REAL_4X4: change native Minecraft biome columns at every height.",
                "TC4_1X1: exact block columns stored separately in this world's dimension data.",
                "Changing mode retains both maps; existing changes are not converted or deleted.")
                .translation("thaumcraft.configuration.world.biomeChangeMode").defineEnum("biomeChangeMode",BiomeChangeMode.REAL_4X4);
        BIOME_OVERLAY_GLOBAL=builder.comment("In TC4_1X1 mode, expose exact columns to general biome queries through the Mixin.",
                "Disable to use the overlay only for Thaumcraft mechanics. Native quart storage remains unchanged.")
                .translation("thaumcraft.configuration.world.biomeOverlayGlobal").define("biomeOverlayGlobal",true);
        HARD_MODE_NODES=builder.comment("Original TC4 hard_mode_nodes: hungry attraction/damage, dark spawns, tainted fibres.").translation("thaumcraft.configuration.world.hardModeNodes").define("hardModeNodes",true);
        NODE_RARITY=builder.translation("thaumcraft.configuration.world.nodeRarity").defineInRange("nodeRarity",36,1,100000);
        SPECIAL_NODE_RARITY=builder.translation("thaumcraft.configuration.world.specialNodeRarity").defineInRange("specialNodeRarity",18,2,100000);
        TAINT_SPREAD_RATE=builder.translation("thaumcraft.configuration.world.taintSpreadRate").defineInRange("taintSpreadRate",200,0,100000);
        TAINT_FROM_FLUX=builder.comment("Original TC4 taint_from_flux: concentrated flux goo can seed taint.").translation("thaumcraft.configuration.world.taintFromFlux").define("taintFromFlux",true);
        SPAWN_TAINT_SPORE=builder.comment("Original TC4 spawn_taint_spore: fibrous taint can spawn spore swarmers.").translation("thaumcraft.configuration.world.spawnTaintSpore").define("spawnTaintSpore",true);
        BIOME_CREATURE_SPAWNS=builder.comment("Allow natural TC4 creatures in changed biomes in both native and exact-column modes.","Disabling natural spawns retains existing creatures and direct node/taint effects.").translation("thaumcraft.configuration.world.biomeCreatureSpawns").define("biomeCreatureSpawns",true);
        LEGACY_EERIE_BAT_CATEGORY=builder.comment("Restore TC4's extra Eerie bat in the CREATURE category.","Default false uses AMBIENT to avoid NeoForge mob-cap category mismatch.").translation("thaumcraft.configuration.world.legacyEerieBatCreatureCategory").define("legacyEerieBatCreatureCategory",false);
        builder.pop();
        builder.push("research");
        ASPECT_TOTAL_CAP=builder.comment("TC4 aspect pool soft cap; rewards taper above this value.").translation("thaumcraft.configuration.research.aspectTotalCap").defineInRange("aspectTotalCap",100,1,32767);
        builder.pop();
        builder.push("runicShield");
        RUNIC_RECHARGE_MS=builder.comment("TC4 runic recharge interval in milliseconds before charged-ring reductions.").translation("thaumcraft.configuration.runicShield.rechargeSpeed").defineInRange("rechargeSpeed",2000,500,1000000);
        RUNIC_RECHARGE_DELAY=builder.comment("Ticks to wait after the shield is depleted.").translation("thaumcraft.configuration.runicShield.rechargeDelay").defineInRange("rechargeDelay",80,0,1000000);
        RUNIC_VIS_COST=builder.comment("Centivis of Aer and Terra consumed per restored shield point.").translation("thaumcraft.configuration.runicShield.visCost").defineInRange("visCost",50,0,1000000);
        builder.pop();
        SPEC = builder.build();
    }

    private ThaumcraftConfig() {}
}
