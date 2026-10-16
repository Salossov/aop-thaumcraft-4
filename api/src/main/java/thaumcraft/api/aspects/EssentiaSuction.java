package thaumcraft.api.aspects;

/** Original tube propagation and conflict rules; world lookup belongs to the implementation. */
public final class EssentiaSuction {
    public record Demand(Aspect aspect, int amount) {
        public Demand { if (amount < 0) throw new IllegalArgumentException("Negative suction"); }
    }
    public static Demand consider(Demand current, Demand neighbour, Aspect filter, Aspect contents, boolean restricted) {
        if (filter != null && neighbour.aspect != null && filter != neighbour.aspect) return current;
        if (contents != null && neighbour.aspect != null && contents != neighbour.aspect) return current;
        if (neighbour.amount <= 0 || neighbour.amount <= current.amount + 1) return current;
        return new Demand(neighbour.aspect == null ? filter : neighbour.aspect,
                restricted ? neighbour.amount / 2 : neighbour.amount - 1);
    }
    public static boolean conflicts(Demand current, Demand neighbour) {
        return current.amount > 0 && (neighbour.amount == current.amount || neighbour.amount == current.amount - 1)
                && current.aspect != neighbour.aspect;
    }
    public static boolean canPull(int suction, int sourceSuction, int sourceMinimum) {
        return suction > sourceSuction && suction >= sourceMinimum;
    }
    private EssentiaSuction() {}
}
