package thaumcraft.world;

import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.*;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.storage.loot.LootTable;

/** Native placement for the original support tests, chest containers and spawners. */
public final class AncientStructureSupport {
    public static final ResourceKey<LootTable> DUNGEON=ResourceKey.create(Registries.LOOT_TABLE,ResourceLocation.parse("thaumcraft:chests/double_dungeon"));
    public static boolean validSurface(WorldGenLevel level,BlockPos pos){
        if(level.isOutsideBuildHeight(pos)||level.isOutsideBuildHeight(pos.above(2)))return false;
        int distance=0;
        while(!level.isEmptyBlock(pos.above(distance))){if(++distance>2)return false;}
        var ground=pos.above(distance-1);var state=level.getBlockState(ground);
        if(!level.isEmptyBlock(ground.above()))return false;
        if(state.is(Blocks.STONE)||state.is(Blocks.GRASS_BLOCK)||state.is(Blocks.DIRT))return true;
        if(!state.is(Blocks.SNOW)&&!state.is(Blocks.SHORT_GRASS)&&!state.is(Blocks.FERN))return false;
        var below=level.getBlockState(ground.below());return below.is(Blocks.STONE)||below.is(Blocks.GRASS_BLOCK)||below.is(Blocks.DIRT);
    }
    public static void chest(WorldGenLevel level,BlockPos pos,boolean trapped,RandomSource random){
        level.setBlock(pos,(trapped?Blocks.TRAPPED_CHEST:Blocks.CHEST).defaultBlockState(),3);
        if(level.getBlockEntity(pos) instanceof ChestBlockEntity chest)chest.setLootTable(DUNGEON,random.nextLong());
    }
    public static void spawner(WorldGenLevel level,BlockPos pos,EntityType<?> type,RandomSource random){
        level.setBlock(pos,Blocks.SPAWNER.defaultBlockState(),3);
        if(level.getBlockEntity(pos) instanceof SpawnerBlockEntity spawner)spawner.setEntityId(type,random);
    }
    private AncientStructureSupport(){}
}
