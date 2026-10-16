package thaumcraft.entities;

import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.syncher.*;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.blocks.TaintFibresBlock;
import thaumcraft.blocks.TaintSolidBlock;
import thaumcraft.registry.R2Content;
import thaumcraft.registry.R2Sounds;

/** TC4 falling taint, including deferred source removal and the dense-goo landing rule. */
public final class FallingTaint extends Entity {
    private static final EntityDataAccessor<BlockState> BLOCK=SynchedEntityData.defineId(FallingTaint.class,EntityDataSerializers.BLOCK_STATE);
    private static final double CENTRE_OFFSET=(double).98f/2;
    public static Consumer<FallingTaint> clientLanding=entity->{};
    private BlockPos source=BlockPos.ZERO;
    private int fallTime;

    public FallingTaint(EntityType<? extends FallingTaint> type,Level level){super(type,level);}
    public FallingTaint(Level level,BlockPos source,BlockPos target,BlockState state){
        this(R2Entities.FALLING_TAINT.get(),level);
        this.source=source.immutable();entityData.set(BLOCK,state);
        // Legacy posY was the centre of the .98-high collision box, rather than its feet.
        setPos(target.getX()+.5,target.getY()+.5-CENTRE_OFFSET,target.getZ()+.5);
        xo=getX();yo=getY();zo=getZ();setDeltaMovement(0,0,0);
    }
    public BlockState blockState(){return entityData.get(BLOCK);}
    public double centreY(){return getY()+CENTRE_OFFSET;}
    public double renderOffsetY(){return CENTRE_OFFSET-.5;}
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder){builder.define(BLOCK,Blocks.AIR.defaultBlockState());}
    @Override public boolean isPickable(){return isAlive();}
    @Override public boolean displayFireAnimation(){return false;}
    @Override public boolean causeFallDamage(float distance,float multiplier,DamageSource damage){return false;}

    @Override public void tick(){
        if(blockState().isAir()){discard();return;}
        super.tick();xo=getX();yo=getY();zo=getZ();++fallTime;
        setDeltaMovement(getDeltaMovement().add(0,-(double).04f,0));
        move(MoverType.SELF,getDeltaMovement());setDeltaMovement(getDeltaMovement().scale((double).98f));
        var landing=BlockPos.containing(getX(),centreY(),getZ());
        if(level().isClientSide){
            if(onGround()||fallTime==1)clientLanding.accept(this);
            return;
        }
        if(fallTime==1){
            if(!level().getBlockState(source).is(blockState().getBlock())){discard();return;}
            level().removeBlock(source,false);
        }
        if(onGround()||TaintSolidBlock.isDenseGoo(level().getBlockState(landing.below()))){
            var velocity=getDeltaMovement();setDeltaMovement(velocity.x*(double).7f,velocity.y*-.5,velocity.z*(double).7f);
            var existing=level().getBlockState(landing);
            if(existing.is(Blocks.PISTON)||existing.is(Blocks.PISTON_HEAD)||existing.is(Blocks.MOVING_PISTON))return;
            playSound(R2Sounds.GORE.get(),.5f,((random.nextFloat()-random.nextFloat())*.2f+1)*.8f);
            discard();
            if(canPlace(landing,existing)&&!TaintSolidBlock.canFallBelow(level(),landing.below()))level().setBlock(landing,blockState(),3);
        }else if(fallTime>600||fallTime>100&&(centreY()<level().getMinBuildHeight()+1||centreY()>level().getMaxBuildHeight()))discard();
    }
    private boolean canPlace(BlockPos pos,BlockState existing){
        return existing.getBlock() instanceof TaintFibresBlock||existing.is(R2Content.FLUX_GOO.get())
            ||existing.canBeReplaced()&&blockState().canSurvive(level(),pos);
    }
    @Override protected void addAdditionalSaveData(CompoundTag tag){
        tag.put("BlockState",NbtUtils.writeBlockState(blockState()));tag.putByte("Time",(byte)fallTime);
        tag.putInt("OldX",source.getX());tag.putInt("OldY",source.getY());tag.putInt("OldZ",source.getZ());
    }
    @Override protected void readAdditionalSaveData(CompoundTag tag){
        entityData.set(BLOCK,NbtUtils.readBlockState(level().holderLookup(Registries.BLOCK),tag.getCompound("BlockState")));
        fallTime=tag.getByte("Time")&255;source=new BlockPos(tag.getInt("OldX"),tag.getInt("OldY"),tag.getInt("OldZ"));
    }
}
