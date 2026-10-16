package thaumcraft.blocks;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.world.*;
public final class MagicalSaplingBlock extends SaplingBlock {
    private final boolean silver;
    public MagicalSaplingBlock(boolean silver){super(new TreeGrower("thaumcraft:"+(silver?"silverwood":"greatwood"),Optional.empty(),Optional.of(ResourceKey.create(Registries.CONFIGURED_FEATURE,ResourceLocation.fromNamespaceAndPath("thaumcraft",silver?"silverwood_tree":"greatwood_tree"))),Optional.empty()),Properties.ofFullCopy(Blocks.OAK_SAPLING).lightLevel(s->silver?8:0));this.silver=silver;}
    @Override protected void randomTick(BlockState state,ServerLevel level,BlockPos pos,RandomSource random){if(level.getMaxLocalRawBrightness(pos.above())>=9&&random.nextInt(silver?50:25)==0)grow(level,pos,state,random);}
    @Override public boolean isValidBonemealTarget(net.minecraft.world.level.LevelReader level,BlockPos pos,BlockState state){return false;}
    @Override public void advanceTree(ServerLevel level,BlockPos pos,BlockState state,RandomSource random){grow(level,pos,state,random);}
    private void grow(ServerLevel level,BlockPos pos,BlockState state,RandomSource random){level.setBlock(pos,Blocks.AIR.defaultBlockState(),2);boolean grown=silver?new LegacySilverwoodTree(true,7,5).func_76484_a(level,random,pos.getX(),pos.getY(),pos.getZ()):new LegacyGreatwoodTree().generate(level,new java.util.Random(random.nextLong()),pos.getX(),pos.getY(),pos.getZ(),false);if(!grown)level.setBlock(pos,state,2);}
}
