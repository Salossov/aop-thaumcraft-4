package thaumcraft.world;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.*;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.*;
import thaumcraft.registry.R3Content;
import thaumcraft.config.ThaumcraftConfig;
public final class R3Worldgen {
    private static final DeferredRegister<Feature<?>> FEATURES=DeferredRegister.create(Registries.FEATURE,"thaumcraft");
    public static final DeferredHolder<Feature<?>,Feature<NoneFeatureConfiguration>> MATERIAL_ORES=FEATURES.register("material_ores",()->new Feature<>(NoneFeatureConfiguration.CODEC){
        @Override public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> c){var l=c.level();var r=c.random();var o=c.origin();boolean placed=false;
            if(ThaumcraftConfig.GENERATE_CINNABAR.get())for(int i=0;i<18;i++){var p=new BlockPos(o.getX()+r.nextInt(16),r.nextInt(51),o.getZ()+r.nextInt(16));if(l.getBlockState(p).is(Blocks.STONE)){l.setBlock(p,R3Content.CINNABAR_ORE.get().defaultBlockState(),2);placed=true;}}
            if(ThaumcraftConfig.GENERATE_AMBER.get())for(int i=0;i<20;i++){int x=o.getX()+r.nextInt(16),z=o.getZ()+r.nextInt(16);var p=new BlockPos(x,l.getHeight(Heightmap.Types.WORLD_SURFACE_WG,x,z)-r.nextInt(25),z);if(l.getBlockState(p).is(Blocks.STONE)){l.setBlock(p,R3Content.AMBER_ORE.get().defaultBlockState(),2);placed=true;}}
            return placed;
        }
    });
    public static final DeferredHolder<Feature<?>,Feature<NoneFeatureConfiguration>> GREATWOOD_TREE=FEATURES.register("greatwood_tree",()->new Feature<>(NoneFeatureConfiguration.CODEC){public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> c){var p=c.origin();return new LegacyGreatwoodTree().generate(c.level(),new java.util.Random(c.random().nextLong()),p.getX(),p.getY(),p.getZ(),false);}});
    public static final DeferredHolder<Feature<?>,Feature<NoneFeatureConfiguration>> SILVERWOOD_TREE=FEATURES.register("silverwood_tree",()->new Feature<>(NoneFeatureConfiguration.CODEC){public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> c){var p=c.origin();return new LegacySilverwoodTree(true,7,5).func_76484_a(c.level(),c.random(),p.getX(),p.getY(),p.getZ());}});
    public static final DeferredHolder<Feature<?>,Feature<NoneFeatureConfiguration>> MAGIC_VEGETATION=FEATURES.register("magic_vegetation",()->new Feature<>(NoneFeatureConfiguration.CODEC){
        @Override public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context){
            var origin=context.origin();var level=context.level();
            LegacyNaturalPopulation.decorate(level,context.random(),origin.getX()>>4,origin.getZ()>>4);
            return true;
        }
    });
    public static void register(IEventBus bus){FEATURES.register(bus);}
    public static final DeferredHolder<Feature<?>,Feature<NoneFeatureConfiguration>> MOUND=FEATURES.register("mound",()->new Feature<>(NoneFeatureConfiguration.CODEC){public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> c){if(!new LegacyMound().generate(c.level(),c.random(),c.origin()))return false;c.random().nextInt(200);NodeFactory.createStructure(c.level(),c.origin().offset(9,8,9),c.random());return true;}});
    public static final DeferredHolder<Feature<?>,Feature<NoneFeatureConfiguration>> HILLTOP_STONES=FEATURES.register("hilltop_stones",()->new Feature<>(NoneFeatureConfiguration.CODEC){public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> c){if(!new LegacyHilltopStones().generate(c.level(),c.random(),c.origin()))return false;NodeFactory.createStructure(c.level(),c.origin().above(5),c.random());return true;}});
    private R3Worldgen(){}
}
