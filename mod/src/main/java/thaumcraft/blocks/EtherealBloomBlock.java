package thaumcraft.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.*;
import thaumcraft.registry.R4Content;

public final class EtherealBloomBlock extends BushBlock implements EntityBlock {
    public static final MapCodec<EtherealBloomBlock> CODEC=simpleCodec(EtherealBloomBlock::new);
    public EtherealBloomBlock(){this(Properties.ofFullCopy(Blocks.DANDELION).lightLevel(s->15));}
    public EtherealBloomBlock(Properties p){super(p);}
    @Override protected MapCodec<? extends BushBlock> codec(){return CODEC;}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter level,BlockPos pos,CollisionContext c){return box(2,0,2,14,16,14);}
    @Override protected RenderShape getRenderShape(BlockState state){return RenderShape.INVISIBLE;}
    @Override public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new EtherealBloomBlockEntity(p,s);}
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level,BlockState state,BlockEntityType<T> type){return type==R4Content.BLOOM_ENTITY.get()?(l,p,s,t)->((EtherealBloomBlockEntity)t).tick():null;}
}
