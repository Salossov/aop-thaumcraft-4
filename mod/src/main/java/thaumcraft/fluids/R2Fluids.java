package thaumcraft.fluids;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.*;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.*;
import thaumcraft.registry.R2Content;

public final class R2Fluids {
    private static final DeferredRegister<FluidType> TYPES=DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES,"thaumcraft");
    private static final DeferredRegister<Fluid> FLUIDS=DeferredRegister.create(Registries.FLUID,"thaumcraft");
    public static final DeferredHolder<FluidType,FluidType> GOO_TYPE=TYPES.register("flux_goo",()->type(false));
    public static final DeferredHolder<FluidType,FluidType> GAS_TYPE=TYPES.register("flux_gas",()->type(true));
    public static final DeferredHolder<Fluid,FiniteFluxFluid> GOO=FLUIDS.register("flux_goo",()->new FiniteFluxFluid(false));
    public static final DeferredHolder<Fluid,FiniteFluxFluid> GAS=FLUIDS.register("flux_gas",()->new FiniteFluxFluid(true));
    private static FluidType type(boolean gas) {
        return new FluidType(FluidType.Properties.create().density(gas?-4:8).viscosity(gas?2500:6000)
            .lightLevel(7).canConvertToSource(false).canHydrate(false).canSwim(false).canDrown(false)
            .supportsBoating(false).canExtinguish(false).canPushEntity(!gas).motionScale(gas?0:0.448)) {};
    }
    public static void register(IEventBus bus) { TYPES.register(bus);FLUIDS.register(bus);bus.addListener(R2Fluids::capabilities); }
    private static void capabilities(RegisterCapabilitiesEvent event) {
        event.registerBlock(Capabilities.FluidHandler.BLOCK,(level,pos,state,be,side)->new FluxFluidHandler(level,pos,state.getBlock()),R2Content.FLUX_GOO.get(),R2Content.FLUX_GAS.get());
    }
    private R2Fluids() {}
}
