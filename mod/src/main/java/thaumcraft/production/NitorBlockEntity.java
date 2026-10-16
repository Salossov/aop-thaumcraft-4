package thaumcraft.production;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.registry.R3Content;
public final class NitorBlockEntity extends BlockEntity{
 public static java.util.function.Consumer<NitorBlockEntity> clientTick=tile->{};
 public NitorBlockEntity(BlockPos p,BlockState s){super(R3Content.NITOR_ENTITY.get(),p,s);}
}
