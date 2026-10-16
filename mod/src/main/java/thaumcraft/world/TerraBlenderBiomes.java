package thaumcraft.world;

import com.mojang.datafixers.util.Pair;
import java.util.function.Consumer;
import net.minecraft.core.Registry;
import net.minecraft.resources.*;
import net.minecraft.world.level.biome.*;
import terrablender.api.*;

/** Loaded exclusively when TerraBlender is installed. */
public final class TerraBlenderBiomes {
    public static final ResourceLocation ID=ResourceLocation.fromNamespaceAndPath("thaumcraft","overworld");
    public static void register(){Regions.register(new TC4Region());}
    private static final class TC4Region extends Region {
        TC4Region(){super(ID,RegionType.OVERWORLD,1);}
        @Override public void addBiomes(Registry<Biome> registry,Consumer<Pair<Climate.ParameterPoint,ResourceKey<Biome>>> mapper){
            registry.getHolderOrThrow(WorldBiomes.MAGICAL);registry.getHolderOrThrow(WorldBiomes.TAINT);
            for(var dedicated:NaturalBiomeClimate.dedicated())mapper.accept(dedicated);
        }
    }
    private TerraBlenderBiomes(){}
}
