package thaumcraft.api.client;

import java.util.Objects;
import java.util.function.BooleanSupplier;

/** Read-only client placement shared by the wand dial and addon HUDs. */
public final class WandHudSettings {
    private static volatile BooleanSupplier bottom=()->false;
    public static void install(BooleanSupplier dialBottom){bottom=Objects.requireNonNull(dialBottom);}
    /** Current client configuration; false (top left) until client setup. */
    public static boolean dialBottom(){return bottom.getAsBoolean();}
    private WandHudSettings(){}
}
