package thaumcraft.network;

/** Client mirror without client class references, also safe to load on a dedicated server. */
public final class ClientGuardianMist {
    private static int remaining;
    private static float density;
    public static void accept() {remaining=Math.max(remaining,200);}
    public static void tick() {if(remaining<=0){density=0;return;}density=remaining<100?0.1f*remaining/100:Math.min(0.1f,density+0.001f);--remaining;}
    public static float density() {return density;}
    public static int remaining() {return remaining;}
    public static void clear() {remaining=0;density=0;}
    private ClientGuardianMist() {}
}
