package thaumcraft.api;

import java.util.Random;
import thaumcraft.api.aspects.*;

public final class EssentiaRulesTest {
    private static int checks;
    private static void check(boolean value, String label) { checks++; if (!value) throw new AssertionError(label); }
    private static void invalid(Runnable action, String label) { try { action.run(); throw new AssertionError(label); } catch (IllegalArgumentException expected) { checks++; } }
    public static void main(String[] args) {
        var air = Aspect.getAspect("aer"); var fire = Aspect.getAspect("ignis");
        var jar = new EssentiaStore(64, false, false);
        check(jar.addToContainer(air, 80) == 16 && jar.amount() == 64, "jar capacity and remainder");
        check(jar.addToContainer(fire, 1) == 1, "jar never mixes types");
        check(!jar.takeFromContainer(air, 65) && jar.amount() == 64, "failed withdrawal is atomic");
        check(jar.takeFromContainer(air, 64) && jar.first() == null, "emptying clears type");
        jar.filter(fire); check(jar.addToContainer(air, 1) == 1, "label rejects wrong type");
        check(jar.addToContainer(fire, 8) == 0, "label accepts its type");
        invalid(() -> jar.setAspects(new AspectList().add(air, 1)), "invalid NBT filter");
        check(jar.amount() == 8 && jar.first() == fire, "failed NBT load retains store");
        invalid(() -> jar.addToContainer(fire, -1), "negative input");
        invalid(() -> jar.takeFromContainer(fire, -1), "negative output");
        var voidJar = new EssentiaStore(64, false, true);
        check(voidJar.addToContainer(air, 90) == 0 && voidJar.amount() == 64, "void jar consumes overflow");
        check(voidJar.addToContainer(fire, 1) == 1 && voidJar.amount() == 64, "void jar does not discard other types");
        var buffer = new EssentiaStore(8, true, false);
        check(buffer.addToContainer(air, 4) == 0 && buffer.addToContainer(fire, 6) == 2 && buffer.amount() == 8, "mixed buffer total capacity");
        var reservoir = new EssentiaStore(256, true, false); var random = new Random(4235);
        int expected = 0;
        for (int i = 0; i < 1000; i++) {
            var a = random.nextBoolean() ? air : fire; int n = random.nextInt(21);
            if (random.nextBoolean()) expected += n - reservoir.addToContainer(a, n);
            else { int before = reservoir.containerContains(a); boolean taken = reservoir.takeFromContainer(a, n); check(taken == (before >= n), "withdrawal precondition"); if (taken) expected -= n; }
            check(reservoir.amount() == expected && expected >= 0 && expected <= 256, "random conservation");
        }
        var saved = reservoir.getAspects(); var restored = new EssentiaStore(256, true, false); restored.setAspects(saved);
        check(restored.getAspects().asMap().equals(saved.asMap()), "store roundtrip");
        var zero = new EssentiaSuction.Demand(null, 0);
        var demand = EssentiaSuction.consider(zero, new EssentiaSuction.Demand(air, 64), null, null, false);
        check(demand.amount() == 63 && demand.aspect() == air, "normal suction decrement");
        check(EssentiaSuction.consider(zero, new EssentiaSuction.Demand(air, 64), null, null, true).amount() == 32, "restricted suction division");
        check(EssentiaSuction.consider(zero, new EssentiaSuction.Demand(air, 64), fire, null, false).amount() == 0, "filter ignores other demand");
        check(EssentiaSuction.consider(zero, new EssentiaSuction.Demand(air, 64), null, fire, false).amount() == 0, "stored type ignores other demand");
        check(EssentiaSuction.conflicts(demand, new EssentiaSuction.Demand(fire, 63)), "equal competing suction vents");
        check(EssentiaSuction.conflicts(demand, new EssentiaSuction.Demand(fire, 62)), "adjacent competing suction vents");
        check(!EssentiaSuction.conflicts(demand, new EssentiaSuction.Demand(air, 63)), "same type does not vent");
        check(!EssentiaSuction.canPull(32, 32, 0) && !EssentiaSuction.canPull(31, 0, 32) && EssentiaSuction.canPull(32, 0, 32), "strict suction and source minimum");
        // Fixed-point propagation through a branch, a cycle and a chain longer than its source reach.
        var network = new EssentiaSuction.Demand[70]; java.util.Arrays.fill(network, zero);
        for (int pass = 0; pass < 100; pass++) for (int i = 0; i < network.length; i++) {
            var next = i == 0 ? new EssentiaSuction.Demand(air, 64) : zero;
            if (i > 0) next = EssentiaSuction.consider(next, network[i - 1], null, null, false);
            if (i + 1 < network.length) next = EssentiaSuction.consider(next, network[i + 1], null, null, false);
            network[i] = next;
        }
        for (int i = 0; i < network.length; i++) check(network[i].amount() == Math.max(0, 64 - i), "bounded chain reach");
        var ring = new EssentiaSuction.Demand[8]; java.util.Arrays.fill(ring, zero);
        for (int pass = 0; pass < 10; pass++) for (int i = 0; i < ring.length; i++) ring[i] = EssentiaSuction.consider(zero, ring[(i + 1) % ring.length], null, null, false);
        check(java.util.Arrays.stream(ring).allMatch(d -> d.amount() == 0), "cycle cannot create suction");
        System.out.println("TC4_R3_ESSENTIA_RULES_PASS checks=" + checks);
    }
}
