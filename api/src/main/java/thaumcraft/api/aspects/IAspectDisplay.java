package thaumcraft.api.aspects;

import java.util.Map;

/**
 * Read-only aspect view for devices whose displayed data is not mutable essentia
 * storage: infusion requirements, nodes and the vis of a pedestal's held item.
 */
public interface IAspectDisplay {
    /** Independent snapshot; null has the same empty-display meaning as an empty list. */
    AspectList displayedAspects();
    static AspectList snapshot(Map<String, Integer> values) {
        var result = new AspectList();
        values.forEach((tag, amount) -> { var aspect = Aspect.getAspect(tag); if (aspect != null) result.add(aspect, amount); });
        return result;
    }
}
