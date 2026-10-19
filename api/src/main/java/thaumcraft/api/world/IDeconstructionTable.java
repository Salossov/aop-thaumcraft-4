package thaumcraft.api.world;

import net.minecraft.world.Container;
import thaumcraft.api.aspects.Aspect;

/** The real table inventory and its unclaimed primal research aspect. */
public interface IDeconstructionTable extends Container {
    Aspect pendingAspect();
}
