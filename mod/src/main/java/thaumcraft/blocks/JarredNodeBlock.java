package thaumcraft.blocks;

import java.util.List;
import net.minecraft.core.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.shapes.*;

public final class JarredNodeBlock extends Block implements EntityBlock {
    public JarredNodeBlock(){super(Properties.of().strength(.3f).noOcclusion().sound(SoundType.GLASS));}
    @Override public BlockEntity newBlockEntity(BlockPos pos,BlockState state){return new JarredNodeBlockEntity(pos,state);}
    @Override protected VoxelShape getShape(BlockState state,BlockGetter level,BlockPos pos,CollisionContext context){return box(3,0,3,13,14,13);}
    @Override protected List<ItemStack> getDrops(BlockState state,LootParams.Builder params){return params.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof JarredNodeBlockEntity jar?List.of(jar.item()):List.of();}
    @Override public ItemStack getCloneItemStack(BlockState state,net.minecraft.world.phys.HitResult hit,LevelReader level,BlockPos pos,net.minecraft.world.entity.player.Player player){return level.getBlockEntity(pos) instanceof JarredNodeBlockEntity jar?jar.item():new ItemStack(asItem());}
}
