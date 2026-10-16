package thaumcraft.visnet;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.registry.R3Content;

public final class CrystalClusterBlockEntity extends BlockEntity {
    public CrystalClusterBlockEntity(BlockPos pos,BlockState state){super(R3Content.CRYSTAL_CLUSTER_ENTITY.get(),pos,state);}
}
