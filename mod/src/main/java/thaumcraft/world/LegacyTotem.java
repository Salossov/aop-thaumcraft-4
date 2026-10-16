package thaumcraft.world;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import thaumcraft.config.ThaumcraftConfig;
import thaumcraft.registry.R4Content;

/** Includes TC4's unconditional false return and fallback node above the fourth body. */
public final class LegacyTotem {
    public static boolean generate(WorldGenLevel level,RandomSource random,int chunkX,int chunkZ,boolean auraGen,boolean newGen){
        if(!ThaumcraftConfig.GENERATE_STRUCTURES.get()||!(level.getLevel().dimension().equals(Level.OVERWORLD)||level.getLevel().dimension().equals(Level.END))||!newGen||auraGen||random.nextInt(ThaumcraftConfig.NODE_RARITY.get()*10)!=0)return false;
        int x=chunkX*16+random.nextInt(16),z=chunkZ*16+random.nextInt(16),y=level.getHeight(Heightmap.Types.MOTION_BLOCKING,x,z)-1;
        if(y>level.getMaxBuildHeight())return false;
        if(level.getBlockState(new BlockPos(x,y,z)).is(BlockTags.LEAVES))while(--y>40&&!level.getBlockState(new BlockPos(x,y,z)).is(Blocks.GRASS_BLOCK)){}
        var base=new BlockPos(x,y,z);if(clear(level,base)&&!level.isEmptyBlock(base))base=base.below();
        var ground=level.getBlockState(base);
        if(!(ground.is(Blocks.GRASS_BLOCK)||ground.is(Blocks.SAND)||ground.is(Blocks.DIRT)||ground.is(Blocks.STONE)||ground.is(Blocks.NETHERRACK)))return false;
        int count=1;while(clear(level,base.above(count))&&count<3)count++;
        if(count<2)return false;
        level.setBlock(base,R4Content.OBSIDIAN_TILE.get().defaultBlockState(),3);
        count=1;
        while(clear(level,base.above(count))&&count<5){
            put(level,base.above(count),false);
            if(count>1&&random.nextInt(4)==0){put(level,base.above(count),true);NodeFactory.createTotem(level,base.above(count),random);count=5;auraGen=true;}
            if(++count>=5&&!auraGen){put(level,base.above(5),true);NodeFactory.createTotem(level,base.above(5),random);}
        }
        return false;
    }
    private static boolean clear(WorldGenLevel level,BlockPos pos){var s=level.getBlockState(pos);return s.isAir()||s.is(Blocks.SNOW)||s.is(Blocks.SHORT_GRASS)||s.is(Blocks.FERN);}
    private static void put(WorldGenLevel level,BlockPos pos,boolean node){var block=node?R4Content.OBSIDIAN_TOTEM_NODE.get():R4Content.OBSIDIAN_TOTEM.get();level.setBlock(pos,block.at(level,pos),3);}
    private LegacyTotem(){}
}
