package thaumcraft.api.knowledge;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.Aspects;

/** One player's state. Ownership, persistence and mutations are managed by the server. */
public final class PlayerKnowledge {
    public enum ScanKind { OBJECT, ENTITY, PHENOMENON }
    private final Set<String> research = new LinkedHashSet<>();
    private final Map<String, Integer> pools = new LinkedHashMap<>();
    private final Map<ScanKind, Set<String>> scans = new java.util.EnumMap<>(ScanKind.class);
    private int permanentWarp, stickyWarp, temporaryWarp, warpCounter;
    private long revision;

    public PlayerKnowledge() {
        for (var kind : ScanKind.values()) scans.put(kind, new LinkedHashSet<>());
        for (var aspect : Aspects.PRIMALS) pools.put(aspect.getTag(), 0);
    }
    public PlayerKnowledge copy() {
        var copy = new PlayerKnowledge(); copy.research.addAll(research); copy.pools.putAll(pools);
        scans.forEach((kind, keys) -> copy.scans.get(kind).addAll(keys));
        copy.permanentWarp=permanentWarp; copy.stickyWarp=stickyWarp; copy.temporaryWarp=temporaryWarp;
        copy.warpCounter=warpCounter; copy.revision=revision; return copy;
    }
    public Set<String> completedResearch() { return Collections.unmodifiableSet(research); }
    public Map<String, Integer> aspectPools() { return Collections.unmodifiableMap(pools); }
    public Set<String> scanned(ScanKind kind) { return Collections.unmodifiableSet(scans.get(kind)); }
    public boolean completeResearch(String key) { return research.add(key); }
    public boolean forgetResearch(String key) { return research.remove(key); }
    public boolean recordScan(ScanKind kind, String key) { return scans.get(kind).add(key); }
    public void clearScans() { scans.values().forEach(Set::clear); }
    public boolean knows(Aspect aspect) { return aspect != null && pools.containsKey(aspect.getTag()); }
    public boolean knowsParents(Aspect aspect) { return aspect != null && aspect.getComponents().stream().allMatch(this::knows); }
    public boolean discover(Aspect aspect) { if (knows(aspect)) return false; pools.put(aspect.getTag(),0); return true; }
    public int pool(Aspect aspect) { return pools.getOrDefault(aspect.getTag(),0); }
    public void setPool(String tag, int amount) { pools.put(tag,amount); }
    public boolean spend(Aspect aspect, int amount) {
        if (amount < 0 || pool(aspect) < amount) return false;
        pools.put(aspect.getTag(),pool(aspect)-amount); return true;
    }
    public void addPool(Aspect aspect, int amount) { pools.merge(aspect.getTag(),amount,Integer::sum); }
    public int permanentWarp() { return permanentWarp; }
    public int stickyWarp() { return stickyWarp; }
    public int temporaryWarp() { return temporaryWarp; }
    public int warpCounter() { return warpCounter; }
    public int totalWarp() { return permanentWarp + stickyWarp + temporaryWarp; }
    public void setWarp(int permanent,int sticky,int temporary,int counter) {
        permanentWarp=Math.max(0,permanent); stickyWarp=Math.max(0,sticky); temporaryWarp=Math.max(0,temporary); warpCounter=counter;
    }
    public long revision() { return revision; }
    public void revision(long value) { revision=value; }
    public void changed() { revision++; }
}
