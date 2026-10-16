package thaumcraft.world;
import net.minecraft.core.*;
import net.minecraft.util.*;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.*;
import thaumcraft.registry.R3Content;
/** Original silverwood canopy, branches and embedded pure-node chance. */
public final class LegacySilverwoodTree {

    private final int minTreeHeight;
    private final int randomTreeHeight;
    boolean worldgen = false;

    public LegacySilverwoodTree(boolean doBlockNotify, int minTreeHeight, int randomTreeHeight) {
        this.worldgen = !doBlockNotify;
        this.minTreeHeight = minTreeHeight;
        this.randomTreeHeight = randomTreeHeight;
    }

    public boolean func_76484_a(WorldGenLevel world, RandomSource random, int x, int y, int z) {
        int height = random.nextInt(this.randomTreeHeight) + this.minTreeHeight;
        boolean flag = true;
        if (y > world.getMinBuildHeight() && y + height + 1 < world.getMaxBuildHeight()) {
            for (int i1 = y; i1 <= y + 1 + height; ++i1) {
                int spread = 1;
                if (i1 == y) {
                    spread = 0;
                }
                if (i1 >= y + 1 + height - 2) {
                    spread = 3;
                }
                for (int j1 = x - spread; j1 <= x + spread && flag; ++j1) {
                    for (int k1 = z - spread; k1 <= z + spread && flag; ++k1) {
                        if (i1 >= world.getMinBuildHeight() && i1 < world.getMaxBuildHeight()) {
                            Block block = world.getBlockState(new BlockPos(j1, i1, k1)).getBlock();
                            if (world.getBlockState(new BlockPos(j1, i1, k1)).isAir() || world.getBlockState(new BlockPos(j1, i1, k1)).is(net.minecraft.tags.BlockTags.LEAVES) || world.getBlockState(new BlockPos(j1, i1, k1)).canBeReplaced() || i1 <= y) continue;
                            flag = false;
                            continue;
                        }
                        flag = false;
                    }
                }
            }
            if (!flag) {
                return false;
            }
            Block block1 = world.getBlockState(new BlockPos(x, y - 1, z)).getBlock();
            boolean isSoil = Blocks.OAK_SAPLING.defaultBlockState().canSurvive(world,new BlockPos(x,y,z));
            if (isSoil && y < world.getMaxBuildHeight() - height - 1) {
                int k2;
                world.setBlock(new BlockPos(x,y-1,z),Blocks.DIRT.defaultBlockState(),2);
                int start = y + height - 5;
                int end = y + height + 3 + random.nextInt(3);
                for (k2 = start; k2 <= end; ++k2) {
                    int cty = Mth.clamp((int)k2, (int)(y + height - 3), (int)(y + height));
                    for (int xx = x - 5; xx <= x + 5; ++xx) {
                        for (int zz = z - 5; zz <= z + 5; ++zz) {
                            double d3 = xx - x;
                            double d4 = k2 - cty;
                            double d5 = zz - z;
                            double dist = d3 * d3 + d4 * d4 + d5 * d5;
                            if (!(dist < (double)(10 + random.nextInt(8))) || !leafReplace(world,new BlockPos(xx,k2,zz))) continue;
                            put(world, xx, k2, zz, R3Content.SILVERWOOD_LEAVES.get(), 1);
                        }
                    }
                }
                int chance = (int)((double)height * 1.5);
                boolean lastblock = false;
                for (k2 = 0; k2 < height; ++k2) {
                    Block block2 = world.getBlockState(new BlockPos(x, y + k2, z)).getBlock();
                    if (!world.getBlockState(new BlockPos(x, y + k2, z)).isAir() && !world.getBlockState(new BlockPos(x, y + k2, z)).is(net.minecraft.tags.BlockTags.LEAVES) && !world.getBlockState(new BlockPos(x, y + k2, z)).canBeReplaced()) continue;
                    if (k2 > 0 && !lastblock && random.nextInt(chance) == 0) {
                        put(world, x, y + k2, z, R3Content.SILVERWOOD_LOG.get(), 2);
                        NodeFactory.createSilverwood(world,new BlockPos(x,y+k2,z),random);
                        chance += height;
                        lastblock = true;
                    } else {
                        put(world, x, y + k2, z, R3Content.SILVERWOOD_LOG.get(), 1);
                        lastblock = false;
                    }
                    put(world, x - 1, y + k2, z, R3Content.SILVERWOOD_LOG.get(), 1);
                    put(world, x + 1, y + k2, z, R3Content.SILVERWOOD_LOG.get(), 1);
                    put(world, x, y + k2, z - 1, R3Content.SILVERWOOD_LOG.get(), 1);
                    put(world, x, y + k2, z + 1, R3Content.SILVERWOOD_LOG.get(), 1);
                }
                put(world, x, y + k2, z, R3Content.SILVERWOOD_LOG.get(), 1);
                put(world, x - 1, y, z - 1, R3Content.SILVERWOOD_LOG.get(), 1);
                put(world, x + 1, y, z + 1, R3Content.SILVERWOOD_LOG.get(), 1);
                put(world, x - 1, y, z + 1, R3Content.SILVERWOOD_LOG.get(), 1);
                put(world, x + 1, y, z - 1, R3Content.SILVERWOOD_LOG.get(), 1);
                if (random.nextInt(3) != 0) {
                    put(world, x - 1, y + 1, z - 1, R3Content.SILVERWOOD_LOG.get(), 1);
                }
                if (random.nextInt(3) != 0) {
                    put(world, x + 1, y + 1, z + 1, R3Content.SILVERWOOD_LOG.get(), 1);
                }
                if (random.nextInt(3) != 0) {
                    put(world, x - 1, y + 1, z + 1, R3Content.SILVERWOOD_LOG.get(), 1);
                }
                if (random.nextInt(3) != 0) {
                    put(world, x + 1, y + 1, z - 1, R3Content.SILVERWOOD_LOG.get(), 1);
                }
                put(world, x - 2, y, z, R3Content.SILVERWOOD_LOG.get(), 5);
                put(world, x + 2, y, z, R3Content.SILVERWOOD_LOG.get(), 5);
                put(world, x, y, z - 2, R3Content.SILVERWOOD_LOG.get(), 9);
                put(world, x, y, z + 2, R3Content.SILVERWOOD_LOG.get(), 9);
                put(world, x - 2, y - 1, z, R3Content.SILVERWOOD_LOG.get(), 1);
                put(world, x + 2, y - 1, z, R3Content.SILVERWOOD_LOG.get(), 1);
                put(world, x, y - 1, z - 2, R3Content.SILVERWOOD_LOG.get(), 1);
                put(world, x, y - 1, z + 2, R3Content.SILVERWOOD_LOG.get(), 1);
                put(world, x - 1, y + (height - 4), z - 1, R3Content.SILVERWOOD_LOG.get(), 1);
                put(world, x + 1, y + (height - 4), z + 1, R3Content.SILVERWOOD_LOG.get(), 1);
                put(world, x - 1, y + (height - 4), z + 1, R3Content.SILVERWOOD_LOG.get(), 1);
                put(world, x + 1, y + (height - 4), z - 1, R3Content.SILVERWOOD_LOG.get(), 1);
                if (random.nextInt(3) == 0) {
                    put(world, x - 1, y + (height - 5), z - 1, R3Content.SILVERWOOD_LOG.get(), 1);
                }
                if (random.nextInt(3) == 0) {
                    put(world, x + 1, y + (height - 5), z + 1, R3Content.SILVERWOOD_LOG.get(), 1);
                }
                if (random.nextInt(3) == 0) {
                    put(world, x - 1, y + (height - 5), z + 1, R3Content.SILVERWOOD_LOG.get(), 1);
                }
                if (random.nextInt(3) == 0) {
                    put(world, x + 1, y + (height - 5), z - 1, R3Content.SILVERWOOD_LOG.get(), 1);
                }
                put(world, x - 2, y + (height - 4), z, R3Content.SILVERWOOD_LOG.get(), 5);
                put(world, x + 2, y + (height - 4), z, R3Content.SILVERWOOD_LOG.get(), 5);
                put(world, x, y + (height - 4), z - 2, R3Content.SILVERWOOD_LOG.get(), 9);
                put(world, x, y + (height - 4), z + 2, R3Content.SILVERWOOD_LOG.get(), 9);
                if(this.worldgen) MagicalTreeFlowers.place(world,random,new BlockPos(x,y,z));
                return true;
            }
            return false;
        }
        return false;
    }

    private static boolean leafReplace(WorldGenLevel l,BlockPos p){var s=l.getBlockState(p);return s.isAir()||s.is(net.minecraft.tags.BlockTags.LEAVES)||s.canBeReplaced();}
    private static void put(WorldGenLevel l,int x,int y,int z,Block b,int meta){var p=new BlockPos(x,y,z);var s=meta==2&&b==R3Content.SILVERWOOD_LOG.get()?R3Content.SILVERWOOD_NODE_LOG.get().defaultBlockState():b.defaultBlockState();if(s.getBlock() instanceof RotatedPillarBlock)s=s.setValue(RotatedPillarBlock.AXIS,(meta&12)==4?Direction.Axis.X:(meta&12)==8?Direction.Axis.Z:Direction.Axis.Y);l.setBlock(p,s,2);}

}
