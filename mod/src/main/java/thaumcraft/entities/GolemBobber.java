package thaumcraft.entities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.syncher.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.entity.IEntityWithComplexSpawn;

/** TC4 golem cast and buoyancy. Catch selection belongs to the golem's fishing job. */
public final class GolemBobber extends Entity implements IEntityWithComplexSpawn {
    private static final EntityDataAccessor<Integer> FISHER=SynchedEntityData.defineId(GolemBobber.class,EntityDataSerializers.INT);
    private boolean inBlock;
    public GolemBobber(EntityType<? extends GolemBobber> type,Level level){super(type,level);noPhysics=true;}
    public GolemBobber(ServerLevel level,ThaumicGolem fisher,BlockPos water){
        // Keep the original velocity, but clear the modern collision box of the launch surface.
        this(R3Entities.GOLEM_BOBBER.get(),level);entityData.set(FISHER,fisher.getId());setPos(fisher.position().add(0,fisher.getBbHeight()*.65,0));
        double x=water.getX()+.5-fisher.getX(),y=water.getY()+1-fisher.getY(),z=water.getZ()+.5-fisher.getZ();
        double length=(float)Math.sqrt(x*x+y*y+z*z);
        setDeltaMovement(x*.1,y*.1+(double)(float)Math.sqrt(length)*.08,z*.1);
    }
    public ThaumicGolem fisher(){return level().getEntity(entityData.get(FISHER)) instanceof ThaumicGolem golem?golem:null;}
    @Override public void writeSpawnData(RegistryFriendlyByteBuf buffer){var velocity=getDeltaMovement();buffer.writeDouble(velocity.x);buffer.writeDouble(velocity.y);buffer.writeDouble(velocity.z);buffer.writeInt(entityData.get(FISHER));}
    @Override public void readSpawnData(RegistryFriendlyByteBuf buffer){setDeltaMovement(buffer.readDouble(),buffer.readDouble(),buffer.readDouble());entityData.set(FISHER,buffer.readInt());}
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder){builder.define(FISHER,-1);}
    @Override protected void addAdditionalSaveData(CompoundTag tag){}
    @Override protected void readAdditionalSaveData(CompoundTag tag){}
    @Override public void tick(){
        super.tick();
        if(level() instanceof ServerLevel server){
            if(fisher()==null||!fisher().isAlive()){discard();return;}
            if(random.nextFloat()<.02f)server.sendParticles(ParticleTypes.SPLASH,getX()+random.nextFloat()-random.nextFloat(),getY()+random.nextFloat(),getZ()+random.nextFloat()-random.nextFloat(),2+random.nextInt(2),(double).1f,0,(double).1f,0);
        }
        // The original increments ticksExisted here in addition to Entity's normal tick.
        if(tickCount++>4000){discard();return;}
        if(inBlock){inBlock=false;setDeltaMovement(getDeltaMovement().multiply(random.nextFloat()*.2f,random.nextFloat()*.2f,random.nextFloat()*.2f));}
        var hit=level().clip(new ClipContext(position(),position().add(getDeltaMovement()),ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,this));
        if(hit.getType()!=HitResult.Type.MISS){inBlock=true;if(!level().getFluidState(hit.getBlockPos()).is(FluidTags.WATER))discard();}
        if(inBlock)return;
        move(MoverType.SELF,getDeltaMovement());var velocity=getDeltaMovement();
        float yaw=(float)(Math.atan2(velocity.x,velocity.z)*180/Math.PI),pitch=(float)(Math.atan2(velocity.y,(float)Math.sqrt(velocity.x*velocity.x+velocity.z*velocity.z))*180/Math.PI);
        setYRot(yRotO+Mth.wrapDegrees(yaw-yRotO)*.2f);setXRot(xRotO+Mth.wrapDegrees(pitch-xRotO)*.2f);
        double water=submergedFraction();float drag=onGround()||horizontalCollision?.5f:.92f;
        if(level() instanceof ServerLevel&&water>0){
            // These weather draws are present even though the legacy bite counters are never armed.
            if(random.nextFloat()<.25f)level().isRainingAt(blockPosition().above());
            if(random.nextFloat()<.5f)level().canSeeSky(blockPosition().above());
        }
        velocity=velocity.add(0,(double).04f*(water*2-1),0);
        if(water>0){drag=(float)(drag*.9);velocity=velocity.multiply(1,.8,1);}
        setDeltaMovement(velocity.scale(drag));
    }
    private double submergedFraction(){
        var bounds=getBoundingBox();double fraction=0;
        for(int slice=0;slice<5;slice++){
            var box=new AABB(bounds.minX,bounds.minY+bounds.getYsize()*slice/5,bounds.minZ,bounds.maxX,bounds.minY+bounds.getYsize()*(slice+1)/5,bounds.maxZ);
            boolean wet=false;
            for(var pos:BlockPos.betweenClosed(BlockPos.containing(box.minX,box.minY,box.minZ),BlockPos.containing(box.maxX,box.maxY,box.maxZ))){
                if(!level().hasChunkAt(pos))continue;var fluid=level().getFluidState(pos);
                if(!fluid.is(FluidTags.WATER))continue;
                var state=level().getBlockState(pos);double height=fluid.getHeight(level(),pos);
                if(state.hasProperty(net.minecraft.world.level.block.LiquidBlock.LEVEL)){
                    int metadata=state.getValue(net.minecraft.world.level.block.LiquidBlock.LEVEL);
                    height=metadata<8?1-metadata/8d:1;
                }
                if(pos.getY()+height>=box.minY){wet=true;break;}
            }
            if(wet)fraction+=1/5d;
        }
        return fraction;
    }
}
