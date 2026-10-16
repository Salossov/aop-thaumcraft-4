package thaumcraft.blocks;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.shapes.*;
import thaumcraft.items.AncientLootBlockItem;
import thaumcraft.loot.LegacyLoot;

public final class AncientLootBlock extends Block {
    public static final IntegerProperty RARITY=IntegerProperty.create("rarity",0,2);
    private final boolean crate;
    public AncientLootBlock(boolean crate){super(Properties.of().strength(.15f,0).noOcclusion().sound(crate?SoundType.WOOD:new net.neoforged.neoforge.common.util.DeferredSoundType(1,.7f,thaumcraft.registry.R2Sounds.URN_BREAK,()->net.minecraft.sounds.SoundEvents.STONE_STEP,()->net.minecraft.sounds.SoundEvents.STONE_PLACE,()->net.minecraft.sounds.SoundEvents.STONE_HIT,()->net.minecraft.sounds.SoundEvents.STONE_FALL)));this.crate=crate;registerDefaultState(stateDefinition.any().setValue(RARITY,0));}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(RARITY);}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return crate?box(1,0,1,15,14,15):box(2,1,2,14,13,14);}
    @Override protected List<ItemStack> getDrops(BlockState s,LootParams.Builder params){var l=params.getLevel();int rarity=s.getValue(RARITY),rolls=1+rarity+l.random.nextInt(3);var result=new ArrayList<ItemStack>();for(int i=0;i<rolls;i++)result.add(LegacyLoot.generate(l,rarity,l.random));return result;}
    @Override public ItemStack getCloneItemStack(BlockState s,HitResult hit,LevelReader l,BlockPos p,Player player){return AncientLootBlockItem.stack(this,s.getValue(RARITY));}
}
