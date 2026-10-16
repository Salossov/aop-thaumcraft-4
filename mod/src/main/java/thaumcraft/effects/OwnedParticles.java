package thaumcraft.effects;

import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.*;

public final class OwnedParticles {
    private static final DeferredRegister<net.minecraft.core.particles.ParticleType<?>> TYPES=DeferredRegister.create(Registries.PARTICLE_TYPE,"thaumcraft");
    public static final DeferredHolder<net.minecraft.core.particles.ParticleType<?>,SimpleParticleType> WARD=TYPES.register("block_ward",()->new SimpleParticleType(false));
    public static void register(IEventBus bus){TYPES.register(bus);}
    private OwnedParticles(){}
}
