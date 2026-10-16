package thaumcraft.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import thaumcraft.registry.R4Content;

/** TC4 lift acceleration and obstacle/stack range. Sneaking is per entity in multiplayer. */
public final class LevitatorBlockEntity extends BlockEntity {
    private int ticks,rangeAbove;
    private boolean requiresUpdate=true,lastPowerState;
    public LevitatorBlockEntity(BlockPos p,BlockState s){super(R4Content.LEVITATOR_ENTITY.get(),p,s);}
    public int rangeAbove(){return rangeAbove;}
    public boolean lastPowerState(){return lastPowerState;}
    public boolean gettingPower(){return level!=null&&(level.hasNeighborSignal(worldPosition)||level.hasNeighborSignal(worldPosition.above()));}
    public void invalidateRange(){requiresUpdate=true;}
    public void tick(){
        if(level==null)return;
        ++ticks;if(requiresUpdate||ticks%100==0){lastPowerState=gettingPower();requiresUpdate=false;int max=10;for(var p=worldPosition.below();p.getY()>=level.getMinBuildHeight()&&level.getBlockState(p).is(R4Content.LEVITATOR.get())&&!level.hasNeighborSignal(p);p=p.below())max+=10;rangeAbove=0;while(rangeAbove<max&&worldPosition.getY()+1+rangeAbove<level.getMaxBuildHeight()){var p=worldPosition.above(1+rangeAbove);if(level.getBlockState(p).isSolidRender(level,p))break;rangeAbove++;}}
        boolean powered=gettingPower();if(!level.isClientSide&&getBlockState().getValue(LevitatorBlock.POWERED)!=powered)level.setBlock(worldPosition,getBlockState().setValue(LevitatorBlock.POWERED,powered),3);
        if(rangeAbove<=0||powered)return;
        for(var e:level.getEntitiesOfClass(Entity.class,new AABB(worldPosition.above()).expandTowards(0,rangeAbove-1,0))){if(!(e instanceof ItemEntity)&&!e.isPushable()&&!(e instanceof AbstractHorse))continue;var velocity=e.getDeltaMovement();double y=velocity.y;if(e.isShiftKeyDown()){if(y<0)y*=.9f;}else if(y<.35f)y+=.1f;e.fallDistance=0;if(y!=velocity.y){e.setDeltaMovement(velocity.x,y,velocity.z);e.hasImpulse=true;if(!level.isClientSide&&e instanceof Player)e.hurtMarked=true;}}
    }
}
