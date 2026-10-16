package thaumcraft.production;

import java.util.List;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.storage.loot.*;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.*;
import thaumcraft.registry.R3Content;

public final class BrainJarBlock extends Block implements EntityBlock {
    public BrainJarBlock(){super(Properties.of().strength(.3f).noOcclusion().sound(SoundType.GLASS));}
    @Override public BlockEntity newBlockEntity(BlockPos pos,BlockState state){return new BrainJarBlockEntity(pos,state);}
    @Override protected VoxelShape getShape(BlockState state,BlockGetter level,BlockPos pos,CollisionContext context){return box(3,0,3,13,14,13);}
    @Override protected InteractionResult useWithoutItem(BlockState state,Level level,BlockPos pos,Player player,BlockHitResult hit){if(level.getBlockEntity(pos) instanceof BrainJarBlockEntity jar){if(!level.isClientSide)jar.release();return InteractionResult.sidedSuccess(level.isClientSide);}return InteractionResult.PASS;}
    @Override protected List<ItemStack> getDrops(BlockState state,LootParams.Builder params){var drops=super.getDrops(state,params);if(params.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof BrainJarBlockEntity jar){var tag=jar.saveWithoutMetadata(params.getLevel().registryAccess());tag.putString("id","thaumcraft:brain_in_a_jar");for(var stack:drops)if(stack.is(asItem()))stack.set(DataComponents.BLOCK_ENTITY_DATA,CustomData.of(tag));}return drops;}
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level,BlockState state,BlockEntityType<T> type){return type==R3Content.BRAIN_JAR_ENTITY.get()?(l,p,s,t)->{var jar=(BrainJarBlockEntity)t;if(l.isClientSide)jar.clientTick();else jar.serverTick();}:null;}
}
