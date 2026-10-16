package thaumcraft.fluids;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.fluids.*;
import net.neoforged.neoforge.registries.*;
import thaumcraft.registry.R4Content;

public final class R4Fluids {
    private static final DeferredRegister<FluidType> TYPES=DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES,"thaumcraft");
    private static final DeferredRegister<Fluid> FLUIDS=DeferredRegister.create(Registries.FLUID,"thaumcraft");
    public static final DeferredHolder<FluidType,FluidType> PURE_TYPE=TYPES.register("purifying_fluid",()->new FluidType(FluidType.Properties.create().lightLevel(10).viscosity(1000).canConvertToSource(false).canHydrate(false)){});
    public static final DeferredHolder<FluidType,FluidType> DEATH_TYPE=TYPES.register("liquid_death",()->new FluidType(FluidType.Properties.create().lightLevel(8).viscosity(1500).canConvertToSource(false).canHydrate(false)){});
    public static final DeferredHolder<Fluid,BaseFlowingFluid.Source> PURE=FLUIDS.register("purifying_fluid",()->new BaseFlowingFluid.Source(pureProperties()));
    public static final DeferredHolder<Fluid,BaseFlowingFluid.Flowing> PURE_FLOWING=FLUIDS.register("flowing_purifying_fluid",()->new BaseFlowingFluid.Flowing(pureProperties()));
    public static final DeferredHolder<Fluid,FiniteDeathFluid> DEATH=FLUIDS.register("liquid_death",FiniteDeathFluid::new);
    private static BaseFlowingFluid.Properties pureProperties(){return new BaseFlowingFluid.Properties(PURE_TYPE,PURE,PURE_FLOWING).bucket(R4Content.PURE_BUCKET).block(R4Content.PURIFYING_FLUID).tickRate(5);}
    public static void register(IEventBus bus){TYPES.register(bus);FLUIDS.register(bus);bus.addListener((net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent e)->e.registerBlock(net.neoforged.neoforge.capabilities.Capabilities.FluidHandler.BLOCK,(level,pos,state,be,side)->new DeathFluidHandler(level,pos),R4Content.LIQUID_DEATH.get()));}
    private R4Fluids(){}
}
