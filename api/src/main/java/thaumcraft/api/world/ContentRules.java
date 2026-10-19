package thaumcraft.api.world;

import java.util.Objects;

/** Read-only core options which affect addon content and equipment behavior. */
public final class ContentRules {
    public interface Access { boolean mirrorsEnabled(); boolean modernWeaponCooldown(); }
    private static Access access;
    public static void install(Access implementation) {
        if (access != null) throw new IllegalStateException("Content rules already installed");
        access = Objects.requireNonNull(implementation);
    }
    public static boolean mirrorsEnabled() { return access.mirrorsEnabled(); }
    public static boolean modernWeaponCooldown() { return access.modernWeaponCooldown(); }
    private ContentRules() { }
}
