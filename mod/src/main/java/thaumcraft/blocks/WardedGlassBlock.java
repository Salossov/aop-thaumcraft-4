package thaumcraft.blocks;

import java.util.*;
import java.util.function.BiConsumer;
import net.minecraft.core.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.loot.LootParams;
import thaumcraft.config.ThaumcraftConfig;

public final class WardedGlassBlock extends HalfTransparentBlock implements EntityBlock {
    public WardedGlassBlock(){super(Properties.of().strength(5,999).sound(SoundType.GLASS).noOcclusion().pushReaction(PushReaction.BLOCK).isViewBlocking((s,l,p)->false).isSuffocating((s,l,p)->false));}
    @Override public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new OwnedBlockEntity(p,s);}
    @Override public void setPlacedBy(Level l,BlockPos p,BlockState s,LivingEntity entity,ItemStack stack){if(entity instanceof Player player&&l.getBlockEntity(p) instanceof OwnedBlockEntity tile)tile.assign(player);}
    @Override protected float getDestroyProgress(BlockState s,Player player,BlockGetter l,BlockPos p){return ThaumcraftConfig.WARDED_STONE.get()?0:super.getDestroyProgress(s,player,l,p);}
    @Override protected List<ItemStack> getDrops(BlockState s,LootParams.Builder context){return ThaumcraftConfig.WARDED_STONE.get()?List.of():List.of(new ItemStack(asItem()));}
    @Override public boolean canEntityDestroy(BlockState s,BlockGetter l,BlockPos p,Entity entity){return false;}
    @Override public void onBlockExploded(BlockState s,Level l,BlockPos p,Explosion explosion){}
    @Override protected void onExplosionHit(BlockState s,Level l,BlockPos p,Explosion explosion,BiConsumer<ItemStack,BlockPos> drops){}
    @Override protected float getShadeBrightness(BlockState s,BlockGetter l,BlockPos p){return 1;}
    @Override protected boolean propagatesSkylightDown(BlockState s,BlockGetter l,BlockPos p){return true;}
}
