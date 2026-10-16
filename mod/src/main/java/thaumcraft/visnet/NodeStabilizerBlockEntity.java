package thaumcraft.visnet;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.registry.R3Content;

/** The original piston extension is a client animation, not persisted progression. */
public final class NodeStabilizerBlockEntity extends BlockEntity {
    private int extension,previousExtension;
    public NodeStabilizerBlockEntity(BlockPos pos,BlockState state) {super(R3Content.NODE_STABILIZER_ENTITY.get(),pos,state);}
    public int extension() {return extension;}
    public float extension(float partial){return net.minecraft.util.Mth.lerp(partial,previousExtension,extension);}
    public void clientTick() {
        if(level==null)return;
        previousExtension=extension;
        var above=worldPosition.above();boolean active=!level.hasNeighborSignal(worldPosition)&&level.hasChunkAt(above)
            &&(level.getBlockEntity(above) instanceof thaumcraft.blocks.AuraNodeBlockEntity||level.getBlockEntity(above) instanceof EnergizedNodeBlockEntity);
        extension=Math.clamp(extension+(active?1:-1),0,37);
    }
}
