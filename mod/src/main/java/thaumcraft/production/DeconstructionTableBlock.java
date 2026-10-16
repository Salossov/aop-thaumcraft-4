package thaumcraft.production;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import thaumcraft.registry.R4Content;

public final class DeconstructionTableBlock extends Block implements EntityBlock {
    public DeconstructionTableBlock(){super(Properties.of().strength(2.5f).sound(SoundType.WOOD).noOcclusion());}
    @Override public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new DeconstructionTableBlockEntity(p,s);}
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level l,BlockState s,BlockEntityType<T> type){return !l.isClientSide&&type==R4Content.DECONSTRUCTOR_ENTITY.get()?(world,p,state,tile)->((DeconstructionTableBlockEntity)tile).serverTick():null;}
    @Override protected InteractionResult useWithoutItem(BlockState s,Level level,BlockPos p,Player player,BlockHitResult hit){if(player.isShiftKeyDown())return InteractionResult.PASS;if(player instanceof ServerPlayer server&&level.getBlockEntity(p) instanceof DeconstructionTableBlockEntity tile)server.openMenu(tile);return InteractionResult.sidedSuccess(level.isClientSide);}
    @Override protected void onRemove(BlockState s,Level level,BlockPos p,BlockState next,boolean moving){if(!s.is(next.getBlock())&&level.getBlockEntity(p) instanceof DeconstructionTableBlockEntity tile)Containers.dropContents(level,p,tile);super.onRemove(s,level,p,next,moving);}
}
