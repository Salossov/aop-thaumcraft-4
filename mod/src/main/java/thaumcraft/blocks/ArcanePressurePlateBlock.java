package thaumcraft.blocks;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.*;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.shapes.*;
import thaumcraft.config.ThaumcraftConfig;

public final class ArcanePressurePlateBlock extends Block implements EntityBlock {
    public static final BooleanProperty POWERED=BlockStateProperties.POWERED;
    public static final IntegerProperty MODE=IntegerProperty.create("mode",0,2);
    public ArcanePressurePlateBlock(){super(Properties.of().strength(2,999).sound(SoundType.WOOD).noOcclusion().pushReaction(PushReaction.BLOCK));registerDefaultState(stateDefinition.any().setValue(POWERED,false).setValue(MODE,0));}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(POWERED,MODE);}
    @Override public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new OwnedBlockEntity(p,s);}
    @Override public void setPlacedBy(Level l,BlockPos p,BlockState s,LivingEntity entity,ItemStack stack){if(entity instanceof Player player&&l.getBlockEntity(p) instanceof OwnedBlockEntity tile)tile.assign(player);}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return Block.box(1,0,1,15,s.getValue(POWERED)?.5:1,15);}
    @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return Shapes.empty();}
    @Override protected boolean isSignalSource(BlockState s){return true;}
    @Override protected int getSignal(BlockState s,BlockGetter l,BlockPos p,Direction side){return s.getValue(POWERED)?15:0;}
    @Override protected int getDirectSignal(BlockState s,BlockGetter l,BlockPos p,Direction side){return getSignal(s,l,p,side);}
    @Override public boolean canConnectRedstone(BlockState s,BlockGetter l,BlockPos p,Direction side){return true;}
    @Override protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos p,Player player,BlockHitResult hit){if(!l.isClientSide&&l.getBlockEntity(p) instanceof OwnedBlockEntity tile&&tile.administers(player.getUUID())){tile.setting=(tile.setting+1)%3;l.setBlock(p,s.setValue(MODE,tile.setting),2);tile.sync();player.displayClientMessage(Component.translatable("thaumcraft.plate.mode."+tile.setting),false);l.playSound(null,p,SoundEvents.STONE_BUTTON_CLICK_ON,SoundSource.BLOCKS,.1f,.9f);}return InteractionResult.sidedSuccess(l.isClientSide);}
    public boolean accepts(Entity entity,OwnedBlockEntity tile){if(entity.isIgnoringBlockTriggers())return false;return tile.setting==0||tile.setting==1&&(!(entity instanceof Player player)||!tile.permits(player.getUUID()))||tile.setting==2&&entity instanceof Player player&&tile.permits(player.getUUID());}
    public void checkEntities(ServerLevel l,BlockPos p){var s=l.getBlockState(p);if(!s.is(this)||!(l.getBlockEntity(p) instanceof OwnedBlockEntity tile))return;boolean pressed=l.getEntities((Entity)null,new AABB(p.getX()+.125,p.getY(),p.getZ()+.125,p.getX()+.875,p.getY()+.25,p.getZ()+.875),e->accepts(e,tile)).size()>0;
        if(pressed!=s.getValue(POWERED)){l.setBlock(p,s.setValue(POWERED,pressed),2);l.updateNeighborsAt(p,this);l.updateNeighborsAt(p.below(),this);l.playSound(null,p,pressed?SoundEvents.STONE_BUTTON_CLICK_ON:SoundEvents.STONE_BUTTON_CLICK_OFF,SoundSource.BLOCKS,.2f,pressed?.6f:.5f);}if(pressed)l.scheduleTick(p,this,20);
    }
    @Override protected void entityInside(BlockState s,Level l,BlockPos p,Entity entity){if(l instanceof ServerLevel server&&!s.getValue(POWERED))checkEntities(server,p);}
    @Override protected void tick(BlockState s,ServerLevel l,BlockPos p,RandomSource random){if(s.getValue(POWERED))checkEntities(l,p);}
    @Override protected void onRemove(BlockState s,Level l,BlockPos p,BlockState next,boolean moving){super.onRemove(s,l,p,next,moving);if(!s.is(next.getBlock())&&s.getValue(POWERED)){l.updateNeighborsAt(p,this);l.updateNeighborsAt(p.below(),this);}}
    @Override protected float getDestroyProgress(BlockState s,Player player,BlockGetter l,BlockPos p){return ThaumcraftConfig.WARDED_STONE.get()?0:super.getDestroyProgress(s,player,l,p);}
    @Override protected List<ItemStack> getDrops(BlockState s,LootParams.Builder context){return ThaumcraftConfig.WARDED_STONE.get()?List.of():List.of(new ItemStack(asItem()));}
    @Override public boolean canEntityDestroy(BlockState s,BlockGetter l,BlockPos p,Entity entity){return false;}
}
