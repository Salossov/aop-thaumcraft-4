package thaumcraft.data;

import java.util.*;

/** Legacy book keys resolve to native recipe IDs; original R3 keys retain their lowercase ID. */
public final class ResearchRecipePages {
    private static final Map<String,String> RECIPES=Map.ofEntries(Map.entry("Goggles","goggles_of_revealing"),Map.entry("Thaumometer","thaumometer"),Map.entry("Table","table"),Map.entry("ScribingTools","scribing_tools"),Map.entry("Scribe1","scribing_tools"),Map.entry("Scribe2","scribing_tools"),Map.entry("Scribe3","scribing_tools"),Map.entry("WandCapIron","iron_wand_cap"),Map.entry("WandBasic","iron_capped_wooden_wand"),Map.entry("ArcTable","@arcane_workbench"),Map.entry("ResTable","@research_table"),Map.entry("Thaumonomicon","@thaumonomicon"),Map.entry("Alumentum","alumentum"),Map.entry("Nitor","nitor"),Map.entry("Thaumium","thaumium_ingot"),Map.entry("Tallow","tallow"),Map.entry("BalancedShard_0","balanced_shard_0"),Map.entry("BalancedShard_1","balanced_shard_1"),Map.entry("BalancedShard_2","balanced_shard_2"),Map.entry("BalancedShard_3","balanced_shard_3"),Map.entry("BalancedShard_4","balanced_shard_4"),Map.entry("BalancedShard_5","balanced_shard_5"),Map.entry("WardedJar","warded_jar"),Map.entry("JarVoid","void_jar"),Map.entry("Filter","vis_filter"),Map.entry("AlchemyFurnace","alchemy_furnace"),Map.entry("Alembic","arcane_alembic"),Map.entry("Bellows","arcane_bellows"),Map.entry("Tube","essentia_tube"),Map.entry("Resonator","essentia_resonator"),Map.entry("TubeValve","essentia_valve"),Map.entry("TubeFilter","filtered_essentia_tube"),Map.entry("TubeRestrict","restricted_essentia_tube"),Map.entry("ArcaneStone1","arcane_stone"),Map.entry("TubeOneway","directional_essentia_tube"),Map.entry("InfusionAltar","@infusion_altar"),Map.entry("InfusionMatrix","infusion_matrix"),Map.entry("ArcanePedestal","arcane_pedestal"),Map.entry("Pedestal","arcane_pedestal"),Map.entry("Centrifuge","alchemical_centrifuge"),Map.entry("TubeBuffer","essentia_buffer"),Map.entry("EssentiaCrystal","essentia_crystallizer"),Map.entry("Thaumatorium","@thaumatorium"),Map.entry("MnemonicMatrix","thaumatorium_brainbox"),Map.entry("AlchemicalConstruct","alchemical_construct"));
    private static final Map<String,String> ADDITIONAL=Map.ofEntries(
        Map.entry("ArcaneDoor","arcane_door"),Map.entry("WardedGlass","warded_glass"),Map.entry("IronKey","iron_key"),Map.entry("GoldKey","gold_key"),Map.entry("ArcanePressurePlate","arcane_pressure_plate"),
        Map.entry("BottleTaint","bottle_taint"),Map.entry("BoneBow","bone_bow"),Map.entry("PrimalArrow_0","air_primal_arrow"),Map.entry("PrimalArrow_1","fire_primal_arrow"),Map.entry("PrimalArrow_2","water_primal_arrow"),Map.entry("PrimalArrow_3","earth_primal_arrow"),Map.entry("PrimalArrow_4","order_primal_arrow"),Map.entry("PrimalArrow_5","entropy_primal_arrow"),
        Map.entry("ArcaneEar","arcane_ear"),Map.entry("Levitator","levitator"),
        Map.entry("ArcaneSpa","arcane_spa"),Map.entry("AdvAlchemyConstruct","advanced_alchemical_construct"),Map.entry("AdvAlchemyFurnace","@advanced_alchemy_furnace"),
        Map.entry("ArcaneStone3","arcane_stone_stairs"),Map.entry("TallowCandle","white_tallow_candle"),
        Map.entry("PaveTravel","paving_stone_travel"),Map.entry("PaveWard","paving_stone_warding"),
        Map.entry("ArcaneStone2","arcane_stone_bricks"),Map.entry("EssentiaCrystalizer","essentia_crystallizer"),
        Map.entry("JarLabel","jar_label"),Map.entry("PlankGreatwood","greatwood_planks"),Map.entry("PlankSilverwood","silverwood_planks"),
        Map.entry("BlockFlesh","tainted_flesh"),Map.entry("BlockThaumium","thaumium_block"),Map.entry("BlockTallow","tallow_block"),Map.entry("Phial","empty_phial"),
        Map.entry("NodeJar","@node_jar"),Map.entry("InfernalFurnace","@infernal_furnace"));
    public static String resolve(String key){return RECIPES.getOrDefault(key,ADDITIONAL.getOrDefault(key,key.toLowerCase(Locale.ROOT)));}
    private ResearchRecipePages(){}
}
