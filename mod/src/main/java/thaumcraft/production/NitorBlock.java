package thaumcraft.production;
import net.minecraft.core.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.*;
public final class NitorBlock extends Block implements EntityBlock{
 public NitorBlock(){super(Properties.of().noCollission().noOcclusion().lightLevel(s->15).strength(0));}
 @Override public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new NitorBlockEntity(p,s);}
 @Override protected RenderShape getRenderShape(BlockState state){return RenderShape.ENTITYBLOCK_ANIMATED;}
 @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return box(4,4,4,12,12,12);}
 @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level,BlockState state,BlockEntityType<T> type){return level.isClientSide&&type==thaumcraft.registry.R3Content.NITOR_ENTITY.get()?(l,p,s,t)->NitorBlockEntity.clientTick.accept((NitorBlockEntity)t):null;}
}
