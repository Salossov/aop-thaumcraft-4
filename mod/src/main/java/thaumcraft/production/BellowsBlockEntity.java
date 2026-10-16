package thaumcraft.production;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.mixin.FurnaceCookingAccessor;
import thaumcraft.registry.R3Content;
public final class BellowsBlockEntity extends BlockEntity{
 public float inflation=.7f,previousInflation=.7f;
 private boolean expanding,first=true;
 private int delay;
 public BellowsBlockEntity(BlockPos p,BlockState s){super(R3Content.BELLOWS_ENTITY.get(),p,s);}
 public void tick(){if(level.hasNeighborSignal(worldPosition))return;
  if(level.isClientSide){previousInflation=inflation;if(first){inflation=.35f+level.random.nextFloat()*.55f;previousInflation=inflation;first=false;}if(inflation>.35f&&!expanding)inflation-=.075f;if(inflation<=.35f&&!expanding)expanding=true;if(inflation<1&&expanding)inflation+=.025f;if(inflation>=1&&expanding){expanding=false;level.playLocalSound(worldPosition.getX()+.5,worldPosition.getY()+.5,worldPosition.getZ()+.5,SoundEvents.GHAST_SHOOT,SoundSource.BLOCKS,.01f,.5f+(level.random.nextFloat()-level.random.nextFloat())*.2f,false);}}
  else if(++delay>=2){delay=0;var p=worldPosition.relative(getBlockState().getValue(BellowsBlock.FACING));if(level.hasChunkAt(p)&&level.getBlockEntity(p) instanceof FurnaceBlockEntity furnace){var cooking=(FurnaceCookingAccessor)furnace;int progress=cooking.thaumcraft$cookingProgress();if(progress>0&&progress<cooking.thaumcraft$cookingTotalTime()-1){cooking.thaumcraft$cookingProgress(progress+1);furnace.setChanged();}}}
 }
}
