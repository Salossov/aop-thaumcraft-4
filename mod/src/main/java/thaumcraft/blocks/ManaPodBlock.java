package thaumcraft.blocks;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.loot.*;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.shapes.*;
import thaumcraft.api.aspects.Aspects;
import thaumcraft.items.ManaBeanItem;
import thaumcraft.registry.R3Content;

public final class ManaPodBlock extends Block implements EntityBlock {
    public static final net.minecraft.world.level.block.state.properties.IntegerProperty AGE=BlockStateProperties.AGE_7;
    public static final TagKey<net.minecraft.world.level.biome.Biome> MAGICAL=TagKey.create(Registries.BIOME,ResourceLocation.parse("thaumcraft:magical_biomes"));
    private static final int[] LOWER={12,10,8,6,5,4,3,2};
    public ManaPodBlock(){super(Properties.of().strength(0).sound(SoundType.GRASS).noOcclusion().randomTicks().lightLevel(s->s.getValue(AGE)));registerDefaultState(stateDefinition.any().setValue(AGE,0));}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(AGE);}
    @Override public BlockEntity newBlockEntity(BlockPos pos,BlockState state){return new ManaPodBlockEntity(pos,state);}
    public static boolean magic(LevelReader level,BlockPos pos){return level instanceof ServerLevel server?thaumcraft.world.WorldBiomes.effective(server,pos).is(MAGICAL):level.getBiome(pos).is(MAGICAL);}
    public static boolean supportState(BlockState state){return state.is(Blocks.OAK_LOG)||state.is(Blocks.SPRUCE_LOG)||state.is(Blocks.BIRCH_LOG)||state.is(Blocks.JUNGLE_LOG)||state.is(Blocks.ACACIA_LOG)||state.is(Blocks.DARK_OAK_LOG)||state.is(R3Content.GREATWOOD_LOG.get())||state.is(R3Content.SILVERWOOD_LOG.get())||state.is(R3Content.SILVERWOOD_NODE_LOG.get());}
    @Override protected boolean canSurvive(BlockState state,LevelReader level,BlockPos pos){return magic(level,pos)&&supportState(level.getBlockState(pos.above()));}
    @Override protected VoxelShape getShape(BlockState state,BlockGetter level,BlockPos pos,CollisionContext context){return box(4,LOWER[state.getValue(AGE)],4,12,16,12);}
    @Override protected void randomTick(BlockState state,ServerLevel level,BlockPos pos,RandomSource random){if(!state.canSurvive(level,pos))level.destroyBlock(pos,true);else if(level.random.nextInt(30)==0&&level.getBlockEntity(pos) instanceof ManaPodBlockEntity tile)tile.checkGrowth();}
    @Override protected BlockState updateShape(BlockState state,Direction direction,BlockState neighbor,LevelAccessor level,BlockPos pos,BlockPos adjacent){if(!state.canSurvive(level,pos))level.scheduleTick(pos,this,1);return state;}
    @Override protected void tick(BlockState state,ServerLevel level,BlockPos pos,RandomSource random){if(!state.canSurvive(level,pos))level.destroyBlock(pos,true);}
    @Override protected List<net.minecraft.world.item.ItemStack> getDrops(BlockState state,LootParams.Builder params){
        int age=state.getValue(AGE);if(age<2)return List.of();int amount=age==7&&params.getLevel().random.nextFloat()>.33f?2:1;
        var aspect=params.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof ManaPodBlockEntity tile&&tile.aspect!=null?tile.aspect:Aspects.PLANT;
        var stack=ManaBeanItem.of(aspect);stack.setCount(amount);return List.of(stack);
    }
}
