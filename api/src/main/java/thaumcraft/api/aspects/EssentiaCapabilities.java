package thaumcraft.api.aspects;

import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.capabilities.BlockCapability;

/** Addons may expose TC4 transport without subclassing its block entities. */
public final class EssentiaCapabilities {
    public static final BlockCapability<IEssentiaTransport, Direction> TRANSPORT = BlockCapability.createSided(
            ResourceLocation.fromNamespaceAndPath("thaumcraft", "essentia_transport"), IEssentiaTransport.class);
    public static final BlockCapability<IAspectContainer, Void> CONTAINER = BlockCapability.createVoid(
            ResourceLocation.fromNamespaceAndPath("thaumcraft", "aspect_container"), IAspectContainer.class);
    public static final net.neoforged.neoforge.capabilities.BlockCapability<IAspectContainer, Void> SOURCE = net.neoforged.neoforge.capabilities.BlockCapability.createVoid(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("thaumcraft", "essentia_source"), IAspectContainer.class);
    private EssentiaCapabilities() {}
}
