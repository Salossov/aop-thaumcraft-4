package thaumcraft.api.aspects;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/** Bounded, typed storage shared by TC4 machines. No zero entries or negative quantities. */
public final class EssentiaStore implements IAspectContainer {
    private final int capacity;
    private final boolean mixed;
    private final boolean voidOverflow;
    private final Map<Aspect, Integer> contents = new LinkedHashMap<>();
    private Aspect filter;

    public EssentiaStore(int capacity, boolean mixed, boolean voidOverflow) {
        if (capacity < 1 || (mixed && voidOverflow)) throw new IllegalArgumentException("Invalid essentia store");
        this.capacity = capacity;
        this.mixed = mixed;
        this.voidOverflow = voidOverflow;
    }
    public int capacity() { return capacity; }
    public int amount() { return contents.values().stream().mapToInt(Integer::intValue).sum(); }
    public Aspect first() { return contents.keySet().stream().findFirst().orElse(null); }
    public Aspect filter() { return filter; }
    public void filter(Aspect aspect) {
        if (aspect != null && !mixed && first() != null && first() != aspect) throw new IllegalArgumentException("Filter differs from contents");
        filter = aspect;
    }
    @Override public AspectList getAspects() {
        var result = new AspectList(); contents.forEach(result::add); return result;
    }
    @Override public void setAspects(AspectList aspects) {
        Objects.requireNonNull(aspects);
        long total = 0;
        int types = 0;
        for (var entry : aspects.asMap().entrySet()) {
            if (entry.getKey() == null || entry.getValue() < 0) throw new IllegalArgumentException("Invalid aspect quantity");
            if (entry.getValue() > 0) {
                types++; total += entry.getValue();
                if (filter != null && !mixed && filter != entry.getKey()) throw new IllegalArgumentException("Aspect filter mismatch");
            }
        }
        if (total > capacity || (!mixed && types > 1)) throw new IllegalArgumentException("Essentia capacity/type exceeded");
        contents.clear(); aspects.asMap().forEach((a, n) -> { if (n > 0) contents.put(a, n); });
    }
    @Override public boolean doesContainerAccept(Aspect aspect) {
        return aspect != null && (mixed || ((filter == null || filter == aspect) && (first() == null || first() == aspect)))
                && (voidOverflow || amount() < capacity);
    }
    @Override public int addToContainer(Aspect aspect, int amount) {
        if (amount < 0) throw new IllegalArgumentException("Negative input");
        if (amount == 0 || !doesContainerAccept(aspect)) return amount;
        int accepted = Math.min(amount, capacity - amount());
        if (accepted > 0) contents.merge(aspect, accepted, Integer::sum);
        return voidOverflow ? 0 : amount - accepted;
    }
    @Override public boolean takeFromContainer(Aspect aspect, int amount) {
        if (amount < 0) throw new IllegalArgumentException("Negative output");
        if (!doesContainerContainAmount(aspect, amount)) return false;
        if (amount > 0) {
            int remaining = containerContains(aspect) - amount;
            if (remaining == 0) contents.remove(aspect); else contents.put(aspect, remaining);
        }
        return true;
    }
    @Override public boolean doesContainerContainAmount(Aspect aspect, int amount) {
        return aspect != null && amount >= 0 && containerContains(aspect) >= amount;
    }
    @Override public int containerContains(Aspect aspect) { return contents.getOrDefault(aspect, 0); }
    public void clear() { contents.clear(); }
}
