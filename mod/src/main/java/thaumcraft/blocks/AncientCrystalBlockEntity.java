package thaumcraft.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.registry.R5Content;

public final class AncientCrystalBlockEntity extends BlockEntity {
    public AncientCrystalBlockEntity(BlockPos pos,BlockState state){super(R5Content.ANCIENT_CRYSTAL_ENTITY.get(),pos,state);}
}
