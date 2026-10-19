package thaumcraft.world;

import net.minecraft.core.*;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.*;
import thaumcraft.entities.R2Entities;
import thaumcraft.registry.R4Content;

public final class LegacyHilltopStones {
    public boolean generate(WorldGenLevel level,RandomSource random,BlockPos origin){return generate(level,random,origin,false);}
    public boolean generateNative(WorldGenLevel level,RandomSource random,BlockPos origin){return generate(level,random,origin,true);}
    private boolean generate(WorldGenLevel level,RandomSource random,BlockPos origin,boolean prepared){
        if(origin.getY()<85||level.isOutsideBuildHeight(origin.below(4))||level.isOutsideBuildHeight(origin.above(5)))return false;
        if(!prepared)for(var offset:new int[][]{{-2,-2},{0,0},{2,0},{2,2},{0,2}})if(!AncientStructureSupport.validSurface(level,origin.offset(offset[0],0,offset[1])))return false;
        var top=level.getBlockState(origin);if(top.isAir()||top.is(Blocks.SNOW)||top.is(Blocks.SHORT_GRASS)||top.is(Blocks.FERN))top=level.getBlockState(origin.below());
        boolean vines=level.getBiome(origin).value().getPrecipitationAt(origin)!=net.minecraft.world.level.biome.Biome.Precipitation.SNOW;
        for(int x=-3;x<=3;x++)for(int z=-3;z<=3;z++){
            if(Math.abs(x)==3&&Math.abs(z)==3)continue;var p=origin.offset(x,0,z);
            level.setBlock(p,(random.nextBoolean()?R4Content.OBSIDIAN_TILE.get():Blocks.MOSSY_COBBLESTONE).defaultBlockState(),3);
            boolean stop=false;
            for(int y=1;y<5;y++){
                var below=p.below(y);var state=level.getBlockState(below);
                if(state.isAir()||state.is(Blocks.SNOW)||state.is(Blocks.POPPY)||state.is(Blocks.DANDELION)||state.is(Blocks.SHORT_GRASS)||state.is(Blocks.FERN))level.setBlock(below,top,3);
                if(x==0&&z==0&&y==1){level.setBlock(p.above(),R4Content.OBSIDIAN_TILE.get().defaultBlockState(),3);AncientStructureSupport.chest(level,p.above(2),false,random);AncientStructureSupport.spawner(level,p,R2Entities.WISP.get(),random);}
                if(stop||!(Math.abs(x)==3&&Math.abs(z%2)==1||Math.abs(z)==3&&Math.abs(x%2)==1))continue;
                level.setBlock(p.above(y),R4Content.OBSIDIAN_TOTEM.get().at(level,p.above(y)),3);
                if(y<2||!random.nextBoolean())continue;stop=true;if(!vines)continue;
                for(var direction:new Direction[]{Direction.WEST,Direction.EAST,Direction.NORTH,Direction.SOUTH}){var v=p.above(y).relative(direction);if(random.nextInt(3)==0&&level.isEmptyBlock(v))growVines(level,v,direction.getOpposite());}
            }
        }
        // WorldGenRegion does not propagate neighbor shapes while placing the columns.
        for(int x=-3;x<=3;x++)for(int z=-3;z<=3;z++)for(int y=1;y<=4;y++){
            var p=origin.offset(x,y,z);
            if(level.getBlockState(p).is(R4Content.OBSIDIAN_TOTEM))level.setBlock(p,R4Content.OBSIDIAN_TOTEM.get().at(level,p),2);
        }
        return true;
    }
    private static void growVines(WorldGenLevel level,BlockPos pos,Direction support){var state=Blocks.VINE.defaultBlockState().setValue(VineBlock.getPropertyForFace(support),true);level.setBlock(pos,state,3);for(int y=1;y<=4&&level.isEmptyBlock(pos.below(y));y++)level.setBlock(pos.below(y),state,3);}
}
