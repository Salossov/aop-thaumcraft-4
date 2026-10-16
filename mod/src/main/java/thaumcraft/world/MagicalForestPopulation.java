package thaumcraft.world;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import thaumcraft.config.ThaumcraftConfig;
import thaumcraft.registry.R5Content;

/** TC4 additions around the approved native 1.21.1 forest decoration base. */
public final class MagicalForestPopulation {
    public static void populate(WorldGenLevel level,RandomSource random,int chunkX,int chunkZ){
        // TC4 adds +8 to decoration positions. During modern FEATURES, shift
        // that origin back so wide crowns stay inside its writable 3x3 chunks.
        int offset=level instanceof net.minecraft.server.level.WorldGenRegion?-8:0;
        int x=chunkX*16+offset,z=chunkZ*16+offset;
        int boulders=random.nextInt(3);
        for(int i=0;i<boulders;i++){int xx=x+random.nextInt(16)+8,zz=z+random.nextInt(16)+8;LegacyForestDecorations.boulder(level,random,new BlockPos(xx,LegacyNaturalPopulation.surface(level,xx,zz),zz));}
        for(int a=0;a<4;a++)for(int b=0;b<4;b++){int xx=x+a*4+9+random.nextInt(3),zz=z+b*4+9+random.nextInt(3),yy=LegacyNaturalPopulation.surface(level,xx,zz);if(random.nextInt(40)==0)LegacyForestDecorations.mushroom(level,random,new BlockPos(xx,yy,zz));}
        if(ThaumcraftConfig.GENERATE_MAGIC_TREES.get()){
            int trees=6+(random.nextInt(10)==0?1:0);var bigTree=new LegacyBigMagicTree(false);var adapter=new Random(){@Override public long nextLong(){return random.nextLong();}@Override public int nextInt(int bound){return random.nextInt(bound);}@Override public float nextFloat(){return random.nextFloat();}};
            for(int i=0;i<trees;i++){
                int xx=x+random.nextInt(16)+8,zz=z+random.nextInt(16)+8,yy=LegacyNaturalPopulation.surface(level,xx,zz);
                if(random.nextInt(14)==0)new LegacySilverwoodTree(false,8,5).func_76484_a(level,random,xx,yy,zz);
                else if(random.nextInt(10)==0)new LegacyGreatwoodTree().generate(level,adapter,xx,yy,zz,false);
                else{bigTree.func_76487_a(1,1,1);bigTree.generate(level,adapter,xx,yy,zz);}
            }
        }
        for(int i=0;i<10;i++)LegacyManaPods.generate(level,random,x+random.nextInt(16)+8,64,z+random.nextInt(16)+8);
        for(int i=0;i<8;i++){
            int xx=x+random.nextInt(16),zz=z+random.nextInt(16),yy=LegacyNaturalPopulation.surface(level,xx,zz);
            while(yy>50&&!level.getBlockState(new BlockPos(xx,yy,zz)).is(Blocks.GRASS_BLOCK))yy--;
            var pos=new BlockPos(xx,yy+1,zz);
            if(level.getBlockState(pos.below()).is(Blocks.GRASS_BLOCK)&&level.getBlockState(pos).canBeReplaced()&&adjacentWood(level,pos))level.setBlock(pos,R5Content.VISHROOM.get().defaultBlockState(),2);
        }
    }
    public static boolean adjacentWood(WorldGenLevel level,BlockPos pos){for(var neighbor:BlockPos.betweenClosed(pos.offset(-1,-1,-1),pos.offset(1,1,1)))if(!neighbor.equals(pos)&&level.getBlockState(neighbor).is(net.minecraft.tags.BlockTags.LOGS))return true;return false;}
    private MagicalForestPopulation(){}
}
