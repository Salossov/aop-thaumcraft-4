package thaumcraft.api.visnet;

import thaumcraft.api.aspects.Aspect;

/** A finite, server-owned virtual source in front of a host block entity. */
public interface VirtualVisSource {
    /** Source/consumer range in blocks; 0..32767. ThE's original proxy uses 1. */
    int range();
    byte attunement();
    /** True only while this exact addon endpoint still belongs to its live host. */
    boolean valid();
    /** Return only the centivis actually removed, in 0..requestedCentivis. */
    int drain(Aspect primal, int requestedCentivis);
}
