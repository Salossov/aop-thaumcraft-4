package thaumcraft.world;

import java.util.Random;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.features.TreeFeatures;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.*;
import thaumcraft.config.ThaumcraftConfig;
import thaumcraft.registry.R2Content;

/** BiomeGenTaint: two trees, mossy boulders, ten surface and eight sheltered fibre attempts. */
public final class LegacyTaintPopulation {
    public static void populate(WorldGenLevel level,RandomSource random,int chunkX,int chunkZ){
        int x=chunkX*16,z=chunkZ*16;
        // Original +8 decoration positions are recentered for the FEATURES writable 3x3 region.
        if(ThaumcraftConfig.GENERATE_MAGIC_TREES.get()){
            int count=2+(random.nextInt(10)==0?1:0);var big=new LegacyBigMagicTree(false);
            for(int i=0;i<count;i++){
                int xx=x+random.nextInt(16),zz=z+random.nextInt(16),yy=LegacyNaturalPopulation.surface(level,xx,zz);
                if(random.nextInt(8)==0){big.func_76487_a(1,1,1);big.generate(level,new Random(random.nextLong()),xx,yy,zz);}
                else{var key=random.nextInt(10)==0?TreeFeatures.FANCY_OAK:TreeFeatures.OAK;level.registryAccess().registryOrThrow(Registries.CONFIGURED_FEATURE).getHolderOrThrow(key).value().place(level,level.getLevel().getChunkSource().getGenerator(),random,new BlockPos(xx,yy,zz));}
            }
        }
        int boulders=random.nextInt(3);
        for(int i=0;i<boulders;i++){int xx=x+random.nextInt(16),zz=z+random.nextInt(16);LegacyForestDecorations.boulder(level,random,new BlockPos(xx,LegacyNaturalPopulation.surface(level,xx,zz),zz));}
        for(int i=0;i<10;i++){
            int xx=x+random.nextInt(16),zz=z+random.nextInt(16),yy=LegacyNaturalPopulation.surface(level,xx,zz)-1;var at=new BlockPos(xx,yy,zz);var state=level.getBlockState(at);
            if(state.is(Blocks.GRASS_BLOCK))fibre(level,at.above());
            else if(state.canBeReplaced()&&level.getBlockState(at.below()).is(Blocks.GRASS_BLOCK))fibre(level,at);
        }
        for(int i=0;i<8;i++){
            int xx=x+random.nextInt(16),zz=z+random.nextInt(16),yy=level.getMinBuildHeight();
            // Utils.getFirstUncoveredBlockHeight starts at y=0 in TC4; adapt only world minimum.
            yy=Math.max(0,yy);while(yy<level.getMaxBuildHeight()-2&&!level.isEmptyBlock(new BlockPos(xx,yy+1,zz)))yy++;
            var at=new BlockPos(xx,yy+1,zz);if(level.isEmptyBlock(at))fibre(level,at);
        }
    }
    private static void fibre(WorldGenLevel level,BlockPos pos){
        var state=R2Content.TAINT_FIBRES.get().defaultBlockState();boolean supported=false;
        for(var side:Direction.values()){var neighbor=pos.relative(side);boolean face=level.getBlockState(neighbor).isFaceSturdy(level,neighbor,side.getOpposite());state=state.setValue(MultifaceBlock.getFaceProperty(side),face);supported|=face;}
        if(supported)level.setBlock(pos,state,2);
    }
    private LegacyTaintPopulation(){}
}
