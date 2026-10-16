package thaumcraft.blocks;

import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;
import thaumcraft.api.entities.IEldritchMob;
import thaumcraft.registry.R5Content;

public final class EldritchObeliskBlockEntity extends BlockEntity {
    public static BiConsumer<Vec3,LivingEntity> wisp=(p,e)->{};
    public EldritchObeliskBlockEntity(BlockPos pos,BlockState state){super(R5Content.OBELISK_ENTITY.get(),pos,state);}
    public AABB renderBounds(){return new AABB(worldPosition.getX(),worldPosition.getY(),worldPosition.getZ(),worldPosition.getX()+1,worldPosition.getY()+5,worldPosition.getZ()+1);}
    public static void tick(Level level,BlockPos pos,BlockState state,EldritchObeliskBlockEntity tile){
        double x=pos.getX()+.5,y=pos.getY(),z=pos.getZ()+.5;
        // TC4's counter is never incremented: the server condition runs every tick, not every 20 ticks.
        for(var entity:level.getEntitiesOfClass(LivingEntity.class,new AABB(x-6,y-6,z-6,x+6,y+6,z+6),e->e instanceof IEldritchMob)){
            if(level.isClientSide)wisp.accept(new Vec3(x,y+1+level.random.nextFloat()*3,z),entity);
            else if(!entity.hasEffect(MobEffects.REGENERATION)){entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST,40,0,true,true));entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION,40,0,true,true));}
        }
    }
}
