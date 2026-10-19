package thaumcraft.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.*;
import thaumcraft.registry.R3Content;

public final class FocusMemoryBlock extends Block implements EntityBlock {
    private final boolean hole;
    public FocusMemoryBlock(boolean hole){super(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE).strength(-1,3600000).noLootTable().noOcclusion().isSuffocating((state,level,pos)->false).isViewBlocking((state,level,pos)->false));this.hole=hole;}
    @Override protected RenderShape getRenderShape(BlockState state){return RenderShape.ENTITYBLOCK_ANIMATED;}
    @Override public BlockEntity newBlockEntity(BlockPos pos,BlockState state){return new FocusMemoryBlockEntity(pos,state);}
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level,BlockState state,BlockEntityType<T> type){return type==R3Content.FOCUS_MEMORY_ENTITY.get()?(world,pos,block,tile)->((FocusMemoryBlockEntity)tile).tick():null;}
    @Override protected VoxelShape getShape(BlockState state,BlockGetter level,BlockPos pos,CollisionContext context){if(hole)return Shapes.empty();return level.getBlockEntity(pos) instanceof FocusMemoryBlockEntity tile?tile.original().getShape(level,pos,context):Shapes.block();}
    @Override protected VoxelShape getCollisionShape(BlockState state,BlockGetter level,BlockPos pos,CollisionContext context){if(hole)return Shapes.empty();return level.getBlockEntity(pos) instanceof FocusMemoryBlockEntity tile?tile.original().getCollisionShape(level,pos,context):Shapes.block();}
    @Override public int getLightEmission(BlockState state,BlockGetter level,BlockPos pos){return hole?10:level.getBlockEntity(pos) instanceof FocusMemoryBlockEntity tile?tile.original().getLightEmission():0;}
}
