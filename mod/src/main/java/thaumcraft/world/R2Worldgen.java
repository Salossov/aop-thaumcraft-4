package thaumcraft.world;

import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.*;
import net.minecraft.world.level.levelgen.feature.configurations.*;
import net.minecraft.world.level.levelgen.structure.templatesystem.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.*;
import thaumcraft.api.aspects.*;
import thaumcraft.config.ThaumcraftConfig;
import thaumcraft.registry.R2Content;

public final class R2Worldgen {
    private static final DeferredRegister<Feature<?>> FEATURES=DeferredRegister.create(Registries.FEATURE,"thaumcraft");
    public static final DeferredHolder<Feature<?>,Feature<NoneFeatureConfiguration>> INFUSED=FEATURES.register("infused_stone",()->new Feature<>(NoneFeatureConfiguration.CODEC) {
        @Override public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
            var world=context.level();var random=context.random();var origin=context.origin();boolean placed=false;
            for(int i=0;i<8;i++) {
                int x=origin.getX()+random.nextInt(16),z=origin.getZ()+random.nextInt(16);int floor=ThaumcraftConfig.DEEP_INFUSED_STONE.get()?world.getMinBuildHeight():0;
                int ceiling=Math.max(floor+5,world.getHeight(Heightmap.Types.WORLD_SURFACE_WG,x,z)-5);int y=floor+random.nextInt(ceiling-floor);
                int element=random.nextInt(6);if(random.nextInt(3)==0) {var aspect=BiomeProfiles.randomAspect(world.getBiome(new BlockPos(x,y,z)),random);if(aspect==null) element=random.nextInt(6);else if(aspect.isPrimal()) element=Aspects.PRIMALS.indexOf(aspect);}
                var block=R2Content.INFUSED_STONE.get(element).get().defaultBlockState();
                placed|=LegacyInfusedVein.place(world,random,new BlockPos(x,y,z),block,ThaumcraftConfig.DEEP_INFUSED_STONE.get());
            }return placed;
        }
    });
    public static final DeferredHolder<Feature<?>,Feature<NoneFeatureConfiguration>> NODE=FEATURES.register("aura_node",()->new Feature<>(NoneFeatureConfiguration.CODEC) {
        @Override public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
            if(context.random().nextInt(ThaumcraftConfig.NODE_RARITY.get())!=0) return false;
            var world=context.level();var random=context.random();int x=context.origin().getX()+random.nextInt(16),z=context.origin().getZ()+random.nextInt(16);
            // TC4 starts at Y=5 and finds the first uncovered block, including a cave.
            int y=5;while(y<world.getMaxBuildHeight()-1&&!world.isEmptyBlock(new BlockPos(x,y+1,z))) y++;
            if(world.isEmptyBlock(new BlockPos(x,y+1,z))) y++;
            int offset=random.nextInt(4);var candidate=new BlockPos(x,y+offset,z);
            if(world.isEmptyBlock(candidate)||world.getBlockState(candidate).canBeReplaced()) y+=offset;
            if(y>=world.getMaxBuildHeight()) return false;var pos=new BlockPos(x,y,z);
            return NodeFactory.create(world,pos,random);
        }
    });
    public static void register(IEventBus bus) { FEATURES.register(bus); }
    private R2Worldgen() {}
}
