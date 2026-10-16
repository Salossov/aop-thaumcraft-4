package thaumcraft.entities;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import thaumcraft.effects.TaintEffects;
import thaumcraft.registry.*;
import thaumcraft.world.WorldBiomes;

public final class BottleTaintProjectile extends ThrowableItemProjectile {
    public BottleTaintProjectile(EntityType<? extends BottleTaintProjectile> type,Level level){super(type,level);}
    @Override protected Item getDefaultItem(){return R3Equipment.BOTTLE_TAINT.get();}
    @Override protected double getDefaultGravity(){return .05f;}
    @Override public void shoot(double x,double y,double z,float speed,float scatter){LegacyThrowableLaunch.shoot(this,random,x,y,z,speed,scatter);}
    public static BottleTaintProjectile thrown(Level level,LivingEntity owner){
        var e=new BottleTaintProjectile(R3Entities.BOTTLE_TAINT.get(),level);LegacyThrowableLaunch.owner(e,owner,.5f,-20);return e;
    }
    @Override protected void onHit(HitResult hit){
        super.onHit(hit);if(!(level() instanceof net.minecraft.server.level.ServerLevel server))return;
        for(var living:server.getEntitiesOfClass(LivingEntity.class,new AABB(position(),position()).inflate(5)))
            if(!(living instanceof TaintedMob)&&!living.isInvertedHealAndHarm())living.addEffect(new MobEffectInstance(TaintEffects.FLUX_TAINT,100,0));
        // TC4 truncates rather than floors negative impact coordinates.
        int x=(int)getX(),y=(int)getY(),z=(int)getZ();
        for(int i=0;i<10;i++){
            int xx=x+(int)((random.nextFloat()-random.nextFloat())*5),zz=z+(int)((random.nextFloat()-random.nextFloat())*5);
            var p=new BlockPos(xx,y,zz);
            if(!server.hasChunkAt(p)||!server.random.nextBoolean()||WorldBiomes.is(server,p,WorldBiomes.TAINT))continue;
            WorldBiomes.change(server,xx,zz,WorldBiomes.TAINT);
            if(server.getBlockState(p.below()).isFaceSturdy(server,p.below(),net.minecraft.core.Direction.UP)&&server.getBlockState(p).canBeReplaced())server.setBlockAndUpdate(p,R2Content.TAINT_FIBRES.get().defaultBlockState());
        }
        net.neoforged.neoforge.network.PacketDistributor.sendToPlayersNear(server,null,getX(),getY(),getZ(),64,new thaumcraft.network.BottleTaintBreakPayload(position(),getBbHeight()));
        discard();
    }
}
