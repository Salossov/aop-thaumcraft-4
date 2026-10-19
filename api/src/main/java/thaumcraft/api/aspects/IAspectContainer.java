package thaumcraft.api.aspects;

/** TC4 container contract. addToContainer returns the unaccepted remainder. */
public interface IAspectContainer {
    enum JarType { NONE, NORMAL, VOID }
    /** Concrete jar identity, not a forwarded container's target identity. */
    default JarType getJarType(){return JarType.NONE;}
    AspectList getAspects();
    /** A jar's label/filter, including when empty; ordinary containers return null. */
    default Aspect getAspectFilter() { return null; }
    void setAspects(AspectList aspects);
    boolean doesContainerAccept(Aspect aspect);
    int addToContainer(Aspect aspect, int amount);
    boolean takeFromContainer(Aspect aspect, int amount);
    boolean doesContainerContainAmount(Aspect aspect, int amount);
    int containerContains(Aspect aspect);

    @Deprecated
    default boolean doesContainerContain(AspectList aspects) {
        return aspects.asMap().entrySet().stream().allMatch(e -> doesContainerContainAmount(e.getKey(), e.getValue()));
    }

    @Deprecated
    default boolean takeFromContainer(AspectList aspects) {
        if (!doesContainerContain(aspects)) return false;
        aspects.asMap().forEach(this::takeFromContainer);
        return true;
    }
}
