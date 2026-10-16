package thaumcraft.world;

import net.minecraft.core.*;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;

/** Owned vanilla 1.7.10 WorldGenBlockBlob and WorldGenBigMushroom algorithms. */
public final class LegacyForestDecorations {
    public static boolean boulder(WorldGenLevel level,RandomSource random,BlockPos pos){
        int x=pos.getX(),y=pos.getY(),z=pos.getZ();
        while(y>3){var below=level.getBlockState(new BlockPos(x,y-1,z));if(!below.isAir()&&(below.is(Blocks.GRASS_BLOCK)||below.is(Blocks.DIRT)||below.is(Blocks.STONE)))break;y--;}
        if(y<=3)return false;
        for(int i=0;i<3;i++){int rx=random.nextInt(2),ry=random.nextInt(2),rz=random.nextInt(2);float radius=(rx+ry+rz)*.333f+.5f;
            for(int xx=x-rx;xx<=x+rx;xx++)for(int zz=z-rz;zz<=z+rz;zz++)for(int yy=y-ry;yy<=y+ry;yy++){float dx=xx-x,dz=zz-z,dy=yy-y;if(dx*dx+dz*dz+dy*dy<=radius*radius)level.setBlock(new BlockPos(xx,yy,zz),Blocks.MOSSY_COBBLESTONE.defaultBlockState(),4);}
            x+=-1+random.nextInt(2);z+=-1+random.nextInt(2);y-=random.nextInt(2);
        }return true;
    }
    public static boolean mushroom(WorldGenLevel level,RandomSource random,BlockPos pos){
        int kind=random.nextInt(2),height=random.nextInt(3)+4,x=pos.getX(),y=pos.getY(),z=pos.getZ();
        if(y<level.getMinBuildHeight()+1||y+height+1>=level.getMaxBuildHeight())return false;
        for(int yy=y;yy<=y+1+height;yy++){int radius=yy<=y+3?0:3;for(int xx=x-radius;xx<=x+radius;xx++)for(int zz=z-radius;zz<=z+radius;zz++){var state=level.getBlockState(new BlockPos(xx,yy,zz));if(!state.isAir()&&!state.is(net.minecraft.tags.BlockTags.LEAVES))return false;}}
        var below=level.getBlockState(pos.below());if(!below.is(Blocks.DIRT)&&!below.is(Blocks.GRASS_BLOCK)&&!below.is(Blocks.MYCELIUM))return false;
        for(int yy=kind==1?y+height-3:y+height;yy<=y+height;yy++){
            int radius=kind==0?3:yy<y+height?2:1;
            for(int xx=x-radius;xx<=x+radius;xx++)for(int zz=z-radius;zz<=z+radius;zz++){
                int meta=5;if(xx==x-radius)meta--;if(xx==x+radius)meta++;if(zz==z-radius)meta-=3;if(zz==z+radius)meta+=3;
                if(kind==0||yy<y+height){
                    if((xx==x-radius||xx==x+radius)&&(zz==z-radius||zz==z+radius))continue;
                    if(xx==x-(radius-1)&&zz==z-radius||xx==x-radius&&zz==z-(radius-1))meta=1;
                    if(xx==x+(radius-1)&&zz==z-radius||xx==x+radius&&zz==z-(radius-1))meta=3;
                    if(xx==x-(radius-1)&&zz==z+radius||xx==x-radius&&zz==z+(radius-1))meta=7;
                    if(xx==x+(radius-1)&&zz==z+radius||xx==x+radius&&zz==z+(radius-1))meta=9;
                }
                if(meta==5&&yy<y+height)meta=0;
                var target=new BlockPos(xx,yy,zz);if(meta==0&&y<y+height-1||level.getBlockState(target).isSolidRender(level,target))continue;
                level.setBlock(target,cap(kind,meta),2);
            }
        }
        for(int i=0;i<height;i++){var target=pos.above(i);if(!level.getBlockState(target).isSolidRender(level,target))level.setBlock(target,Blocks.MUSHROOM_STEM.defaultBlockState().setValue(HugeMushroomBlock.UP,false).setValue(HugeMushroomBlock.DOWN,false),2);}
        return true;
    }
    private static BlockState cap(int kind,int meta){return (kind==0?Blocks.BROWN_MUSHROOM_BLOCK:Blocks.RED_MUSHROOM_BLOCK).defaultBlockState().setValue(HugeMushroomBlock.UP,meta!=0).setValue(HugeMushroomBlock.DOWN,false).setValue(HugeMushroomBlock.NORTH,meta<=3&&meta>=1).setValue(HugeMushroomBlock.SOUTH,meta>=7).setValue(HugeMushroomBlock.WEST,meta==1||meta==4||meta==7).setValue(HugeMushroomBlock.EAST,meta==3||meta==6||meta==9);}
    private LegacyForestDecorations(){}
}
