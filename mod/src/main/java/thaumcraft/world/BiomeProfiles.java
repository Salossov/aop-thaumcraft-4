package thaumcraft.world;

import java.util.*;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.util.RandomSource;
import thaumcraft.api.aspects.*;

/** Explicit legacy biome mapping; new unknown biomes retain TC4's 100/null fallback. */
public final class BiomeProfiles {
    private record Type(int aura,Aspect aspect) {}
    private static final Map<String,Type> TYPES=new LinkedHashMap<>();
    private static final Map<String,List<String>> BIOMES=new HashMap<>();
    static {
        type("HOT",100,Aspects.FIRE);type("COLD",80,Aspects.ORDER);type("SPARSE",80,Aspects.ENTROPY);type("DENSE",100,Aspects.ORDER);
        type("WET",80,Aspects.WATER);type("DRY",80,Aspects.ENTROPY);type("SAVANNA",80,Aspects.AIR);type("CONIFEROUS",100,Aspects.EARTH);
        type("JUNGLE",100,Aspects.PLANT);type("SPOOKY",80,Aspects.SOUL);type("DEAD",50,Aspects.DEATH);type("LUSH",100,Aspects.PLANT);
        type("NETHER",120,Aspects.FIRE);type("END",80,Aspects.VOID);type("MUSHROOM",140,Aspects.ORDER);type("MAGICAL",100,null);
        type("OCEAN",120,Aspects.WATER);type("RIVER",100,Aspects.WATER);type("WATER",100,Aspects.WATER);type("MESA",80,Aspects.FIRE);
        type("FOREST",120,Aspects.EARTH);type("PLAINS",80,Aspects.AIR);type("MOUNTAIN",100,Aspects.AIR);type("HILLS",120,Aspects.AIR);
        type("SWAMP",120,Aspects.ENTROPY);type("SANDY",80,Aspects.EARTH);type("SNOWY",80,Aspects.ORDER);type("WASTELAND",80,Aspects.ENTROPY);type("BEACH",80,Aspects.EARTH);
        profile("ocean,deep_ocean","OCEAN");profile("plains,sunflower_plains","PLAINS");profile("desert","HOT,DRY,SANDY");
        profile("windswept_hills","MOUNTAIN,HILLS");profile("forest,birch_forest,old_growth_birch_forest,flower_forest","FOREST");
        profile("taiga,old_growth_pine_taiga,old_growth_spruce_taiga","COLD,CONIFEROUS,FOREST");profile("swamp","WET,SWAMP");profile("river","RIVER");
        profile("frozen_ocean,deep_frozen_ocean","COLD,OCEAN,SNOWY");profile("frozen_river","COLD,RIVER,SNOWY");profile("snowy_plains,ice_spikes","COLD,SNOWY,WASTELAND");
        profile("beach,stony_shore","BEACH");profile("snowy_beach","COLD,BEACH,SNOWY");profile("jungle","HOT,WET,DENSE,JUNGLE");
        profile("sparse_jungle","HOT,WET,JUNGLE,FOREST");profile("the_end","COLD,DRY,END");profile("nether_wastes","HOT,DRY,NETHER");
        profile("mushroom_fields","MUSHROOM");profile("dark_forest","SPOOKY,DENSE,FOREST");profile("snowy_taiga","COLD,CONIFEROUS,FOREST,SNOWY");
        profile("windswept_forest","MOUNTAIN,FOREST,SPARSE");profile("savanna,savanna_plateau,windswept_savanna","HOT,SAVANNA,PLAINS,SPARSE");
        profile("badlands,eroded_badlands","MESA,SANDY");profile("wooded_badlands","MESA,SPARSE,SANDY");
        BIOMES.put("thaumcraft:eerie",ordered("SPOOKY,MAGICAL"));BIOMES.put("thaumcraft:magical_forest",ordered("FOREST,MAGICAL"));BIOMES.put("thaumcraft:tainted_land",ordered("MAGICAL,WASTELAND"));
    }
    private static void type(String key,int aura,Aspect aspect) { TYPES.put(key,new Type(aura,aspect)); }
    private static List<String> ordered(String csv) { var set=Set.of(csv.split(","));return TYPES.keySet().stream().filter(set::contains).toList(); }
    private static void profile(String biomes,String types) { for(var biome:biomes.split(",")) BIOMES.put("minecraft:"+biome,ordered(types)); }
    private static List<String> types(Holder<Biome> biome) { return biome.unwrapKey().map(key->BIOMES.getOrDefault(key.location().toString(),List.of())).orElse(List.of()); }
    public static int aura(Holder<Biome> biome) { var types=types(biome);return types.isEmpty()?100:types.stream().mapToInt(t->TYPES.get(t).aura).sum()/types.size(); }
    public static Aspect randomAspect(Holder<Biome> biome,RandomSource random) { var types=types(biome);return types.isEmpty()?null:TYPES.get(types.get(random.nextInt(types.size()))).aspect; }
    public static float greatwoodChance(Holder<Biome> biome){for(var type:types(biome)){switch(type){case "CONIFEROUS","SAVANNA","PLAINS","SWAMP":return .2f;case "FOREST","MAGICAL":return 1;case "LUSH":return .5f;default:}}return 0;}
    public static boolean supportsSilverwood(Holder<Biome> biome){return !types(biome).contains("MAGICAL")&&!biome.is(net.minecraft.resources.ResourceLocation.parse("minecraft:forest"))&&!biome.is(net.minecraft.resources.ResourceLocation.parse("minecraft:birch_forest"));}
    private BiomeProfiles() {}
}
