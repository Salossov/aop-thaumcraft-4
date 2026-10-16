package thaumcraft.blocks;
import net.minecraft.core.*;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.effect.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import thaumcraft.registry.R4Content;

public final class PavingStoneBlock extends Block implements EntityBlock {
    public record Rune(double x,double y,double z,float red,float green,float blue,int duration,float gravity) {}
    public static java.util.function.IntUnaryOperator particleCount=base->0;
    public static java.util.function.Consumer<Rune> runes=effect->{};
    public static java.util.function.BiConsumer<BlockPos,Integer> sparkles=(pos,count)->{};
    public final boolean warding;
    // 1.7.10 setResistance(10) stored 30, then returned resistance / 5 to explosions.
    public PavingStoneBlock(boolean warding){super(Properties.of().strength(2,6).lightLevel(s->warding?0:9).isValidSpawn((s,l,p,e)->false));this.warding=warding;}
    @Override public void stepOn(Level l,BlockPos p,BlockState s,Entity e){if(l.getBlockState(p).is(this)&&!warding&&e instanceof LivingEntity living){if(l.isClientSide)sparkles.accept(p,5);living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED,40,1));living.addEffect(new MobEffectInstance(MobEffects.JUMP,40,0));}super.stepOn(l,p,s,e);}
    @Override public BlockEntity newBlockEntity(BlockPos p,BlockState s){return warding?new WardingStoneBlockEntity(p,s):null;}
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level l,BlockState s,BlockEntityType<T> t){return warding&&t==R4Content.WARDING_ENTITY.get()&&!l.isClientSide?(world,pos,state,tile)->((WardingStoneBlockEntity)tile).tick():null;}
    @Override public void animateTick(BlockState s,Level l,BlockPos p,RandomSource random){
        if(!warding||!l.getBlockState(p).is(this))return;
        var r=l.random;
        if(l.hasNeighborSignal(p)){
            for(int i=0;i<particleCount.applyAsInt(2);i++)runes.accept(new Rune(p.getX(),(float)p.getY()+.7f,p.getZ(),.2f+r.nextFloat()*.4f,r.nextFloat()*.3f,.8f+r.nextFloat()*.2f,20,-.02f));
            return;
        }
        var first=l.getBlockState(p.above());var second=l.getBlockState(p.above(2));
        // TC4 checks the first block's normal-cube status twice; retain that branch behavior.
        boolean open=(first.is(R4Content.WARDING_BARRIER.get())||!first.isSolidRender(l,p.above()))
            &&(second.is(R4Content.WARDING_BARRIER.get())||!first.isSolidRender(l,p.above()));
        if(!open){
            for(int i=0;i<particleCount.applyAsInt(3);i++)runes.accept(new Rune(p.getX(),(float)p.getY()+.7f,p.getZ(),.9f+r.nextFloat()*.1f,r.nextFloat()*.3f,r.nextFloat()*.3f,24,-.02f));
        }else for(var entity:l.getEntities(null,new net.minecraft.world.phys.AABB(p).inflate(1))){
            if(entity instanceof LivingEntity&&!(entity instanceof net.minecraft.world.entity.player.Player)){
                runes.accept(new Rune(p.getX(),(float)p.getY()+.6f+r.nextFloat()*Math.max(.8f,entity.getEyeHeight()),p.getZ(),.6f+r.nextFloat()*.4f,0,.3f+r.nextFloat()*.7f,20,0));break;
            }
        }
    }
}
