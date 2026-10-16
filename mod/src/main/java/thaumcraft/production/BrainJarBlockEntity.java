package thaumcraft.production;

import java.util.Comparator;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.*;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import thaumcraft.registry.*;

public final class BrainJarBlockEntity extends BlockEntity {
    private int xp,eatDelay;
    private float rotation,previousRotation;
    private long nextSigh;
    public BrainJarBlockEntity(BlockPos pos,BlockState state){super(R3Content.BRAIN_JAR_ENTITY.get(),pos,state);}
    public int experience(){return xp;}
    public float rotation(float partial){return net.minecraft.util.Mth.rotLerp(partial,previousRotation,rotation);}
    public void release(){
        if(!(level instanceof ServerLevel server))return;eatDelay=40;
        int released=server.random.nextInt(Math.min(xp+1,64));
        if(released>0){xp-=released;ExperienceOrb.award(server,worldPosition.getCenter(),released);changed();}
        server.playSound(null,worldPosition,R2Sounds.JAR.get(),SoundSource.BLOCKS,.2f,1);
    }
    public void serverTick(){
        if(!(level instanceof ServerLevel server))return;
        if(eatDelay>0){eatDelay--;return;}if(xp>=2000)return;
        var center=worldPosition.getCenter();var nearby=server.getEntitiesOfClass(ExperienceOrb.class,new AABB(worldPosition).inflate(6));
        var closest=nearby.stream().filter(orb->orb.isAlive()).min(Comparator.comparingDouble(orb->orb.position().distanceToSqr(center))).orElse(null);
        if(closest!=null){var delta=center.subtract(closest.position()).scale(1.0/7);double distance=delta.length(),strength=1-distance;if(strength>0&&distance>1e-8)closest.setDeltaMovement(closest.getDeltaMovement().add(delta.multiply(.15,.33,.15).scale(strength*strength/distance)));}
        for(var orb:nearby){if(xp>=2000)break;if(!orb.isAlive()||!orb.getBoundingBox().intersects(new AABB(worldPosition).inflate(.1)))continue;
            // 1.21.1 may merge many equal-valued orbs into one entity; account for all of them.
            var saved=new CompoundTag();orb.saveWithoutId(saved);int amount=orb.getValue()*Math.max(1,saved.getInt("Count"));
            int stored=Math.min(2000-xp,amount);xp+=stored;var at=orb.position();orb.discard();if(amount>stored)ExperienceOrb.award(server,at,amount-stored);
            server.playSound(null,worldPosition,SoundEvents.GENERIC_EAT,SoundSource.BLOCKS,.1f,1+(server.random.nextFloat()-server.random.nextFloat())*.2f);changed();
        }
    }
    public void clientTick(){
        if(level==null)return;previousRotation=rotation;var center=worldPosition.getCenter();var closest=level.getEntitiesOfClass(ExperienceOrb.class,new AABB(worldPosition).inflate(6)).stream().min(Comparator.comparingDouble(orb->orb.position().distanceToSqr(center))).orElse(null);
        var target=closest!=null?closest:level.getNearestPlayer(center.x,center.y,center.z,6,false);
        float wanted=target==null?rotation+.57f:(float)(Math.atan2(target.getZ()-center.z,target.getX()-center.x)*180/Math.PI);
        rotation+=net.minecraft.util.Mth.wrapDegrees(wanted-rotation)*.04f;
        if(closest==null&&target!=null&&level.getGameTime()>=nextSigh){level.playLocalSound(center.x,center.y,center.z,R2Sounds.BRAIN.get(),SoundSource.BLOCKS,.15f,.8f+level.random.nextFloat()*.4f,false);nextSigh=level.getGameTime()+100+level.random.nextInt(500);}
    }
    private void changed(){setChanged();if(level!=null)level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),3);}
    @Override protected void saveAdditional(CompoundTag tag,HolderLookup.Provider lookup){super.saveAdditional(tag,lookup);tag.putInt("XP",xp);tag.putInt("EatDelay",eatDelay);}
    @Override protected void loadAdditional(CompoundTag tag,HolderLookup.Provider lookup){super.loadAdditional(tag,lookup);xp=Math.clamp(tag.getInt("XP"),0,2000);eatDelay=Math.clamp(tag.getInt("EatDelay"),0,40);}
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider lookup){return saveWithoutMetadata(lookup);}
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket(){return ClientboundBlockEntityDataPacket.create(this);}
}
