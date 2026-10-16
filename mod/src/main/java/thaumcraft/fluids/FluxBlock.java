package thaumcraft.fluids;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.material.*;
import net.minecraft.world.phys.shapes.*;
import thaumcraft.config.ThaumcraftConfig;
import thaumcraft.effects.TaintEffects;
import thaumcraft.entities.*;
import thaumcraft.registry.R2Content;
import thaumcraft.world.WorldBiomes;

/** TC4 finite flow, including its overfull metadata states and ascending gas. */
public final class FluxBlock extends Block {
    private static final Direction[] SIDES={Direction.NORTH,Direction.SOUTH,Direction.WEST,Direction.EAST};
    private final boolean gas;
    public FluxBlock(boolean gas) {
        super(Properties.of().mapColor(MapColor.COLOR_PURPLE).noCollission().noOcclusion().strength(100).noLootTable().randomTicks()
            .lightLevel(s->Math.min(15,(s.getValue(FiniteFluxFluid.QUANTA)-1)*7/8)).sound(SoundType.SLIME_BLOCK));
        this.gas=gas;registerDefaultState(stateDefinition.any().setValue(FiniteFluxFluid.QUANTA,8));
    }
    public FiniteFluxFluid fluid() {return gas?R2Fluids.GAS.get():R2Fluids.GOO.get();}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder) {builder.add(FiniteFluxFluid.QUANTA);}
    @Override protected FluidState getFluidState(BlockState state) {return fluid().defaultFluidState().setValue(FiniteFluxFluid.QUANTA,state.getValue(FiniteFluxFluid.QUANTA));}
    @Override protected RenderShape getRenderShape(BlockState state) {return RenderShape.INVISIBLE;}
    @Override protected VoxelShape getShape(BlockState state,BlockGetter level,BlockPos pos,CollisionContext context) {return Shapes.empty();}
    @Override protected boolean canBeReplaced(BlockState state,BlockPlaceContext context) {return state.getValue(FiniteFluxFluid.QUANTA)<3;}
    @Override protected void onPlace(BlockState state,Level level,BlockPos pos,BlockState old,boolean moved) {level.scheduleTick(pos,this,fluid().getTickDelay(level));}
    @Override protected void neighborChanged(BlockState state,Level level,BlockPos pos,Block neighbor,BlockPos from,boolean moving) {level.scheduleTick(pos,this,fluid().getTickDelay(level));}
    @Override protected void randomTick(BlockState state,ServerLevel level,BlockPos pos,RandomSource random) {tick(state,level,pos,random);}
    @Override protected void tick(BlockState state,ServerLevel level,BlockPos pos,RandomSource random) {
        flow(level,pos,random);if(!gas) ageGoo(level,pos,random);
    }
    public static void setUnits(Level level,BlockPos pos,FluxBlock block,int amount) {
        if(amount<=0) level.setBlockAndUpdate(pos,Blocks.AIR.defaultBlockState());
        else {level.setBlockAndUpdate(pos,block.defaultBlockState().setValue(FiniteFluxFluid.QUANTA,Math.min(16,amount)));level.scheduleTick(pos,block,block.fluid().getTickDelay(level));}
    }
    private int units(ServerLevel level,BlockPos pos) {var state=level.getBlockState(pos);return state.is(this)?state.getValue(FiniteFluxFluid.QUANTA):state.isAir()?0:-1;}
    private boolean displace(ServerLevel level,BlockPos pos) {
        if(!level.hasChunkAt(pos)||level.isOutsideBuildHeight(pos)) return false;var state=level.getBlockState(pos);
        if(state.is(this)||state.is(BlockTags.DOORS)||state.is(BlockTags.SIGNS)||state.is(Blocks.SUGAR_CANE)||state.is(Blocks.NETHER_PORTAL)||state.is(Blocks.END_PORTAL)) return false;
        if(state.getBlock() instanceof FluxBlock other) {
            if(fluid().getFluidType().getDensity()<=other.fluid().getFluidType().getDensity()) return false;
        } else if(state.blocksMotion()) return false;
        if(!state.isAir()&&state.getFluidState().isEmpty()) Block.dropResources(state,level,pos);
        level.setBlock(pos,Blocks.AIR.defaultBlockState(),2);return true;
    }
    /** Public for the dedicated-server conservation harness; ageing is deliberately separate. */
    public void flow(ServerLevel level,BlockPos pos,RandomSource random) {
        if(!level.getBlockState(pos).is(this)) return;int amount=units(level,pos);var vertical=gas?pos.above():pos.below();
        if(level.isOutsideBuildHeight(vertical)) {setUnits(level,pos,this,0);return;}
        if(level.hasChunkAt(vertical)) {
            int below=units(level,vertical);
            if(below>=0&&below<8) {
                int total=amount+below;setUnits(level,vertical,this,Math.min(8,total));amount=Math.max(0,total-8);setUnits(level,pos,this,amount);
                if(amount<=1) return;
            } else if(below<0) {
                var other=level.getBlockState(vertical);
                if(other.getBlock() instanceof FluxBlock otherFlux) {
                    int density=fluid().getFluidType().getDensity(),otherDensity=otherFlux.fluid().getFluidType().getDensity();
                    if(gas?density<otherDensity:density>otherDensity) {
                        level.setBlockAndUpdate(pos,other);setUnits(level,vertical,this,amount);level.scheduleTick(pos,otherFlux,otherFlux.fluid().getTickDelay(level));return;
                    }
                } else if(displace(level,vertical)) {setUnits(level,vertical,this,amount);setUnits(level,pos,this,0);return;}
            }
        }
        if(amount<=1) return;
        int total=amount,count=1;var eligible=new LinkedHashMap<BlockPos,Integer>();
        for(var dir:SIDES) {
            var target=pos.relative(dir);if(!level.hasChunkAt(target)) continue;displace(level,target);int q=units(level,target);
            if(q>=0&&q<amount-1) {eligible.put(target,q);total+=q;count++;}
        }
        if(count==1) return;int each=total/count,remainder=total%count;
        for(var entry:eligible.entrySet()) {
            int assigned=each;if(remainder==count||remainder>1&&random.nextInt(count-remainder)!=0) {assigned++;remainder--;}
            if(assigned!=entry.getValue()) setUnits(level,entry.getKey(),this,assigned);count--;
        }
        setUnits(level,pos,this,each+(remainder>0?1:0));
    }
    private void ageGoo(ServerLevel level,BlockPos pos,RandomSource random) {
        var state=level.getBlockState(pos);if(!state.is(this)) return;int meta=state.getValue(FiniteFluxFluid.QUANTA)-1;boolean open=level.isEmptyBlock(pos.above());
        if(meta>=2&&meta<6&&open&&random.nextInt(25)==0) slime(level,pos,1);
        else if(meta>=6&&open) {
            if(random.nextInt(25)==0) slime(level,pos,2);
            else if(ThaumcraftConfig.TAINT_FROM_FLUX.get()&&random.nextInt(50)==0) {WorldBiomes.change(level,pos.getX(),pos.getZ(),WorldBiomes.TAINT);level.setBlockAndUpdate(pos,R2Content.TAINT_FIBRES.get().defaultBlockState());}
        } else if(random.nextInt(30)==0) {
            setUnits(level,pos,this,meta);if(meta>0&&open&&random.nextBoolean()) setUnits(level,pos.above(),R2Content.FLUX_GAS.get(),1);
        }
    }
    private void slime(ServerLevel level,BlockPos pos,int size) {var slime=R2Entities.THAUMIC_SLIME.get().create(level);slime.size(size);slime.moveTo(pos.getX()+0.5,pos.getY(),pos.getZ()+0.5,0,0);setUnits(level,pos,this,0);level.addFreshEntity(slime);}
    @Override protected void entityInside(BlockState state,Level level,BlockPos pos,Entity entity) {
        int meta=state.getValue(FiniteFluxFluid.QUANTA)-1;
        if(!gas&&!(entity instanceof ThaumicSlime)) {var speed=entity.getDeltaMovement();double factor=1-(meta+1)/8d;entity.setDeltaMovement(speed.x*factor,speed.y,speed.z*factor);}
        if(level.isClientSide) return;
        if(!gas) {
            if(entity instanceof ThaumicSlime slime) {if(slime.size()<meta&&level.random.nextBoolean()) {slime.size(slime.size()+1);setUnits(level,pos,this,meta>1?meta:0);}}
            else if(entity instanceof LivingEntity living) exhaust(living,600,meta/3);
        } else if(level.random.nextInt(10)==0&&entity instanceof LivingEntity living&&!(entity instanceof TaintedMob)&&!living.isInvertedHealAndHarm()
            &&!living.hasEffect(TaintEffects.VIS_EXHAUST)&&!living.hasEffect(MobEffects.CONFUSION)) {
            if(level.random.nextBoolean()) exhaust(living,1200,meta/3);
            else living.addEffect(new MobEffectInstance(MobEffects.CONFUSION,80+meta*20));setUnits(level,pos,this,meta);
        }
    }
    private static void exhaust(LivingEntity living,int duration,int amplifier){var effect=new MobEffectInstance(TaintEffects.VIS_EXHAUST,duration,amplifier,true,false);effect.getCures().clear();living.addEffect(effect);}

}
