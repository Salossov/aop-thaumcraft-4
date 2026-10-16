package thaumcraft.production;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.phys.shapes.*;
import thaumcraft.registry.R3Content;

public final class LampLightBlock extends Block implements EntityBlock {
    public LampLightBlock(){super(BlockBehaviour.Properties.of().noCollission().noOcclusion().replaceable().noLootTable().lightLevel(state->15).air());}
    @Override protected RenderShape getRenderShape(BlockState state){return RenderShape.INVISIBLE;}
    @Override protected VoxelShape getShape(BlockState state,BlockGetter level,BlockPos pos,CollisionContext context){return Shapes.empty();}
    @Override public BlockEntity newBlockEntity(BlockPos pos,BlockState state){return new LampLightBlockEntity(pos,state);}
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level,BlockState state,BlockEntityType<T> type){return !level.isClientSide&&type==R3Content.LAMP_LIGHT_ENTITY.get()?(world,pos,block,tile)->((LampLightBlockEntity)tile).serverTick():null;}
}
