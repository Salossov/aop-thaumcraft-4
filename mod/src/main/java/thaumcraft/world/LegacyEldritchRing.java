package thaumcraft.world;

import net.minecraft.core.*;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.*;
import thaumcraft.blocks.ThaumicBannerBlock;
import thaumcraft.registry.*;
import thaumcraft.world.dim.LabyrinthData;

/** Original five asymmetric support tests and all ordered ring placements. */
public final class LegacyEldritchRing {
    public int chunkX,chunkZ,width,height;
    public static boolean validSurface(WorldGenLevel level,BlockPos pos){
        int distance=0;
        while(!level.getBlockState(pos.above(distance)).is(Blocks.AIR)){if(++distance>2)return false;}
        var ground=pos.above(distance-1);var state=level.getBlockState(ground);
        if(!level.getBlockState(ground.above()).is(Blocks.AIR))return false;
        if(substrate(state))return true;
        return (state.is(Blocks.SNOW)||state.is(Blocks.SHORT_GRASS)||state.is(Blocks.FERN))&&substrate(level.getBlockState(ground.below()));
    }
    private static boolean substrate(net.minecraft.world.level.block.state.BlockState s){return s.is(Blocks.STONE)||s.is(Blocks.SAND)||s.is(Blocks.PACKED_ICE)||s.is(Blocks.GRASS_BLOCK)||s.is(Blocks.GRAVEL)||s.is(Blocks.DIRT);}
    public boolean generate(WorldGenLevel level,RandomSource random,BlockPos origin){
        if(level.isOutsideBuildHeight(origin.below(4))||level.isOutsideBuildHeight(origin.above(7)))return false;
        for(var offset:new int[][]{{-3,-3},{0,0},{3,0},{3,3},{0,3}})if(!validSurface(level,origin.offset(offset[0],0,offset[1])))return false;
        var data=LabyrinthData.get(level.getLevel());
        synchronized(data){
            if(data.inRange(chunkX,chunkZ,width,height))return false;
            for(int x=-3;x<=3;x++)for(int z=-3;z<=3;z++){
                if(Math.abs(x)==3&&Math.abs(z)==3)continue;
                var pos=origin.offset(x,0,z);
                for(int q=-4;q<5;q++){
                    var target=pos.above(q);var state=level.getBlockState(target);
                    // TC4's banner used solid wood material even though its native thin model has no collision.
                    // Preserve that material test here, including the RNG draw it suppresses.
                    if(q<=0||state.canBeReplaced()||!state.blocksMotion()&&!state.is(R4Content.BANNER.get())||state.is(net.minecraft.tags.BlockTags.LEAVES))level.setBlock(target,(random.nextInt(4)==0?Blocks.MOSSY_COBBLESTONE:R4Content.OBSIDIAN_TILE.get()).defaultBlockState(),3);
                    if(q>0)level.setBlock(target,Blocks.AIR.defaultBlockState(),3);
                }
                if(x==0&&z==0){
                    level.setBlock(pos.above(),R3Content.ELDRITCH_ALTAR.get().defaultBlockState(),3);
                    level.setBlock(pos,R4Content.OBSIDIAN_TILE.get().defaultBlockState(),3);
                    int variation=random.nextInt(10);
                    if(level.getBlockEntity(pos.above()) instanceof thaumcraft.production.EldritchAltarBlockEntity altar){
                        if(variation>=1&&variation<=4){altar.setSpawner(true);altar.setSpawnType((byte)0);
                            for(var direction:new Direction[]{Direction.NORTH,Direction.SOUTH,Direction.WEST,Direction.EAST}){
                                var banner=pos.offset(-direction.getStepX()*3,1,direction.getStepZ()*3);
                                int facing=switch(direction){case NORTH->8;case SOUTH->0;case WEST->12;default->4;};
                                level.setBlock(banner,R4Content.BANNER.get().defaultBlockState().setValue(ThaumicBannerBlock.ROTATION,facing),3);
                            }
                        }else if(variation==6||variation==7){altar.setSpawner(true);altar.setSpawnType((byte)1);}
                    }
                    level.setBlock(pos.above(3),R5Content.OBELISK.get().defaultBlockState(),3);
                    for(int y=4;y<=7;y++)level.setBlock(pos.above(y),R5Content.ELDRITCH_BODY.get().defaultBlockState(),3);
                }else if((Math.abs(x)==3&&Math.abs(z%2)==1||Math.abs(z)==3&&Math.abs(x%2)==1)&&Math.abs(x)!=Math.abs(z)){
                    level.setBlock(pos,R4Content.OBSIDIAN_TILE.get().defaultBlockState(),3);
                    level.setBlock(pos.above(),R5Content.ELDRITCH_CAP.get().defaultBlockState(),3);
                }
            }
            return true;
        }
    }
}
