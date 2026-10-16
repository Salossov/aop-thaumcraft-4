package thaumcraft.production;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.fluids.FluidUtil;
import thaumcraft.registry.R4Content;

public final class ArcaneSpaBlock extends Block implements EntityBlock {
    public ArcaneSpaBlock(){super(Properties.of().strength(3,25).sound(SoundType.STONE));}
    @Override public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new ArcaneSpaBlockEntity(p,s);}
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level l,BlockState s,BlockEntityType<T> type){return !l.isClientSide&&type==R4Content.SPA_ENTITY.get()?(world,p,state,tile)->((ArcaneSpaBlockEntity)tile).serverTick():null;}
    @Override protected ItemInteractionResult useItemOn(ItemStack stack,BlockState s,Level l,BlockPos p,Player player,InteractionHand hand,BlockHitResult hit){if(player.isShiftKeyDown())return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;if(FluidUtil.getFluidContained(stack).isPresent()&&l.getBlockEntity(p) instanceof ArcaneSpaBlockEntity tile){if(l.isClientSide||FluidUtil.interactWithFluidHandler(player,hand,tile.tank))return ItemInteractionResult.sidedSuccess(l.isClientSide);}return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;}
    @Override protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos p,Player player,BlockHitResult hit){if(player.isShiftKeyDown())return InteractionResult.PASS;if(player instanceof ServerPlayer server&&l.getBlockEntity(p) instanceof ArcaneSpaBlockEntity tile)server.openMenu(tile);return InteractionResult.sidedSuccess(l.isClientSide);}
    @Override protected void onRemove(BlockState s,Level l,BlockPos p,BlockState next,boolean moving){if(!s.is(next.getBlock())&&l.getBlockEntity(p) instanceof ArcaneSpaBlockEntity tile)Containers.dropContents(l,p,tile);super.onRemove(s,l,p,next,moving);}
}
