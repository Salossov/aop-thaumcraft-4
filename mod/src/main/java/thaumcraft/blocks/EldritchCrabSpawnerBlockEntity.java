package thaumcraft.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.*;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;
import thaumcraft.entities.*;
import thaumcraft.registry.*;

public final class EldritchCrabSpawnerBlockEntity extends BlockEntity {
    public static java.util.function.Consumer<EldritchCrabSpawnerBlockEntity> vent=tile->{};
    private int count=150,ticks,venting;
    public EldritchCrabSpawnerBlockEntity(BlockPos pos,BlockState state){super(R5Content.CRAB_SPAWNER_ENTITY.get(),pos,state);}
    public int countdown(){return count;}
    public int venting(){return venting;}
    public boolean activated(){var p=Vec3.atCenterOf(worldPosition);return level.getNearestPlayer(p.x,p.y,p.z,16,false)!=null;}
    public boolean crowded(){return level.getEntitiesOfClass(EldritchCrab.class,new AABB(worldPosition).inflate(32)).size()>5;}
    public void tick(){if(level==null)return;if(ticks==0)ticks=level.random.nextInt(500);++ticks;if(level instanceof ServerLevel server){--count;if(count<0){count=50+level.random.nextInt(50);return;}if(count==15&&activated()&&!crowded()){level.blockEvent(worldPosition,getBlockState().getBlock(),1,0);level.playSound(null,worldPosition,SoundEvents.FIRE_EXTINGUISH,SoundSource.BLOCKS,.5f,1);}if(count<=0&&activated()&&!crowded()){count=150+level.random.nextInt(100);spawn(server);level.playSound(null,worldPosition,R2Sounds.GORE.get(),SoundSource.BLOCKS,.5f,1);}}else if(venting>0){--venting;for(int i=0;i<3;i++)vent.accept(this);}else if(level.random.nextInt(20)==0)vent.accept(this);}
    private void spawn(ServerLevel server){var face=getBlockState().getValue(EldritchCrabSpawnerBlock.FACING);var pos=Vec3.atCenterOf(worldPosition.relative(face));var crab=R5Entities.CRAB.get().create(server);if(crab==null)return;crab.moveTo(pos.x,pos.y,pos.z,0,0);crab.finalizeSpawn(server,server.getCurrentDifficultyAt(crab.blockPosition()),MobSpawnType.SPAWNER,null);crab.setHelm(false);crab.setDeltaMovement(face.getStepX()*.2f,face.getStepY()*.2f,face.getStepZ()*.2f);server.addFreshEntity(crab);}
    @Override public boolean triggerEvent(int id,int value){if(id==1){venting=20;return true;}return super.triggerEvent(id,value);}
    // Native facing is persisted by the block state; the original countdown and vent state reset on load.
}
