package thaumcraft.api.aspects;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;

/** TC4 arithmetic: merge takes the maximum; reduce retains discovered zero entries. */
public final class AspectList {
    private final Map<Aspect, Integer> amounts = new LinkedHashMap<>();
    public AspectList add(Aspect aspect, int amount) { amounts.merge(aspect, amount, Integer::sum); return this; }
    public AspectList merge(Aspect aspect, int amount) { amounts.merge(aspect, amount, Math::max); return this; }
    public boolean reduce(Aspect aspect, int amount) {
        if (getAmount(aspect) < amount) return false;
        amounts.put(aspect, getAmount(aspect) - amount); return true;
    }
    public int getAmount(Aspect aspect) { return amounts.getOrDefault(aspect, 0); }
    public int size() { return amounts.size(); }
    /** Total units, distinct from size(), which counts aspect types. */
    public int visSize() { return amounts.values().stream().mapToInt(Integer::intValue).sum(); }
    public Aspect[] getAspects() { return amounts.keySet().toArray(Aspect[]::new); }
    public Aspect[] getAspectsSorted() { return amounts.keySet().stream().sorted(Comparator.comparing(Aspect::getTag)).toArray(Aspect[]::new); }
    public Map<Aspect, Integer> asMap() { return Map.copyOf(amounts); }
}
