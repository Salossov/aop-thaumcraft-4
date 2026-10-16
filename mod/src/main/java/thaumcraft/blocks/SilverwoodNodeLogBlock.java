package thaumcraft.blocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.registry.R2Content;
public final class SilverwoodNodeLogBlock extends RotatedPillarBlock implements EntityBlock {
    public SilverwoodNodeLogBlock(){super(Properties.ofFullCopy(Blocks.OAK_LOG).strength(2.5f));}
    @Override public int getFlammability(BlockState state,net.minecraft.world.level.BlockGetter l,BlockPos p,net.minecraft.core.Direction face){return 5;}
    @Override public int getFireSpreadSpeed(BlockState state,net.minecraft.world.level.BlockGetter l,BlockPos p,net.minecraft.core.Direction face){return 5;}
    public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new AuraNodeBlockEntity(p,s);}
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level,BlockState s,BlockEntityType<T> type){return !level.isClientSide&&type==R2Content.NODE_ENTITY.get()?(l,p,state,t)->((AuraNodeBlockEntity)t).serverTick():null;}
}
