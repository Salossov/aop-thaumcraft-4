package thaumcraft.blocks;

import java.util.*;
import java.util.function.BiConsumer;
import net.minecraft.core.*;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.*;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import thaumcraft.config.ThaumcraftConfig;
import thaumcraft.registry.*;

public final class ArcaneDoorBlock extends DoorBlock implements EntityBlock {
    public ArcaneDoorBlock(){super(BlockSetType.IRON,Properties.of().strength(15,999).noOcclusion().pushReaction(PushReaction.BLOCK));}
    @Override public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new OwnedBlockEntity(p,s);}
    @Override public BlockState getStateForPlacement(BlockPlaceContext c){
        if(c.getClickedFace()!=Direction.UP)return null;var p=c.getClickedPos();var l=c.getLevel();if(p.getY()>=l.getMaxBuildHeight()-1||!l.getBlockState(p.above()).canBeReplaced(c)||!l.getBlockState(p.below()).isFaceSturdy(l,p.below(),Direction.UP))return null;
        int orientation=net.minecraft.util.Mth.floor((c.getRotation()+180)*4/360-.5)&3;var facing=new Direction[]{Direction.EAST,Direction.SOUTH,Direction.WEST,Direction.NORTH}[orientation];var right=facing.getClockWise();var a=p.relative(right.getOpposite());var b=p.relative(right);int leftSolid=solid(l,a)+solid(l,a.above()),rightSolid=solid(l,b)+solid(l,b.above());boolean leftDoor=l.getBlockState(a).is(this)||l.getBlockState(a.above()).is(this),rightDoor=l.getBlockState(b).is(this)||l.getBlockState(b.above()).is(this);boolean hinge=leftDoor&&!rightDoor||rightSolid>leftSolid;
        return defaultBlockState().setValue(FACING,facing).setValue(HINGE,hinge?DoorHingeSide.RIGHT:DoorHingeSide.LEFT);
    }
    private static int solid(Level l,BlockPos p){return l.getBlockState(p).isCollisionShapeFullBlock(l,p)?1:0;}
    @Override public void setPlacedBy(Level l,BlockPos p,BlockState s,LivingEntity entity,ItemStack stack){l.setBlock(p.above(),s.setValue(HALF,DoubleBlockHalf.UPPER),2);if(entity instanceof Player player){if(l.getBlockEntity(p) instanceof OwnedBlockEntity tile)tile.assign(player);if(l.getBlockEntity(p.above()) instanceof OwnedBlockEntity tile)tile.assign(player);}l.updateNeighborsAt(p,this);l.updateNeighborsAt(p.above(),this);}
    @Override protected BlockState updateShape(BlockState s,Direction side,BlockState neighbor,LevelAccessor l,BlockPos p,BlockPos from){var half=s.getValue(HALF);if(side.getAxis()==Direction.Axis.Y&&(half==DoubleBlockHalf.LOWER)==(side==Direction.UP))return neighbor.is(this)&&neighbor.getValue(HALF)!=half?neighbor.setValue(HALF,half):Blocks.AIR.defaultBlockState();return s;}
    @Override protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos p,Player player,BlockHitResult hit){if(!l.isClientSide){var lower=OwnedBlockInteractions.lower(l,p);if(l.getBlockEntity(lower) instanceof OwnedBlockEntity tile&&tile.permits(player.getUUID()))setOpen(player,l,l.getBlockState(lower),lower,!s.getValue(OPEN));else{player.displayClientMessage(Component.translatable("thaumcraft.door.refuses"),false);l.playSound(null,p,R2Sounds.DOOR_FAIL.get(),SoundSource.BLOCKS,.66f,1);}}return InteractionResult.sidedSuccess(l.isClientSide);}
    @Override public void setOpen(Entity player,Level l,BlockState s,BlockPos p,boolean open){p=OwnedBlockInteractions.lower(l,p);s=l.getBlockState(p);if(!s.is(this)||s.getValue(OPEN)==open)return;l.setBlock(p,s.setValue(OPEN,open),3);l.playSound(null,p,l.random.nextBoolean()?SoundEvents.WOODEN_DOOR_OPEN:SoundEvents.WOODEN_DOOR_CLOSE,SoundSource.BLOCKS,1,.9f+l.random.nextFloat()*.1f);}
    @Override protected void neighborChanged(BlockState s,Level l,BlockPos p,Block neighbor,BlockPos from,boolean moving){
        if(l.isClientSide||!(neighbor instanceof ArcanePressurePlateBlock))return;var lower=OwnedBlockInteractions.lower(l,p);if(!(l.getBlockEntity(lower) instanceof OwnedBlockEntity door))return;Boolean opened=null;
        for(var direction:Direction.Plane.HORIZONTAL){var at=lower.relative(direction);var plateState=l.getBlockState(at);if(!(plateState.getBlock() instanceof ArcanePressurePlateBlock)||!(l.getBlockEntity(at) instanceof OwnedBlockEntity plate)||!door.users().contains(plate.owner()))continue;opened=plateState.getValue(ArcanePressurePlateBlock.POWERED);if(opened)break;}
        if(opened!=null)setOpen(null,l,l.getBlockState(lower),lower,opened);
    }
    @Override protected float getDestroyProgress(BlockState s,Player player,BlockGetter l,BlockPos p){return ThaumcraftConfig.WARDED_STONE.get()?0:super.getDestroyProgress(s,player,l,p);}
    @Override protected List<ItemStack> getDrops(BlockState s,LootParams.Builder context){return !ThaumcraftConfig.WARDED_STONE.get()&&s.getValue(HALF)==DoubleBlockHalf.LOWER?List.of(new ItemStack(asItem())):List.of();}
    @Override public boolean canEntityDestroy(BlockState s,BlockGetter l,BlockPos p,Entity entity){return false;}
    @Override public void onBlockExploded(BlockState s,Level l,BlockPos p,Explosion explosion){}
    @Override protected void onExplosionHit(BlockState s,Level l,BlockPos p,Explosion explosion,BiConsumer<ItemStack,BlockPos> drops){}
}
