package thaumcraft.blocks;

import net.minecraft.core.*;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.shapes.*;
import thaumcraft.registry.R4Content;

public final class LevitatorBlock extends Block implements EntityBlock {
    public static final BooleanProperty POWERED=BlockStateProperties.POWERED;
    public static java.util.function.Consumer<net.minecraft.world.phys.Vec3> sparkle=p->{};
    private static final VoxelShape SUPPORT=Shapes.or(box(0,0,0,16,16,1),box(0,0,15,16,16,16),box(0,0,0,1,16,16),box(15,0,0,16,16,16));
    public LevitatorBlock(){super(Properties.of().strength(2.5f,15).sound(SoundType.WOOD).instrument(NoteBlockInstrument.BASS));registerDefaultState(stateDefinition.any().setValue(POWERED,false));}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(POWERED);}
    @Override protected VoxelShape getBlockSupportShape(BlockState s,BlockGetter l,BlockPos p){return SUPPORT;}
    @Override public boolean canConnectRedstone(BlockState s,BlockGetter l,BlockPos p,Direction d){return d!=null&&d.getAxis().isHorizontal();}
    @Override public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new LevitatorBlockEntity(p,s);}
    @Override public void animateTick(BlockState s,Level l,BlockPos p,RandomSource r){if(l.getBlockEntity(p) instanceof LevitatorBlockEntity lift&&!lift.gettingPower()&&lift.rangeAbove()>0)sparkle.accept(new net.minecraft.world.phys.Vec3(p.getX()+.2+r.nextFloat()*.6,p.getY()+1,p.getZ()+.2+r.nextFloat()*.6));}
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level l,BlockState s,BlockEntityType<T> t){return t==R4Content.LEVITATOR_ENTITY.get()?(w,p,state,tile)->((LevitatorBlockEntity)tile).tick():null;}
    @Override protected void onPlace(BlockState s,Level l,BlockPos p,BlockState old,boolean moving){super.onPlace(s,l,p,old,moving);invalidateStack(l,p);}
    @Override protected void onRemove(BlockState s,Level l,BlockPos p,BlockState next,boolean moving){super.onRemove(s,l,p,next,moving);if(!s.is(next.getBlock()))invalidateStack(l,p);}
    @Override protected void neighborChanged(BlockState s,Level l,BlockPos p,Block neighbor,BlockPos from,boolean moving){if(l.getBlockEntity(p) instanceof LevitatorBlockEntity lifter&&lifter.gettingPower()!=lifter.lastPowerState())invalidateStack(l,p);}
    public static void invalidateStack(Level l,BlockPos p){for(var q=p;q.getY()>=l.getMinBuildHeight()&&l.getBlockState(q).is(R4Content.LEVITATOR.get());q=q.below())if(l.getBlockEntity(q) instanceof LevitatorBlockEntity lift)lift.invalidateRange();for(var q=p.below();q.getY()>=l.getMinBuildHeight()&&l.getBlockState(q).is(R4Content.LEVITATOR.get());q=q.below())if(l.getBlockEntity(q) instanceof LevitatorBlockEntity lift)lift.invalidateRange();}
}
