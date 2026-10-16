package thaumcraft.blocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.registry.R5Content;
/** Original void tile has no ticker or additional persistent data. */
public final class EldritchVoidBlockEntity extends BlockEntity {
    public EldritchVoidBlockEntity(BlockPos pos,BlockState state){super(R5Content.VOID_ENTITY.get(),pos,state);}
    @Override public boolean isValidBlockState(BlockState state){return super.isValidBlockState(state)&&state.getValue(EldritchVoidBlock.EXPOSED);}
}
