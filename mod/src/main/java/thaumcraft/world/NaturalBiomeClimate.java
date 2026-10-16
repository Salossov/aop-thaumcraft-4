package thaumcraft.world;

import com.mojang.datafixers.util.Pair;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.*;

/**
 * Modern climate adaptation of TC4's WARM/COOL biome groups.
 * Instead of slicing the humidity axis inside each vanilla cell (which drew the new biomes as
 * thin bands exactly on vanilla boundaries), each TC4 biome gets its own contiguous climate
 * parameter point in otherwise-unclaimed ranges, producing large blob biomes like the original.
 */
public final class NaturalBiomeClimate {
    public static final int FOREST_WEIGHT=5, TAINT_WEIGHT=2, FALLBACK_VANILLA_WEIGHT=70;
    /** Broad temperate forest-climate range for the magical forest blob. */
    private static final Climate.ParameterPoint MAGICAL_POINT=new Climate.ParameterPoint(
        param(Climate.quantizeCoord(-0.4f),Climate.quantizeCoord(0.4f)),   // temperature: temperate
        param(Climate.quantizeCoord(-1f),Climate.quantizeCoord(0f)),     // humidity: drier half -> fewer vanilla collisions
        param(Climate.quantizeCoord(0.03f),Climate.quantizeCoord(1f)),   // continentalness: inland
        param(Climate.quantizeCoord(-1f),Climate.quantizeCoord(0.3f)),   // erosion: hills/forests
        Climate.Parameter.point(0f),                                                     // depth: surface
        param(Climate.quantizeCoord(-1f),Climate.quantizeCoord(1f)),   // weirdness
        0);
    /** Hot, humid, low-lying swath for the tainted land blob. */
    private static final Climate.ParameterPoint TAINT_POINT=new Climate.ParameterPoint(
        param(Climate.quantizeCoord(-0.45f),Climate.quantizeCoord(0.55f)),  // temperature: warm
        param(Climate.quantizeCoord(0f),Climate.quantizeCoord(1f)),      // humidity: wet half
        param(Climate.quantizeCoord(-0.2f),Climate.quantizeCoord(0.3f)),  // continentalness: coast/inland mix
        param(Climate.quantizeCoord(0.3f),Climate.quantizeCoord(1f)),    // erosion: flat
        Climate.Parameter.point(0f),                                                     // depth: surface
        param(Climate.quantizeCoord(-1f),Climate.quantizeCoord(1f)),   // weirdness
        0);
    private static Climate.Parameter param(long a,long b){return new Climate.Parameter(Math.min(a,b),Math.max(a,b));}
    /**
     * Dedicated TC4 climate regions. Each occupies a contiguous swath instead of a thin
     * humidity band inside a vanilla cell, so magical forest and tainted land form large
     * blobs rather than distorted edge patches. Emitted once per parameter list, never
     * per vanilla cell.
     */
    public static List<Pair<Climate.ParameterPoint,ResourceKey<Biome>>> dedicated(){
        return List.of(Pair.of(MAGICAL_POINT,WorldBiomes.MAGICAL),Pair.of(TAINT_POINT,WorldBiomes.TAINT));
    }
    /**
     * Pure passthrough. Dedicated TC4 regions are appended once by the caller
     * (NaturalBiomePresetMixin / TerraBlenderBiomes), not per vanilla cell — per-cell
     * emission produced dozens of duplicate parameter points.
     */
    public static void map(Pair<Climate.ParameterPoint,ResourceKey<Biome>> entry,int vanillaWeight,
            ResourceKey<Biome> unchanged,Consumer<Pair<Climate.ParameterPoint,ResourceKey<Biome>>> out) {
        out.accept(Pair.of(entry.getFirst(),unchanged));
    }
    public static List<Pair<Climate.ParameterPoint,ResourceKey<Biome>>> vanilla() {
        return MultiNoiseBiomeSourceParameterList.knownPresets().get(MultiNoiseBiomeSourceParameterList.Preset.OVERWORLD).values();
    }
    private NaturalBiomeClimate(){}
}
