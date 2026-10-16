package thaumcraft.production;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/** Original eldritch metadata zero. */
public final class EldritchAltarBlock extends BaseEntityBlock {
    public EldritchAltarBlock(){super(Properties.of().strength(50,12000).lightLevel(s->8).noOcclusion());}
    @Override protected MapCodec<? extends BaseEntityBlock> codec(){return simpleCodec(properties->new EldritchAltarBlock());}
    @Override protected RenderShape getRenderShape(BlockState state){return RenderShape.ENTITYBLOCK_ANIMATED;}
    @Override protected void onRemove(BlockState state,Level level,BlockPos pos,BlockState replacement,boolean moving){thaumcraft.blocks.EldritchStructure.removed(level,pos,state,replacement);super.onRemove(state,level,pos,replacement,moving);}
    @Override public BlockEntity newBlockEntity(BlockPos pos,BlockState state){return new EldritchAltarBlockEntity(pos,state);}
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level,BlockState state,BlockEntityType<T> type){return createTickerHelper(type,thaumcraft.registry.R3Content.ELDRITCH_ALTAR_ENTITY.get(),EldritchAltarBlockEntity::tick);}
    @Override protected ItemInteractionResult useItemOn(ItemStack stack,BlockState state,Level level,BlockPos pos,Player player,InteractionHand hand,BlockHitResult hit){if(player.isShiftKeyDown()||!stack.is(thaumcraft.registry.R3Equipment.ELDRITCH_EYE.get())||!(level.getBlockEntity(pos) instanceof EldritchAltarBlockEntity altar)||altar.eyes>=4)return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;if(!level.isClientSide){if(!level.mayInteract(player,pos))return ItemInteractionResult.FAIL;altar.installEye();stack.shrink(1);}return ItemInteractionResult.sidedSuccess(level.isClientSide);}
}
