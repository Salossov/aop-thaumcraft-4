package thaumcraft.verification;

import java.util.function.BiConsumer;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import thaumcraft.effects.TaintEffects;
import thaumcraft.entities.*;
import thaumcraft.fluids.*;
import thaumcraft.registry.R2Content;

final class FluxVerification {
    static void run(ServerLevel level,BiConsumer<Boolean,String> check) {
        var goo=R2Content.FLUX_GOO.get();var gas=R2Content.FLUX_GAS.get();var random=RandomSource.create(420235);
        var centre=new BlockPos(8,90,8);
        for(var p:BlockPos.betweenClosed(centre.offset(-3,-1,-3),centre.offset(3,2,3))) {
            boolean wall=Math.abs(p.getX()-centre.getX())==3||Math.abs(p.getZ()-centre.getZ())==3||p.getY()==89||p.getY()==92;
            level.setBlockAndUpdate(p,wall?Blocks.STONE.defaultBlockState():Blocks.AIR.defaultBlockState());
        }
        FluxBlock.setUnits(level,centre,goo,9);
        check.accept(level.getFluidState(centre).is(R2Fluids.GOO.get())&&level.getFluidState(centre).getAmount()==9,"goo is a real Fluid, preserving TC4 overfull amount 9");
        for(int step=0;step<64;step++) {
            for(var p:BlockPos.betweenClosed(centre.offset(-2,0,-2),centre.offset(2,1,2))) goo.flow(level,p,random);
            int total=0;for(var p:BlockPos.betweenClosed(centre.offset(-2,0,-2),centre.offset(2,1,2))) {var state=level.getBlockState(p);if(state.is(goo))total+=state.getValue(FiniteFluxFluid.QUANTA);}
            check.accept(total==9,"closed basin conserves all 9 quanta, step "+step);
        }
        var column=new BlockPos(20,100,8);level.getChunkAt(column);
        for(var p:BlockPos.betweenClosed(column.offset(-1,-1,-1),column.offset(1,2,1))) level.setBlockAndUpdate(p,Blocks.STONE.defaultBlockState());
        FluxBlock.setUnits(level,column,goo,9);FluxBlock.setUnits(level,column.below(),goo,7);goo.flow(level,column,random);
        check.accept(level.getFluidState(column).getAmount()==8&&level.getFluidState(column.below()).getAmount()==8,"vertical overflow preserves 16 quanta without an infinite source");
        FluxBlock.setUnits(level,column,gas,3);level.setBlockAndUpdate(column.above(),Blocks.AIR.defaultBlockState());gas.flow(level,column,random);
        check.accept(level.getBlockState(column).isAir()&&level.getFluidState(column.above()).is(R2Fluids.GAS.get())&&level.getFluidState(column.above()).getAmount()==3,"gas rises with its complete finite volume");
        FluxBlock.setUnits(level,column,goo,5);FluxBlock.setUnits(level,column.below(),gas,2);goo.flow(level,column,random);
        check.accept(level.getFluidState(column).is(R2Fluids.GAS.get())&&level.getFluidState(column).getAmount()==2&&level.getFluidState(column.below()).getAmount()==5,"density swap preserves both fluids and volumes");
        FluxBlock.setUnits(level,column,goo,9);var handler=level.getCapability(Capabilities.FluidHandler.BLOCK,column,Direction.UP);
        check.accept(handler!=null&&handler.getFluidInTank(0).getAmount()==1125,"world FluidHandler exposes exact 125 mB per quantum");
        check.accept(handler.drain(250,FluidAction.SIMULATE).getAmount()==250&&level.getFluidState(column).getAmount()==9,"simulated drain leaves world unchanged");
        check.accept(handler.drain(124,FluidAction.EXECUTE).isEmpty()&&level.getFluidState(column).getAmount()==9,"partial quantum drain is rejected without losing volume");
        check.accept(handler.drain(250,FluidAction.EXECUTE).getAmount()==250&&level.getFluidState(column).getAmount()==7,"executed drain removes only requested whole quanta");
        check.accept(handler.fill(new FluidStack(R2Fluids.GAS.get(),1000),FluidAction.EXECUTE)==0,"handler rejects mixed fluid");
        check.accept(handler.fill(new FluidStack(R2Fluids.GOO.get(),500),FluidAction.SIMULATE)==500&&level.getFluidState(column).getAmount()==7,"simulated fill leaves world unchanged");
        check.accept(handler.fill(new FluidStack(R2Fluids.GOO.get(),500),FluidAction.EXECUTE)==500&&level.getFluidState(column).getAmount()==11,"executed fill retains finite volume");
        handler.drain(2000,FluidAction.EXECUTE);level.setBlockAndUpdate(column,Blocks.STONE.defaultBlockState());
        check.accept(handler.fill(new FluidStack(R2Fluids.GOO.get(),500),FluidAction.EXECUTE)==0&&level.getBlockState(column).is(Blocks.STONE),"stale handler cannot replace another block");
        FluxBlock.setUnits(level,column,goo,8);var cow=EntityType.COW.create(level);level.getBlockState(column).entityInside(level,column,cow);
        check.accept(cow.hasEffect(TaintEffects.VIS_EXHAUST)&&cow.getEffect(TaintEffects.VIS_EXHAUST).getAmplifier()==2&&cow.getEffect(TaintEffects.VIS_EXHAUST).getDuration()==600,"goo applies original 600-tick vis exhaustion strength");
        check.accept(cow.getEffect(TaintEffects.VIS_EXHAUST).getCures().isEmpty(),"flux vis exhaustion has no milk cure");
        for(var type:java.util.List.of(R2Entities.BRAINY,R2Entities.GIANT_BRAINY,R2Entities.TAINT_SPORE,R2Entities.TAINT_SPIDER,R2Entities.TAINT_SWARMER,R2Entities.TAINT_SWARM,R2Entities.THAUMIC_SLIME,R2Entities.TAINT_PIG,R2Entities.TAINT_COW,R2Entities.TAINT_CHICKEN,R2Entities.TAINT_SHEEP,R2Entities.TAINT_VILLAGER,R2Entities.TAINT_CREEPER)) {
            var entity=type.get().create(level);check.accept(entity!=null&&((LivingEntity)entity).getMaxHealth()>0,"rare-node dependency constructs with registered attributes: "+type.getId());
        }
        var slime=R2Entities.THAUMIC_SLIME.get().create(level);slime.size(16);slime.moveTo(24,95,24,0,0);level.addFreshEntity(slime);slime.hurt(level.damageSources().genericKill(),1000);
        check.accept(level.getEntitiesOfClass(ThaumicSlime.class,new AABB(20,90,20,28,100,28),e->e!=slime&&e.isAlive()&&e.size()==1).size()==4,"dead 16-mass slime splits into four living 1-mass children");
        var pig=EntityType.PIG.create(level);pig.moveTo(32,95,24,0,0);level.addFreshEntity(pig);pig.addEffect(new MobEffectInstance(TaintEffects.FLUX_TAINT,100));pig.hurt(level.damageSources().genericKill(),1000);
        check.accept(level.getEntitiesOfClass(TaintedCreature.class,new AABB(31,94,23,33,97,25),e->e.getType()==R2Entities.TAINT_PIG.get()).size()==1,"death under flux taint converts pig to its TC4 tainted creature");
    }
    private FluxVerification() {}
}
