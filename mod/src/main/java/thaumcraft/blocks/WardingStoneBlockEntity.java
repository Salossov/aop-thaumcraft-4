package thaumcraft.blocks;
import net.minecraft.core.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import thaumcraft.registry.R4Content;

/** Original five-tick airborne repulsion and hundred-tick barrier maintenance. */
public final class WardingStoneBlockEntity extends BlockEntity {
    private int count;
    public WardingStoneBlockEntity(BlockPos p,BlockState s){super(R4Content.WARDING_ENTITY.get(),p,s);}
    public void tick(){
        if(level==null||level.isClientSide)return;if(count==0)count=level.random.nextInt(100);
        if(count%5==0&&!level.hasNeighborSignal(worldPosition))for(var entity:level.getEntitiesOfClass(LivingEntity.class,new AABB(worldPosition).expandTowards(0,2,0).inflate(.1))){if(entity.onGround()||entity instanceof Player)continue;double angle=Math.toRadians(entity.getYRot()+180);entity.push(-Math.sin(angle)*.2,-.1,Math.cos(angle)*.2);}
        if(++count%100==0)for(int y=1;y<=2;y++){var p=worldPosition.above(y);if(level.getBlockState(p).canBeReplaced())level.setBlockAndUpdate(p,R4Content.WARDING_BARRIER.get().defaultBlockState());}
    }
}
