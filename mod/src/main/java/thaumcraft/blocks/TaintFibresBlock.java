package thaumcraft.blocks;

import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.phys.shapes.*;
import thaumcraft.effects.TaintEffects;
import thaumcraft.entities.*;
import thaumcraft.registry.R2Content;
import thaumcraft.world.WorldBiomes;
import thaumcraft.config.ThaumcraftConfig;

public final class TaintFibresBlock extends Block {
    public enum Kind { FIBRES,GROWTH,TENDRILS,SPORE }
    public static final BooleanProperty OCCUPIED=BooleanProperty.create("occupied");
    public final Kind kind;
    public TaintFibresBlock(Kind kind) { super(Properties.of().strength(0.1f).noCollission().noOcclusion().randomTicks().sound(SoundType.GRASS));this.kind=kind;var initial=stateDefinition.any().setValue(OCCUPIED,false);for(var d:Direction.values())initial=initial.setValue(MultifaceBlock.getFaceProperty(d),false);registerDefaultState(initial); }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder) { builder.add(OCCUPIED);for(var d:Direction.values())builder.add(MultifaceBlock.getFaceProperty(d)); }
    private static boolean visibleFace(LevelAccessor level,BlockPos pos,Direction direction){var neighbour=pos.relative(direction);var state=level.getBlockState(neighbour);return !(state.getBlock() instanceof TaintSolidBlock)&&state.isFaceSturdy(level,neighbour,direction.getOpposite());}
    private static BlockState faces(BlockState state,LevelAccessor level,BlockPos pos){for(var d:Direction.values())state=state.setValue(MultifaceBlock.getFaceProperty(d),visibleFace(level,pos,d));return state;}
    @Override public BlockState getStateForPlacement(BlockPlaceContext context){return faces(defaultBlockState(),context.getLevel(),context.getClickedPos());}
    @Override protected BlockState updateShape(BlockState state,Direction direction,BlockState neighbour,LevelAccessor level,BlockPos pos,BlockPos other){return state.setValue(MultifaceBlock.getFaceProperty(direction),visibleFace(level,pos,direction));}
    public static boolean taint(BlockState state) { return state.getBlock() instanceof TaintFibresBlock||state.getBlock() instanceof TaintSolidBlock; }
    public static int adjacent(LevelAccessor level,BlockPos pos) {int count=0;for(var direction:Direction.values()) if(taint(level.getBlockState(pos.relative(direction)))) count++;return count;}
    private static boolean supported(LevelAccessor level,BlockPos pos) {for(var d:Direction.values()) if(level.getBlockState(pos.relative(d)).isFaceSturdy(level,pos.relative(d),d.getOpposite())) return true;return false;}
    private static boolean onlyTaint(LevelAccessor level,BlockPos pos) {
        boolean found=false;for(var direction:Direction.values()) {var state=level.getBlockState(pos.relative(direction));if(!state.isAir()) {found=true;if(!taint(state)) return false;}}return found;
    }
    public static boolean spread(ServerLevel level,BlockPos pos) {
        if(!level.hasChunkAt(pos)) return false;var old=level.getBlockState(pos);
        if(!old.getFluidState().isEmpty()||!supported(level,pos)||onlyTaint(level,pos)||!(old.isAir()||old.canBeReplaced()||old.is(BlockTags.FLOWERS)||old.is(BlockTags.LEAVES))) return false;
        var state=R2Content.TAINT_FIBRES.get().defaultBlockState();
        if(level.random.nextInt(10)==0&&level.isEmptyBlock(pos.above())&&level.getBlockState(pos.below()).isFaceSturdy(level,pos.below(),Direction.UP)) {
            state=(level.random.nextInt(10)<9?R2Content.TAINT_GROWTH.get():level.random.nextInt(12)<10?R2Content.TAINT_TENDRILS.get():R2Content.TAINT_SPORE_STALK.get()).defaultBlockState();
        }
        level.setBlock(pos,faces(state,level,pos),3);return true;
    }
    public static void spreadBiome(ServerLevel level,BlockPos pos,RandomSource random) {
        int rate=ThaumcraftConfig.TAINT_SPREAD_RATE.get();if(rate<=0) return;int x=pos.getX()+random.nextInt(3)-1,z=pos.getZ()+random.nextInt(3)-1;
        if(!WorldBiomes.is(level,new BlockPos(x,pos.getY(),z),WorldBiomes.TAINT)&&random.nextInt(rate*5)==0&&adjacent(level,pos)>=2) WorldBiomes.change(level,x,z,WorldBiomes.TAINT);
    }
    @Override protected void randomTick(BlockState state,ServerLevel level,BlockPos pos,RandomSource random) {
        spreadBiome(level,pos,random);
        if((kind==Kind.FIBRES&&onlyTaint(level,pos))||!WorldBiomes.is(level,pos,WorldBiomes.TAINT)) {level.removeBlock(pos,false);return;}
        var target=pos.offset(random.nextInt(3)-1,random.nextInt(5)-3,random.nextInt(3)-1);
        if(!level.hasChunkAt(target)||!WorldBiomes.is(level,target,WorldBiomes.TAINT)||spread(level,target)) return;
        var old=level.getBlockState(target);int count=adjacent(level,target);
        // Original: logs + cactus-like + gourd-like blocks become fibrous taint at 2+ taint
        // neighbours; sand/ground/grass/clay become tainted soil at 3+.
        if(count>=2&&(old.is(BlockTags.LOGS)||old.is(Blocks.CACTUS)||old.is(Blocks.PUMPKIN)||old.is(Blocks.MELON)||old.is(Blocks.CARVED_PUMPKIN))) level.setBlock(target,R2Content.FIBROUS_TAINT.get().defaultBlockState(),3);
        if(count>=3&&!old.isAir()&&(old.is(BlockTags.DIRT)||old.is(BlockTags.SAND)||old.is(BlockTags.TERRACOTTA)||old.is(BlockTags.SNOW))) level.setBlock(target,R2Content.TAINTED_SOIL.get().defaultBlockState(),3);
        if(kind==Kind.SPORE&&!state.getValue(OCCUPIED)&&ThaumcraftConfig.SPAWN_TAINT_SPORE.get()&&random.nextInt(10)==0&&level.isEmptyBlock(pos.above())) {
            level.setBlock(pos,state.setValue(OCCUPIED,true),3);var spore=R2Entities.TAINT_SPORE.get().create(level);spore.moveTo(pos.getX()+0.5,pos.getY()+1,pos.getZ()+0.5,0,0);level.addFreshEntity(spore);
        }else if(kind==Kind.SPORE&&state.getValue(OCCUPIED)&&level.getEntitiesOfClass(TaintSpore.class,new net.minecraft.world.phys.AABB(pos.above())).isEmpty()) level.setBlock(pos,state.setValue(OCCUPIED,false),3);
    }
    @Override protected void entityInside(BlockState state,Level level,BlockPos pos,Entity entity) {
        if(!level.isClientSide&&entity instanceof LivingEntity living&&!living.isInvertedHealAndHarm()&&level.random.nextInt(entity instanceof Player?1000:500)==0) living.addEffect(new MobEffectInstance(TaintEffects.FLUX_TAINT,entity instanceof Player?80:160,0,false,false));
    }
    @Override protected VoxelShape getShape(BlockState state,BlockGetter level,BlockPos pos,CollisionContext context) { if(kind!=Kind.FIBRES)return box(3,0,3,13,kind==Kind.SPORE?16:12,13);
        VoxelShape shape=Shapes.empty();for(var direction:Direction.values())if(state.getValue(MultifaceBlock.getFaceProperty(direction)))shape=Shapes.or(shape,switch(direction){case DOWN->box(0,0,0,16,1,16);case UP->box(0,15,0,16,16,16);case NORTH->box(0,0,0,16,16,1);case SOUTH->box(0,0,15,16,16,16);case WEST->box(0,0,0,1,16,16);case EAST->box(15,0,0,16,16,16);});return shape; }
}
