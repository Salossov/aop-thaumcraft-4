package thaumcraft.blocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.registry.R5Content;
/** Original cap has no ticker or custom persistent state. */
public final class EldritchCapBlockEntity extends BlockEntity {
    public EldritchCapBlockEntity(BlockPos pos,BlockState state){super(R5Content.CAP_ENTITY.get(),pos,state);}
}
